package tovele.eunencio.taxigocliente.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import android.app.Notification;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.R;
import tovele.eunencio.taxigocliente.RateActivity;


/**
 * Created by Eunencio Tovele on 9/3/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getData() != null) {

            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");

            if (title.equals("Cancel")) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

                LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                        .sendBroadcast(new Intent("cancel_request"));

            } else if (title.equals("Chegou")) {
                showArrivedNotification(message);
            } else if (title.equals("DropOff")) {
                openRateActivity(message);
            }
        }

    }

    private void openRateActivity(String body) {

        LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                .sendBroadcast(new Intent(Common.BROADCAST_DROP_OFF));

        Intent intent = new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Chegou")
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build() );


    }
}