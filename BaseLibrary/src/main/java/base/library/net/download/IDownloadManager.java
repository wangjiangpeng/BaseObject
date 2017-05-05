package base.library.net.download;

/**
 * Created by wangjiangpeng01 on 2017/4/28.
 */

public interface IDownloadManager {

    int STATE_WAIT = 0;
    int STATE_DOWNLOADING = 1;
    int STATE_DOWNLOADED = 2;
    int STATE_STOP = 3;
    int STATE_CANCLE = 4;
    int STATE_ERROR = 5;

    /**
     * 添加到下载队列
     *
     * @param param
     * @return
     */
    long enqueue(DownloadParam param);

    /**
     * 删除下载
     *
     * @param id
     * @return
     */
    boolean remove(long id);

    /**
     * 暂停下载
     *
     * @param id
     * @return
     */
    boolean stop(long id);

    /**
     * 添加下载监听
     * 弱引用，无需手动移除监听
     *
     * @param listener
     */
    void addDownloadListener(DownloadListener listener);



}
