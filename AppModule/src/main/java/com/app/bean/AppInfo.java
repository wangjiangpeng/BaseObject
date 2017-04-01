package com.app.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by wangjiangpeng01 on 2017/3/31.
 */

public class AppInfo {

    private String lable;//程序名
    private Drawable icon;//图标
    private String packageName;//包名
    private int versionCode;//版本号
    private String versionName;//版本名
    private String sourceDir;//安装路径

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }
}
