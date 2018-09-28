package com.logic.geekchat.protocol;

import android.content.Context;
import android.util.Log;

import com.logic.geekchat.login.LoginPresenter;
import com.xuhao.android.common.basic.bean.OriginalData;
import com.xuhao.android.common.interfacies.IReaderProtocol;
import com.xuhao.android.common.interfacies.client.msg.ISendable;
import com.xuhao.android.libsocket.impl.client.AbsConnectionManager;
import com.xuhao.android.libsocket.sdk.OkSocket;
import com.xuhao.android.libsocket.sdk.client.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.client.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.client.action.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.client.connection.IConnectionManager;

import org.json.JSONObject;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.zip.CRC32;

public class OkClient {

    public static final int SIZE_MAX = 1024;
    public static final int SIZE_HEAD = 8;

    public static final int SIZE_CRC32 = 4;
    public static final int SIZE_LENGTH = 2;
    public static final int SIZE_TYPE = 2;

    public static final int INDEX_CRC32 = 0;
    public static final int INDEX_LENGTH = 4;
    public static final int INDEX_TYPE = 6;
    public static final int INDEX_DATA = 8;

    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    class ReaderProtocolImpl implements IReaderProtocol {

        @Override
        public int getHeaderLength() {
            return SIZE_HEAD;
        }

        @Override
        public int getBodyLength(byte[] header, ByteOrder byteOrder) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(header).order(BYTE_ORDER);
            return byteBuffer.getShort(INDEX_LENGTH);
        }
    }

    public class StringPacket implements ISendable {
        String mData;
        public StringPacket(String data) {
            mData = data;
            //Log.e("json send", data);
        }
        @Override
        public byte[] parse() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE_HEAD + mData.length()).order(BYTE_ORDER);
            //temp crc
            byteBuffer.putInt(0x00);
            //length
            byteBuffer.putShort((short) mData.length());
            //type
            byteBuffer.putShort((short) 0x00);
            //data
            try {
                for (byte b : mData.getBytes("UTF-8")) {
                    byteBuffer.put(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //crc
            CRC32 crc32 = new CRC32();
            crc32.update(byteBuffer.array(), SIZE_CRC32, SIZE_HEAD + mData.length() - SIZE_CRC32);
            byteBuffer.putInt(INDEX_CRC32, (int) crc32.getValue());

            return byteBuffer.array();
        }
    }

    static public interface LoginListener {
        int RESULT_LOGIN_SUCCEED = 0;
        int RESULT_LOGIN_FAILED = 1;
        void onResult(int result);
    }

    private ConnectionInfo mConnectionInfo = new ConnectionInfo("149.28.70.170", 1200);
    private IConnectionManager mConnectionManager;
    private static OkClient mClient;
    private String mId,mPassword;
    private String mToken;
    private LoginListener mLoginListener;
    private ReceiverAfterLogin mReceiverAfterLogin = new ReceiverAfterLogin();

    public OkClient() {
        mConnectionManager = OkSocket.open(mConnectionInfo);
        OkSocketOptions options = new OkSocketOptions.Builder()
                .setReaderProtocol(new ReaderProtocolImpl())
                .setReadByteOrder(BYTE_ORDER)
                .setWriteByteOrder(BYTE_ORDER)
                .setIOThreadMode(OkSocketOptions.IOThreadMode.DUPLEX)
                .setConnectionHolden(false)
                .build();
        mConnectionManager.option(options);
        mConnectionManager.registerReceiver(mReceiverAfterLogin);
        mConnectionManager.connect();
    }

    public static OkClient getInstance() {
        if (mClient == null) {
            mClient = new OkClient();
        }
        return mClient;
    }

    public void login(String id, String password, LoginListener listener) {
        mLoginListener = listener;
        //build effective id and password
        StringBuffer effectivePassword = new StringBuffer(password);
        for (int i = 0; i < 32-password.length(); i += 1) {
            effectivePassword.append('\0');
        }
        mId = id;
        mPassword = effectivePassword.toString();
        Log.e("pw len", ""+mPassword.length());
        //register receiver
        //mConnectionManager.registerReceiver(mReceiverAfterLogin);
        //build login request
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("method", "com.login.seed.request");
            jsonObject.put("username", mId);
            //login step 1:
            StringPacket packet = new StringPacket(jsonObject.toString());
            Log.e("json send", jsonObject.toString());
            mConnectionManager.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class ReceiverAfterLogin extends SocketActionAdapter {
        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            try {
                JSONObject jsonReceived = new JSONObject(new String(data.getBodyBytes(), "UTF-8"));
                Log.e("receiver id", ""+this.hashCode());
                Log.e("json rec", jsonReceived.toString());
                if (jsonReceived.getString("method").equals("com.login.seed.respond")) {
                    //login step 2:
                    String seed = jsonReceived.getString("seed");
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    //messageDigest.update((seed+mPassword).getBytes("UTF-8"));
                    messageDigest.update(seed.getBytes("UTF-8"));
                    messageDigest.update(mPassword.getBytes("UTF-8"));
                    JSONObject jsonToSend = new JSONObject();
                    jsonToSend.put("method", "com.login.request");
                    jsonToSend.put("username", mId);
                    jsonToSend.put("crypto", bytesToHexString(messageDigest.digest()));
                    /*
                    final String finalString = jsonToSend.toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int msec = 2000;
                                Log.e("json send", "wait for "+msec+"ms");
                                Thread.sleep(msec);
                                Log.e("json send", finalString);
                                mConnectionManager.send(new StringPacket(finalString));
                            } catch (Exception e) {}
                        }
                    }).start();*/
                    Log.e("json send", jsonToSend.toString());
                    mConnectionManager.send(new StringPacket(jsonToSend.toString()));
                } else if (jsonReceived.getString("method").equals("com.login.respond")) {
                    //login step 3:
                    if (jsonReceived.getBoolean("status")) {
                        mToken = jsonReceived.getString("token");
                        mLoginListener.onResult(LoginListener.RESULT_LOGIN_SUCCEED);
                    } else {
                        mLoginListener.onResult(LoginListener.RESULT_LOGIN_FAILED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bytes) {
            String hexByte = Integer.toHexString(b & 0xff).toUpperCase();
            if (hexByte.length() == 1) {
                stringBuffer.append('0');
                stringBuffer.append(hexByte);
            } else if (hexByte.length() == 2) {
                stringBuffer.append(hexByte);
            }
        }
        return stringBuffer.toString();
    }

    @Deprecated
    private byte[] hexStringToBytes(String hex) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 1) {
            //byteBuffer.put(Byte.valueOf(new String(hex.charAt(i)), 16));
        }
        return byteBuffer.array();
    }
}
