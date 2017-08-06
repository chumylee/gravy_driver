package com.wareproz.mac.gravydriver;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    Globals g = Globals.getInstance();

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        registerToken(token);
    }

    private void registerToken(String token) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",token)
                .build();

        g.setToken(token);

        /*
        Request request = new Request.Builder()
                .url("http://wareproz.com/gravy/webservice/register.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
