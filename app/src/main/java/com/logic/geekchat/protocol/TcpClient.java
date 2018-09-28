package com.logic.geekchat.protocol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClient implements IClient {

    private static final String KEY_PACKET = "packet";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 1;

    private static TcpClient mInstance;
    private String mAddress;
    private int mPort;
    private OnConnectionListener mOnConnectionListener;
    private OnPacketListener mOnPacketListener;
    private Socket mSocket;
    //private final Object mSocketLock = new Object();
    private MyHandler mHandler;
    private int mState = STATE_DISCONNECTED;

    static public TcpClient getInstance() {
        if (mInstance == null) {
            mInstance = new TcpClient("149.28.70.170", 1200);
        }
        return mInstance;
    }

    private TcpClient(String address, int port) {
        mAddress = address;
        mPort = port;
        mHandler = new MyHandler();
    }

    @Override
    public void setOnConnectionListener(OnConnectionListener listener) {
        mOnConnectionListener = listener;
    }

    @Override
    public void setOnPacketListener(OnPacketListener listener) {
        mOnPacketListener = listener;
    }

    @Override
    public void connect() {
        if (mState == STATE_CONNECTED)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(mAddress, mPort);
                    if (mSocket.isConnected()) {
                        mHandler.sendEmptyMessage(MyHandler.MSG_CONNECT_SUCCEED);
                        mState = STATE_CONNECTED;
                    } else {
                        Log.e("failed", "tag 1");
                        mHandler.sendEmptyMessage(MyHandler.MSG_CONNECT_FAILED);
                        mSocket.close();
                        mState = STATE_DISCONNECTED;
                    }
                    if (mState == STATE_CONNECTED) {
                        receiveLoop();
                    }
                 } catch (Exception e) {
                     Log.e("connect", e.toString());
                     mHandler.sendEmptyMessage(MyHandler.MSG_CONNECT_FAILED);
                     mState = STATE_DISCONNECTED;
                 }
            }
        }).start();
    }

    private void receiveLoop() {
        byte[] buff = new byte[Packet.SIZE_MAX];
        int offset = 0;
        int count = 0;

        while (mState == STATE_CONNECTED) {
            try {
                try {
                    if (!mSocket.isConnected()) {
                        mState = STATE_DISCONNECTED;
                        mHandler.sendEmptyMessage(MyHandler.MSG_DISCONNECTED);
                     break;
                    }
                    Log.e("receiveLoop", "tag a");
                    InputStream inputStream = mSocket.getInputStream();
                    Log.e("receiveLoop", "tag b");

                    count = inputStream.read(buff, offset, Packet.SIZE_MAX);

                    Log.e("receiveLoop", "tag c");
                    if (count >= 0)
                        offset += count;
                } catch (Exception e) {
                    Log.e("receiveLoop", e.toString());
                    mSocket.close();
                    mState = STATE_DISCONNECTED;
                    mHandler.sendEmptyMessage(MyHandler.MSG_DISCONNECTED);
                    break;
                }
                Log.e("receiveLoop", "here");
                try {
                    Packet packet = Packet.unPack(buff, offset);
                    if (packet != null) {
                        Message msg = new Message();
                        msg.what = MyHandler.MSG_NEW_PACKET;
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(KEY_PACKET, packet);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        offset = 0;
                    }
                } catch (Packet.CrcErrorException e) {
                    mHandler.sendEmptyMessage(MyHandler.MSG_WRONG_PACKET);
                    offset = 0;
                }
            } catch (Exception e) {
                Log.e("receiveLoop", e.toString());
            }
        }
    }

    @Override
    public void sendPacket(final Packet packet) {
        if (mState != STATE_CONNECTED) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buff = packet.pack();
                    OutputStream outputStream = mSocket.getOutputStream();
                    outputStream.write(buff);
                    Log.e("sendPacket", "have send");
                } catch (Exception e) {
                    Log.e("sendPacket", e.toString());
                    mHandler.sendEmptyMessage(MyHandler.MSG_IO_ERROR);
                }
            }
        }).start();
    }

    @Override
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    @Override
    public void close() {
        if (mState == STATE_DISCONNECTED) {
            Log.e("close", "already close");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("close", "tag a");
                try {
                    Log.e("close", "try to close");
                    mSocket.close();
                    //mState = STATE_DISCONNECTED;
                    //mHandler.sendEmptyMessage(MyHandler.MSG_DISCONNECTED);
                } catch (Exception e) {
                    Log.e("close", e.toString());
                }
            }
        }).start();
    }

    private class MyHandler extends Handler {
        public static final int MSG_CONNECT_FAILED = 0;
        public static final int MSG_CONNECT_SUCCEED = 1;
        public static final int MSG_DISCONNECTED = 2;
        public static final int MSG_NEW_PACKET = 3;
        public static final int MSG_WRONG_PACKET = 4;
        public static final int MSG_IO_ERROR = 5;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_FAILED:
                    if (mOnConnectionListener != null) {
                        mOnConnectionListener.onFailed();
                    }
                    break;
                case MSG_CONNECT_SUCCEED:
                    if (mOnConnectionListener != null) {
                        mOnConnectionListener.onConnected();
                    }
                    break;
                case MSG_DISCONNECTED:
                    if (mOnConnectionListener != null) {
                        mOnConnectionListener.onDisconnected();
                    }
                    break;
                case MSG_NEW_PACKET:
                    if (mOnPacketListener != null) {
                        Bundle bundle = msg.getData();
                        mOnPacketListener.onNewPacket((Packet) bundle.get(KEY_PACKET));
                    }
                    break;
                case MSG_WRONG_PACKET:
                    if (mOnPacketListener != null) {
                        mOnPacketListener.onWrongPacket();
                    }
                    break;
                case  MSG_IO_ERROR:
                    if (mOnConnectionListener != null) {
                        mOnConnectionListener.onIOError();
                    }
                    break;
            }
        }
    }
}
