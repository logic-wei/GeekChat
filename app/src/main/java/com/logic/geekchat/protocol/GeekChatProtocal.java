package com.logic.geekchat.protocol;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.logic.geekchat.account.ServerConfig;
import com.logic.geekchat.protocol.packer.IBodyPacker;
import com.logic.geekchat.protocol.packer.JsonPacker;
import com.logic.geekchat.protocol.packer.PackerFactory;
import com.logic.geekchat.protocol.packer.WatchdogPacker;
import com.xuhao.android.common.basic.bean.OriginalData;
import com.xuhao.android.common.interfacies.IReaderProtocol;
import com.xuhao.android.common.interfacies.client.msg.ISendable;
import com.xuhao.android.libsocket.sdk.OkSocket;
import com.xuhao.android.libsocket.sdk.client.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.client.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.client.action.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.client.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.client.connection.IConnectionManager;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeekChatProtocal {
    private final static String TAG = "GeekChatProtocal";
    private static GeekChatProtocal instance = new GeekChatProtocal();
    private static IConnectionManager mManager = null;


    private static Map<String, GeekChatJsonMethodsRpc> rpcMap = new LinkedHashMap<String, GeekChatJsonMethodsRpc>();
    private static List<ProtocalHead> bodyPackers = new LinkedList<ProtocalHead>();

    private GeekChatProtocal() {}


    private static class ReaderProtocolImpl implements IReaderProtocol {
        private ProtocalHead protoHeader = new ProtocalHead();
        @Override
        public int getHeaderLength() {
            return protoHeader.getHeadLength();
        }

        @Override
        public int getBodyLength(byte[] header, ByteOrder byteOrder) {
            protoHeader.setHeadData(header);
            return protoHeader.getBodyLengthInHead();
        }
    }

    private static class WatchdogPulse implements IPulseSendable {
        private ProtocalHead header = new ProtocalHead();
        private byte[] watchdogBytes = null;
        public WatchdogPulse() {
            try {
                this.header.setBodyPacker(new WatchdogPacker());
                this.watchdogBytes = header.parse();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public byte[] parse() {
            return this.watchdogBytes;
        }
    }

    private static ProtocalListener watchdogListener = new ProtocalListener() {
        @Override
        public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
            mManager.getPulseManager().feed();
        }

        @Override
        public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
            Log.i(TAG, "onError");
        }
    };

    public static GeekChatProtocal getInstance() {
        if(mManager == null) {
            synchronized (instance) {
                if(mManager == null) {
                    ConnectionInfo mConnectionInfo = new ConnectionInfo(ServerConfig.GEEKCHAT_SERVER_IP, ServerConfig.GEEKCHAT_SERVER_PORT);
                    mManager = OkSocket.open(mConnectionInfo);
                    OkSocketOptions options = new OkSocketOptions.Builder()
                            .setReaderProtocol(new ReaderProtocolImpl())
                            .setReadByteOrder(ByteOrder.LITTLE_ENDIAN)
                            .setWriteByteOrder(ByteOrder.LITTLE_ENDIAN)
                            .setIOThreadMode(OkSocketOptions.IOThreadMode.DUPLEX)
                            .setConnectionHolden(true)
                            .setPulseFrequency(30)
                            .setPulseFeedLoseTimes(2)
                            .build();
                    mManager.option(options);
                    mManager.registerReceiver(new GeekChatSocketAction());
                    mManager.connect();

                    instance.registerMethodsRpc(new GeekChatJsonMethodsRpc("com.heartbeat.respond", watchdogListener));
                }
            }
        }

        return instance;
    }

    private static class GeekChatSocketAction extends SocketActionAdapter {
        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
            Log.i(TAG, action);
            ProtocalHead protoHeader = new ProtocalHead();

            try {
                protoHeader.setDataByBytes(data.getHeadBytes(), data.getBodyBytes());
                if (protoHeader.getPackerType() == PackerFactory.PROTOCAL_TYPE_JSON) {
                    JsonPacker jsonPacker = (JsonPacker) protoHeader.getBodyPacker();
                    String methods = jsonPacker.getMethods();
                    GeekChatJsonMethodsRpc jsonRpc = rpcMap.get(methods);
                    if(jsonRpc != null) {
                        jsonRpc.handleBodyPacker(jsonPacker);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                for(GeekChatJsonMethodsRpc rpc : rpcMap.values()) {
                    rpc.handlePackerError(ex);
                }
            }
        }

        @Override
        public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(context, info, action, data);
            Log.i(TAG, action);
        }

        @Override
        public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
            super.onSocketConnectionSuccess(context, info, action);
            Log.i(TAG, action);
            //mManager.getPulseManager().setPulseSendable(new WatchdogPulse());
            //mManager.getPulseManager().pulse();
            for(ProtocalHead header : bodyPackers) {
                mManager.send(header);
            }
        }
    }

    public void registerMethodsRpc(GeekChatJsonMethodsRpc rpc) {
        if(!rpcMap.containsKey(rpc.getMethods())) {
            rpcMap.put(rpc.getMethods(), rpc);
        }
    }

    public void unregisterMethodsRpc(GeekChatJsonMethodsRpc rpc) {
        if(rpcMap.containsKey(rpc.getMethods())) {
            rpcMap.remove(rpc.getMethods());
        }
    }

    public void SendPacker(IBodyPacker bodyPacker) throws UnsupportedEncodingException, BodyLengthException, CRCMistakeException, InterruptedException {
        ProtocalHead header = new ProtocalHead();
        header.setBodyPacker(bodyPacker);
        if(mManager.isConnect()) {
            mManager.send(header);
        } else {
            if(bodyPackers.size() >= 10) {
                bodyPackers.remove(bodyPackers.size() - 1);
            }
            bodyPackers.add(header);
        }
    }
}
