package com.taxi_riderapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Common {

    public static final String RIDER_INFO_REFFERENCE ="Riders";
    public static final String DRIVERS_LOCATION_REFFERNCES ="DriversLocation" ;
    public static final String DRIVERS_INFO_REFFERENCE = "USERS";
//    public static final String DRIVER_LOCATION_REFFERENCE ="RiderLocation";

    public  static Rider_Helperclass currentRider;

    public static final String TOKEN_REFFERENCE = "Token" ;
    public static final String NOTI_TITLE = "title" ;
    public static final String NOTI_CONTENT = "body" ;
    public static Set<DriverGeoModel> driversFound = new HashSet<DriverGeoModel> (  );
    public static HashMap<String, Marker> markerlist = new HashMap<> (  );


    public static void showNotification(Context context, int id, String title, String body, Intent i) {

        PendingIntent pendingIntent  = null;
        if(i!= null)
            pendingIntent = PendingIntent.getActivity (context,id,i,PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFCATION_CANNEL_ID = "hds";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService (Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel (NOTIFCATION_CANNEL_ID,
                    "hds taxi",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription ("hds");
            notificationChannel.enableLights (true);
            notificationChannel.setLightColor (Color.RED);
            notificationChannel.setVibrationPattern (new long[]{0,1000,500,1000});
            notificationChannel.enableVibration (true);
            notificationManager.createNotificationChannel (notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder (context,NOTIFCATION_CANNEL_ID);
        builder.setContentTitle (title)
                .setContentText (body)
                .setAutoCancel (false)
                .setPriority (NotificationCompat.PRIORITY_HIGH)
                .setDefaults (Notification.DEFAULT_VIBRATE)
                .setSmallIcon (R.drawable.ic_car)
                .setLargeIcon (BitmapFactory.decodeResource (context.getResources (),R.drawable.ic_car));
        if (pendingIntent != null){
            builder.setContentIntent (pendingIntent);

        }
        Notification notification = builder.build ();
        notificationManager.notify ( id,notification );



    }

    public static String buildname(String name) {

        return name;
    }
}
