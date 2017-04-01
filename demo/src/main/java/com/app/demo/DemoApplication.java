package com.app.demo;

import com.app.AppModule;

import base.library.BaseApplication;
import base.library.task.EnvInitTask;
import base.library.task.TaskManager;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class DemoApplication extends BaseApplication {

    @Override
    public void envInit() {
        try {
            AppModule.getInstance().load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
