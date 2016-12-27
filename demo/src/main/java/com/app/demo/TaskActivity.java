package com.app.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import base.library.task.ATask;
import base.library.task.EnvInitTask;
import base.library.task.ResultReceiver;
import base.library.task.TaskManager;

/**
 * 测试
 *
 * Created by wangjiangpeng01 on 2016/12/21.
 */
public class TaskActivity extends Activity implements ResultReceiver{

    private static final String TAG = "TaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("TaskActivity");
        setContentView(tv);

        ATask task = TaskManager.getInstance().getTask(EnvInitTask.class);
        task.execute(this, "aaaaa");

    }

    @Override
    public void receiver(ATask task, Object e) {
        if(task instanceof EnvInitTask){
            String str = (String)e;
            Log.e(TAG, "receiver:"+str);
        }
    }

}
