package base.library.store;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库
 *
 * Created by wangjiangpeng01 on 2017/4/21.
 */

public class DBHelper extends SQLiteOpenHelper {
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
        public static final String URL = "url";
        public static final String PATH = "path";
        public static final String TOTAL_LENGTH = "total_length";
        public static final String DOWNLOADED_LENGTH = "downlaoded_length";
    }

    public DBHelper(Context context) {
        super(context, NAME, null, VERSION_CODE);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +DownloadColumns.TABLE_NAME + "("
                + DownloadColumns.ID + " integer primary key autoincrement, "
                + DownloadColumns.URL + " text, "
                + DownloadColumns.PATH + " text, "
                + DownloadColumns.TOTAL_LENGTH + " integer, "
                + DownloadColumns.DOWNLOADED_LENGTH + " integer"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
