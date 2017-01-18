package base.library;

import android.util.Log;

/**
 * 日志
 *
 * Created by wangjiangpeng01 on 2017/1/10.
 */

public class MLog {

    private static boolean HTTP = true;

    public static void http(String tag, String msg) {
        if (HTTP)
            Log.v(tag, msg);
    }

}
