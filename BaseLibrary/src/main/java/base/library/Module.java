package base.library;

/**
 * 模块接口
 *
 * Created by wangjiangpeng01 on 2017/2/7.
 */

public interface Module {

    /**
     * 模块加载
     */
    public void load();

    /**
     * 模块卸载
     */
    public void unload();

}
