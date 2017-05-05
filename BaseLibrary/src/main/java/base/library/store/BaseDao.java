package base.library.store;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.locks.ReentrantLock;

import base.library.BaseApplication;
import base.library.net.download.DownloadItem;
import base.library.store.DBHelper.DownloadColumns;

/**
 * 数据库操作
 * <p>
 * Created by wangjiangpeng01 on 2017/4/21.
 */

public class BaseDao {

    private DBHelper dbHelper;
    private ReentrantLock lock;

    private static BaseDao sBaseDao;

    public static BaseDao getInstance() {
        if (sBaseDao == null) {
            synchronized (BaseDao.class) {
                if (sBaseDao == null) {
                    sBaseDao = new BaseDao();
                }
            }
        }
        return sBaseDao;
    }

    private BaseDao() {
        BaseApplication context = BaseApplication.getInstance();
        dbHelper = new DBHelper(context);
        lock = new ReentrantLock();
    }

    /**
     * 插入下载记录
     *
     * @param url
     * @param downloadPath
     * @return
     */
    public long insertDownload(String url, String downloadPath) {
        ContentValues values = new ContentValues();
        values.put(DownloadColumns.URL, url);
        values.put(DownloadColumns.PATH, downloadPath);

        lock.lock();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long id = db.insert(DownloadColumns.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            return id;

        } finally {
            db.endTransaction();
            lock.unlock();
        }
    }

    /**
     * 删除下载记录
     *
     * @param id
     */
    public long removeDownload(long id) {
        lock.lock();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long dId = db.delete(DownloadColumns.TABLE_NAME, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return dId;

        } finally {
            db.endTransaction();
            lock.unlock();
        }
    }

    /**
     * 修改下载记录
     *
     * @param id
     * @param totalLength
     * @param downloadedLength
     * @return
     */
    public long updateDownload(long id, long totalLength, long downloadedLength) {
        ContentValues values = new ContentValues();
        values.put(DownloadColumns.TOTAL_LENGTH, totalLength);
        values.put(DownloadColumns.DOWNLOADED_LENGTH, downloadedLength);

        lock.lock();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long dId = db.update(DownloadColumns.TABLE_NAME, values, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return dId;

        } finally {
            db.endTransaction();
            lock.unlock();
        }
    }

    /**
     * 查找下载数据
     *
     * @param id
     * @return 无数据为空
     */
    public DownloadItem queryDownload(long id) {
        DownloadItem item = null;
        Cursor cursor = null;

        lock.lock();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            cursor = db.query(DownloadColumns.TABLE_NAME, null, DownloadColumns.ID + "=? ", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                item = new DownloadItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.ID)));
                item.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.URL)));
                item.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DownloadColumns.PATH)));
                item.setTotalLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.TOTAL_LENGTH)));
                item.setDownloadedLength(cursor.getLong(cursor.getColumnIndexOrThrow(DownloadColumns.DOWNLOADED_LENGTH)));
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
            lock.unlock();
            if (cursor != null) {
                cursor.close();
            }
        }
        return item;
    }


}
