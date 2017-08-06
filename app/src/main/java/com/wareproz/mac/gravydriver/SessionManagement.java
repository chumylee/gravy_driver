package com.wareproz.mac.gravydriver;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

public class SessionManagement {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "GravyDriverPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    //
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ID = "id";
    public static final String TRIP_COUNT = "trip_count";
    public static final String ACC_BAL = "account_balance";
    public static final String FIRST_NAME = "first_name";
    public static final String CAR_NAME = "car_name";
    public static final String RATING = "rating";

    // Constructor
    public SessionManagement(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String email, String id,
                                   String trip_count, String acc_bal,
                                   String first_name, String car_name,
                                   String rating){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ID, id);
        editor.putString(TRIP_COUNT, trip_count);
        editor.putString(ACC_BAL, acc_bal);
        editor.putString(FIRST_NAME, first_name);
        editor.putString(CAR_NAME, car_name);
        editor.putString(RATING, rating);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(TRIP_COUNT, pref.getString(TRIP_COUNT, null));
        user.put(ACC_BAL, pref.getString(ACC_BAL, null));
        user.put(FIRST_NAME, pref.getString(FIRST_NAME, null));
        user.put(CAR_NAME, pref.getString(CAR_NAME, null));
        user.put(RATING, pref.getString(RATING, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        //clear user data too ... to clear push note token
        clearApplicationData();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    public void clearApplicationData() {
        File cache = _context.getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s +" DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

}

