package com.wareproz.mac.gravydriver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RideRequestActivity extends Activity {

    TextView txtTitleMode,txtAddress,txtRideType,txtRiderRating;
    Button btnaccept,btndecline;
    String ride_id,payment_method,address,ride_type,rider_rating,driver_id,
            pickup_gps, pickup_address, rider_name, rider_picture, rider_phone,
            dropoff_gps, dropoff_name, driverLocation;
    Globals g = Globals.getInstance();
    private ProgressDialog pDialog;
    SessionManagement session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request);

        // Session Manager
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        driver_id = user.get(SessionManagement.KEY_ID);

        txtTitleMode = (TextView) findViewById(R.id.titlenmode);
        txtAddress = (TextView) findViewById(R.id.address);
        txtRideType = (TextView) findViewById(R.id.ridetype);
        txtRiderRating = (TextView) findViewById(R.id.riderrating);
        btnaccept = (Button) findViewById(R.id.btnaccept);
        btndecline = (Button) findViewById(R.id.btndecline);

        //update the interfaces
        Bundle bundle = getIntent().getExtras();
        ride_id = bundle.getString("ride_id");
        payment_method = bundle.getString("payment_method");
        address = bundle.getString("address");
        ride_type = bundle.getString("ride_type");
        rider_rating = bundle.getString("rider_rating");

        txtTitleMode.setText("NEW REQUEST ("+payment_method+")");
        txtAddress.setText(address);
        txtRideType.setText(ride_type);
        txtRiderRating.setText(rider_rating);

        btnaccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check ride status and see if ride is still available
                //if yes change ride status, driver available status and push to the requesting rider (driver enroute)
                //if no toast message "ride accepted by another driver" then finish()

                new DriverAcceptRide().execute();
            }
        });

        btndecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private class DriverAcceptRide extends AsyncTask<Void, Void, Void> {

        String json_result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RideRequestActivity.this);
            pDialog.setMessage("Accepting ride...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url = "driver_accept_ride.php?rideId="+ ride_id +"&driverId="+ driver_id;
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    json_result = jsonObj.getString("json_result");
                    pickup_gps = jsonObj.getString("pickup_gps");
                    pickup_address = jsonObj.getString("pickup_address");
                    rider_name = jsonObj.getString("rider_name");
                    rider_picture = jsonObj.getString("rider_picture");
                    rider_phone = jsonObj.getString("rider_phone");
                    dropoff_gps = jsonObj.getString("dropoff_gps");
                    dropoff_name = jsonObj.getString("dropoff_name");


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

            //do something with what is returned
            if(json_result != null) {
                if (json_result.equals("1")) {
                    //driver enroute
                    Intent mIntent = new Intent(RideRequestActivity.this, EnrouteRiderActivity.class);
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
                } else {
                    //
                    Toast.makeText(RideRequestActivity.this, "Ride Accepted By Another Driver", Toast.LENGTH_LONG).show();
                    finish();
                }
            }else{
                Toast.makeText(RideRequestActivity.this, "Unable to connect to server.", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        g.setRideRequestVisibility(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        g.setRideRequestVisibility(false);
    }


}
