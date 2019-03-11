package com.logic.geekchat.presenter.login;

import com.logic.geekchat.LogUtil;
import com.logic.geekchat.model.ChatClient.ChatClient;
import com.logic.geekchat.model.ChatClient.IChatClient;
import com.logic.geekchat.view.login.ILoginView;

public class LoginPresenterImpl implements ILoginPresenter {
    private static String TAG = "LoginPresenterImpl";

    private ILoginView mLoginView;
    private IChatClient mChatClient;

    public LoginPresenterImpl(ILoginView loginView) {
        mLoginView = loginView;
        mChatClient = ChatClient.getInstance();
    }

    @Override
    public void login(String id, String password, boolean force) {
        LogUtil.i(TAG, "login()");
        mLoginView.changeState(ILoginView.STATE_TRYING);
        mChatClient.login(id, password, force, new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mLoginView.changeState(ILoginView.STATE_SUCCEED);
                        break;
                    case RESULT_FAILED:
                        mLoginView.changeState(ILoginView.STATE_FAILED);
                        mLoginView.changeState(ILoginView.STATE_SUCCEED);
                        break;
                }
            }
        });
    }
}
