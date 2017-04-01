package com.app;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import base.library.task.TaskThreadPool;

/**
 * 模块
 * 负责程序管理，应用下载
 * <p>
 * Created by wangjiangpeng01 on 2017/2/6.
 */

public class AppModule {

    private Object lock = new Object();
    private volatile boolean loaded = false;

    private AppManager appManager;

    private static AppModule sAppModule;

    /**
     * 单例
     *
     * @return
     */
    public static AppModule getInstance() {
        if (sAppModule == null) {
            synchronized (AppModule.class) {
                if (sAppModule == null) {
                    sAppModule = new AppModule();
                }
            }
        }

        return sAppModule;
    }

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
