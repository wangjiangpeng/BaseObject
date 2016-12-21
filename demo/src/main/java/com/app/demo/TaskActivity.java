package com.app.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import base.library.task.ATask;
import base.library.task.TaskPool;

/**
 * Created by wangjiangpeng01 on 2016/12/21.
 */
public class TaskActivity extends Activity {

    private static final String TAG = "TaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("TaskActivity");
        setContentView(tv);

        TaskPool.execute(new Task(), "task params");
    }

    private class Task extends ATask<String ,String> {

        @Override
        protected String doInBackground(Object... objs) {
            Object[] os = objs;
            Log.e(TAG, "doInBackground" + (String)os[0]);

            return "Task";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.e(TAG, "onPostExecute:"+s);
        }
    }
}
