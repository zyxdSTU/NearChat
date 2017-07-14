package nearchat.xiaoxiong.com.nearchat.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.util.Manager;

public class AutoUpdateService extends Service {
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private PendingIntent pi;

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /**在这里执行更新程序**/
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "服务");
                Manager.getInstance().updateContactList();
                Manager.getInstance().updateContactImage();
            }
        }).start();

        /**十分钟**/
        AlarmManager manager = (AlarmManager) this.getSystemService(ALARM_SERVICE);

        int recycleTime = 10 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + recycleTime;
        Intent i = new Intent(this, AutoUpdateService.class);
        pi = PendingIntent.getService(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.LOGOUT_SUCCESS);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(Constant.LOGOUT_SUCCESS)) {
                    stopSelf();
                    AlarmManager manager = (AlarmManager) AutoUpdateService.this.getSystemService(ALARM_SERVICE);
                    manager.cancel(pi);
                }
            }
        };

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
        super.onCreate();
    }
}
