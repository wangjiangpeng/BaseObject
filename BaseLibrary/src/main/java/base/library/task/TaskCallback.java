package base.library.task;

/**
 * 任务回调
 *
 * Created by wangjiangpeng01 on 2017/4/18.
 */

public interface TaskCallback {
    /**
     * 任务执行完毕
     *
     * @param task
     * @param result
     */
    void onFinished(ATask task, Object result);
}
