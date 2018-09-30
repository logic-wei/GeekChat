package com.logic.geekchat.protocol;

import com.logic.geekchat.protocol.packer.IBodyPacker;

public class GeekChatJsonMethodsRpc {
    private String methods = null;
    private ProtocalListener listener = null;

    public GeekChatJsonMethodsRpc(String methods, ProtocalListener listener) {
        this.methods = methods;
        this.listener = listener;
    }

    public void handleBodyPacker(IBodyPacker packer) {
        listener.onPackerReceived(this, packer);
    }

    public void handlePackerError(Exception ex) {
        listener.onError(this, ex);
    }

    public String getMethods() {
        return this.methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }
}
