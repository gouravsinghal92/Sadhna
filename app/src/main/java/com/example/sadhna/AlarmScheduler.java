package com.example.sadhna;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.AlarmManagerCompat;

import java.util.Calendar;

public class AlarmScheduler {
    private static final int ALARM_REQUEST_CODE = 123; // Unique request code
    private static final String ALARM_PREFERENCE = "alarm_preference";
    private static final String ALARM_ENABLED_KEY = "alarm_enabled";

    private static PendingIntent mPendingIntent;
    public static void scheduleAlarm(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an intent to trigger the alarm
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction("com.example.sadhna.ALARM_TRIGGERED");

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        mPendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, alarmIntent, flag);

        // Set the alarm to trigger at 9 PM daily
        long alarmTime = getAlarmTime();
       // alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, mPendingIntent);

        AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                mPendingIntent
        );

        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, mPendingIntent);

        SharedPreferences sharedPreferences = context.getSharedPreferences(ALARM_PREFERENCE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(ALARM_ENABLED_KEY, true).apply();
    }

    private static long getAlarmTime() {
        // Calculate the time for 9 PM today
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12); // 9 PM
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0);

        long currentTime = System.currentTimeMillis();
        long alarmTime = calendar.getTimeInMillis();

        // If the alarm time is in the past, schedule it for tomorrow
        if (alarmTime <= currentTime) {
            alarmTime += AlarmManager.INTERVAL_DAY;
        }

        return alarmTime;
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an intent to cancel the alarm
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, alarmIntent, flag);

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);

        SharedPreferences sharedPreferences = context.getSharedPreferences(ALARM_PREFERENCE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(ALARM_ENABLED_KEY, false).apply();
    }

    public static boolean isAlarmScheduled(Context context) {
        // Load the alarm state from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(ALARM_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ALARM_ENABLED_KEY, false);
    }
}

