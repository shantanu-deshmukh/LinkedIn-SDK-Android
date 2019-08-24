package com.shantanudeshmukh.linkedinsdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.Random;

public class LinkedInBuilder {

    private Activity context;
    private Intent intent;
    private String state;

    static final String CLIENT_ID = "client_id";
    static final String CLIENT_SECRET_KEY = "client_secret";
    static final String REDIRECT_URI = "redirect_uri";
    static final String STATE = "state";

    static final String TAG = "LinkedInAuth";

    public static final int ERROR_USER_DENIED = 11;
    public static final int ERROR_FAILED = 12;


    private LinkedInBuilder(Activity context) {
        this.context = context;
        this.intent = new Intent(context, LinkedInAuthenticationActivity.class);
    }


    public static LinkedInBuilder getInstance(Activity context) {
        return new LinkedInBuilder(context);
    }

    public LinkedInBuilder setClientID(String clientID) {
        intent.putExtra(CLIENT_ID, clientID);
        return this;
    }

    public LinkedInBuilder setClientSecret(String clientSecret) {
        intent.putExtra(CLIENT_SECRET_KEY, clientSecret);
        return this;
    }

    public LinkedInBuilder setRedirectURI(String redirectURI) {
        intent.putExtra(REDIRECT_URI, redirectURI);
        return this;
    }

    public LinkedInBuilder setState(String state) {
        this.state = state;
        intent.putExtra(STATE, state);
        return this;
    }


    public void authenticate(int requestCode) {
        if(validateAuthenticationParams()){
            if(state == null){
                generateState();
            }
            context.startActivityForResult(intent, requestCode);
        }
    }

    private boolean validateAuthenticationParams() {

        if(intent.getStringExtra(CLIENT_ID) == null){
            Log.e(TAG,"Client ID is required", new IllegalArgumentException());
            return  false;
        }

        if(intent.getStringExtra(CLIENT_SECRET_KEY) == null){
            Log.e(TAG,"Client Secret is required", new IllegalArgumentException());
            return  false;
        }

        if(intent.getStringExtra(REDIRECT_URI) == null){
            Log.e(TAG,"Redirect URI is required", new IllegalArgumentException());
            return  false;
        }

        return true;
    }

    private void generateState() {
        String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnmMNBVCXZLKJHGFDSAQWERTYUIOP";
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(16);
        for(int i=0;i<16;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        this.state = sb.toString();
        intent.putExtra(STATE, state);
    }

}
