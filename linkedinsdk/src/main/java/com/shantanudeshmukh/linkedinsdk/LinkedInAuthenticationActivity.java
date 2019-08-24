package com.shantanudeshmukh.linkedinsdk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shantanudeshmukh.linkedinsdk.HelpClasses.LinkedInUser;
import com.shantanudeshmukh.linkedinsdk.HelpClasses.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class LinkedInAuthenticationActivity extends AppCompatActivity {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET_KEY;
    private static String STATE; //for security
    private static String REDIRECT_URI;

    private static final String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
    private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
    private static final String SECRET_KEY_PARAM = "client_secret";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String GRANT_TYPE_PARAM = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";

    private WebView webView;
    private AlertDialog progressDialog;

    private LinkedInUser linkedInUser = new LinkedInUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkedin_authentication);

        //enable fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        CLIENT_ID = getIntent().getStringExtra(LinkedInBuilder.CLIENT_ID);
        CLIENT_SECRET_KEY = getIntent().getStringExtra(LinkedInBuilder.CLIENT_SECRET_KEY);
        REDIRECT_URI = getIntent().getStringExtra(LinkedInBuilder.REDIRECT_URI);
        STATE = getIntent().getStringExtra(LinkedInBuilder.STATE);

        webView = findViewById(R.id.web_view_linkedin_login);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.clearHistory();
        webView.clearCache(true);

        showProgressDialog();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgressDialog();
            }

            //to support below Android N we need to use the deprecated method only
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {

                showProgressDialog();

                if (authorizationUrl.startsWith(REDIRECT_URI)) {

                    Uri uri = Uri.parse(authorizationUrl);
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        Log.e(LinkedInBuilder.TAG, "State token doesn't match");
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Intent intent = new Intent();
                        intent.putExtra("err_code", LinkedInBuilder.ERROR_USER_DENIED);
                        intent.putExtra("err_message", "Authorization not received. User didn't allow access to account.");
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                    }


                    new RetrieveDataAsyncTask().execute(authorizationToken);


                } else {
                    //Default behaviour
                    webView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        String authUrl = getAuthorizationUrl();
        webView.loadUrl(authUrl);
    }


    private class RetrieveDataAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(String... tokens) {
            if (tokens.length > 0) {
                String authorizationToken = tokens[0];

                try {

                    retrieveAccessTokenFromAPI(authorizationToken);

                    if (linkedInUser.getAccessToken() != null) {

                        retrieveBasicProfile();

                        retrieveEmailFromAPI();

                        return linkedInUser.getId() != null;

                    } else {

                        return false;

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }

            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean didSuccess) {
            super.onPostExecute(didSuccess);
            hideProgressDialog();

            Intent intent = new Intent();
            if (didSuccess) {
                intent.putExtra("social_login", linkedInUser);
                setResult(Activity.RESULT_OK, intent);
            } else {
                intent.putExtra("err_code", LinkedInBuilder.ERROR_FAILED);
                intent.putExtra("err_message", "AUTHORIZATION FAILED");
                setResult(Activity.RESULT_CANCELED, intent);
            }
            finish();
        }
    }


    /**
     * Method that retrieves authentication token using authorization token
     */
    private void retrieveAccessTokenFromAPI(String authorizationToken) throws IOException, JSONException {
        String accessTokenUrl = getAccessTokenUrl(authorizationToken);
        String result = RequestHandler.sendPost(accessTokenUrl, new JSONObject());
        if (result != null) {
            JSONObject resultJson = new JSONObject(result);
            int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
            String accessToken1 = resultJson.has("access_token") ? resultJson.getString("access_token") : null;

            if (expiresIn > 0 && accessToken1 != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, expiresIn);
                long expireDate = calendar.getTimeInMillis();
                linkedInUser.setAccessToken(accessToken1);
                linkedInUser.setAccessTokenExpiry(expireDate);
            } else {
                Log.e(LinkedInBuilder.TAG, "Access Token Expired or Doesn't exist");
            }
        } else {
            Log.e(LinkedInBuilder.TAG, "Failed To Retrieve Access Token");
        }
    }


    /**
     * Method that generates the url for get the access token from the Service
     *
     * @return String - access token url
     */
    private static String getAccessTokenUrl(String authorizationToken) {

        return ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + CLIENT_ID
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + CLIENT_SECRET_KEY;
    }


    /**
     * Method that generates the url for get the authorization token from the Service
     *
     * @return String - authorization url
     */
    private static String getAuthorizationUrl() {
        return AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + CLIENT_ID
                + AMPERSAND + STATE_PARAM + EQUALS + STATE
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND + "scope=r_liteprofile%20r_emailaddress";
    }


    /**
     * Method that retrieves user basic info from LinkedIn API
     */
    public void retrieveBasicProfile() throws IOException, JSONException {

        String profileUrl = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))";
        String result = RequestHandler.sendGet(profileUrl, linkedInUser.getAccessToken());
        if (result != null) {

            JSONObject jsonObject = new JSONObject(result);

            String linkedInUserId = jsonObject.getString("id");
            String country = jsonObject.getJSONObject("firstName").getJSONObject("preferredLocale").getString("country");
            String language = jsonObject.getJSONObject("firstName").getJSONObject("preferredLocale").getString("language");
            String firstNameKey = language + "_" + country;
            String linkedInUserFirstName = jsonObject.getJSONObject("firstName").getJSONObject("localized").getString(firstNameKey);
            String linkedInUserLastName = jsonObject.getJSONObject("lastName").getJSONObject("localized").getString(firstNameKey);
            String linkedInUserProfile = jsonObject.getJSONObject("profilePicture").getJSONObject("displayImage~").getJSONArray("elements").getJSONObject(0).getJSONArray("identifiers").getJSONObject(0).getString("identifier");

            linkedInUser.setId(linkedInUserId);
            linkedInUser.setFirstName(linkedInUserFirstName);
            linkedInUser.setLastName(linkedInUserLastName);
            linkedInUser.setProfileUrl(linkedInUserProfile);

        } else {
            Log.e(LinkedInBuilder.TAG, "Failed To Retrieve Basic Profile");
        }
    }


    /**
     * Method that retrieves user email from LinkedIn emailAddress API
     */
    private void retrieveEmailFromAPI() throws IOException, JSONException {
        String emailUrl = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";
        String result = RequestHandler.sendGet(emailUrl, linkedInUser.getAccessToken());
        if (result != null) {
            JSONObject jsonObject = new JSONObject(result);
            String email = jsonObject.getJSONArray("elements").getJSONObject(0).getJSONObject("handle~").getString("emailAddress");
            linkedInUser.setEmail(email);
        } else {
            Log.e(LinkedInBuilder.TAG, "Failed To Retrieve Email from LinkedIn API");
        }
    }


    private void showProgressDialog() {
        if (!LinkedInAuthenticationActivity.this.isFinishing()) {
            if (progressDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LinkedInAuthenticationActivity.this);
                builder.setCancelable(false); // if you want user to wait for some process to finish,
                builder.setView(R.layout.layout_progress_dialog);
                progressDialog = builder.create();
            }
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (!LinkedInAuthenticationActivity.this.isFinishing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
