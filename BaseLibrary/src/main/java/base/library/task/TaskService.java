package base.library.task;

import java.util.HashMap;
import java.util.Map;

import base.library.module.ModuleManager;

/**
 * 任务池
 * <p/>
 * Created by wangjiangpeng01 on 2016/12/23.
 */
public class TaskService {

    private Map<String, ATask> taskMap = new HashMap<>();

    private static TaskService sTaskService;

    public static TaskService getInstance() {
        if (sTaskService == null) {
            synchronized (ModuleManager.class) {
                if (sTaskService == null) {
                    sTaskService = new TaskService();
                }
            }
        }
        return sTaskService;
    }

    private TaskService(){

    }

    /**
     * 通过类获得实例
     * 获取出来的任务都是单例模式
     * 线程安全
     *
     * @param cls 任务类
     * @return 任务对象
     */
    public <D extends ATask> D getTask(Class<D> cls) {
        String className = cls.getName();
        D task = (D) taskMap.get(className);
        if (task == null) {
            try {
                // 线程安全
                synchronized (taskMap) {
                    task = (D) taskMap.get(className);
                    if (task == null) {
                        task = cls.newInstance();
                        taskMap.put(className, task);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ATask " + className + ": " + e.toString(), e);
            }
        }

        return task;
    }

}
