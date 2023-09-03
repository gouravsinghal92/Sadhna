package com.example.sadhna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Display a notification or perform any other action when the alarm triggers
        // You can create and show a notification here

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            AlarmScheduler.scheduleAlarm(context);
        }
        else {

            Toast.makeText(context, "Hare Krishna Prabhu!", Toast.LENGTH_LONG).show();

            // Get the default alarm ringtone
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            // Create a Ringtone object
            Ringtone ringtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);

            // Play the ringtone
            if (ringtone != null) {
                ringtone.play();
            }

            AlarmScheduler.scheduleAlarm(context);
        }
    }
}