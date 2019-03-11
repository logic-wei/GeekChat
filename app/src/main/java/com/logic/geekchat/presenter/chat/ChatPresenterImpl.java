package com.logic.geekchat.presenter.chat;

import android.util.Log;

import com.logic.geekchat.LogUtil;
import com.logic.geekchat.model.ChatClient.ChatClient;
import com.logic.geekchat.model.ChatClient.IChatClient;
import com.logic.geekchat.view.chat.IChatView;

public class ChatPresenterImpl implements IChatPresenter {
    private static String TAG = "ChatPresenterImpl";

    private IChatView mChatView;
    private IChatClient mChatClient;

    public ChatPresenterImpl(IChatView chatView) {
        mChatView = chatView;
        mChatClient = ChatClient.getInstance();
        mChatClient.registerMessageReceiver(new IChatClient.MessageReceiver() {
            @Override
            public void onNewMessage(String message, String from, String uuid) {
                mChatView.onNewMessage(message);
            }
        });
    }

    @Override
    public void send(String message, String to) {
        LogUtil.i(TAG, "send()");
        mChatClient.send(message, to, new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                LogUtil.i(TAG, "result:"+result);
            }
        });
    }
}
