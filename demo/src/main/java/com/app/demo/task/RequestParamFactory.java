package com.app.demo.task;

import com.app.demo.R;

import base.library.net.RequestParam;

/**
 * 请求数据工厂
 *
 * Created by wangjiangpeng01 on 2017/1/11.
 */
public class RequestParamFactory {

    private static final String DEFAULT_DOMAIN = "https://172.17.148.207:8443/TomcatTest/api.do?";

    public static RequestParam createTestParam(){
        RequestParam requestParam = createDefaultParam();
        requestParam.setGetData("ac=test");

        return requestParam;
    }

    private static RequestParam createDefaultParam(){
        RequestParam requestParam = new RequestParam();
        requestParam.setDomain(DEFAULT_DOMAIN);
        requestParam.addHeader("phonetype", "SAMSUNG");
        requestParam.setSSLMutual(false);
        requestParam.setTrustStoreId(R.raw.server);
        requestParam.setTrustStorePass("123456");

        return requestParam;
    }

}
