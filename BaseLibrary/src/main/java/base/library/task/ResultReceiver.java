package base.library.task;

/**
 * 任务返回接收器
 *
 * Created by wangjiangpeng01 on 2016/12/22.
 */
public interface ResultReceiver {

    /**
     * 接收
     *
     * @param task 任务
     * @param result 结果
     */
    void receiver(ATask task, Object result);

}
