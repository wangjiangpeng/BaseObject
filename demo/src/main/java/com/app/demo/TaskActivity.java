package com.app.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.demo.task.TestTask;

import base.library.BaseActivity;
import base.library.task.ATask;
import base.library.task.TaskCallbacks;
import base.library.task.TaskService;

/**
 * 测试
 * <p>
 * Created by wangjiangpeng01 on 2016/12/21.
 */
public class TaskActivity extends BaseActivity implements View.OnClickListener, TaskCallbacks {

    private static final String TAG = "TaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.task);

        findViewById(R.id.task_btn1).setOnClickListener(this);
        findViewById(R.id.task_btn2).setOnClickListener(this);
        findViewById(R.id.task_btn3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_btn1: {
                TestTask task = TaskService.getInstance().getTask(TestTask.class);
                task.reExecute(this);
            }
            break;

            case R.id.task_btn2: {
                TestTask task = TaskService.getInstance().getTask(TestTask.class);
                task.cancel(true);
            }
            break;

            case R.id.task_btn3: {
            }
            break;
        }
    }

    @Override
    public void onFinished(ATask task, Object result) {
        Log.e(TAG, "receiver:callbacks1" + result);
    }
}
