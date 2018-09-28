package com.logic.geekchat;

import android.app.Application;
import android.util.Log;

import com.xuhao.android.libsocket.sdk.OkSocket;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkSocket.initialize(this);
        Log.e("myapp", "create");
    }
}
