package base.library.net.download;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import base.library.net.HttpClient;
import base.library.store.BaseDao;
import base.library.task.ATask;
import base.library.task.TaskCallbacks;

/**
 * 下载管理
 *
 * Created by wangjiangpeng01 on 2017/4/28.
 */

public class DownloadManagerImpl implements IDownloadManager {

    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    /**
     * 根据处理器个数，设置最大下载线程数
     */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_DOWNLOAD_CAPACITY = PROCESSORS > 1 ? PROCESSORS : 1;

    /**
     * 下载等候队列
     */
    private final PriorityBlockingQueue<DownloadTask> mWaits;
    /**
     * 正在下载
     */
    private final DownloadTask[] mDownloads;

    /**
     * 暂停
     */
    private final ArrayList<DownloadTask> mStops = new ArrayList<>();

    private final ArrayList<WeakReference<DownloadListener>> mListeners = new ArrayList<>();

    private TaskCallbacks mCallback = new TaskCallbacks() {
        @Override
        public void onFinished(ATask task, Object result) {
            tryDownload();
        }
    };

    private DownloadListener mAllListener = new DownloadListener() {

        @Override
        public void onDownloaded(long id, long total, long downloadLength) {

        }
    };

    protected DownloadManagerImpl() {
        mWaits = new PriorityBlockingQueue<>(DEFAULT_INITIAL_CAPACITY, new DownloadComparator());
        mDownloads = new DownloadTask[DEFAULT_DOWNLOAD_CAPACITY];
    }

    @Override
    public synchronized long enqueue(DownloadParam param) {
        if (param == null) {
            throw new NullPointerException();
        }

        BaseDao baseDao = BaseDao.getInstance();
        long id = baseDao.insertDownload(param.getUrl(), param.getDownloadPath());
        param.setDownloadId(id);
        mWaits.add(new DownloadTask(param, mAllListener));// 添加到等候下载队列
        tryDownload();

        return id;
    }

    @Override
    public synchronized boolean remove(long id) {
        // downloading
        for (int index = 0; index < mDownloads.length; index++) {
            DownloadTask task = mDownloads[index];
            if (task != null && task.getDownloadId() == id ) {
                task.setDownloadState(IDownloadManager.STATE_CANCLE);
                mDownloads[index] = null;
                return true;
            }
        }

        // wait
        Iterator<DownloadTask> itr = mWaits.iterator();
        while(itr.hasNext()){
            DownloadTask task = itr.next();
            if (task.getDownloadId() == id ) {
                task.setDownloadState(IDownloadManager.STATE_CANCLE);
                itr.remove();
                return true;
            }
        }

        // stop
        itr = mStops.iterator();
        while(itr.hasNext()){
            DownloadTask task = itr.next();
            if (task.getDownloadId() == id ) {
                task.setDownloadState(IDownloadManager.STATE_CANCLE);
                itr.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized boolean stop(long id) {
        return false;
    }

    /**
     * 尝试下载
     *
     * @return 是否开始下载
     */
    private boolean tryDownload() {
        for (int index = 0; index < mDownloads.length; index++) {
            DownloadTask task = mDownloads[index];
            if (task == null || task.isFinished()) {
                task = mWaits.poll();
                if (task != null) {
                    task.reExecute(mCallback);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 通知当前内容变更
     */
    private void notifyContentChange() {

    }

    /**
     * 是否有空闲
     *
     * @return
     */
    public boolean hasFree() {
        for (int index = 0; index < mDownloads.length; index++) {
            if (mDownloads[index] == null) {
                return true;
            } else if (mDownloads[index].isFinished()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void addDownloadListener(DownloadListener listener) {
        WeakReference<DownloadListener> reference = new WeakReference<DownloadListener>(listener);
        mListeners.add(reference);
    }

    private static class DownloadComparator implements Comparator<DownloadTask> {

        @Override
        public int compare(DownloadTask lhs, DownloadTask rhs) {
            return lhs.getPriority() > rhs.getPriority() ? 1 : -1;
        }
    }

    private static class DownloadTask extends ATask<Integer> {

        private DownloadParam mParam;

        private DownloadListener mListener;

        public DownloadTask(DownloadParam param, DownloadListener listener) {
            mParam = param;
            mListener = listener;
        }

        @Override
        protected Object doInBackground(Object... objs) {
            HttpClient httpClient = new HttpClient();
            httpClient.download(mParam, mListener);

            return null;
        }

        public int getPriority() {
            return mParam.getPriority();
        }

        public long getDownloadId() {
            return mParam.getDownloadId();
        }

        public void setDownloadState(int state) {
            mParam.setState(state);
        }
    }

}
