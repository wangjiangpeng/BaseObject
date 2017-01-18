package com.app.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.app.demo.task.TestTask;

import base.library.task.ATask;
import base.library.task.TaskManager;

/**
 * 测试
 * <p>
 * Created by wangjiangpeng01 on 2016/12/21.
 */
public class TaskActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TaskActivity";

    private TaskManager.ResultCallbacks callbacks1 = new TaskManager.ResultCallbacks() {
        @Override
        public void onFinished(ATask task, Object result) {
            Log.e(TAG, "receiver:callbacks1" + result);
        }
    };

    private TaskManager.ResultCallbacks callbacks2 = new TaskManager.ResultCallbacks() {
        @Override
        public void onFinished(ATask task, Object result) {
            Log.e(TAG, "receiver:callbacks2" + result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.task);

        Button btn1 = (Button) findViewById(R.id.task_btn1);
        btn1.setOnClickListener(this);


        Button btn2 = (Button) findViewById(R.id.task_btn2);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_btn1: {
                TaskManager taskManager = TaskManager.getInstance();
                taskManager.restartTask(TestTask.class, callbacks1);
            }
            break;

            case R.id.task_btn2: {
                TaskManager taskManager = TaskManager.getInstance();
                taskManager.restartTask(TestTask.class, callbacks2);
            }
            break;
        }
    }

}
