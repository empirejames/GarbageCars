package com.james.garbagecar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 101716 on 2018/7/31.
 */

public class LongRunningService extends Service {

    public static final String BROADCAST_ACTION = "com.example.test";
    private Intent intent = null;
    boolean isStop = false;
    @Override
    public void onCreate()
    {
        super.onCreate();

        new Thread(){//新建线程，每隔1秒发送一次广播，同时把i放进intent传出
            public void run(){
                while(!isStop){
                    Intent intent=new Intent();
                    intent.putExtra("i", getDateTime());
                    intent.setAction("android.intent.action.test");//action与接收器相同
                    sendBroadcast(intent);
                    try {
                        sleep(90* 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LongRunningService", "executed at " + new Date().
                        toString());
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 90 * 1000; // 这是一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }
    public String getDateTime(){
        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date();
        String strDate = sdFormat.format(date);

        return strDate;
    }
}
