package base.library.task;

import android.app.Application;

import java.util.HashMap;

import base.library.BaseApplication;

/**
 * 任务管理
 * <p/>
 * Created by wangjiangpeng01 on 2016/12/23.
 */
public class TaskManager {

    private HashMap<String, ATask> taskMap = new HashMap<String, ATask>();
    private ClassLoader classLoader;
    private static TaskManager sTaskManager;

    /**
     * 单例
     *
     * @return
     */
    public static TaskManager getInstance() {
        if (sTaskManager == null) {
            synchronized (TaskThreadPool.class) {
                if (sTaskManager == null) {
                    sTaskManager = new TaskManager();
                }
            }
        }

        return sTaskManager;
    }

    private TaskManager() {
        Application application = BaseApplication.getInstance();
        classLoader = application.getClassLoader();
    }

    public ATask getTask(Class cls) {
        String className = cls.getName();
        ATask task = taskMap.get(className);
        if (task == null) {
            try {
                task = newTask(className);
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ATask " + className + ": " + e.toString(), e);
            }
        }

        return task;
    }

    private ATask newTask(String className) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        ATask task = (ATask) classLoader.loadClass(className).newInstance();
        taskMap.put(className, task);

        return task;
    }


    public void clear() {
        taskMap.clear();
    }

}
