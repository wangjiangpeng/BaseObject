package com.app.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.app.demo.task.TestTask;

import base.library.BaseActivity;
import base.library.task.ATask;
import base.library.task.TaskCallbacks;
import base.library.task.TaskPool;

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

        Button btn1 = (Button) findViewById(R.id.task_btn1);
        btn1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_btn1: {
                TestTask task = TaskPool.getTask(TestTask.class);
                task.reExecute(this);
            }
            break;
        }
    }

    @Override
    public void onFinished(ATask task, Object result) {
        Log.e(TAG, "receiver:callbacks1" + result);
    }
}
