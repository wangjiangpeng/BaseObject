package com.app.demo;

import android.os.Environment;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import base.library.BaseApplication;
import base.library.MLog;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class DemoApplication extends BaseApplication {

    @Override
    public void envInit() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
