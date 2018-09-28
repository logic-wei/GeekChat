package com.logic.geekchat.register;

public interface IRegisterMVP {
    interface IModel {
        interface OnRegisterResult {
            int RESULT_SUCCEED = 0;
            int RESULT_UNKNOWN_ERROR = 1;
            int RESULT_WRONG_PASSWORD = 2;
            int RESULT_REPEATING_ID = 3;

            void onResult(int result);
        }
        void register(String id, String password, OnRegisterResult onRegisterResult);
    }

    interface IView {
        int STATE_TRYING = 0;
        int STATE_FAILED = 1;
        int STATE_SUCCEED = 2;
        void changeState(int state);
        //for test
        void test(String info);
    }

    interface IPresenter {
        void register(String id, String password);
        //for test
        void test(String info);
    }
}
