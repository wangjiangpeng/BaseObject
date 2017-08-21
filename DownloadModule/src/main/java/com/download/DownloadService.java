package com.download;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import base.library.MLog;
import base.library.task.ATask;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Created by wangjiangpeng01 on 2017/5/11.
 */

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    private static final int MSG_UPDATE = 1; // 更新
    private static final int MSG_FINAL_UPDATE = 2; // 更新


    private HandlerThread mUpdateThread;
    private Handler mUpdateHandler;

    /**
     * 最后一个启动id
     */
    private volatile int mLastStartId;


    /**
     * 默认下载队列空间大小，根据处理器个数，设置最大下载线程数
     */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_DOWNLOAD_CAPACITY = PROCESSORS > 1 ? PROCESSORS : 1;
    /**
     * 正在下载
     */
    private final Map<Long, DownloadInfo> mDownloads = new HashMap<>();

    /**
     * 正在下载
     */
    private Map<Long, DownloadTask> mTasks = new HashMap<>();

    /**
     * 下载池
     */
    private DownloadPool mPool;

    private int maxDownloadCount;

    private DownloadProvider mProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        mUpdateThread = new HandlerThread(TAG + "-UpdateThread");
        mUpdateThread.start();
        mUpdateHandler = new Handler(mUpdateThread.getLooper(), mUpdateCallback);
        mPool = new DownloadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int returnValue = super.onStartCommand(intent, flags, startId);
        MLog.v(TAG, "Service onStart");

        mLastStartId = startId;
        enqueueUpdate();

        return returnValue;
    }

    public void enqueueUpdate() {
        if (mUpdateHandler != null) {
            mUpdateHandler.removeMessages(MSG_UPDATE);
            mUpdateHandler.obtainMessage(MSG_UPDATE, mLastStartId, -1).sendToTarget();
        }
    }

    private void enqueueFinalUpdate() {
        mUpdateHandler.removeMessages(MSG_FINAL_UPDATE);
        mUpdateHandler.sendMessageDelayed(mUpdateHandler.obtainMessage(MSG_FINAL_UPDATE, mLastStartId, -1), 5 *
                MINUTE_IN_MILLIS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Handler.Callback mUpdateCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final int startId = msg.arg1;
            MLog.d(TAG, "Updating for startId " + startId);

            // 以数据库为准，下载状态都依赖于数据库。
            // 一旦操作完成就保存到服务器，总是得到最新的状态
            final boolean isActive;
            synchronized (mDownloads) {
                isActive = updateLocked();
            }

            return true;
        }
    };

    private boolean updateLocked() {
        final long now = System.currentTimeMillis();

        boolean isActive = false;

        final Set<Long> staleIds = new HashSet<>(mDownloads.keySet());

        final ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(Downloads.CONTENT_URI, null, null, null, null);
        try {
            final Reader reader = new Reader(resolver, cursor);
            final int idColumn = cursor.getColumnIndexOrThrow(Downloads.Info.ID);
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(idColumn);
                staleIds.remove(id);

                DownloadInfo info = mDownloads.get(id);
                if (info != null) {
                    updateDownload(reader, info, now);
                } else {
                    info = insertDownloadLocked(reader);
                }

                if (info.isDeleted()) {
                    // 如果要求删除下载，清理
                    resolver.delete(ContentUris.withAppendedId(Downloads.CONTENT_URI, id), null, null);
                    deleteFileIfExists(info.getFileName());

                } else {
                    // 开始下载任务是否准备好了
                    final boolean activeDownload = startDownloadIfReady(info);

                    isActive |= activeDownload;
                }
            }

        } finally {
            cursor.close();
        }

        return isActive;
    }

    /**
     * 更新了下载信息的本地副本。
     *
     * @param reader
     * @param info
     * @param now
     */
    private void updateDownload(Reader reader, DownloadInfo info, long now) {
        reader.updateFromDatabase(info);
    }

    /**
     * 保存有关下载信息的本地副本，并在适当情况下启动下载。
     *
     * @param reader
     * @param now
     * @return
     */
    private DownloadInfo insertDownloadLocked(Reader reader) {
        final DownloadInfo info = reader.newDownloadInfo(this);
        mDownloads.put(info.getId(), info);

        return info;
    }

    /**
     * 如果下载已准备好启动，并且尚未挂起或正在执行,表示准备好
     *
     * @param executor
     * @return
     */
    private boolean startDownloadIfReady(DownloadInfo info) {
        synchronized (this) {
            final boolean isReady = isReadyToDownload(info);
            ATask task = mTasks.get(info.getId());
            final boolean isActive = task != null && !task.isFinished();
            if (isReady && !isActive) {

                if (info.getStatus() != Downloads.STATUS_RUNNING) {
                    info.setStatus(Downloads.STATUS_RUNNING);
                    ContentValues values = new ContentValues();
                    values.put(Downloads.Info.STATUS, info.getStatus());
                    getContentResolver().update(ContentUris.withAppendedId(Downloads.CONTENT_URI, info.getId()),
                            values, null, null);
                }

                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(ContentUris.withAppendedId(Downloads.CONTENT_URI_REQUEST,
                            info.getId()), null, null, null, null);
                    cursor.moveToFirst();
                    DownloadManager.Request request = new DownloadManager.Request();
                    request.copyCursorData(cursor);

                    DownloadTask t = new DownloadTask(this, request, info);
                    t.execute(mPool);
                    mTasks.put(info.getId(), t);

                }catch (Exception e){
                    e.printStackTrace();

                }finally {
                    if(cursor != null){
                        cursor.close();
                    }

                }
            }

            return isReady;
        }
    }

    /**
     * 返回是否可以进入下载队列
     */
    private boolean isReadyToDownload(DownloadInfo info) {
        if (info.getControl() == Downloads.CONTROL_PAUSED) {
            return false;
        }

        switch (info.getStatus()) {
            case 0:
            case Downloads.STATUS_PENDING:
            case Downloads.STATUS_RUNNING:
                return true;

            case Downloads.STATUS_WAITING_FOR_NETWORK:
            case Downloads.STATUS_QUEUED_FOR_WIFI:
                return checkCanUseNetwork(info);

            case Downloads.STATUS_WAITING_TO_RETRY:
                return restart(info);

            case Downloads.STATUS_DEVICE_NOT_FOUND_ERROR:
                final File file = new File(info.getFileName());
                return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(file));

            case Downloads.STATUS_INSUFFICIENT_SPACE_ERROR:
                // 避免重复重试下载
                return false;

        }
        return false;
    }

    /**
     * 返回此下载是否允许使用网络。
     */
    public boolean checkCanUseNetwork(DownloadInfo info) {
        ConnectivityManager mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo mBluetooth = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

        if (mWifi.isAvailable()) {
            if (mWifi.isConnected() && (info.getAllowedNetworkTypes() & Downloads.NETWORK_WIFI) != 0) {
                return true;
            }
        }
        if (mMobile.isAvailable()) {
            if (mMobile.isConnected() && (info.getAllowedNetworkTypes() & Downloads.NETWORK_MOBILE) != 0) {
                return true;
            }
        }
        if (mBluetooth.isAvailable()) {
            if (mBluetooth.isConnected() && (info.getAllowedNetworkTypes() & Downloads.NETWORK_MOBILE) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回时，应重新启动下载。
     */
    private boolean restart(DownloadInfo info) {
        if (info.getFailedConnections() > Downloads.RETRY_MAX) {
            return true;
        }
        return false;
    }

    private void deleteFileIfExists(String path) {
        if (!TextUtils.isEmpty(path)) {
            final File file = new File(path);
            if (file.exists() && !file.delete()) {
                MLog.w(TAG, "file: '" + path + "' couldn't be deleted");
            }
        }
    }

    /**
     * 下载信息读取类
     */
    private static class Reader {
        private ContentResolver mResolver;
        private Cursor mCursor;

        public Reader(ContentResolver resolver, Cursor cursor) {
            mResolver = resolver;
            mCursor = cursor;
        }

        public DownloadInfo newDownloadInfo(Context context) {
            final DownloadInfo info = new DownloadInfo();
            updateFromDatabase(info);

            return info;
        }

        public void updateFromDatabase(DownloadInfo info) {
            info.setId(getLong(Downloads.Info.ID));
            info.setFileName(getString(Downloads.Info.FILE_NAME));
            info.setTotalBytes(getLong(Downloads.Info.TOTAL_BYTES));
            info.setCurrentBytes(getLong(Downloads.Info.CURRENT_BYTES));
            info.setStatus(getInt(Downloads.Info.STATUS));
            info.setDeleted(getInt(Downloads.Info.DELETED) != 0);
            info.setAllowedNetworkTypes(getInt(Downloads.Info.ALLOWED_NETWORK_TYPES));
            info.setVisibility(getInt(Downloads.Info.VISIBILITY) != 0);
            info.setError(getInt(Downloads.Info.ERROR));
            info.setFailedConnections(getInt(Downloads.Info.FAILED_CONNECTIONS));
        }

        private String getString(String column) {
            int index = mCursor.getColumnIndexOrThrow(column);
            String s = mCursor.getString(index);
            return (TextUtils.isEmpty(s)) ? null : s;
        }

        private Integer getInt(String column) {
            return mCursor.getInt(mCursor.getColumnIndexOrThrow(column));
        }

        private Long getLong(String column) {
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(column));
        }

    }

    /**
     * 下载任务
     */
    private static class DownloadTask extends ATask<Integer> {

        private Context mContext;
        private DownloadManager.Request mRequest;
        private DownloadInfo mListener;

        public DownloadTask(Context context, DownloadManager.Request request, DownloadInfo listener) {
            this.mContext = context;
            this.mRequest = request;
            mListener = listener;
        }

        @Override
        protected Object doInBackground(Object... objs) {
            DownloadHttpClient httpClient = new DownloadHttpClient(mContext, mRequest, mListener);
            httpClient.download();

            return null;
        }

    }

    /**
     * 下载池
     */
    private static class DownloadPool implements Executor {

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
        private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
        private static final int KEEP_ALIVE = 1;

        private final ThreadFactory mThreadFactory;
        private final BlockingQueue<Runnable> mPoolWorkQueue;
        private final ThreadPoolExecutor mThreadPoolExecutor;

        protected DownloadPool() {
            mThreadFactory = new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
                }
            };
            mPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
            mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit
                    .SECONDS, mPoolWorkQueue, mThreadFactory);
        }

        @Override
        public void execute(Runnable runnable) {
            mThreadPoolExecutor.execute(runnable);
        }

    }

}
