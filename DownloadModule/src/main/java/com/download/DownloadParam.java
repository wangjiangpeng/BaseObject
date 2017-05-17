package com.download;

import android.net.ConnectivityManager;

import base.library.net.RequestParam;

/**
 * 下载请求参数
 * <p>
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
    private long id;
    /**
     * 本地下载路径
     */
    private String downloadPath;

    /**
     * 已下载大小
     */
    private long downloadedLength;

    /**
     * 总大小
     */
    private long totalLength;

    /**
     * 状态
     */
    private int status;

    /**
     * 是否删除
     */
    private boolean isDeleted;

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

    public long getDownloadedLength() {
        return downloadedLength;
    }

    public void setDownloadedLength(long downloadedLength) {
        this.downloadedLength = downloadedLength;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public int getAllowedNetworkTypes() {
        return allowedNetworkTypes;
    }

    public void setAllowedNetworkTypes(int allowedNetworkTypes) {
        this.allowedNetworkTypes = allowedNetworkTypes;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void update(DownloadParam param){
        if(param.id != id){
            throw new IllegalArgumentException("param id not equal");
        }
        // 只更新下载相关，网络数据不更新
        downloadedLength = param.downloadedLength;
        totalLength = param.totalLength;
        status = param.status;
        isDeleted = param.isDeleted;
        allowedNetworkTypes = param.allowedNetworkTypes;
    }

}
