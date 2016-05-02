package it.near.sdk.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import it.near.sdk.R;

/**
 * @author cattaneostefano
 */
public class NearNotification {

    public static void send(Context _context, int _imgRes, String _title, String _message, Intent _resultIntent, int _code) {
        if (_imgRes == 0){
            _imgRes = R.drawable.ic_place_white_24dp;
        }
        // imposto notifica di sistema
        Uri sound_notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(_context)
                .setSmallIcon(_imgRes)
                .setLights(Color.RED, 500, 500)
                .setSound(sound_notification)
                .setVibrate(new long[]{100, 200, 100, 500})
                .setContentTitle(_title)
                .setContentText(_message);

        // imposto azione notifica
        PendingIntent resultPendingIntent = PendingIntent.getActivity(_context, (int) (System.currentTimeMillis() / 1024), _resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

        // avvio notifica
        Notification notification = mBuilder.build();
        notification.flags|=Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(_code, notification);
    }
}
