package com.download;

import com.download.store.DownloadDao;

import java.util.List;

import static android.R.attr.id;

/**
 * Created by wangjiangpeng01 on 2017/5/10.
 *
 */

public class DownloadProvider {

    private final DownloadService mDownloadService;
    private final DownloadDao mDownloadDao;

    public DownloadProvider(){
        mDownloadService = new DownloadService(this);
        mDownloadDao = new DownloadDao();
    }

    /**
     * 插入下载记录
     *
     * @param param
     * @return
     */
    public long insert(DownloadParam param){
        long id = mDownloadDao.insert(param);
        param.setId(id);
        mDownloadService.startService();

        return id;
    }

    /**
     * 删除下载记录，物理删除
     *
     * @param id
     * @return
     */
    public long deleted(long id){
        long dId = mDownloadDao.deleted(id);
        return dId;
    }

    /**
     * 查找下载数据
     *
     * @param id
     * @return 无数据为空
     */
    public DownloadParam query(long id){
        return mDownloadDao.query(id);
    }

    /**
     * 查找所有活动的下载数据
     *
     * @return
     */
    public List<DownloadParam> queryAllActive(){
        return mDownloadDao.queryAllActive();
    }

}
