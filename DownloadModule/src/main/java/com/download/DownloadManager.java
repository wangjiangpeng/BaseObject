package com.download;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 下载管理
 * <p>
 * Created by wangjiangpeng01 on 2017/4/28.
 */

public class DownloadManager {

    /**
     * 等待
     */
    public final static int STATUS_PENDING = 1 << 0;
    /**
     * 正在执行
     */
    public final static int STATUS_RUNNING = 1 << 1;
    /**
     * 暂停
     */
    public final static int STATUS_PAUSED = 1 << 2;
    /**
     * 成功
     */
    public final static int STATUS_SUCCESSFUL = 1 << 3;
    /**
     * 失败
     */
    public final static int STATUS_FAILED = 1 << 4;
    /**
     * 错误
     */
    public static final int STATE_ERROR = 5;
    /**
     * 未知错误
     */
    public final static int ERROR_UNKNOWN = 1000;
    /**
     * 文件错误，存储出现问题的时候
     */
    public final static int ERROR_FILE_ERROR = 1001;
    /**
     * 当接收到HTTP CODE时，下载管理器无法处理
     */
    public final static int ERROR_UNHANDLED_HTTP_CODE = 1002;
    /**
     * 当接收或处理数据的错误发生在HTTP级别时
     */
    public final static int ERROR_HTTP_DATA_ERROR = 1004;
    /**
     * 当有太多的改变
     */
    public final static int ERROR_TOO_MANY_REDIRECTS = 1005;
    /**
     * 当存储空间不足时。通常情况下，这是因为SD卡已满。
     */
    public final static int ERROR_INSUFFICIENT_SPACE = 1006;
    /**
     * 当没有发现外部存储设备时。通常情况下，这是因为SD卡没有安装。
     */
    public final static int ERROR_DEVICE_NOT_FOUND = 1007;
    /**
     * 当一些可能出现暂时错误，但我们不能恢复下载。
     */
    public final static int ERROR_CANNOT_RESUME = 1008;
    /**
     * 当请求的目标文件已经存在（下载管理器不会覆盖现有文件）。
     */
    public final static int ERROR_FILE_ALREADY_EXISTS = 1009;

    private final ArrayList<WeakReference<DownloadListener>> mListeners = new ArrayList<>();

    private DownloadListener mAllListener = new DownloadListener() {

        @Override
        public void onDownloaded(long id, long total, long downloadLength) {

        }
    };

    private final DownloadProvider mProvider;

    public DownloadManager() {
        mProvider = new DownloadProvider();
    }

    public synchronized long enqueue(DownloadParam param) {
        if (param == null) {
            throw new NullPointerException();
        }

        return mProvider.insert(param);
    }

    /**
     * 删除下载
     *
     * @param id
     * @return
     */
    public synchronized boolean deleted(long id) {
        return mProvider.deleted(id) > 0;
    }

    public synchronized boolean stop(long id) {
        return false;
    }


    /**
     * 通知当前内容变更
     */
    private void notifyContentChange() {

    }

    public synchronized void addDownloadListener(DownloadListener listener) {
        WeakReference<DownloadListener> reference = new WeakReference<DownloadListener>(listener);
        mListeners.add(reference);
    }

}
