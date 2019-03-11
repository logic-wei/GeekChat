package com.logic.geekchat.view.login;

public interface ILoginView {
    int STATE_TRYING = 0;
    int STATE_SUCCEED = 1;
    int STATE_FAILED = 2;

    void changeState(int state);
}
