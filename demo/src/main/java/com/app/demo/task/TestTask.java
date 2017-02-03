package com.app.demo.task;

import base.library.MLog;
import base.library.net.HttpRequest;
import base.library.net.ResponseData;
import base.library.task.ATask;

/**
 * Created by wangjiangpeng01 on 2017/1/13.
 */

public class TestTask extends ATask {
    @Override
    protected Object doInBackground(Object... objs) {
        HttpRequest request = new HttpRequest();
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
