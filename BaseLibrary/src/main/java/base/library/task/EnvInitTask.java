package base.library.task;

/**
 * 环境初始化任务
 *
 * Created by wangjiangpeng01 on 2016/12/22.
 */
public class EnvInitTask extends ATask<Void, String> {

    @Override
    protected String doInBackground(Object... objs) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return (String)objs[0];
    }

}
