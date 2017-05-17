package com.download.store;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库
 *
 * Created by wangjiangpeng01 on 2017/4/21.
 */

public class DownloadDBHelper extends SQLiteOpenHelper {
    /**
     * 库名
     */
    public static final String NAME = "store.db";
    /**
     * 版本号
     */
    public static final int VERSION_CODE = 1;

    protected static class DownloadColumns {
        public static final String TABLE_NAME = "download";
        public static final String ID = "id";
        public static final String DOMAIN = "domain";
        public static final String HEADERS = "headers";
        public static final String POSTS = "posts";
        public static final String GET = "get";
        public static final String IS_SSL_MUTUAL = "isSSLMutual";
        public static final String KEY_STORE_ID = "keyStoreId";
        public static final String TRUST_STORE_ID = "trustStoreId";
        public static final String KEY_STORE_PASS = "keyStorePass";
        public static final String TRUST_STORE_PASS = "trustStorePass";
        public static final String CONNECT_TIMEOUT = "connectTimeout";
        public static final String READ_TIMEOUT = "readTimeout";
        public static final String DOWNLOAD_PATH = "downloadPath";
        public static final String TOTAL_LENGTH = "total_length";
        public static final String DOWNLOADED_LENGTH = "downlaoded_length";
        public static final String STATUS = "status";
        public static final String IS_DELETED = "is_deleted";
    }

    public DownloadDBHelper(Context context) {
        super(context, NAME, null, VERSION_CODE);
    }

    public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DownloadColumns.TABLE_NAME + "("
                + DownloadColumns.ID + " integer primary key autoincrement, "
                + DownloadColumns.DOMAIN + " text, "
                + DownloadColumns.HEADERS + " text, "
                + DownloadColumns.POSTS + " text, "
                + DownloadColumns.GET + " text, "
                + DownloadColumns.IS_SSL_MUTUAL + " integer, "
                + DownloadColumns.KEY_STORE_ID + " integer, "
                + DownloadColumns.TRUST_STORE_ID + " integer, "
                + DownloadColumns.KEY_STORE_PASS + " text, "
                + DownloadColumns.TRUST_STORE_PASS + " text, "
                + DownloadColumns.CONNECT_TIMEOUT + " integer, "
                + DownloadColumns.READ_TIMEOUT + " integer, "
                + DownloadColumns.DOWNLOAD_PATH + " text, "
                + DownloadColumns.TOTAL_LENGTH + " integer, "
                + DownloadColumns.DOWNLOADED_LENGTH + " integer, "
                + DownloadColumns.STATUS + " integer"
                + DownloadColumns.IS_DELETED + " integer"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
