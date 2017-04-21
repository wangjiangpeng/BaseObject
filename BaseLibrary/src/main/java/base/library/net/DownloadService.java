package base.library.net;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import base.library.store.BaseDao;
import base.library.task.ATask;
import base.library.task.TaskCallbacks;

import static android.R.attr.id;

/**
 * 下载服务
 * <p>
 * Created by wangjiangpeng01 on 2017/4/11.
 */

public class DownloadService {

    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    /** 根据处理器个数，设置最大下载线程数 */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_DOWNLOAD_CAPACITY = PROCESSORS > 1 ? PROCESSORS : 1;

    /** 下载等候队列 */
    private final PriorityBlockingQueue<DownloadParam> queue;
    /** 正在下载数组 */
    private final DownloadTask[] items;
    /** 锁 */
    private final ReentrantLock lock = new ReentrantLock();

    private TaskCallbacks mCallback = new TaskCallbacks() {
        @Override
        public void onFinished(ATask task, Object result) {

        }
    };

    public DownloadService() {
        queue = new PriorityBlockingQueue<>(DEFAULT_INITIAL_CAPACITY, new DownloadComparator());
        items = new DownloadTask[DEFAULT_DOWNLOAD_CAPACITY];
    }

    /**
     * 开始下载
     *
     * @param param 下载参数
     */
    public long startDownload(DownloadParam param) {
        if (param == null) {
            throw new NullPointerException("DownloadParam is null");
        }

        long id = enqueue(param);
        tryDownload();
        notifyContentChange();

        return id;
    }

    /**
     * 加入下载队列
     *
     * @param param
     * @return
     */
    private long enqueue(DownloadParam param){
        BaseDao baseDao = BaseDao.getInstance();
        long id = baseDao.addDownload(param.getUrl(), param.getDownloadPath());
        queue.add(param);// 添加到等候下载队列
        return id;
    }

    /**
     * 尝试下载
     */
    private void tryDownload() {
        lock.lock();
        try {
            for (int index = 0; index < items.length; index++) {
                DownloadTask task= items[index];
                // 如果为空，新建一个下载任务
                if (task == null) {
                    DownloadParam param = queue.poll();
                    if (param != null) {
                        items[index] = new DownloadTask(param);
                        items[index].execute(mCallback);
                        return;
                    }

                } else if (task.isFinished()) {
                    // 如果下载任务已经完成，重置后重新下载
                    DownloadParam param = queue.poll();
                    if (param != null) {
                        task.setDownloadParam(param);
                        task.reExecute(mCallback);
                        return;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void notifyContentChange(){

    }

    /**
     * 是否有空闲
     *
     * @return
     */
    public boolean hasFree() {
        for (int index = 0; index < items.length; index++) {
            if (items[index] == null) {
                return true;
            } else if (items[index].isFinished()) {
                return true;
            }
        }
        return false;
    }

    private static class DownloadComparator implements Comparator<DownloadParam> {

        @Override
        public int compare(DownloadParam lhs, DownloadParam rhs) {
            return lhs.getPriority() > rhs.getPriority() ? 1 : -1;
        }
    }

    private static class DownloadTask extends ATask<Integer> {

        private DownloadParam mParam;

        public DownloadTask(DownloadParam param) {
            mParam = param;
        }

        public void setDownloadParam(DownloadParam param) {
            mParam = param;
        }

        @Override
        protected Object doInBackground(Object... objs) {
            HttpClient httpClient = new HttpClient();
            httpClient.download(mParam, null);

            return null;
        }
    }

}
