package com.wareproz.mac.gravydriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class RateRiderActivity extends AppCompatActivity {

    TextView txtUserName, txtamount, txtpayment;
    String ride_id, pickup_gps, pickup_address, rider_name, rider_picture,
            rider_phone, id, dropoff_gps, dropoff_name, ride_amount, payment_method, rider_id;
    RatingBar rating;

    private ProgressDialog pDialog;
    SessionManagement session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_rider);

        // Session Manager
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();

        txtamount = (TextView) findViewById(R.id.amount);
        txtUserName = (TextView) findViewById(R.id.rider_name);
        txtpayment = (TextView) findViewById(R.id.payment);
        rating = (RatingBar) findViewById(R.id.rating);

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
        ride_amount = bundle.getString("ride_amount");
        payment_method = bundle.getString("payment_method");
        rider_id = bundle.getString("rider_id");

        txtamount.setText(ride_amount);
        txtUserName.setText(rider_name);
        txtpayment.setText(payment_method);

        //display the riders image
        //new DownloadImageTask((ImageView) findViewById(R.id.user_pic)).execute("https://gravy.com.ng/portal/uploads/"+rider_picture);

        // Session class instance
        session = new SessionManagement(getApplicationContext());

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        id = user.get(SessionManagement.KEY_ID);

        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                //txtRatingValue.setText(String.valueOf(rating));
                new RateUser().execute();

            }
        });
    }

    private class RateUser extends AsyncTask<Void, Void, Void> {

        String json_result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RateRiderActivity.this);
            pDialog.setMessage("Rating Rider ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url_rating = null;
            try {
                url_rating = URLEncoder.encode(String.valueOf(rating), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "rate_rider.php?riderId="+ rider_id +"&rating="+ url_rating;
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

            Intent mIntent = new Intent(RateRiderActivity.this, HomeActivity.class);
            startActivity(mIntent);
            finish();
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
