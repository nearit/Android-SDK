package it.near.sdk.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

public class NearNotification {

    private static final int LIGHTS_ON_MILLIS = 500;
    private static final int LIGHTS_OFF_MILLIS = 500;
    private static final long[] VIBRATE_PATTERN = {100, 200, 100, 500};
    private static final String NOTIFICATION_CHANNEL_ID = "NearNotifications";

    public static void send(Context context,
                            int imgRes,
                            String title,
                            String message,
                            Intent resultIntent,
                            int code) {
        NotificationCompat.Builder mBuilder = getBuilder(context, title, message, imgRes, resultIntent);

        Notification notification = new NotificationCompat.BigTextStyle(mBuilder)
                .bigText(message)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildChannel(context, mBuilder);
        }

        showNotification(context, code, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void buildChannel(Context context, NotificationCompat.Builder mBuilder) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        // Configure the notification channel.
        notificationChannel.setDescription("Channel description");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setVibrationPattern(VIBRATE_PATTERN);
        notificationChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    private static NotificationCompat.Builder getBuilder(Context context,
                                                         String title,
                                                         String message,
                                                         int imgRes,
                                                         Intent resultIntent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentText(message)
                .setContentIntent(getPendingIntent(context, resultIntent))
                .setSmallIcon(imgRes)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setLights(Color.RED, LIGHTS_ON_MILLIS, LIGHTS_OFF_MILLIS)
                .setSound(getSoundNotificationUri())
                .setVibrate(VIBRATE_PATTERN);

        return builderWithTitlePreNougat(builder, title);
    }

    private static PendingIntent getPendingIntent(Context context, Intent resultIntent) {
        return PendingIntent.getBroadcast(
                context,
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private static Uri getSoundNotificationUri() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    private static NotificationCompat.Builder builderWithTitlePreNougat(NotificationCompat.Builder builder, String title) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return builder.setContentTitle(title);
        else
            return builder;
    }


    private static void showNotification(Context context, int code, Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(code, notification);
    }
}
