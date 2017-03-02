package com.app.demo;

import base.library.BaseApplication;
import base.library.MLog;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class DemoApplication extends BaseApplication {

    @Override
    public void envInit() {
        try {
            MLog.e("WJP", "envInit");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
