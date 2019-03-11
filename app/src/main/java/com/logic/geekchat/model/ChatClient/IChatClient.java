package com.logic.geekchat.model.ChatClient;

import java.util.List;

public interface IChatClient {
    interface MessageReceiver {
        void onNewMessage(String message, String from, String uuid);
    }

    interface ResultListener {
        int RESULT_SUCCEED = 0;
        int RESULT_FAILED = 1;
        void onResult(int result);
    }

    void login(String id, String password, boolean force, ResultListener listener);
    void logout(ResultListener listener);
    void send(String message, String to, ResultListener listener);
    void registerMessageReceiver(MessageReceiver receiver);
    void syncFriends(ResultListener listener);
    List<Friend> getFriends();
    void register(String id, String password, ResultListener listener);
    void addFriend(String id, ResultListener listener);
    void heartbeat(ResultListener listener);
    String getToken();
    void setToken(String token);
}
