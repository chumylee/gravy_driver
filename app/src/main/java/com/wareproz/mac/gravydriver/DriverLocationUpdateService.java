package com.wareproz.mac.gravydriver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.Map;

public class DriverLocationUpdateService extends Service {

    public double currentLatitude;
    public double currentLongitude;
    public LocationListener locationListener;
    String latLng,uid;

    public DriverLocationUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //the current longitude and latitude
                currentLongitude = location.getLongitude();
                currentLatitude = location.getLatitude();

                //convert it to string
                latLng = Double.toString(currentLatitude) + ',' + Double.toString(currentLongitude);

                //update server
                new updateDriverLocationOnServer().execute();

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //
        }else{
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }


    private class updateDriverLocationOnServer extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            SessionManagement session = new SessionManagement(getApplicationContext());
            Map<String,String> user = session.getUserDetails();
            uid = user.get(SessionManagement.KEY_ID);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url = "update_driver_location.php?uid="+uid+"&location="+latLng;
            String jsonStr = sh.makeServiceCall(url);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

    }

}
