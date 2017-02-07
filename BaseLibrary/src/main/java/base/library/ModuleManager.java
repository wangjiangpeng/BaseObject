package base.library;

import java.util.HashMap;

/**
 * 模块管理
 * <p>
 * Created by wangjiangpeng01 on 2017/2/7.
 */

public class ModuleManager {
    private HashMap<String, Module> moduleMap = new HashMap<>();

    public ModuleManager() {

    }

    /**
     * 模块注册
     *
     * @param cls
     */
    public void registerModule(Class<Module> cls) {
        String className = cls.getName();
        Module module = moduleMap.get(className);
        if (module == null) {
            try {
                module = cls.newInstance();
                module.load();
                moduleMap.put(className, module);

            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate Module " + className + ": " + e.toString(), e);
            }
        }
    }

    /**
     * 模块取消注册
     *
     * @param cls
     */
    public void unregisterModule(Class<Module> cls) {
        String className = cls.getName();
        Module module = moduleMap.get(className);
        if (module != null) {
            module.unload();
            moduleMap.remove(module);
        }
    }

}
