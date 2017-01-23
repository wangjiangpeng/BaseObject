package base.library.cache;

import java.util.HashMap;

/**
 * 缓存
 *
 * Created by wangjiangpeng01 on 2017/1/20.
 */

public abstract class AbsCache<K,E> {

    private HashMap<K, E> map = new HashMap<>();

    /**
     * 通过key查找元素
     *
     * @param key
     * @return
     */
    public synchronized E find(K key){
        E e = map.get(key);
        if(e == null){
            e = create(key);
            map.put(key, e);
        }
        return e;
    }

    /**
     * 创建一个新的元素
     *
     * @param key
     * @return
     */
    protected abstract E create(K key);

    /**
     * 移除一个元素
     *
     * @param key
     * @return
     */
    public synchronized E remove(K key){
        return map.remove(key);
    }

    /**
     * 清空缓存
     */
    public void clear(){
        map.clear();
    }

}
