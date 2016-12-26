package com.app.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import base.library.task.ATask;
import base.library.task.ResultReceiver;

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

        Task[] task = new Task[10];
        for(int i = 0 ; i < 10 ; i++){
            task[i] = new Task();
            task[i].execute(this,i);
        }
        task[2].cancel(true);
        task[9].cancel(true);

    }

    @Override
    public void receiver(ATask task, Object e) {
        if(task instanceof Task){
            String str = (String)e;
            Log.e(TAG, "receiver:"+str);
        }
    }

    private static class Task extends ATask<String ,String> {

        private int i;

        @Override
        protected String doInBackground(Object... objs) {
            i = (int)objs[0];
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "Task:"+i;
        }

    }

}
