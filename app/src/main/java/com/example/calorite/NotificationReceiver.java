package com.example.calorite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    // JURUS 1: Ganti ID Channel. Ini akan memaksa Android mereset pengaturan dan menganggap ini notifikasi tipe baru!
    private static final String CHANNEL_ID = "calorite_meal_channel_urgent";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Makan (Penting)",
                    NotificationManager.IMPORTANCE_HIGH // JURUS 2: Pastikan ini HIGH atau MAX
            );
            channel.setDescription("Pengingat jadwal makan Calorite yang tidak boleh dilewatkan");
            channel.enableVibration(true); // Wajib dihidupkan untuk pop-up
            notificationManager.createNotificationChannel(channel);
        }

        Intent repeatingIntent = new Intent(context, MainActivity.class);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 100, repeatingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Waktunya Makan! 🍽️")
                .setContentText("Jangan lupa foto makananmu di Calorite ya!")
                // JURUS 3: Kombinasi maut PRIORITY_MAX dan DEFAULT_ALL
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Ini yang memicu suara/getar sistem agar notifnya "jatuh" dari atas layar
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}