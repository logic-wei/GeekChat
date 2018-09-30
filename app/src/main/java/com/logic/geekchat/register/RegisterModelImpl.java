package com.logic.geekchat.register;

import android.util.Log;

import com.logic.geekchat.protocol.GeekChatJsonMethodsRpc;
import com.logic.geekchat.protocol.GeekChatProtocal;
import com.logic.geekchat.protocol.ProtocalListener;
import com.logic.geekchat.protocol.packer.IBodyPacker;
import com.logic.geekchat.protocol.packer.JsonPacker;

import org.json.JSONObject;

public class RegisterModelImpl implements IRegisterMVP.IModel {
    private static final String TAG  = "RegisterModelImpl";

    @Override
    public void register(String id, String password, OnRegisterResult onRegisterResult) {
        JSONObject json = new JSONObject();
        ProtocalListener listener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                Log.i(TAG, "onPackerReceived");
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();
                protocal.unregisterMethodsRpc(rpc);
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                Log.i(TAG, "onError");
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();
                protocal.unregisterMethodsRpc(rpc);
            }
        };
        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.register.respond", listener);
        GeekChatProtocal protocal = GeekChatProtocal.getInstance();
        protocal.registerMethodsRpc(rpc);

        try {
            json.put("method", "com.register.request");
            json.put("username", id);
            json.put("password", password);
            protocal.SendPacker(new JsonPacker(json));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
