package com.wareproz.mac.gravydriver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class TokenUpdaterService extends IntentService {

    private Handler handler;
    public TokenUpdaterService() {
        super("TokenUpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String token = FirebaseInstanceId.getInstance().getToken();
        try {
            while (token == null) {
                Thread.sleep(3000);
                token = FirebaseInstanceId.getInstance().getToken();
            }
            updateTokenOnServer(token);
        } catch (InterruptedException e) { }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateTokenOnServer(String token){
        //update token on server
        SessionManagement session = new SessionManagement(getApplicationContext());
        Globals.getInstance().setToken(token);
        session.storeToken();
        Map<String,String> user = session.getUserDetails();
        String uid = user.get(SessionManagement.KEY_ID);
        final String url = "update_token.php?uid="+uid+"&token="+token;
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                HttpHandler http = new HttpHandler();
                String response = http.makeServiceCall(url);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Toast.makeText(getApplicationContext(),
                            jsonResponse.getString("message"),
                            Toast.LENGTH_LONG)
                            .show();

                }catch (JSONException e){
                    Toast.makeText(getApplicationContext(),
                            "Json parsing error: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
}
