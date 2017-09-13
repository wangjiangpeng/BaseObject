package base.library.task;

import base.library.BaseApplication;
import base.library.module.ModuleManager;

/**
 * 环境初始化
 * 此任务是所有任务的前置任务，再这个任务未执行完毕之前，其他任务都要在他之后执行
 * <p>
 * Created by wangjiangpeng01 on 2017/3/1.
 */

public class EnvInitTask extends ATask {

    @Override
    protected Object doInBackground(Object... objs) {
        BaseApplication application = BaseApplication.getInstance();
        ModuleManager manager = ModuleManager.getInstance();

        application.addModule(manager);
        manager.initLoad();

        application.envInit();

        return null;
    }

    @Override
    public void execute(Object... objs) {
        throw new RuntimeException("can not support");
    }

}
