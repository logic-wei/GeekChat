package com.logic.geekchat.presenter.register;

import com.logic.geekchat.BaseActivity;
import com.logic.geekchat.model.ChatClient.ChatClient;
import com.logic.geekchat.model.ChatClient.IChatClient;
import com.logic.geekchat.view.register.IRegisterView;

public class RegisterPresenterImpl extends BaseActivity implements IRegisterPresenter {
    IRegisterView mRegisterView;
    IChatClient mChatClient;

    public RegisterPresenterImpl(IRegisterView registerView) {
        mRegisterView = registerView;
        mChatClient = ChatClient.getInstance();
    }

    @Override
    public void register(String id, String password) {
        mRegisterView.changeState(IRegisterView.STATE_TRYING);
        mChatClient.register(id, password, new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mRegisterView.changeState(IRegisterView.STATE_SUCCEED);
                        break;
                    case RESULT_FAILED:
                        mRegisterView.changeState(IRegisterView.STATE_FAILED);
                        break;
                }
            }
        });
    }
}
