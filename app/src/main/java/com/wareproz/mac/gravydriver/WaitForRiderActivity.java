package com.wareproz.mac.gravydriver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WaitForRiderActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    TextView txtUserName, eta;
    String ride_id, pickup_gps, pickup_address, rider_name, rider_picture, rider_phone, id, dropoff_gps, dropoff_name;
    Button contact, startTrip;
    Globals g = Globals.getInstance();
    private ProgressDialog pDialog;
    SessionManagement session;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = EnrouteRiderActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_rider);

        // Session Manager
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();

        txtUserName = (TextView) findViewById(R.id.user_name);
        eta = (TextView) findViewById(R.id.eta);
        contact = (Button) findViewById(R.id.contact);
        startTrip = (Button) findViewById(R.id.startTrip);

        //update the interfaces
        Bundle bundle = getIntent().getExtras();
        ride_id = bundle.getString("ride_id");
        pickup_gps = bundle.getString("pickup_gps");
        pickup_address = bundle.getString("pickup_address");
        rider_name = bundle.getString("rider_name");
        rider_picture = bundle.getString("rider_picture");
        rider_phone = bundle.getString("rider_phone");
        dropoff_gps = bundle.getString("dropoff_gps");
        dropoff_name = bundle.getString("dropoff_name");

        txtUserName.setText(rider_name);

        //display the riders image
        //new DownloadImageTask((ImageView) findViewById(R.id.user_pic)).execute("https://gravy.com.ng/portal/uploads/"+rider_picture);

        // Session class instance
        session = new SessionManagement(getApplicationContext());

        initMap();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000);

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        id = user.get(SessionManagement.KEY_ID);


        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCall();
            }
        });

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send push notification to user
                new StartTrip().execute();

            }
        });
    }

    public void onCall() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    Integer.parseInt("123"));
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + rider_phone)));
        }
    }

    private class StartTrip extends AsyncTask<Void, Void, Void> {

        String json_result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(WaitForRiderActivity.this);
            pDialog.setMessage("Starting ride ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url = "start_ride.php?rideId="+ ride_id;
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    json_result = jsonObj.getString("json_result");


                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Intent mIntent = new Intent(WaitForRiderActivity.this, EndTripActivity.class);
            mIntent.putExtra("ride_id", ride_id);
            mIntent.putExtra("pickup_gps", pickup_gps);
            mIntent.putExtra("pickup_address", pickup_address);
            mIntent.putExtra("rider_name", rider_name);
            mIntent.putExtra("rider_picture", rider_picture);
            mIntent.putExtra("rider_phone", rider_phone);
            mIntent.putExtra("dropoff_gps", dropoff_gps);
            mIntent.putExtra("dropoff_name", dropoff_name);
            startActivity(mIntent);
            finish();
        }

    }

    public void initMap() {
        MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        if (driverMarker != null){
            driverMarker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.driver_marker))
                .position(latLng);
        driverMarker =  mGoogleMap.addMarker(options);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }




}
