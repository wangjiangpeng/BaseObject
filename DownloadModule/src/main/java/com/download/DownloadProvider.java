package com.download;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import base.library.MLog;


/**
 * 下载提供，处理数据库和中间数据之间的关系
 * <p>
 * Created by wangjiangpeng01 on 2017/5/10.
 */

public class DownloadProvider extends ContentProvider {

    private static final String TAG = "DownloadProvider";

    /**
     * 库名
     */
    public static final String NAME = "/mnt/sdcard/temp/download.db";
    /**
     * 版本号
     */
    public static final int VERSION_CODE = 1;

    private SQLiteOpenHelper mOpenHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI("com.download.provider", "downloads", Downloads.DOWNLOADS);
        sURIMatcher.addURI("com.download.provider", "downloads/#", Downloads.DOWNLOADS_ID);
        sURIMatcher.addURI("com.download.provider", "request", Downloads.REQUEST_HEADERS_URI);
        sURIMatcher.addURI("com.download.provider", "request/#", Downloads.REQUEST_HEADERS_URI_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = new DownloadDBHelper(context);
        context.startService(new Intent(context, DownloadService.class));

        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sURIMatcher.match(uri);
        if (match != Downloads.DOWNLOADS) {
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        }

        // 插入downloadinfo
        ContentValues filteredValues = new ContentValues();
        copyString(Downloads.Info.FILE_NAME, values, filteredValues);
        copyLong(Downloads.Info.TOTAL_BYTES, values, filteredValues);
        copyLong(Downloads.Info.CURRENT_BYTES, values, filteredValues);
        copyInteger(Downloads.Info.STATUS, values, filteredValues);
        copyBoolean(Downloads.Info.DELETED, values, filteredValues);
        copyInteger(Downloads.Info.ALLOWED_NETWORK_TYPES, values, filteredValues);
        copyBoolean(Downloads.Info.VISIBILITY, values, filteredValues);
        copyInteger(Downloads.Info.ERROR, values, filteredValues);
        copyInteger(Downloads.Info.FAILED_CONNECTIONS, values, filteredValues);
        copyInteger(Downloads.Info.CONTROL, values, filteredValues);

        long rowID = db.insert(Downloads.Info.TABLE_NAME, null, filteredValues);
        if (rowID == -1) {
            MLog.d(TAG, "couldn't insert into downloads database");
            return null;
        }

        // 插入头部
        filteredValues = new ContentValues();
        filteredValues.put(Downloads.Request.DOWNLOAD_ID, rowID);
        copyString(Downloads.Request.URL, values, filteredValues);
        copyString(Downloads.Request.HEADERS, values, filteredValues);
        copyString(Downloads.Request.POSTS, values, filteredValues);
        copyBoolean(Downloads.Request.SSL_MUTUAL, values, filteredValues);
        copyLong(Downloads.Request.KEY_STORE_ID, values, filteredValues);
        copyLong(Downloads.Request.TRUST_STORE_ID, values, filteredValues);
        copyString(Downloads.Request.KEY_STORE_PASS, values, filteredValues);
        copyString(Downloads.Request.TRUST_STORE_PASS, values, filteredValues);
        db.insert(Downloads.Request.TABLE_NAME, null, filteredValues);

        Uri uriToNotify = ContentUris.withAppendedId(Downloads.CONTENT_URI, rowID);
        notifyContentChanged(uriToNotify);

        // 通知service数据更新
        final Context context = getContext();
        context.startService(new Intent(context, DownloadService.class));

        return uriToNotify;
    }

    private void notifyContentChanged(final Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private SqlSelection getWhereClause(final Uri uri, final String where, final String[] whereArgs, int uriMatch) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
        if (uriMatch == Downloads.DOWNLOADS_ID || uriMatch == Downloads.REQUEST_HEADERS_URI_ID) {
            selection.appendClause(Downloads.Info.ID + " = ?", getDownloadIdFromUri(uri));
        }

        return selection;
    }

    private String getDownloadIdFromUri(final Uri uri) {
        return uri.getPathSegments().get(1);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        int match = sURIMatcher.match(uri);
        switch (match) {
            case Downloads.DOWNLOADS:
            case Downloads.DOWNLOADS_ID:
            case Downloads.REQUEST_HEADERS_URI:
            case Downloads.REQUEST_HEADERS_URI_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                deleteRequestHeaders(db, selection.getSelection(), selection.getParameters());

                // 删除文件
                String[] columns = new String[]{Downloads.Info.ID, Downloads.Info.FILE_NAME};
                final Cursor cursor = db.query(Downloads.Info.TABLE_NAME, columns, selection.getSelection(),
                        selection.getParameters(), null, null, null);
                try {
                    while (cursor.moveToNext()) {
                        final String path = cursor.getString(1);
                        if (!TextUtils.isEmpty(path)) {
                            final File file = new File(path);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                } finally {
                    try {
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                count = db.delete(Downloads.Info.TABLE_NAME, selection.getSelection(), selection.getParameters());
                break;

            default:
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }

        notifyContentChanged(uri);

        return count;
    }

    private void deleteRequestHeaders(SQLiteDatabase db, String where, String[] whereArgs) {
        String[] projection = new String[]{Downloads.Request.DOWNLOAD_ID};
        Cursor cursor = db.query(Downloads.Request.TABLE_NAME, projection, where, whereArgs, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String idWhere = Downloads.Request.DOWNLOAD_ID + "=" + id;
                db.delete(Downloads.Request.TABLE_NAME, idWhere, null);
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public int update(final Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;
        boolean startService = false;
        if (values.containsKey(Downloads.Info.DELETED)) {
            if (values.getAsInteger(Downloads.Info.DELETED) == 1) {
                startService = true;
            }
        }

        int match = sURIMatcher.match(uri);
        switch (match) {
            case Downloads.DOWNLOADS:
            case Downloads.DOWNLOADS_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                if (values.size() > 0) {
                    count = db.update(Downloads.Info.TABLE_NAME, values, selection.getSelection(), selection
                            .getParameters());
                }
                break;

            default:
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

        notifyContentChanged(uri);
        if (startService) {
            Context context = getContext();
            context.startService(new Intent(context, DownloadService.class));
        }

        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sort) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor = null;
        int match = sURIMatcher.match(uri);
        if (match == -1) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (match == Downloads.REQUEST_HEADERS_URI) {

        }

        if (match == Downloads.REQUEST_HEADERS_URI_ID) {

        }
        switch (match) {
            case Downloads.REQUEST_HEADERS_URI:
                if (projection != null || where != null || sort != null) {
                    throw new UnsupportedOperationException("Request header queries do not support " + "projections, " +
                            "selections or sorting");
                }
                return queryRequestHeaders(db, uri);

            case Downloads.REQUEST_HEADERS_URI_ID: {
                SqlSelection fullSelection = getWhereClause(uri, where, whereArgs, match);
                cursor = db.query(Downloads.Request.TABLE_NAME, projection, fullSelection.getSelection(),
                        fullSelection.getParameters(), null, null, sort);

            }
            break;

            case Downloads.DOWNLOADS:
            case Downloads.DOWNLOADS_ID: {
                SqlSelection fullSelection = getWhereClause(uri, where, whereArgs, match);
                cursor = db.query(Downloads.Info.TABLE_NAME, projection, fullSelection.getSelection(),
                        fullSelection.getParameters(), null, null, sort);

            }
            break;
        }

        return cursor;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return super.openFile(uri, mode);
    }

    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyLong(String key, ContentValues from, ContentValues to) {
        Long i = from.getAsLong(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    /**
     * Handle a query for the custom request headers registered for a download.
     */
    private Cursor queryRequestHeaders(SQLiteDatabase db, Uri uri) {
        String where = Downloads.Request.DOWNLOAD_ID + "=" + getDownloadIdFromUri(uri);
        return db.query(Downloads.Request.TABLE_NAME, null, where, null, null, null, null);
    }

    /**
     * 数据库
     * <p>
     * Created by wangjiangpeng01 on 2017/4/21.
     */

    public static class DownloadDBHelper extends SQLiteOpenHelper {


        public DownloadDBHelper(Context context) {
            super(context, NAME, null, VERSION_CODE);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createHeadersTable(db);
            createDownloadsTable(db);
        }

        private void createHeadersTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + Downloads.Request.TABLE_NAME);
                db.execSQL("create table " + Downloads.Request.TABLE_NAME + "(" +
                        Downloads.Request.ID + " integer primary key autoincrement, " +
                        Downloads.Request.DOWNLOAD_ID + " integer not null, " +
                        Downloads.Request.URL + " text, " +
                        Downloads.Request.HEADERS + " text, " +
                        Downloads.Request.POSTS + " text, " +
                        Downloads.Request.SSL_MUTUAL + " boolean, " +
                        Downloads.Request.KEY_STORE_ID + " integer, " +
                        Downloads.Request.TRUST_STORE_ID + " integer, " +
                        Downloads.Request.KEY_STORE_PASS + " text, " +
                        Downloads.Request.TRUST_STORE_PASS + " text);");

            } catch (SQLException ex) {
                throw ex;
            }
        }

        private void createDownloadsTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + Downloads.Info.TABLE_NAME);
                db.execSQL("create table " + Downloads.Info.TABLE_NAME + "(" +
                        Downloads.Info.ID + " integer primary key autoincrement, " +
                        Downloads.Info.FILE_NAME + " text, " +
                        Downloads.Info.TOTAL_BYTES + " integer, " +
                        Downloads.Info.CURRENT_BYTES + " integer, " +
                        Downloads.Info.STATUS + " integer, " +
                        Downloads.Info.DELETED + " boolean, " +
                        Downloads.Info.ALLOWED_NETWORK_TYPES + " integer, " +
                        Downloads.Info.VISIBILITY + " boolean, " +
                        Downloads.Info.ERROR + " integer, " +
                        Downloads.Info.FAILED_CONNECTIONS + " integer, " +
                        Downloads.Info.CONTROL + " integer);");

            } catch (SQLException ex) {
                throw ex;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    /**
     * This class encapsulates a SQL where clause and its parameters.  It makes it possible for
     * shared methods (like {@link DownloadProvider#getWhereClause(Uri, String, String[], int)})
     * to return both pieces of information, and provides some utility logic to ease piece-by-piece
     * construction of selections.
     */
    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (newClause == null || newClause.isEmpty()) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String getSelection() {
            return mWhereClause.toString();
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }
    }

}
