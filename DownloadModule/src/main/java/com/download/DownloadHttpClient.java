package com.download;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ProtocolException;

import base.library.MLog;
import base.library.net.HttpClient;
import base.library.net.ResponseData;

import static com.download.Downloads.STATUS_CANNOT_RESUME;
import static com.download.Downloads.STATUS_HTTP_DATA_ERROR;
import static com.download.Downloads.STATUS_UNHANDLED_HTTP_CODE;
import static com.squareup.okhttp.internal.http.StatusLine.HTTP_TEMP_REDIRECT;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

/**
 * 下载
 * Created by wangjiangpeng01 on 2017/5/10.
 */

public class DownloadHttpClient extends HttpClient {

    private static final String TAG = "DownloadHttpClient";

    private static final int MIN_PROGRESS_STEP = 1024 * 1024;
    private static final int MIN_PROGRESS_TIME = 1000;

    private Context mContext;
    private long mId;

    private DownloadManager.Request mRequest;
    private DownloadInfo mInfo;

    /**
     * Details from the last time we pushed a database update.
     */
    private long mLastUpdateBytes = 0;
    private long mLastUpdateTime = 0;

    /**
     * Historical bytes/second speed of this download.
     */
    private long mSpeed;
    /**
     * Time when current sample started.
     */
    private long mSpeedSampleStart;
    /**
     * Bytes transferred since current sample started.
     */
    private long mSpeedSampleBytes;

    public DownloadHttpClient(Context context, DownloadManager.Request request, DownloadInfo info) {
        this.mContext = context;
        this.mRequest = request;
        this.mInfo = info;
        this.mId = mInfo.getId();
    }

    /**
     * 下载
     */
    public void download() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (queryDownloadStatus(mContext.getContentResolver(), mId) == Downloads.STATUS_SUCCESS) {
            MLog.d(TAG, "Already finished; skipping");
            return;
        }

        try {
            MLog.d(TAG, "starting");

            executeDownload();
            // If we just finished a chunked file, record total size
            if (mInfo.getTotalBytes() == -1) {
                mInfo.setTotalBytes(mInfo.getCurrentBytes());
            }

            mInfo.setStatus(Downloads.STATUS_SUCCESS);

        } catch (StopRequestException e) {

        } finally {
            writeToDatabaseOrThrow();
        }
    }

    private void executeDownload() throws StopRequestException {
        OkHttpClient client = createOkHttps(mRequest);
        Request.Builder rBuilder = createRequestBuilder(mRequest);

        // 头部续传参数
        File file = new File(mInfo.getFileName());
        long currentBytes = 0;
        if (mInfo.getCurrentBytes() != 0 && file.exists()) {
            currentBytes = mInfo.getCurrentBytes();
            rBuilder.addHeader("RANGE", "bytes=" + currentBytes + "-");
        }
        // 下载
        ResponseData responseData = new ResponseData();
        Request req = rBuilder.build();
        try {
            Response response = client.newCall(req).execute();
            final int responseCode = response.code();
            switch (responseCode) {
                case HTTP_OK:
                    if (currentBytes != 0) {
                        throw new StopRequestException(STATUS_CANNOT_RESUME, "Expected partial, but received OK");
                    }
                    parseOkHeaders(response);
                    transferData(response);
                    break;

                case HTTP_PARTIAL:
                    if (currentBytes != 0) {
                        throw new StopRequestException(STATUS_CANNOT_RESUME, "Expected OK, but received partial");
                    }
                    transferData(response);
                    break;

                case HTTP_MOVED_PERM:
                case HTTP_MOVED_TEMP:
                case HTTP_SEE_OTHER:
                case HTTP_TEMP_REDIRECT:
                    // 重定向暂时不考虑
                    break;

                case HTTP_PRECON_FAILED:
                    throw new StopRequestException(STATUS_CANNOT_RESUME, "Precondition failed");

                case HTTP_UNAVAILABLE:
                    throw new StopRequestException(HTTP_UNAVAILABLE, response.message());

                case HTTP_INTERNAL_ERROR:
                    throw new StopRequestException(HTTP_INTERNAL_ERROR, response.message());

                default:
                    throw new StopRequestException(responseCode, response.message());
            }


        } catch (IOException e) {
            if (e instanceof ProtocolException && e.getMessage().startsWith("Unexpected status line")) {
                throw new StopRequestException(STATUS_UNHANDLED_HTTP_CODE, e);
            } else {
                // Trouble with low-level sockets
                throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);
            }

        }
    }

    private void transferData(Response response) throws StopRequestException {
        final byte[] buf = new byte[1024];
        InputStream is = null;
        RandomAccessFile raf = null;
        try {
            is = response.body().byteStream();
            long total = response.body().contentLength();
            if (mInfo.getCurrentBytes() != 0) {
                raf = new RandomAccessFile(mInfo.getFileName(), "rw");
                raf.seek(mInfo.getCurrentBytes());

            } else {
                File file = new File(mInfo.getFileName());
                if (!file.exists()) {
                    file.createNewFile();
                }
                raf = new RandomAccessFile(mInfo.getFileName(), "rw");
                if (total > 0) {
                    raf.setLength(total);
                }
            }

            while (true) {
                checkPausedOrCanceled();
                int len = -1;
                try {
                    len = is.read(buf);
                } catch (IOException e) {
                    throw new StopRequestException(STATUS_HTTP_DATA_ERROR, "Failed reading response: " + e, e);
                }

                if (len == -1) {
                    break;
                }
                raf.write(buf, 0, len);
                mInfo.setCurrentBytes(mInfo.getCurrentBytes() + len);
                updateProgress();
            }

        } catch (Exception e) {
            throw new StopRequestException(Downloads.STATUS_FILE_ERROR, e);

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Process response headers from first server response. This derives its
     * filename, size, and ETag.
     */
    private void parseOkHeaders(Response response) throws StopRequestException {
        final String transferEncoding = response.header("Transfer-Encoding");
        if (transferEncoding == null) {
            mInfo.setTotalBytes(getHeaderFieldLong(response, "Content-Length", -1));
        } else {
            mInfo.setTotalBytes(-1);
        }

        writeToDatabaseOrThrow();
    }

    private static long getHeaderFieldLong(Response response, String field, long defaultValue) {
        try {
            return Long.parseLong(response.header(field));

        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void writeToDatabaseOrThrow() throws StopRequestException {
        if (mContext.getContentResolver().update(ContentUris.withAppendedId(Downloads.CONTENT_URI, mId),
                buildContentValues(), Downloads.Info.DELETED + " == '0'", null) == 0) {
            throw new StopRequestException(Downloads.STATUS_CANCLED, "Download deleted or missing!");
        }
    }

    /**
     * 查询并返回下载请求状态
     */
    public static int queryDownloadStatus(ContentResolver resolver, long id) {
        final Cursor cursor = resolver.query(ContentUris.withAppendedId(Downloads.CONTENT_URI, id), new
                String[]{Downloads.Info.STATUS}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // downloads; this is safe default for now.
                return Downloads.STATUS_PENDING;
            }
        } finally {
            cursor.close();
        }
    }

    private void checkPausedOrCanceled() throws StopRequestException {
        synchronized (mInfo) {
            if (mInfo.getControl() == Downloads.CONTROL_PAUSED) {
                throw new StopRequestException(Downloads.CONTROL_PAUSED, "download paused by owner");
            }
            if (mInfo.getStatus() == Downloads.STATUS_CANCLED || mInfo.isDeleted()) {
                throw new StopRequestException(Downloads.STATUS_CANCLED, "download canceled");
            }
        }
    }


    private void updateProgress() {
        final long now = SystemClock.elapsedRealtime();
        final long currentBytes = mInfo.getCurrentBytes();

        final long sampleDelta = now - mSpeedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((currentBytes - mSpeedSampleBytes) * 1000) / sampleDelta;

            if (mSpeed == 0) {
                mSpeed = sampleSpeed;

            } else {
                mSpeed = ((mSpeed * 3) + sampleSpeed) / 4;
            }

            // Only notify once we have a full sample window
            if (mSpeedSampleStart != 0) {
                Log.e("WJP", "mId:" + mId + " mSpeed:" + mSpeed);
            }

            mSpeedSampleStart = now;
            mSpeedSampleBytes = currentBytes;
        }

        final long bytesDelta = currentBytes - mLastUpdateBytes;
        final long timeDelta = now - mLastUpdateTime;
        if (bytesDelta > MIN_PROGRESS_STEP && timeDelta > MIN_PROGRESS_TIME) {
            writeToDatabaseOrThrow();

            mLastUpdateBytes = currentBytes;
            mLastUpdateTime = now;
        }
    }

    private ContentValues buildContentValues() {
        final ContentValues values = new ContentValues();

        values.put(Downloads.Info.FILE_NAME, mInfo.getFileName());
        values.put(Downloads.Info.STATUS, mInfo.getStatus());
        values.put(Downloads.Info.FAILED_CONNECTIONS, mInfo.getFailedConnections());
        values.put(Downloads.Info.CONTROL, mInfo.getControl());
        values.put(Downloads.Info.CURRENT_BYTES, mInfo.getCurrentBytes());
        values.put(Downloads.Info.TOTAL_BYTES, mInfo.getTotalBytes());

        return values;
    }

}
