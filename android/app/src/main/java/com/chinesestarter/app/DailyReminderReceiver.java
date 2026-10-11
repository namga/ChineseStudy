package com.chinesestarter.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class DailyReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "chinese_starter_learning";
    @Override public void onReceive(Context context, Intent intent) {
        if (!context.getSharedPreferences("learning_reminder", Context.MODE_PRIVATE).getBoolean("enabled", false)) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(new NotificationChannel(CHANNEL, "Daily Chinese practice", NotificationManager.IMPORTANCE_DEFAULT));
        Intent launch = new Intent(context, MainActivity.class);
        PendingIntent open = PendingIntent.getActivity(context, 6108, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("Time for Chinese Starter!")
            .setContentText("A few minutes of Mandarin practice today will keep your progress going.")
            .setContentIntent(open).setAutoCancel(true);
        try { manager.notify(6107, builder.build()); } catch (SecurityException ignored) { }
    }
}
