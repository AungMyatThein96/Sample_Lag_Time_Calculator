package com.example.lagtimecalculator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class SampleAlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "sample_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Sample Collection Alarm",
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Lag-time sample collection reminders");
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
            context, 2001, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26)
            b = new android.app.Notification.Builder(context, CHANNEL_ID);
        else
            b = new android.app.Notification.Builder(context);

        b.setSmallIcon(R.drawable.ic_launcher)
         .setContentTitle("Sample Collection Time")
         .setContentText("Lag time has elapsed — collect the sample at surface.")
         .setAutoCancel(true)
         .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT < 26)
            b.setPriority(android.app.Notification.PRIORITY_MAX);

        nm.notify(2001, b.build());
    }
}
