package com.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

import base.library.BaseApplication;

/**
 * 程序管理
 * <p>
 * Created by wangjiangpeng01 on 2017/2/6.
 */

public class AppManager {
    private List<PackageInfo> packageInfos;
    private AppInstallReceiver receiver;
    private PackageManager packageManager;

    public AppManager() {
        Application app = BaseApplication.getInstance();
        packageManager = app.getPackageManager();
        packageInfos = packageManager.getInstalledPackages(0);

        receiver = new AppInstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_MY_PACKAGE_REPLACED);
        app.registerReceiver(receiver, filter);
    }

    /**
     * 已安装程序
     *
     * @return 程序列表
     */
    public List<PackageInfo> getInstalledPackages() {
        return packageInfos;
    }

    /**
     * 是否安装
     *
     * @param packageName 包名
     * @return
     */
    public boolean isInstalled(String packageName) {
        for (PackageInfo p : packageInfos) {
            if (p.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否安装
     *
     * @param packageName 包名
     * @param versionCode 版本号
     * @return
     */
    public boolean isInstalled(String packageName, int versionCode) {
        for (PackageInfo p : packageInfos) {
            if (p.packageName.equals(packageName) && p.versionCode == versionCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 新安装了软件
     *
     * @param packageName 包名
     */
    private void packageAdd(String packageName) {
        try {
            PackageInfo p = packageManager.getPackageInfo(packageName, 0);
            if (!isInstalled(packageName)) {
                packageInfos.add(p);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 卸载了软件
     *
     * @param packageName 包名
     */
    private void packageRemoved(String packageName) {
        for (PackageInfo p : packageInfos) {
            if (p.packageName.equals(packageName)) {
                packageInfos.remove(p);
                return;
            }
        }
    }

    /**
     * 替换了软件
     *
     * @param packageName 包名
     */
    private void packageReplaced(String packageName) {
        try {
            PackageInfo p = packageManager.getPackageInfo(packageName, 0);
            for (PackageInfo pi : packageInfos) {
                if (pi.packageName.equals(packageName)) {
                    packageInfos.remove(pi);
                    packageInfos.add(p);
                    return;
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public class AppInstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getDataString();
                packageAdd(packageName);

            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getDataString();
                packageRemoved(packageName);

            } else if (action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                String packageName = intent.getDataString();
                packageReplaced(packageName);
            }
        }

    }

}
