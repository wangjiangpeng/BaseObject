package base.library.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import base.library.BaseApplication;
import dalvik.system.DexFile;

/**
 * 模块管理
 *
 * <p>
 * Created by wangjiangpeng01 on 2017/4/18.
 */

public class ModuleManager {

    private final ReentrantLock takeLock;
    private final Condition notEmpty;

    private Map<String, Module> moduleMap = new HashMap<>();

    private static ModuleManager sModuleManager;

    public static ModuleManager getInstance() {
        if (sModuleManager == null) {
            synchronized (ModuleManager.class) {
                if (sModuleManager == null) {
                    sModuleManager = new ModuleManager();
                }
            }
        }
        return sModuleManager;
    }

    private ModuleManager() {
        takeLock = new ReentrantLock();
        notEmpty = takeLock.newCondition();
    }

    /**
     * 模块的初始化和加载
     * 先从apk包中过滤出继承module的类，然后再加载模块，过滤类大概用了0.5s，后期可优化
     * 初始化和加载分开，防止部分模块加载过久，导致阻塞过长
     */
    public void initLoad() {
        // 先初始化
        List<String> classes = getExtendsModuleClasses();
        for (String name : classes) {
            try {
                Class cls = Class.forName(name);
                Module module = (Module) cls.newInstance();
                moduleMap.put(name, module);
                notEmpty.signalAll();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 再加载模块
        Set<String> set = moduleMap.keySet();
        for (String key : set) {
            Module module = moduleMap.get(key);
            module.load();
        }
    }

    /**
     * 获得模块对象，如果模块还未初始化，则阻塞
     *
     * @param cls
     * @param <D>
     * @return
     */
    public <D extends Module> D getModule(Class<D> cls) {
        D module;
        while ((module = (D) moduleMap.get(cls.getName())) == null) {
            try {
                notEmpty.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return module;
    }

    /**
     * 获得apk包中的所有实现Module的类
     *
     * @return
     */
    private List<String> getExtendsModuleClasses() {
        List<String> list = new ArrayList<>();
        try {
            BaseApplication application = BaseApplication.getInstance();
            DexFile dexFile = new DexFile(application.getPackageCodePath());
            Enumeration<String> enumeration = dexFile.entries();
            while (enumeration.hasMoreElements()) {
                String className = enumeration.nextElement();
                try {
                    Class cls = Class.forName(className);
                    if (Module.class.isAssignableFrom(cls) && !Module.class.equals(cls)) {
                        list.add(className);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return list;
    }

}
