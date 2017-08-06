package com.wareproz.mac.gravydriver;

/**
 * Created by mac on 26/11/16.
 */
public class Globals{
    private static Globals instance;

    // Global variable
    private String Token;
    private boolean RideRequestVisibility;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setToken(String d){
        this.Token=d;
    }
    public String getToken(){
        return this.Token;
    }

    public void setRideRequestVisibility(boolean d){
        this.RideRequestVisibility=d;
    }
    public boolean getRideRequestVisibility(){
        return this.RideRequestVisibility;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
