package com.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.app.bean.AppInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.library.BaseApplication;

/**
 * 程序管理
 * <p>
 * Created by wangjiangpeng01 on 2017/2/6.
 */

public class AppManager {
    private Map<String, AppInfo> appInfos = new HashMap<>();
    private AppInstallReceiver receiver;
    private PackageManager packageManager;

    private List<WeakReference<OnAppStateListener>> listenerList = new ArrayList<>();

    protected AppManager() {
        Application app = BaseApplication.getInstance();
        packageManager = app.getPackageManager();

        queryAppInfo();

        receiver = new AppInstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        app.registerReceiver(receiver, filter);
    }

    private void queryAppInfo() {
        List<ApplicationInfo> appList = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo info : appList) {
            try {
                PackageInfo pInfo = packageManager.getPackageInfo(info.packageName, PackageManager.GET_ACTIVITIES);
                AppInfo appInfo = new AppInfo();
                appInfo.setIcon(info.loadIcon(packageManager));
                appInfo.setLable(info.loadLabel(packageManager).toString());
                appInfo.setPackageName(info.packageName);
                appInfo.setSourceDir(info.sourceDir);
                appInfo.setFlags(info.flags);
                appInfo.setVersionCode(pInfo.versionCode);
                appInfo.setVersionName(pInfo.versionName);
                appInfos.put(info.packageName, appInfo);

            } catch (Exception e) {

            }
        }
    }

    /**
     * 已安装程序
     *
     * @return 程序列表
     */
    public List<AppInfo> getInstalledApps() {
        return new ArrayList<>(appInfos.values());
    }

    /**
     * 是否安装
     *
     * @param packageName 包名
     * @return
     */
    public boolean isInstalled(String packageName) {
        return appInfos.get(packageName) != null;
    }

    /**
     * 是否安装
     *
     * @param packageName 包名
     * @param versionCode 版本号
     * @return
     */
    public boolean isInstalled(String packageName, int versionCode) {
        AppInfo info = appInfos.get(packageName);
        if (info != null && info.getVersionCode() == versionCode) {
            return true;
        }
        return false;
    }

    /**
     * 获得系统软件列表
     *
     * @return
     */
    public List<AppInfo> getSystemApps(){
        List<AppInfo> list = new ArrayList<>();
        for (String key : appInfos.keySet()) {
            AppInfo info = appInfos.get(key);
            if(info.isSystemApp()){
                list.add(info);
            }
        }
        return list;
    }

    /**
     * 获得第三方软件列表
     *
     * @return
     */
    public List<AppInfo> getUserApps(){
        List<AppInfo> list = new ArrayList<>();
        for (String key : appInfos.keySet()) {
            AppInfo info = appInfos.get(key);
            if(info.isUserApp()){
                list.add(info);
            }
        }
        return list;
    }

    /**
     * 新安装了软件
     *
     * @param packageName 包名
     */
    private void packageAdd(String packageName) {
        try {
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            ApplicationInfo aInfo = packageManager.getApplicationInfo(packageName, 0);

            AppInfo appInfo = isInstalled(packageName) ? appInfos.get(packageName) : new AppInfo();
            appInfo.setIcon(aInfo.loadIcon(packageManager));
            appInfo.setLable(aInfo.loadLabel(packageManager).toString());
            appInfo.setPackageName(aInfo.packageName);
            appInfo.setSourceDir(aInfo.sourceDir);
            appInfo.setFlags(aInfo.flags);
            appInfo.setVersionCode(pInfo.versionCode);
            appInfo.setVersionName(pInfo.versionName);

            if (!isInstalled(packageName)) {
                appInfos.put(aInfo.packageName, appInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 卸载了软件
     *
     * @param packageName 包名
     */
    private void packageRemoved(String packageName) {
        if (isInstalled(packageName)) {
            appInfos.remove(packageName);
        }
    }

    /**
     * 替换了软件
     *
     * @param packageName 包名
     */
    private void packageReplaced(String packageName) {
        packageAdd(packageName);
    }

    /**
     * 添加app状态监听
     *
     * @param listener
     */
    public void addOnAppStateListener(OnAppStateListener listener) {
        WeakReference<OnAppStateListener> weak = new WeakReference<OnAppStateListener>(listener);
        listenerList.add(weak);
    }

    private void notifyAllListener() {
        for (int index = listenerList.size() - 1; index >= 0; index--) {
            WeakReference<OnAppStateListener> weak = listenerList.get(index);
            OnAppStateListener listener = weak.get();
            if (listener != null) {
                listener.onAppStateChanged();
            } else {
                listenerList.remove(index);
            }
        }
    }

    private class AppInstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                packageAdd(packageName);
                notifyAllListener();

            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                packageRemoved(packageName);
                notifyAllListener();

            } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED) || action.equals(Intent.ACTION_PACKAGE_CHANGED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                packageReplaced(packageName);
                notifyAllListener();
            }
        }

    }

    public interface OnAppStateListener {
        public void onAppStateChanged();
    }

}
