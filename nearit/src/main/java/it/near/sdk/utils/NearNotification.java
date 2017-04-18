package it.near.sdk.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class NearNotification {

    public static void send(Context context, int imgRes, String title, String message, Intent resultIntent, int code) {
        Uri sound_notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getPendingIntent(context, resultIntent))
                .setSmallIcon(imgRes)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setLights(Color.RED, 500, 500)
                .setSound(sound_notification)
                .setVibrate(new long[]{100, 200, 100, 500});


        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(message).build();
                mBuilder.build();

        showNotification(context, code, notification);
    }

    private static PendingIntent getPendingIntent(Context context, Intent resultIntent) {
        return PendingIntent.getActivity(
                context,
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private static void showNotification(Context context, int code, Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(code, notification);
    }
}
