package com.shantanudeshmukh.linkedinsdk;

import android.os.AsyncTask;
import android.util.Log;

import com.shantanudeshmukh.linkedinsdk.HelpClasses.LinkedInUser;
import com.shantanudeshmukh.linkedinsdk.HelpClasses.OnBasicProfileListener;
import com.shantanudeshmukh.linkedinsdk.HelpClasses.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class RetrieveBasicProfileAsyncTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "LinkedInAuth";
    private OnBasicProfileListener basicProfileListener;
    private LinkedInUser linkedInUser = new LinkedInUser();

    RetrieveBasicProfileAsyncTask(String accessToken, long accessTokenExpiry, OnBasicProfileListener basicProfileListener) {
        this.basicProfileListener = basicProfileListener;
        this.linkedInUser.setAccessToken(accessToken);
        this.linkedInUser.setAccessTokenExpiry(accessTokenExpiry);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        basicProfileListener.onDataRetrievalStart();
    }

    @Override
    protected Boolean doInBackground(String... tokens) {


        if (linkedInUser.getAccessToken() != null) {

            try {

                retrieveBasicProfile();

                retrieveEmailFromAPI();

                return linkedInUser.getId() != null;

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

        if (didSuccess) {
            basicProfileListener.onDataSuccess(linkedInUser);
        } else {
            basicProfileListener.onDataFailed(LinkedInBuilder.ERROR_FAILED, "AUTHORIZATION FAILED");
        }
    }


    /**
     * Method that retrieves user basic info from LinkedIn API
     */
    private void retrieveBasicProfile() throws IOException, JSONException {

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
            Log.e(TAG, "Failed To Retrieve Basic Profile");
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
            Log.e(TAG, "Failed To Retrieve Email from LinkedIn API");
        }
    }

}