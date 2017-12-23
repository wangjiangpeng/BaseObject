package base.library;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

/**
 * 守护进程service，保证app进程的常驻
 *
 * @author Administrator
 *
 */
public class DaemonService extends Service {

    private static final int DELAY_TIME = 10000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startId值越大，重启时间越久，为了保证这较短的时间内重启，在startId比较大的时候，结束并重启自己
        // 3的时候，重启时间小于10s
        if (startId > 3) {
            stopSelf();
        }
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // 闹钟定时重启service
        restartService();
    }

    private void restartService() {
        Context context = getApplicationContext();
        Intent intent = new Intent(this, DaemonService.class);
        intent.setPackage(getPackageName());
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + DELAY_TIME, pendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(this, DaemonService.class);
        startService(intent);
    }

}
