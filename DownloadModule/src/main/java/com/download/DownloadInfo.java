package com.download;

/**
 * Created by wangjiangpeng01 on 2017/8/1.
 */

public class DownloadInfo {

    /**
     * 数据库索引id
     */
    private long id;

    /**
     * 状态
     */
    private int status = Downloads.STATUS_PENDING;

    /**
     * 是否删除
     */
    private boolean isDeleted;

    /**
     * 本地下载路径
     */
    private String fileName;

    /**
     * 已下载大小
     */
    private long currentBytes;

    /**
     * 总大小
     */
    private long totalBytes;

    /**
     * 下载是否可见
     */
    private boolean visibility;

    /**
     * 下载失败
     */
    private int error;

    /**
     * 记录下载失败重试的次数
     */
    private int failedConnections;

    /**
     * 允许下载的网络类型，默认所有网络都可下载
     */
    private int allowedNetworkTypes;

    /**
     * 控制
     */
    private int control;

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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getFailedConnections() {
        return failedConnections;
    }

    public void setFailedConnections(int failedConnections) {
        this.failedConnections = failedConnections;
    }

    public int getAllowedNetworkTypes() {
        return allowedNetworkTypes;
    }

    public void setAllowedNetworkTypes(int allowedNetworkTypes) {
        this.allowedNetworkTypes = allowedNetworkTypes;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public void update(DownloadInfo info) {
        this.status = info.status;
        this.isDeleted = info.isDeleted;
    }

}
