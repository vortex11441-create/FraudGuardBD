package com.quantumbuilders.fraudguardbd;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "scam_alerts";

    public static void showScamAlert(Context context, String message, ScamResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "FraudGuard Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Real-time scam detection alerts");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Detailed string for the notification body
        StringBuilder details = new StringBuilder();
        details.append("Risk: ").append(result.risk).append("\n");
        details.append("Reason: ").append(result.reason).append("\n\n");
        
        if (!result.redFlags.isEmpty()) {
            details.append("Red Flags:\n");
            for (String flag : result.redFlags) details.append("• ").append(flag).append("\n");
            details.append("\n");
        }
        
        details.append("Original Message: ").append(message);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("scam_message", message);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ " + result.risk.toUpperCase() + " RISK: " + result.type)
                .setContentText("Expand to see full security analysis.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(details.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if ("High".equals(result.risk)) {
            builder.setColor(0xFFE53935); // Red
        } else if ("Medium".equals(result.risk)) {
            builder.setColor(0xFFFB8C00); // Orange
        }

        if (manager != null) {
            // Use current time as ID to ensure multiple notifications don't overwrite each other
            int uniqueId = (int) System.currentTimeMillis();
            manager.notify(uniqueId, builder.build());
        }
    }
}