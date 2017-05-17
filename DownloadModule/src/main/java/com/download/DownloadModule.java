package com.download;

import base.library.module.Module;

/**
 * Created by wangjiangpeng01 on 2017/5/12.
 */

public class DownloadModule implements Module {

    private Object lock = new Object();
    private volatile boolean loaded = false;

    private DownloadManager mDownloadManager;

    @Override
    public void load() {
        synchronized (lock) {
            if(!loaded){
                loaded = true;
                mDownloadManager = new DownloadManager();
            }
        }
    }

    /**
     * app管理
     *
     * @return
     */
    public DownloadManager getDownloadManager(){
        if(mDownloadManager == null){
            load();
        }
        return mDownloadManager;
    }

}
