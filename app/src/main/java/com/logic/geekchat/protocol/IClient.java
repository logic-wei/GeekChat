package com.logic.geekchat.protocol;

public interface IClient {
    interface OnPacketListener {
        void onNewPacket(Packet packet);
        void onWrongPacket();
    }

    interface OnConnectionListener {
        void onConnected();
        void onDisconnected();
        void onFailed();
        void onIOError();
    }

    //IClient getInstance();
    void setOnPacketListener(OnPacketListener listener);
    void setOnConnectionListener(OnConnectionListener listener);
    void connect();
    void close();
    void sendPacket(final Packet packet);
    boolean isConnected();
}
