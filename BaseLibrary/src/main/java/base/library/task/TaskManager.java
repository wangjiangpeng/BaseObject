package base.library.task;

import java.util.HashMap;

/**
 * 任务管理
 * <p/>
 * Created by wangjiangpeng01 on 2016/12/23.
 */
public class TaskManager {

    private HashMap<String, ATask> taskMap = new HashMap<>();
    private static TaskManager sTaskManager;

    public interface ResultCallbacks {

        void onFinished(ATask task, Object result);
    }

    /**
     * 单例
     *
     * @return 任务管理单例
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

    }

    /**
     * 通过类获得实例
     * 获取出来的任务都是单例模式
     *
     * @param cls 任务类
     * @return 任务对象
     */
    public <D extends ATask> D getTask(Class<D> cls) {
        String className = cls.getName();
        D task = (D) taskMap.get(className);
        if (task == null) {
            try {
                task = cls.newInstance();
                taskMap.put(className, task);

            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ATask " + className + ": " + e.toString(), e);
            }
        }

        return task;
    }

    /**
     * 执行任务
     *
     * @param cls       要执行的任务类
     * @param callbacks 返回数据处理
     * @param objs      执行参数
     * @param <D>       任务类泛型
     * @return 任务对象
     */
    public <D extends ATask> D executeTask(Class<D> cls, ResultCallbacks callbacks, Object... objs) {
        D task = getTask(cls);
        task.setResultCallbacks(callbacks);
        task.execute(objs);

        return task;
    }

    /**
     * 顺序执行任务
     *
     * @param cls       要执行的任务类
     * @param callbacks 返回数据处理
     * @param objs      执行参数
     * @param <D>       任务类泛型
     * @return 任务对象
     */
    public <D extends ATask> D executeSerialTask(Class<D> cls, ResultCallbacks callbacks, Object... objs) {
        D task = getTask(cls);
        task.setResultCallbacks(callbacks);
        task.executeSerial(objs);

        return task;
    }

    /**
     * 清除数据
     */
    public void clear() {
        taskMap.clear();
    }

}
