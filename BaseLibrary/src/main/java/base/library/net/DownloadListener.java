package base.library.net;

/**
 * 下载监听
 *
 * Created by wangjiangpeng01 on 2017/4/11.
 */

public interface DownloadListener {

    /**
     * 下载
     *
     * @param total 文件大小
     * @param downloadLength 已下载长度
     */
    void onDownloaded(long total, long downloadLength);

}
