package com.app;

import base.library.module.Module;

/**
 * 模块
 * 负责程序管理，应用下载
 * <p>
 * Created by wangjiangpeng01 on 2017/2/6.
 */

public class AppModule implements Module {

    private Object lock = new Object();
    private volatile boolean loaded = false;

    private AppManager appManager;

    public AppModule() {
    }

    /**
     * 加载模块
     */
    public void load() {
        synchronized (lock) {
            if(!loaded){
                loaded = true;
                appManager = new AppManager();
            }
        }
    }

    /**
     * app管理
     *
     * @return
     */
    public AppManager getAppManager(){
        if(appManager == null){
            load();
        }
        return appManager;
    }

}
