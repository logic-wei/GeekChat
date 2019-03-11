package com.logic.geekchat.view.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.logic.geekchat.BaseActivity;
import com.logic.geekchat.MainActivity;
import com.logic.geekchat.R;
import com.logic.geekchat.presenter.login.ILoginPresenter;
import com.logic.geekchat.presenter.login.LoginPresenterImpl;
import com.logic.geekchat.view.register.RegisterViewImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginViewImpl extends BaseActivity implements ILoginView {
    @BindView(R.id.edit_text_id)
    EditText mIdEditText;
    @BindView(R.id.edit_text_password)
    EditText mPasswordEditText;
    @BindView(R.id.button_register)
    Button mRegisterButton;
    @BindView(R.id.button_login)
    Button mLoginButton;
    @BindView(R.id.check_box_force)
    CheckBox mForceBox;
    private boolean mForceState = false;

    private ILoginPresenter mPresenter;
    private AlertDialog mTryingDialog;

    @Override
    public void changeState(int state) {
        switch (state) {
            case STATE_TRYING:
                mTryingDialog = new ProgressDialog.Builder(this)
                        .setMessage("trying to login...")
                        .create();
                mTryingDialog.show();
                break;
            case STATE_SUCCEED:
                if (mTryingDialog != null)
                    mTryingDialog.dismiss();
                Toast.makeText(this, "login succeed!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case STATE_FAILED:
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
        mPresenter = new LoginPresenterImpl(this);
        mForceBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mForceState = isChecked;
            }
        });

    }

    @OnClick({R.id.button_login, R.id.button_register})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                startActivity(new Intent(this, RegisterViewImpl.class));
                break;
            case R.id.button_login:
                mPresenter.login(mIdEditText.getText().toString(), mPasswordEditText.getText().toString(), mForceState);
                break;
        }
    }
}
