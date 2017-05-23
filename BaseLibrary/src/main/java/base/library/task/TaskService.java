package base.library.task;

import java.lang.reflect.Constructor;
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

    private TaskService() {

    }

    /**
     * @param cls 任务类
     * @param obj 初始化参数
     * @param <D> 任务对象
     * @return
     */
    public <D extends ATask> D getTask(Class<D> cls, Object... obj) {
        String key = getKey(cls, obj);
        D task = (D) taskMap.get(key);
        if (task == null) {
            try {
                // 线程安全
                synchronized (taskMap) {
                    task = (D) taskMap.get(key);
                    if (task == null) {
                        task = newInstance(cls, obj);
                        taskMap.put(key, task);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ATask " + cls.getName() + ": " + e.toString(), e);
            }
        }

        return task;
    }

    private <D extends ATask> D newInstance(Class<D> cls, Object... obj) throws Exception {
        if (obj == null || obj.length == 0) {
            return cls.newInstance();
        }
        int length = obj.length;
        Class<?>[] classes = new Class<?>[length];
        for (int index = 0; index < length; index++) {
            classes[index] = obj[index].getClass();
        }
        Constructor<D> constructor = cls.getConstructor(classes);
        return constructor.newInstance(obj);

    }

    private String getKey(Class cls, Object... obj) {
        if (obj == null || obj.length == 0) {
            return cls.getName();
        }
        String name = cls.getName();
        int length = obj.length;
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        for (int index = 0; index < length; index++) {
            sb.append(obj[index]);
        }
        return sb.toString();
    }

}
