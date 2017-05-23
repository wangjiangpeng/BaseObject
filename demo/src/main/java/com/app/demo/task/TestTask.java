package com.app.demo.task;

import base.library.MLog;
import base.library.net.HttpClient;
import base.library.net.ResponseData;
import base.library.task.ATask;

/**
 * 测试接口
 * <p>
 * Created by wangjiangpeng01 on 2017/1/13.
 */
public class TestTask extends ATask {

    @Override
    protected Object doInBackground(Object... objs) {
        HttpClient request = new HttpClient();
        ResponseData data = request.request(RequestParamFactory.createTestParam());
        if (data.isSuccessful()) {
            String str = new String(data.getData());
            MLog.http("TestTask", str);

        } else {
            MLog.http("TestTask", "error");
        }

        return null;
    }
}
