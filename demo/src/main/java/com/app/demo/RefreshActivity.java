package com.app.demo;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ui.refresh.RefreshLayout;
import com.ui.refresh.RefreshLayoutt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import base.library.BaseActivity;

/**
 * 下拉刷新
 * <p>
 * Created by wangjiangpeng01 on 2017/3/2.
 */
public class RefreshActivity extends BaseActivity implements RefreshLayout.OnRefreshListener {
    private static final int REFRESH_COMPLETE = 0X110;

    private RefreshLayoutt refreshLayout;
    private ListView listView;
    private ArrayAdapter<String> mAdapter;

    private List<String> mDatas = new ArrayList<String>(Arrays.asList("Java", "Javascript", "C++", "Ruby", "Json",
            "HTML"));

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    mDatas.addAll(Arrays.asList("Lucene", "Canvas", "Bitmap"));
                    mAdapter.notifyDataSetChanged();
//                    refreshLayout.setRefreshing(false);
                    break;

            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.refresh);

        refreshLayout = (RefreshLayoutt) findViewById(R.id.refresh_layout);
        listView = (ListView) findViewById(R.id.refresh_listview);

//        refreshLayout.setOnRefreshListener(this);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDatas);
        listView.setAdapter(mAdapter);
    }


    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }
}
