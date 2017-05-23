package base.library;

import android.app.Application;

import base.library.task.ATask;
import base.library.task.EnvInitTask;
import base.library.task.TaskService;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class BaseApplication extends Application {

    private static BaseApplication application;

    public static BaseApplication getInstance(){
        return application;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        ATask task = TaskService.getInstance().getTask(EnvInitTask.class);
        task.executeSerial();
    }

    /**
     * 环境初始化，在环境初始化任务中被调用
     * (适用程序运行所必须的，耗时的数据初始化)
     */
    public void envInit(){
    }


}
