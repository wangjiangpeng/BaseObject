package com.download;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import base.library.task.ATask;

/**
 * Created by wangjiangpeng01 on 2017/5/11.
 */

public class DownloadService {
    /**
     * 默认下载队列空间大小，根据处理器个数，设置最大下载线程数
     */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_DOWNLOAD_CAPACITY = PROCESSORS > 1 ? PROCESSORS : 1;

    /**
     * 正在下载
     */
    private final Hashtable<Long, DownloadParam> mDownloads;

    /**
     * 正在下载
     */
    private final DownloadTask[] mTasks;

    private int maxDownloadCount;

    private final DownloadProvider mProvider;

    protected DownloadService(DownloadProvider provider) {
        maxDownloadCount = DEFAULT_DOWNLOAD_CAPACITY;
        mDownloads = new Hashtable<>();
        mTasks = new DownloadTask[DEFAULT_DOWNLOAD_CAPACITY];
        mProvider = provider;
    }

    protected void startService() {
        new ServiceThread().start();
    }

    private class ServiceThread extends Thread {

        @Override
        public void run() {
            super.run();

            update();
        }
    }

    private void update() {
        List<DownloadParam> list = mProvider.queryAllActive();
        Iterator<DownloadParam> itr = list.iterator();
        while (itr.hasNext()) {
            DownloadParam tmp = itr.next();
            DownloadParam param = mDownloads.get(tmp.getId());
            if (param != null) {
                param.update(tmp);
                continue;
            }
        }

    }

    private static class DownloadTask extends ATask<Integer> {

        private DownloadParam mParam;

        private DownloadListener mListener;

        public DownloadTask(DownloadParam param, DownloadListener listener) {
            mParam = param;
            mListener = listener;
        }

        @Override
        protected Object doInBackground(Object... objs) {
            DownloadHttpClient httpClient = new DownloadHttpClient();
            httpClient.download(mParam, mListener);

            return null;
        }

        public long getId() {
            return mParam.getId();
        }
    }
}
