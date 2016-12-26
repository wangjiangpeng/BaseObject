package base.library.task;

/**
 * 环境初始化任务
 *
 * Created by wangjiangpeng01 on 2016/12/22.
 */
public class EnvInitTask<Progress, Result> extends ATask<Progress, Result> {

    @Override
    protected Result doInBackground(Object... objs) {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}
