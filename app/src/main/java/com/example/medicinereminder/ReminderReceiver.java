package com.example.medicinereminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MEDICINE_REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Get the data passed from the Activity or previous Alarm
        String name = intent.getStringExtra("medName");
        int alarmId = intent.getIntExtra("alarmId", 0);
        int repeatCount = intent.getIntExtra("repeatCount", 1); // Tracks 1st, 2nd, or 3rd alert

        // 2. Show the Notification
        showNotification(context, name, repeatCount);

        // 3. If we haven't reached 3 notifications yet, schedule the next "nudge"
        if (repeatCount < 3) {
            scheduleNextNudge(context, name, alarmId, repeatCount + 1);
        }
    }

    private void showNotification(Context context, String name, int count) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Setup Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Dose Reminders", NotificationManager.IMPORTANCE_HIGH);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        // What happens when you tap the notification
        Intent mi = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), mi,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification with the specific repeat count in the title
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Medicine Alert (" + count + "/3)")
                .setContentText("It's time for your " + name + ". Please take your dose now!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (nm != null) {
            // Using different IDs for the 3 notifications so they stack in the drawer
            nm.notify(count, builder.build());
        }
    }

    private void scheduleNextNudge(Context context, String name, int id, int nextCount) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("medName", name);
        i.putExtra("alarmId", id);
        i.putExtra("repeatCount", nextCount);

        PendingIntent pi = PendingIntent.getBroadcast(context, id, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // NEXT ALERT: 10 minutes (10 * 60 * 1000 ms) after the current one
        // For your demo, you can change 10 to 1 to see them faster!
        long nudgeDelay = 10 * 60 * 1000;
        long triggerAt = System.currentTimeMillis() + nudgeDelay;

        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        }
    }
}