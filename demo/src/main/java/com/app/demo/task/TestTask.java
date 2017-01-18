package com.app.demo.task;

import java.io.IOException;

import base.library.MLog;
import base.library.net.HttpRequest;
import base.library.task.ATask;

/**
 * Created by wangjiangpeng01 on 2017/1/13.
 */

public class TestTask extends ATask {
    @Override
    protected Object doInBackground(Object... objs) {
        HttpRequest request = new HttpRequest();
        try {
            byte[] bytes = request.request(RequestParamFactory.createTestParam());

            String str = new String(bytes);
            MLog.http("WJP", str);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
