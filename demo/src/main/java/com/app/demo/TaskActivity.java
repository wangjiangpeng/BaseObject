package com.app.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import base.library.task.ATask;
import base.library.task.EnvInitTask;
import base.library.task.TaskManager;

/**
 * 测试
 * <p>
 * Created by wangjiangpeng01 on 2016/12/21.
 */
public class TaskActivity extends Activity implements TaskManager.ResultCallbacks {

    private static final String TAG = "TaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("TaskActivity");
        setContentView(tv);

        TaskManager taskManager = TaskManager.getInstance();
        taskManager.executeTask(EnvInitTask.class, this, "123");
    }

    @Override
    public void onFinished(ATask task, Object result) {
        if (task instanceof EnvInitTask) {
            String str = (String) result;
            Log.e(TAG, "receiver:" + str);
        }

    }
}
