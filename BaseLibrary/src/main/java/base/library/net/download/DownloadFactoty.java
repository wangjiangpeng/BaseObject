package base.library.net.download;

/**
 * Created by wangjiangpeng01 on 2017/4/28.
 */

public class DownloadFactoty {

    private static IDownloadManager iDownloadManager;

    private DownloadFactoty(){

    }

    public static IDownloadManager getDownloadManager(){
        if(iDownloadManager == null){
            synchronized (IDownloadManager.class){
                if(iDownloadManager == null){
                    iDownloadManager = new DownloadManagerImpl();
                }
            }
        }
        return iDownloadManager;
    }



}
