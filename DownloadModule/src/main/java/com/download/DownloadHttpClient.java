package com.download;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import base.library.net.HttpClient;
import base.library.net.ResponseData;

/**
 * Created by wangjiangpeng01 on 2017/5/10.
 */

public class DownloadHttpClient extends HttpClient {

    /**
     * 下载
     */
    public ResponseData download(DownloadParam param, DownloadListener listener) {
        OkHttpClient client = createOkHttps(param);
        Request.Builder rBuilder = createRequestBuilder(param);

        // 头部续传参数
        File file = new File(param.getDownloadPath());
        if (param.getDownloadedLength() != 0 && file.exists()) {
            rBuilder.addHeader("RANGE", "bytes=" + param.getDownloadedLength() + "-");
        }
        // 下载
        ResponseData responseData = new ResponseData();
        Request request = rBuilder.build();
        InputStream is = null;
        RandomAccessFile raf = null;
        try {
            Response response = client.newCall(request).execute();
            is = response.body().byteStream();
            long total = response.body().contentLength();
            if (file.exists()) {
                raf = new RandomAccessFile(param.getDownloadPath(), "rw");
                raf.seek(param.getDownloadedLength());

            } else {
                file.createNewFile();
                raf = new RandomAccessFile(param.getDownloadPath(), "rw");
                if(total > 0){
                    raf.setLength(total);
                }
            }

            int len = 0;
            long sum = 0;
            byte[] buf = new byte[1024];
            while (((len = is.read(buf)) != -1)) {
                raf.write(buf, 0, len);
                sum += len;
                if (listener != null) {
//                    listener.onDownloaded(total, sum);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseData;
    }

    private void checkDownloadStatus(DownloadParam param){
        if(param.getStatus() != DownloadManager.STATUS_RUNNING){
            throw new StatusChangeException();
        }
    }

}
