package com.example.patientmanagementsystemmobile.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.patientmanagementsystemmobile.HomeActivity;
import com.example.patientmanagementsystemmobile.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "medication_alert_channel";
    public static final String CHANNEL_NAME = "Medication Alerts";
    public static final String CHANNEL_DESCRIPTION = "Notifications for medication reminders";

    /**
     * Create notification channel for Android O and above
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show notification for medication alert
     */
    public static void showNotification(Context context, int notificationId, String medicationName, String remarks) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent to open app when notification is clicked
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_fragment", "rx_alert");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification
        String title = "Medication Reminder";
        String message = "Time to take: " + medicationName;
        if (remarks != null && !remarks.isEmpty()) {
            message += "\n" + remarks;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today) // Use system icon or replace with your own
                .setContentTitle(title)
                .setContentText("Time to take: " + medicationName)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
