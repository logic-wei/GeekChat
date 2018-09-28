package com.logic.geekchat.login;

public interface ILoginMVP {
    interface IModel {
        interface OnLoginResult {
            int RESULT_SUCCEED = 0;
            int RESULT_UNKNOWN_ERROR = 1;
            int RESULT_WRONG_PASSWORD = 2;

            void onResult(int result);
        }
        void login(String id, String password, OnLoginResult onLoginResult);
    }

    interface IView {
        int STATE_TRYING = 0;
        int STATE_SUCCEED = 1;
        int STATE_FAILED = 2;

        void changeState(int state);
    }

    interface IPresenter {
        void login(String id, String password);
    }
}
