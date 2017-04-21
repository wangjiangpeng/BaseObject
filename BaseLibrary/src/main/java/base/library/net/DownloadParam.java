package base.library.net;

import static android.R.attr.priority;

/**
 * 下载请求参数
 *
 * Created by wangjiangpeng01 on 2017/4/5.
 */

public class DownloadParam extends RequestParam {
    /**
     * 本地下载路径
     */
    private String downloadPath;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 续传
     */
    private long skip;

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getSkip() {
        return skip;
    }

    public void setSkip(long skip) {
        this.skip = skip;
    }
}
