package com.app.demo;

import com.app.AppModule;
import com.download.DownloadModule;

import base.library.BaseApplication;
import base.library.module.ModuleManager;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class DemoApplication extends BaseApplication {


    @Override
    public void addModule(ModuleManager manager) {
        manager.addModule(new AppModule());
        manager.addModule(new DownloadModule());
    }
}
