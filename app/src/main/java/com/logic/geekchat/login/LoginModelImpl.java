package com.logic.geekchat.login;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.logic.geekchat.protocol.IClient;
import com.logic.geekchat.protocol.OkClient;
import com.logic.geekchat.protocol.Packet;
import com.logic.geekchat.protocol.TcpClient;

import org.json.JSONObject;

import java.security.MessageDigest;

public class LoginModelImpl implements ILoginMVP.IModel {

    final static int STATE_OFFLINE = 0;
    final static int STATE_SEND_ID = 1;
    final static int STATE_SEND_PASSWORD = 2;
    final static int STATE_ONLINE = 3;

    private int mState = STATE_OFFLINE;
    private OnLoginResult mOnLoginResult;

    public LoginModelImpl() {

    }

    @Override
    public void login(final String id, final String password, final OnLoginResult onLoginResult) {
        mOnLoginResult = onLoginResult;
        OkClient.getInstance().login(id, password, new OkClient.LoginListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_LOGIN_SUCCEED:
                        onLoginResult.onResult(OnLoginResult.RESULT_SUCCEED);
                        break;
                    case RESULT_LOGIN_FAILED:
                        onLoginResult.onResult(OnLoginResult.RESULT_WRONG_PASSWORD);
                        break;
                }
            }
        });
    }
}
