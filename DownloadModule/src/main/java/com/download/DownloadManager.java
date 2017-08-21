package com.download;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.library.BaseApplication;
import base.library.net.RequestParam;

/**
 * 下载管理
 * <p>
 * Created by wangjiangpeng01 on 2017/4/28.
 */

public class DownloadManager {

    private final ArrayList<WeakReference<DownloadListener>> mListeners = new ArrayList<>();

    private DownloadListener mAllListener = new DownloadListener() {

        @Override
        public void onDownloaded(long id, long total, long downloadLength) {

        }
    };

    private final ContentResolver mResolver;
    private ContentObserver mObserver;


    public DownloadManager() {
        mResolver = BaseApplication.getInstance().getContentResolver();
        mObserver = new DownloadObserver(null);
        mResolver.registerContentObserver(Downloads.CONTENT_URI, true, mObserver);
    }

    /**
     * 把下载任务放入队列
     *
     * @param request 不能为空
     * @return
     */
    public long enqueue(Request request) {
        ContentValues values = request.toContentValues();
        Uri downloadUri = mResolver.insert(Downloads.CONTENT_URI, values);
        long id = Long.parseLong(downloadUri.getLastPathSegment());
        return id;
    }

    /**
     * 删除下载
     *
     * @param id
     * @return
     */
    public long deleted(long id) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Info.DELETED, 1);

        return mResolver.update(ContentUris.withAppendedId(Downloads.CONTENT_URI, id), values, null, null);
    }

    /**
     * 暂停
     *
     * @param id
     * @return
     */
    public int pause(long id) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Info.CONTROL, Downloads.CONTROL_PAUSED);
        return mResolver.update(ContentUris.withAppendedId(Downloads.CONTENT_URI, id), values, null, null);
    }

    /**
     * 查询所有的下载信息
     *
     * @return
     */
    public List<DownloadInfo> query() {
        List<DownloadInfo> list = new ArrayList<>();
        Cursor cursor = mResolver.query(Downloads.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo();
            info.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.Info.ID)));
            info.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Info.FILE_NAME)));
            info.setTotalBytes(cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.Info.TOTAL_BYTES)));
            info.setCurrentBytes(cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.Info.CURRENT_BYTES)));
            info.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.STATUS)));
            info.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.DELETED)) != 0);
            info.setAllowedNetworkTypes(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info
                    .ALLOWED_NETWORK_TYPES)));
            info.setVisibility(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.VISIBILITY)) != 0);
            info.setError(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.ERROR)));
            info.setFailedConnections(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.FAILED_CONNECTIONS)));
            info.setControl(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Info.CONTROL)));
            list.add(info);
        }
        cursor.close();

        return list;
    }

    /**
     * 通知当前内容变更
     */
    protected void notifyContentChange(long id, long total, long downloadLength) {
        int length = mListeners.size();
        for (int index = length - 1; index >= 0; index++) {
            DownloadListener listener = mListeners.get(index).get();
            if (listener != null) {
                listener.onDownloaded(id, total, downloadLength);

            } else {
                mListeners.remove(index);
            }
        }
    }

    /**
     * 添加监听
     *
     * @param listener
     */
    public void addDownloadListener(DownloadListener listener) {
        WeakReference<DownloadListener> reference = new WeakReference<DownloadListener>(listener);
        mListeners.add(reference);
    }

    /**
     * 删除监听
     *
     * @param listener
     */
    public void removeDownloadListener(DownloadListener listener) {
        mListeners.remove(listener);
    }

    /**
     * 下载请求参数
     * <p>
     * Created by wangjiangpeng01 on 2017/4/5.
     */

    public static class Request extends RequestParam {

        private long id;

        /**
         * 本地下载路径
         */
        private String fileName;

        /**
         * 允许下载的网络类型，默认所有网络都可下载
         */
        private int allowedNetworkTypes = Downloads.NETWORK_ALL;

        /**
         * 可见
         */
        private boolean visibility;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getAllowedNetworkTypes() {
            return allowedNetworkTypes;
        }

        public void setAllowedNetworkTypes(int allowedNetworkTypes) {
            this.allowedNetworkTypes = allowedNetworkTypes;
        }

        public boolean isVisibility() {
            return visibility;
        }

        public void setVisibility(boolean visibility) {
            this.visibility = visibility;
        }

        ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(Downloads.Request.URL, getUrl());
            values.put(Downloads.Request.HEADERS, getHeaders().toString());
            values.put(Downloads.Request.POSTS, getPosts().toString());
            values.put(Downloads.Request.SSL_MUTUAL, isSSLMutual());
            values.put(Downloads.Request.KEY_STORE_PASS, getKeyStorePass());
            values.put(Downloads.Request.KEY_STORE_ID, getKeyStoreId());
            values.put(Downloads.Request.TRUST_STORE_PASS, getTrustStorePass());
            values.put(Downloads.Request.TRUST_STORE_ID, getTrustStoreId());

            values.put(Downloads.Info.FILE_NAME, getFileName());
            values.put(Downloads.Info.TOTAL_BYTES, 0);
            values.put(Downloads.Info.CURRENT_BYTES, 0);
            values.put(Downloads.Info.STATUS, Downloads.STATUS_PENDING);
            values.put(Downloads.Info.DELETED, false);
            values.put(Downloads.Info.ALLOWED_NETWORK_TYPES, getAllowedNetworkTypes());
            values.put(Downloads.Info.VISIBILITY, isVisibility());

            return values;
        }

        public void copyCursorData(Cursor cursor) {
            setId(cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.Request.ID)));
            setUrl(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Request.URL)));
            addHeaders(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Request.HEADERS))));
            addPosts(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Request.POSTS))));
            setSSLMutual(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Request.SSL_MUTUAL)) != 0);
            setKeyStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Request.KEY_STORE_ID)));
            setKeyStorePass(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Request.KEY_STORE_PASS)));
            setTrustStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Request.TRUST_STORE_ID)));
            setTrustStorePass(cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Request.TRUST_STORE_PASS)));
        }

        private Map<String, String> stringToMap(String str) {
            HashMap<String, String> map = new HashMap<>();
            if (!str.startsWith("{") || !str.endsWith("}")) {
                return map;
            }
            try {
                String sub = str.substring(1, str.length() - 2);
                String[] entrys = sub.split(",");
                for (String entry : entrys) {
                    String[] value = entry.split("=");
                    map.put(value[0], value[1]);
                }
            } catch (Throwable e) {
                map.clear();
            }
            return map;
        }

    }

    private static class DownloadObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            Log.e("WJP", "onChangeuri" + selfChange);
            Log.e("WJP", uri.getHost());
        }

    }


}
