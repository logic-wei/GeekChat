package com.logic.geekchat.protocol;

import com.logic.geekchat.protocol.packer.IBodyPacker;

public interface ProtocalListener {
    public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer);
    public void onError(GeekChatJsonMethodsRpc rpc, Exception ex);
}
