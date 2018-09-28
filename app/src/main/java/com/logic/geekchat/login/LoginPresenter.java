package com.logic.geekchat.login;

import android.util.Log;

public class LoginPresenter implements ILoginMVP.IPresenter {

    ILoginMVP.IModel mModel;
    ILoginMVP.IView mView;

    LoginPresenter(ILoginMVP.IView view) {
        mView = view;
        mModel = new LoginModelImpl();
    }

    @Override
    public void login(String id, String password) {
        mView.changeState(ILoginMVP.IView.STATE_TRYING);
        mModel.login(id, password, new ILoginMVP.IModel.OnLoginResult() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mView.changeState(ILoginMVP.IView.STATE_SUCCEED);
                        Log.e("presenter", "login succeed");
                        break;
                    case RESULT_UNKNOWN_ERROR:
                        mView.changeState(ILoginMVP.IView.STATE_FAILED);
                        Log.e("presenter", "login failed");
                        break;
                    case RESULT_WRONG_PASSWORD:
                        mView.changeState(ILoginMVP.IView.STATE_FAILED);
                        Log.e("presenter", "password error");
                        break;
                }
            }
        });
    }
}
