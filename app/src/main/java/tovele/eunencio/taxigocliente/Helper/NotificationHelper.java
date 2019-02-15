package tovele.eunencio.taxigocliente.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import tovele.eunencio.taxigocliente.R;

public class NotificationHelper extends ContextWrapper {

    private static final String ET_CHANNEL_ID = "tovele.eunencio.taxigocliente.ET";
            private static final String ET_CHANNEL_NAME = "ET TaxiGo";

    private NotificationManager manager;
    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel etChannels = new NotificationChannel(ET_CHANNEL_ID,
                ET_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        etChannels.enableLights(true);
        etChannels.enableVibration(true);
        etChannels.setLightColor(Color.GRAY);
        etChannels.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(etChannels);
    }

    public NotificationManager getManager() {
        if(manager ==  null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getTaxiGoNotification(String title, String context, PendingIntent contentIntent,
                                                      Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(), ET_CHANNEL_ID)
                .setContentText(context)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_car);

    }
}
