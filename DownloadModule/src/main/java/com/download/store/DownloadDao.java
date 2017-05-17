package com.download.store;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.download.DownloadManager;
import com.download.DownloadParam;
import com.download.store.DownloadDBHelper.DownloadColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.library.BaseApplication;

import static android.R.attr.id;

/**
 * 数据库操作
 * <p>
 * Created by wangjiangpeng01 on 2017/4/21.
 */

public class DownloadDao {

    private DownloadDBHelper dbHelper;

    public DownloadDao() {
        BaseApplication context = BaseApplication.getInstance();
        dbHelper = new DownloadDBHelper(context);
    }

    /**
     * 插入下载记录
     *
     * @param param
     * @return
     */
    public synchronized long insert(DownloadParam param) {
        ContentValues values = new ContentValues();
        values.put(DownloadColumns.DOMAIN, param.getUrl());
        values.put(DownloadColumns.HEADERS, param.getHeaders().toString());
        values.put(DownloadColumns.POSTS, param.getPosts().toString());
        values.put(DownloadColumns.GET, param.getGetData());
        values.put(DownloadColumns.IS_SSL_MUTUAL, param.isSSLMutual() ? 1 : 0);
        values.put(DownloadColumns.KEY_STORE_ID, param.getKeyStoreId());
        values.put(DownloadColumns.TRUST_STORE_ID, param.getTrustStoreId());
        values.put(DownloadColumns.KEY_STORE_PASS, param.getKeyStorePass());
        values.put(DownloadColumns.TRUST_STORE_PASS, param.getTrustStorePass());
        values.put(DownloadColumns.CONNECT_TIMEOUT, param.getConnectTimeout());
        values.put(DownloadColumns.READ_TIMEOUT, param.getReadTimeout());
        values.put(DownloadColumns.DOWNLOAD_PATH, param.getDownloadPath());
        values.put(DownloadColumns.TOTAL_LENGTH, param.getTotalLength());
        values.put(DownloadColumns.DOWNLOADED_LENGTH, param.getDownloadPath());
        values.put(DownloadColumns.STATUS, param.getStatus());
        values.put(DownloadColumns.IS_DELETED, param.isDeleted() ? 1 : 0);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long id = db.insert(DownloadColumns.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            return id;

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 删除，把记录标记为删除，物理删除，不是数据库删除
     *
     * @param id
     * @return
     */
    public synchronized long deleted(long id) {
        ContentValues values = new ContentValues();
        values.put(DownloadColumns.IS_DELETED, 1);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long dId = db.update(DownloadColumns.TABLE_NAME, values, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return dId;

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 移除下载记录
     *
     * @param id
     */
    public synchronized long remove(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long dId = db.delete(DownloadColumns.TABLE_NAME, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return dId;

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 修改下载记录
     *
     * @param param
     * @return
     */
    public synchronized long update(DownloadParam param) {
        ContentValues values = new ContentValues();
        values.put(DownloadColumns.DOMAIN, param.getUrl());
        values.put(DownloadColumns.HEADERS, param.getHeaders().toString());
        values.put(DownloadColumns.POSTS, param.getPosts().toString());
        values.put(DownloadColumns.GET, param.getGetData());
        values.put(DownloadColumns.IS_SSL_MUTUAL, param.isSSLMutual() ? 1 : 0);
        values.put(DownloadColumns.KEY_STORE_ID, param.getKeyStoreId());
        values.put(DownloadColumns.TRUST_STORE_ID, param.getTrustStoreId());
        values.put(DownloadColumns.KEY_STORE_PASS, param.getKeyStorePass());
        values.put(DownloadColumns.TRUST_STORE_PASS, param.getTrustStorePass());
        values.put(DownloadColumns.CONNECT_TIMEOUT, param.getConnectTimeout());
        values.put(DownloadColumns.READ_TIMEOUT, param.getReadTimeout());
        values.put(DownloadColumns.DOWNLOAD_PATH, param.getDownloadPath());
        values.put(DownloadColumns.TOTAL_LENGTH, param.getTotalLength());
        values.put(DownloadColumns.DOWNLOADED_LENGTH, param.getDownloadPath());
        values.put(DownloadColumns.STATUS, param.getStatus());
        values.put(DownloadColumns.IS_DELETED, param.isDeleted() ? 1 : 0);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long dId = db.update(DownloadColumns.TABLE_NAME, values, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return dId;

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 查找下载数据
     *
     * @param id
     * @return 无数据为空
     */
    public synchronized DownloadParam query(long id) {
        DownloadParam item = null;
        Cursor cursor = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            cursor = db.query(DownloadColumns.TABLE_NAME, null, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                item = new DownloadParam();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.ID)));
                item.setDomain(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.DOMAIN)));
                item.addHeaders(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.HEADERS))));
                item.addPosts(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.POSTS))));
                item.setGetData(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.GET)));
                item.setSSLMutual(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.IS_SSL_MUTUAL)) != 0);
                item.setKeyStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.KEY_STORE_ID)));
                item.setKeyStorePass(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.KEY_STORE_PASS)));
                item.setTrustStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.TRUST_STORE_ID)));
                item.setTrustStorePass(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.TRUST_STORE_PASS)));
                item.setConnectTimeout(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.CONNECT_TIMEOUT)));
                item.setReadTimeout(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.READ_TIMEOUT)));
                item.setDownloadPath(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.DOWNLOAD_PATH)));
                item.setTotalLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.TOTAL_LENGTH)));
                item.setDownloadedLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.DOWNLOADED_LENGTH)));
                item.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.STATUS)));
                item.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.IS_DELETED)) != 0);
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
        return item;
    }

    /**
     * 查找所有下载数据
     *
     * @return
     */
    public synchronized List<DownloadParam> queryAllActive() {
        List<DownloadParam> list = new ArrayList<>();
        Cursor cursor = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            cursor = db.query(DownloadColumns.TABLE_NAME, null, DownloadColumns.IS_DELETED + "=?", new String[]{String.valueOf(0)}, null, null, null);
            while (cursor.moveToNext()) {
                DownloadParam item = new DownloadParam();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.ID)));
                item.setDomain(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.DOMAIN)));
                item.addHeaders(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.HEADERS))));
                item.addPosts(stringToMap(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.POSTS))));
                item.setGetData(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.GET)));
                item.setSSLMutual(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.IS_SSL_MUTUAL)) != 0);
                item.setKeyStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.KEY_STORE_ID)));
                item.setKeyStorePass(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.KEY_STORE_PASS)));
                item.setTrustStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.TRUST_STORE_ID)));
                item.setTrustStorePass(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.TRUST_STORE_PASS)));
                item.setConnectTimeout(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.CONNECT_TIMEOUT)));
                item.setReadTimeout(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.READ_TIMEOUT)));
                item.setDownloadPath(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.DOWNLOAD_PATH)));
                item.setTotalLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.TOTAL_LENGTH)));
                item.setDownloadedLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.DOWNLOADED_LENGTH)));
                item.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.STATUS)));
                item.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadColumns.IS_DELETED)) != 0);

                list.add(item);
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
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
