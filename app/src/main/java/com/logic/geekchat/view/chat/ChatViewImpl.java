package com.logic.geekchat.view.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.logic.geekchat.BaseActivity;
import com.logic.geekchat.R;
import com.logic.geekchat.presenter.chat.ChatPresenterImpl;
import com.logic.geekchat.presenter.chat.IChatPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatViewImpl extends BaseActivity implements IChatView {
    private IChatPresenter mChatPresenter;

    @BindView(R.id.text_view_history)
    TextView mHistoryText;
    @BindView(R.id.edit_text_send)
    EditText mToSendText;
    @BindView(R.id.button_send)
    Button mSendButton;

    private String mChatWith;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.chat);
        ButterKnife.bind(this);
        mChatPresenter = new ChatPresenterImpl(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        mChatWith = intent.getStringExtra(EXTRA_CHAT_WITH);
        Toast.makeText(this, "chat with "+mChatWith, Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.button_send})
    void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.button_send:
                onSendMessage();
                break;
        }
    }

    private void onSendMessage() {
        mChatPresenter.send(mToSendText.getText().toString(), mChatWith);
    }

    @Override
    public void onNewMessage(String message) {
        mHistoryText.append(message);
    }
}
