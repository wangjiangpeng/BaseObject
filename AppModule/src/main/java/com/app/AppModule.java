package com.app;

import base.library.Module;

/**
 * 模块
 * 负责程序管理，应用下载
 * <p>
 * Created by wangjiangpeng01 on 2017/2/6.
 */

public class AppModule implements Module {
    private AppManager appManager;

    public AppModule() {
    }


    @Override
    public void init() {
        appManager = new AppManager();
    }


}
