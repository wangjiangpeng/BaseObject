package base.library;

import android.util.Log;

/**
 * 日志
 * <p>
 * Created by wangjiangpeng01 on 2017/1/10.
 */

public class MLog {

    private static boolean HTTP = true;
    private static boolean E = true;
    private static boolean V = true;
    private static boolean D = true;
    private static boolean I = true;
    private static boolean W = true;

    public static void http(String tag, String msg) {
        if (HTTP)
            Log.v(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (E)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (V)
            Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (D)
            Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (I)
            Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (W)
            Log.w(tag, msg);
    }

}
