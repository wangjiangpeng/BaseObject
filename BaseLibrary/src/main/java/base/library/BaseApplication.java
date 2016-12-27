package base.library;

import android.app.Application;

/**
 * Created by wangjiangpeng01 on 2016/11/23.
 */
public class BaseApplication extends Application {

    private static Application application;

    public static Application getInstance(){
        return application;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

    }




}
