package com.logic.geekchat.view.chat;

public interface IChatView {
    String EXTRA_CHAT_WITH = "chat_with";

    void onNewMessage(String message);
}
