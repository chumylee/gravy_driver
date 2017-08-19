package com.wareproz.mac.gravydriver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; //3 second
    private static final int APP_VERSION = 1; //the app version

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);

        Handler handler = new Handler();

        // run a thread after 3 seconds to start the home screen
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
            new VersionTest().execute();
            }

        }, SPLASH_DURATION);
    }

    private class VersionTest extends AsyncTask<Void, Void, Void> {

        String json_app_version;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            TextView copyright = (TextView) findViewById(R.id.textView);
            copyright.setText("Checking for app update ...");

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            String url = "appversion.php";
            String jsonStr = sh.makeServiceCall(url);


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    json_app_version = jsonObj.getString("version");

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

            //do something with what is returned
            if (json_app_version.equals(Integer.toString(APP_VERSION))){
                // Creating user login session and store some stuff
                //open home page
                Intent mIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(mIntent);
                finish();

            }else{
                //
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashScreenActivity.this);
                alertDialogBuilder.setMessage("An updated version of this app is available on the Appstore, kindly download an update");
                alertDialogBuilder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }
}
