package com.logic.geekchat.register;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.logic.geekchat.BaseActivity;
import com.logic.geekchat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterViewImpl extends BaseActivity implements IRegisterMVP.IView {

    @BindView(R.id.edit_text_id)
    EditText mIdEditText;
    @BindView(R.id.edit_text_password)
    EditText mPasswordEditText;
    @BindView(R.id.button_register)
    Button mRegisterButton;

    private AlertDialog mTryingDialog;
    private IRegisterMVP.IPresenter mPresenter;

    @Override
    public void changeState(int state) {
        switch (state) {
            case IRegisterMVP.IView.STATE_FAILED:
                if (mTryingDialog != null)
                    mTryingDialog.dismiss();
                Toast.makeText(this, "register failed!", Toast.LENGTH_SHORT).show();
                break;
            case IRegisterMVP.IView.STATE_SUCCEED:
                if (mTryingDialog != null)
                    mTryingDialog.dismiss();
                Toast.makeText(this, "register failed!", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case IRegisterMVP.IView.STATE_TRYING:
                mTryingDialog = new ProgressDialog.Builder(this)
                        .setMessage("waiting...")
                        .create();
                mTryingDialog.show();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        ButterKnife.bind(this);
        mPresenter = new RegisterPresenterImpl(this, this);
    }

    @OnClick({R.id.button_register})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                mPresenter.register(mIdEditText.getText().toString(), mPasswordEditText.getText().toString());
                break;
        }
    }

    @Override
    public void test(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }
}
