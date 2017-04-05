package com.app.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.AppManager;
import com.app.AppModule;
import com.app.bean.AppInfo;

import java.util.List;

import base.library.BaseActivity;

/**
 * Created by wangjiangpeng01 on 2017/3/31.
 */
public class AppManagerActivity extends BaseActivity implements AppManager.OnAppStateListener {

    private ListView listView;
    private AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_manager);

        listView = (ListView) findViewById(R.id.app_manager_list);
        adapter = new AppListAdapter();
        listView.setAdapter(adapter);

        AppModule.getInstance().getAppManager().addOnAppStateListener(this);
    }

    @Override
    public void onAppStateChanged() {
        adapter.notifyDataSetChanged();
    }

    private class AppListAdapter extends BaseAdapter {

        private List<AppInfo> appInfos;

        public AppListAdapter() {
            appInfos = AppModule.getInstance().getAppManager().getUserApps();
        }

        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public AppInfo getItem(int position) {
            return appInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            appInfos = AppModule.getInstance().getAppManager().getUserApps();

            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(AppManagerActivity.this).inflate(R.layout.app_manager_item, null);
                vh = new ViewHolder();
                vh.icon = (ImageView)convertView.findViewById(R.id.app_manager_item_icon);
                vh.lable = (TextView)convertView.findViewById(R.id.app_manager_item_lable);
                vh.packageName = (TextView)convertView.findViewById(R.id.app_manager_item_package);
                vh.version = (TextView)convertView.findViewById(R.id.app_manager_item_version);
                convertView.setTag(vh);

            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            AppInfo info = getItem(position);
            vh.icon.setImageDrawable(info.getIcon());
            vh.lable.setText(info.getLable());
            vh.packageName.setText(info.getPackageName());
            vh.version.setText(String.valueOf(info.getVersionCode()));
            return convertView;
        }

    }

    private static class ViewHolder{
        ImageView icon;
        TextView lable;
        TextView packageName;
        TextView version;
    }


}
