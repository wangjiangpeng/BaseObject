package com.app.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import base.library.BaseActivity;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class DemoActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo);

        findViewById(R.id.demo_task).setOnClickListener(this);
        findViewById(R.id.demo_refresh).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.demo_task:
                startActivity(new Intent(DemoActivity.this, TaskActivity.class));
                break;

            case R.id.demo_refresh:
                startActivity(new Intent(DemoActivity.this, RefreshActivity.class));
                break;
        }
    }
}
