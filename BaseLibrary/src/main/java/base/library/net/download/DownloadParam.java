package base.library.net.download;

import android.net.ConnectivityManager;

import base.library.net.RequestParam;

/**
 * 下载请求参数
 *
 * Created by wangjiangpeng01 on 2017/4/5.
 */

public class DownloadParam extends RequestParam {
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
     * 添加到数据库的下载id
     */
    private long downloadId;
    /**
     * 本地下载路径
     */
    private String downloadPath;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 续传
     */
    private long range;

    /**
     * 状态
     */
    private int state;

    /**
     * 允许下载的网络类型，默认所有网络都可下载
     */
    private int allowedNetworkTypes = ~0;

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getRange() {
        return range;
    }

    public void setRange(long range) {
        this.range = range;
    }

    protected long getDownloadId() {
        return downloadId;
    }

    protected void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getAllowedNetworkTypes() {
        return allowedNetworkTypes;
    }

    public void setAllowedNetworkTypes(int allowedNetworkTypes) {
        this.allowedNetworkTypes = allowedNetworkTypes;
    }

}
