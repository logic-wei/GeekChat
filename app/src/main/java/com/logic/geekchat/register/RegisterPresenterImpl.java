package com.logic.geekchat.register;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class RegisterPresenterImpl implements IRegisterMVP.IPresenter {
    private IRegisterMVP.IView mView;
    private IRegisterMVP.IModel mModel;
    private Context mContext;

    public RegisterPresenterImpl(Context context, IRegisterMVP.IView view) {
        mView = view;
        mContext = context;
        mModel = new RegisterModelImpl();
    }

    @Override
    public void register(String id, String password) {
        mView.changeState(IRegisterMVP.IView.STATE_TRYING);
        mModel.register(id, password, new IRegisterMVP.IModel.OnRegisterResult() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mView.changeState(IRegisterMVP.IView.STATE_SUCCEED);
                        break;
                    default:
                        mView.changeState(IRegisterMVP.IView.STATE_FAILED);
                }
            }
        });
    }

    @Override
    public void test(String info) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("149.28.70.170", 24343);
                    InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    final char[] buff = new char[100];
                    bufferedReader.read(buff);
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.test(String.valueOf(buff));
                        }
                    });
                    socket.close();
                } catch (Exception e) {
                    Log.e("test", e.toString());
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.test("failed");
                        }
                    });
                }
            }
        }.start();
    }
}
