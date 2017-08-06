package com.wareproz.mac.gravydriver;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class RideHistoryActivity extends AppCompatActivity {

    private TextView pickupLocation;
    private TextView dropoffLocation;
    private TextView driversName;
    private TextView vehicleName;
    private TextView rideDate;
    private TextView rideAmount;
    private TextView paymentMethod;
    private TextView rideStatus;
    private ListView rideHistoryListView;
    private List<Map<String,String>> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
//        pickupLocation = (TextView)findViewById(R.id.dropoff_location);
//        dropoffLocation = (TextView)findViewById(R.id.dropoff_location);
//        driversName = (TextView)findViewById(R.id.driver_name);
//        vehicleName = (TextView)findViewById(R.id.vehicle_name);
//        rideDate = (TextView)findViewById(R.id.vehicle_name);
//        rideAmount = (TextView)findViewById(R.id.ride_amount);
//        paymentMethod = (TextView)findViewById(R.id.payment_method);
//        rideStatus = (TextView)findViewById(R.id.ride_status);
        rideHistoryListView = (ListView)findViewById(R.id.ride_history_list);
        historyList = new ArrayList<>();
        new RideHistoryAsyncTask().execute();
    }

    private class RideHistoryAsyncTask extends AsyncTask<Void,Void,Void>{

        private ProgressDialog progressDialog;
        private SessionManagement session;
        int resultCount;
//        private String pickupString,dropoffString,driversNameString,rideAmountString,vehicleNameString,paymentMethodString,rideStatusString,date;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            session = new SessionManagement(RideHistoryActivity.this);
            progressDialog = new ProgressDialog(RideHistoryActivity.this);
            progressDialog.setMessage("Fetching ride history. Please wait...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpHandler httpHandler = new HttpHandler();
            Map<String,String> user = session.getUserDetails();

            String url = "driver_ride_history.php?uid="+user.get(SessionManagement.KEY_ID);
//            String url = "driver_ride_history.php?uid=3";
            String jsonStr = httpHandler.makeServiceCall(url);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray histories = jsonObj.getJSONArray("history");
                    resultCount = histories.length();

                    // looping through All Contacts
                    for (int i = 0; i < histories.length(); i++) {
                        JSONObject c = histories.getJSONObject(i);

                        String pickupString = c.getString("pickup_text");
                        String dropoffString = c.getString("dropoff_text");
                        String driversNameString = c.getString("driver_name");
                        String rideAmountString = "NGN "+c.getString("ride_amount");
                        String vehicleNameString = c.getString("vehicle_name");
                        String paymentMethodString = c.getString("pay_method");
                        String rideStatusString = c.getString("ride_status");
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
                        Date parsedDate = null;
                        try {
                            parsedDate = formatter.parse(c.getString("ride_date"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String date = formatter2.format(parsedDate);

                        Map<String,String> history = new HashMap<>();
                        history.put("pickupString", pickupString);
                        history.put("dropoffString", dropoffString);
                        history.put("driversNameString", driversNameString);
                        history.put("rideAmountString", rideAmountString);
                        history.put("vehicleNameString", vehicleNameString);
                        history.put("paymentMethodString", paymentMethodString);
                        history.put("rideStatusString", rideStatusString);
                        history.put("date", date);

                        historyList.add(history);

                    }

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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.hide();
            }
            if(resultCount==0){
                TextView noHistoryTextView = (TextView)findViewById(R.id.noHistoryTextView);
                noHistoryTextView.setText("You have not completed any rides yet");
            }else {
                ListAdapter adapter = new SimpleAdapter(
                        RideHistoryActivity.this, historyList,
                        R.layout.ride_history_single_item, new String[]{"pickupString", "dropoffString",
                        "driversNameString", "rideAmountString", "vehicleNameString", "paymentMethodString", "rideStatusString",
                        "date"}, new int[]{R.id.pickup_location,
                        R.id.dropoff_location, R.id.driver_name, R.id.ride_amount, R.id.vehicle_name, R.id.payment_method, R.id.ride_status, R.id.ride_date});

                rideHistoryListView.setAdapter(adapter);
            }

        }
    }

}

