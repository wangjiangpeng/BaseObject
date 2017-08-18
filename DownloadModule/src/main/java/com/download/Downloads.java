package com.download;

import android.net.ConnectivityManager;
import android.net.Uri;

/**
 * Created by wangjiangpeng01 on 2017/8/18.
 */

public class Downloads {


    /**
     * 访问地址
     */
    public static final Uri CONTENT_URI = Uri.parse("content://com.download.provider/downloads");
    public static final Uri CONTENT_URI_REQUEST = Uri.parse("content://com.download.provider/request");
    public static final int DOWNLOADS = 1;
    public static final int REQUEST_HEADERS_URI = 2;


    /**
     * 标识用于 {@link #setAllowedNetworkTypes} 与
     * {@link ConnectivityManager#TYPE_MOBILE}相一致
     */
    public static final int NETWORK_MOBILE = 1 << 0;

    /**
     * 标识用于 {@link #setAllowedNetworkTypes} 与
     * {@link ConnectivityManager#TYPE_WIFI}相一致
     */
    public static final int NETWORK_WIFI = 1 << 1;

    /**
     * 标识用于 {@link #setAllowedNetworkTypes} 与
     * {@link ConnectivityManager#TYPE_BLUETOOTH}相一致
     */
    public static final int NETWORK_BLUETOOTH = 1<< 2;

    /**
     * 标识用于 {@link #setAllowedNetworkTypes} 与
     * {@link ConnectivityManager#TYPE_WIFI}相一致
     */
    public static final int NETWORK_ALL = ~0;

    /**
     * 最多重试次数
     */
    public static final int RETRY_MAX = 3;

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
     * 此下载中遇到的一些网络错误，重试请求前等待。
     */
    public static final int STATUS_WAITING_TO_RETRY = 194;
    /**
     * 此下载正在等待网络连接继续进行。
     */
    public static final int STATUS_WAITING_FOR_NETWORK = 195;

    /**
     * 此下载超过了移动网络的大小限制，正在等待Wi-Fi连接继续进行。
     */
    public static final int STATUS_QUEUED_FOR_WIFI = 196;
    /**
     * 由于存储空间不足，无法完成此下载。通常，这是因为SD卡是满的。
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;

    /**
     * 此下载无法完成，因为没有找到外部存储设备。通常，这是因为SD卡没有安装。
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;

    /**
     * 错误
     */
    public final static int STATE_ERROR = 5;
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


    /**
     * 控制下载开始
     */
    public static final int CONTROL_RUN = 0;

    /**
     * 控制下载暂停
     */
    public static final int CONTROL_PAUSED = 1;


    protected static class Request {
        public static final String TABLE_NAME = "request";
        public static final String ID = "id";
        public static final String DOWNLOAD_ID = "download_id";
        public static final String URL = "url";
        public static final String HEADERS = "headers";
        public static final String POSTS = "posts";
        public static final String IS_SSL_MUTUAL = "is_ssl_mutual";
        public static final String KEY_STORE_ID = "keyStore_id";
        public static final String TRUST_STORE_ID = "trustStore_id";
        public static final String KEY_STORE_PASS = "keyStore_pass";
        public static final String TRUST_STORE_PASS = "trustStore_pass";
    }

    protected static class Info {
        public static final String TABLE_NAME = "downloadinfo";
        public static final String ID = "id";
        public static final String FILE_NAME = "file_name";
        public static final String TOTAL_BYTES = "total_bytes";
        public static final String CURRENT_BYTES = "current_bytes";
        public static final String STATUS = "status";
        public static final String IS_DELETED = "is_deleted";
        public static final String ALLOWED_NETWORK_TYPES = "allowed_network_types";
        public static final String VISIBILITY = "visibility";
        public static final String ERROR = "error";
        public static final String FAILED_CONNECTIONS = "failed_connections";
        public static final String CONTROL = "control";
    }



}
