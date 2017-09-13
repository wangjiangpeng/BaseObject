package com.app.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import base.library.task.ATask;
import base.library.task.EnvInitTask;
import base.library.task.TaskCallback;
import base.library.task.TaskProgress;
import base.library.task.TaskService;

/**
 * Created by wangjiangpeng01 on 2017/5/23.
 */

public class WelcomeActivity extends Activity implements TaskCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome);

        EnvInitTask task = TaskService.getInstance().getTask(EnvInitTask.class);
        task.setTaskCallback(this);
        task.executeSerial();
    }

    @Override
    public void onFinished(ATask task, Object result) {
        startActivity(new Intent(this, DemoActivity.class));
        finish();
    }

}
