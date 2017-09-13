package base.library.module;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import base.library.BaseApplication;
import dalvik.system.DexFile;

/**
 * 模块管理
 * <p>
 * <p>
 * Created by wangjiangpeng01 on 2017/4/18.
 */

public class ModuleManager {

    private final Object notEmpty = new Object();

    private HashMap<String, Module> moduleMap = new HashMap<>();

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
    }

    /**
     * 添加模块
     *
     * @param module
     */
    public void addModule(Module module) {
        moduleMap.put(module.getClass().getName(), module);
    }

    /**
     * 模块的加载
     * 先从apk包中过滤出继承module的类，然后再加载模块，过滤类大概用了0.5s，后期可优化
     * 初始化和加载分开，防止部分模块加载过久，导致阻塞过长
     */
    public void initLoad() {
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
        if ((module = (D) moduleMap.get(cls.getName())) == null) {
            throw new RuntimeException("not found module, please add " + cls.getName());
        }
        return module;
    }

}
