package com.wareproz.mac.gravydriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    Button login_button;
    TextView txtemail, txtpassword;
    ConnectionDetector connectionDetector;
    String email, password, Token;

    private ProgressDialog pDialog;

    // Session Manager Class
    SessionManagement session;

    Globals g = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);

        connectionDetector = new ConnectionDetector(this);
        login_button = (Button) findViewById(R.id.login_button);
        txtemail = (TextView) findViewById(R.id.email);
        txtpassword = (TextView) findViewById(R.id.password);

        // Session Manager
        session = new SessionManagement(getApplicationContext());

//      check if user is logged on and just send him straight to home
        if(session.isLoggedIn()){
            //open home page
            Intent mIntent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(mIntent);
            finish();
        }

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if there is internet access
                if(!connectionDetector.isConnectingToInternet()){

                    Toast.makeText(LoginActivity.this,"Internet Network Not Avaliable",Toast.LENGTH_LONG).show();

                }else {

                    email = txtemail.getText().toString();
                    password = txtpassword.getText().toString();

                    if(email.trim().length() > 0 && password.trim().length() > 0){

                        new LoginDriver().execute();

                    }else{
                        Toast.makeText(LoginActivity.this,"Email And Password Must Be Entered",Toast.LENGTH_LONG).show();

                    }

                }

            }
        });

        //subscribe to push notification
        FirebaseMessaging.getInstance().subscribeToTopic("gravy");
        FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }

    private class LoginDriver extends AsyncTask<Void, Void, Void> {

        String json_result,id,trip_count,account_balance,first_name,car_name,rating;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            Token = g.getToken();
            String url = "driver_login.php?username="+ email +"&password="+ password +"&role=2&token="+ Token;
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    json_result = jsonObj.getString("json_result");
                    id = jsonObj.getString("id");
                    trip_count = jsonObj.getString("trip_count");
                    account_balance = jsonObj.getString("account_balance");
                    first_name = jsonObj.getString("first_name");
                    car_name = jsonObj.getString("car_name");
                    rating = jsonObj.getString("rating");

                    //JSONArray contacts = jsonObj.getJSONArray("contacts");

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
            if (json_result.equals("1")){
                // Creating user login session and store some stuff
                session.createLoginSession(email,id,trip_count,account_balance,first_name,car_name,rating);

                //open home page
                Intent mIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(mIntent);
                finish();

            }else{
                //
                Toast.makeText(LoginActivity.this,"Invalid Login Details",Toast.LENGTH_LONG).show();
            }
        }

    }
}
