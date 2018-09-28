package com.logic.geekchat;

import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        finish();
    }
}
