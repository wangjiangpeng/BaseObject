package base.library.task;

import java.util.HashMap;

/**
 * 任务管理
 *
 * Created by wangjiangpeng01 on 2016/12/23.
 */
public class TaskManager {

    private HashMap<String, ATask> taskMap = new HashMap<String, ATask>();

    private ATask getTask(Class cls) {
        ATask task = taskMap.get(cls.getName());
        if (task == null) {
//            (Activity)cl.loadClass(cls.getName()).newInstance();
        }

        return task;
    }


    public void clear() {
        taskMap.clear();
    }

}
