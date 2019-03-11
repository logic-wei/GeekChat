package com.logic.geekchat.view.register;

public interface IRegisterView {
    int STATE_TRYING = 0;
    int STATE_SUCCEED = 1;
    int STATE_FAILED = 2;

    void changeState(int state);
}
