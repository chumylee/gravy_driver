package com.wareproz.mac.gravydriver;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    String theactivity,ride_id,payment_method,address,ride_type,rider_rating;
    // Context
    Context _context;
    //get globals
    Globals g = Globals.getInstance();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        this._context = getApplicationContext();

        theactivity = remoteMessage.getData().get("activity");

        if (theactivity.equals("rideRequest") && !g.getRideRequestVisibility()){
            ride_id = remoteMessage.getData().get("ride_id");
            payment_method = remoteMessage.getData().get("payment_method");
            address = remoteMessage.getData().get("address");
            ride_type = remoteMessage.getData().get("ride_type");
            rider_rating = remoteMessage.getData().get("rider_rating");

            Intent i = new Intent(_context, RideRequestActivity.class);
            i.putExtra("ride_id", ride_id);
            i.putExtra("payment_method", payment_method);
            i.putExtra("address", address);
            i.putExtra("ride_type", ride_type);
            i.putExtra("rider_rating", rider_rating);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }


    }
/*
    private void showNotification(String message) {

        Intent i = new Intent(this,HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("FCM Test")
                .setContentText(message)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());

    }
*/

}
