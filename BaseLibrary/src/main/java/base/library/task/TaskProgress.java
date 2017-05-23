package base.library.task;

/**
 * 任务进度更新
 * <p>
 * Created by wangjiangpeng01 on 2017/5/23.
 */

public interface TaskProgress {

    /**
     * task 调用publishProgress方法时触发
     *
     * @param task
     * @param value
     */
    void onProgressUpdate(ATask task, Object value);

}
