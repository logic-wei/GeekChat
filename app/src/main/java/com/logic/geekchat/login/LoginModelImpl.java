package com.logic.geekchat.login;

import android.util.Log;

import com.logic.geekchat.Util;
import com.logic.geekchat.account.ServerConfig;
import com.logic.geekchat.protocol.GeekChatJsonMethodsRpc;
import com.logic.geekchat.protocol.GeekChatProtocal;
import com.logic.geekchat.protocol.ProtocalListener;
import com.logic.geekchat.protocol.packer.IBodyPacker;
import com.logic.geekchat.protocol.packer.JsonPacker;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class LoginModelImpl implements ILoginMVP.IModel {
    static final String TAG = "LoginModelImpl";
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
        JSONObject json = new JSONObject();
        ProtocalListener seedListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                Log.i(TAG, "onPackerReceived");
                JsonPacker jsonPacker = (JsonPacker)packer;
                JSONObject json = jsonPacker.getJson();
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();

                if(rpc.getMethods() == "com.login.seed.respond") {
                    Log.i(TAG, "com.login.seed.respond");
                    String seedString = null;
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(ServerConfig.GEEKCHAT_SERVER_PASSWORD_LENS);
                        seedString = json.getString("seed");
                        if (seedString != null) {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            messageDigest.update(seedString.getBytes("UTF-8"));
                            byteBuffer.put(password.getBytes());
                            messageDigest.update(byteBuffer.array());

                            json = new JSONObject();
                            json.put("method", "com.login.request");
                            json.put("username", id);
                            json.put("crypto", Util.bytesToHexString(messageDigest.digest()));
                            protocal.SendPacker(new JsonPacker(json));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onLoginResult.onResult(OnLoginResult.RESULT_WRONG_PASSWORD);
                    }
                    protocal.unregisterMethodsRpc(rpc);

                    rpc.setMethods("com.login.respond");
                    protocal.registerMethodsRpc(rpc);
                } else if(rpc.getMethods() == "com.login.respond"){
                    Log.i(TAG, "com.login.respond");
                    try {
                        int errno = json.getInt("errno");
                        if(errno == 0) {
                            onLoginResult.onResult(OnLoginResult.RESULT_SUCCEED);
                            /*TODO: save token*/
                        } else {
                            onLoginResult.onResult(OnLoginResult.RESULT_WRONG_PASSWORD);
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        onLoginResult.onResult(OnLoginResult.RESULT_WRONG_PASSWORD);
                    }

                    protocal.unregisterMethodsRpc(rpc);
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                Log.i(TAG, "onError");
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();
                protocal.unregisterMethodsRpc(rpc);
                onLoginResult.onResult(OnLoginResult.RESULT_WRONG_PASSWORD);
            }
        };
        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.login.seed.respond", seedListener);
        GeekChatProtocal protocal = GeekChatProtocal.getInstance();
        protocal.registerMethodsRpc(rpc);

        try {
            json.put("method", "com.login.seed.request");
            json.put("username", id);
            protocal.SendPacker(new JsonPacker(json));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
