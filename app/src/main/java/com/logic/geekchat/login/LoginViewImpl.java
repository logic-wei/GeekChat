package com.logic.geekchat.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.logic.geekchat.BaseActivity;
import com.logic.geekchat.MainActivity;
import com.logic.geekchat.R;
import com.logic.geekchat.friends.FriendsViewImpl;
import com.logic.geekchat.protocol.IClient;
import com.logic.geekchat.protocol.OkClient;
import com.logic.geekchat.protocol.Packet;
import com.logic.geekchat.protocol.TcpClient;
import com.logic.geekchat.register.RegisterViewImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginViewImpl extends BaseActivity implements ILoginMVP.IView {

    @BindView(R.id.edit_text_id)
    EditText mIdEditText;
    @BindView(R.id.edit_text_password)
    EditText mPasswordEditText;
    @BindView(R.id.button_register)
    Button mRegisterButton;
    @BindView(R.id.button_login)
    Button mLoginButton;

    private ILoginMVP.IPresenter mPresenter;
    private AlertDialog mTryingDialog;

    @Override
    public void changeState(int state) {
        switch (state) {
            case ILoginMVP.IView.STATE_TRYING:
                mTryingDialog = new ProgressDialog.Builder(this)
                        .setMessage("trying to login...")
                        .create();
                mTryingDialog.show();
                break;
            case ILoginMVP.IView.STATE_SUCCEED:
                if (mTryingDialog != null)
                    mTryingDialog.dismiss();
                Toast.makeText(this, "login succeed!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, FriendsViewImpl.class));
                break;
            case ILoginMVP.IView.STATE_FAILED:
                if (mTryingDialog != null)
                    mTryingDialog.dismiss();
                Toast.makeText(this, "login failed!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        mPresenter = new LoginPresenter(this);
    }

    @OnClick({R.id.button_login, R.id.button_register, R.id.button_test, R.id.button_test2, R.id.button_test3})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                startActivity(new Intent(this, RegisterViewImpl.class));
                //startActivity(new Intent(this, LoginViewImpl.class));
                break;
            case R.id.button_login:
                //mPresenter.login(mIdEditText.getText().toString(), mPasswordEditText.getText().toString());
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.button_test://connect
                break;
            case R.id.button_test2://send packet

                break;
            case R.id.button_test3://close

                break;
        }
    }
    OkClient mClient;
}
