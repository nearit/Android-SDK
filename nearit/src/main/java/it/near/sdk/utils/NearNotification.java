package it.near.sdk.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NearNotification {

    public static void send(Context context, int imgRes, String title, String message, Intent resultIntent, int code) {
        NotificationCompat.Builder mBuilder = getBuilder(context, title, message, imgRes, resultIntent);

        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(message).build();
        mBuilder.build();

        showNotification(context, code, notification);
    }

    private static NotificationCompat.Builder getBuilder(Context context,
                                                         String title,
                                                         String message,
                                                         int imgRes,
                                                         Intent resultIntent) {
        Uri sound_notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(message)
                .setContentIntent(getPendingIntent(context, resultIntent))
                .setSmallIcon(imgRes)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setLights(Color.RED, 500, 500)
                .setSound(sound_notification)
                .setVibrate(new long[]{100, 200, 100, 500});

        return builderWithTitlePreNougat(builder, title);
    }

    private static NotificationCompat.Builder builderWithTitlePreNougat(NotificationCompat.Builder builder, String title) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return builder.setContentTitle(title);
        else
            return builder;
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
