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
    public static final int DOWNLOADS_ID = 2;
    public static final int REQUEST_HEADERS_URI = 3;
    public static final int REQUEST_HEADERS_URI_ID = 4;


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
     * app暂停
     */
    public static final int STATUS_PAUSED_BY_APP = 193;

    /**
     * 暂停
     */
    public final static int STATUS_PAUSED = 1 << 2;
    /**
     * 取消
     */
    public final static int STATUS_CANCLED = 1 << 3;
    /**
     * 成功
     */
    public final static int STATUS_SUCCESS = 1 << 4;
    /**
     * 失败
     */
    public final static int STATUS_FAILED = 1 << 5;

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
     * This request couldn't be parsed. This is also used when processing
     * requests with unknown/unsupported URI schemes.
     */
    public static final int STATUS_BAD_REQUEST = 400;

    /**
     * This download can't be performed because the content type cannot be
     * handled.
     */
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    /**
     * This download cannot be performed because the length cannot be
     * determined accurately. This is the code for the HTTP error "Length
     * Required", which is typically used when making requests that require
     * a content length but don't have one, and it is also used in the
     * client when a response is received whose length cannot be determined
     * accurately (therefore making it impossible to know when a download
     * completes).
     */
    public static final int STATUS_LENGTH_REQUIRED = 411;

    /**
     * This download was interrupted and cannot be resumed.
     * This is the code for the HTTP error "Precondition Failed", and it is
     * also used in situations where the client doesn't have an ETag at all.
     */
    public static final int STATUS_PRECONDITION_FAILED = 412;

    /**
     * The lowest-valued error status that is not an actual HTTP status code.
     */
    public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;

    /**
     * The requested destination file already exists.
     */
    public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;

    /**
     * Some possibly transient error occurred, but we can't resume the download.
     */
    public static final int STATUS_CANNOT_RESUME = 489;

    /**
     * This download was canceled
     */
    public static final int STATUS_CANCELED = 490;

    /**
     * This download has completed with an error.
     * Warning: there will be other status values that indicate errors in
     * the future. Use isStatusError() to capture the entire category.
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;

    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full.
     * Use the more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR}
     * and {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
     */
    public static final int STATUS_FILE_ERROR = 492;

    /**
     * This download couldn't be completed because of an HTTP
     * redirect response that the download manager couldn't
     * handle.
     */
    public static final int STATUS_UNHANDLED_REDIRECT = 493;

    /**
     * This download couldn't be completed because of an
     * unspecified unhandled HTTP code.
     */
    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;

    /**
     * This download couldn't be completed because of an
     * error receiving or processing data at the HTTP level.
     */
    public static final int STATUS_HTTP_DATA_ERROR = 495;

    /**
     * This download couldn't be completed because of an
     * HttpException while setting up the request.
     */
    public static final int STATUS_HTTP_EXCEPTION = 496;

    /**
     * This download couldn't be completed because there were
     * too many redirects.
     */
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    /**
     * This download has failed because requesting application has been
     * blocked by {@link NetworkPolicyManager}.
     *
     * @hide
     * @deprecated since behavior now uses
     *             {@link #STATUS_WAITING_FOR_NETWORK}
     */
    @Deprecated
    public static final int STATUS_BLOCKED = 498;

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
        public static final String SSL_MUTUAL = "ssl_mutual";
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
        public static final String DELETED = "deleted";
        public static final String ALLOWED_NETWORK_TYPES = "allowed_network_types";
        public static final String VISIBILITY = "visibility";
        public static final String ERROR = "error";
        public static final String FAILED_CONNECTIONS = "failed_connections";
        public static final String CONTROL = "control";
    }



}
