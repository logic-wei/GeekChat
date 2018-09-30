package com.logic.geekchat;

import android.app.Application;
import android.util.Log;
import com.logic.geekchat.account.User;

import com.xuhao.android.libsocket.sdk.OkSocket;

public class GeekChatApplication extends Application {
    private User mUser = new User();

    @Override
    public void onCreate() {
        super.onCreate();
        OkSocket.initialize(this, false);
    }

    public User getUser() {
        return mUser;
    }
}
