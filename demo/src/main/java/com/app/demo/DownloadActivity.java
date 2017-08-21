package com.app.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.download.DownloadInfo;
import com.download.DownloadManager;
import com.download.DownloadModule;

import java.util.List;

import base.library.BaseActivity;
import base.library.module.ModuleManager;

/**
 * Created by wangjiangpeng01 on 2017/8/16.
 */

public class DownloadActivity extends BaseActivity implements OnClickListener {

    long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.download);
        findViewById(R.id.download_enqueue).setOnClickListener(this);
        findViewById(R.id.download_deleted).setOnClickListener(this);
        findViewById(R.id.download_pause).setOnClickListener(this);
        findViewById(R.id.download_query).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        DownloadManager manager = ModuleManager.getInstance().getModule(DownloadModule.class).getDownloadManager();
        switch (v.getId()) {
            case R.id.download_enqueue:
                DownloadManager.Request request = new DownloadManager.Request();
                request.setUrl("http://gh-pages.clockworkmod.com/ROMManagerManifest/devices.js");
                request.setFileName("/mnt/sdcard/demo.js");
                id = manager.enqueue(request);
                Log.e("WJP", "id" + id);
                break;

            case R.id.download_deleted:
                manager.deleted(id);
                break;

            case R.id.download_pause:
                manager.pause(id);
                break;

            case R.id.download_query:
                List<DownloadInfo> list = manager.query();
                for (DownloadInfo info : list) {
                    Log.e("WJP", "id:" + info.getId() + " status:" + info.getStatus());
                }
                break;
        }


    }

}
