package com.shantanudeshmukh.linkedinsdk.helpers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler {

    private static final String TAG = "LinkedInAuth";

    /**
     * Method that handles Post request
     *
     * @return String - response from API, null if failed
     */
    public static String sendPost(String r_url , JSONObject postDataParams) throws IOException, JSONException {
        URL url = new URL(r_url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
        writer.write(encodeParams(postDataParams));
        writer.flush();
        writer.close();
        os.close();

        int responseCode=conn.getResponseCode(); // To Check for 200
        if (responseCode == HttpsURLConnection.HTTP_OK) {

            BufferedReader in=new BufferedReader( new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line="";
            while((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();
            return sb.toString();
        }else{

            BufferedReader err=new BufferedReader( new InputStreamReader(conn.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line="";
            while((line = err.readLine()) != null) {
                sb.append(line);
                break;
            }
            err.close();
            Log.e(TAG, "Error Code "+conn.getResponseCode());
            Log.e(TAG, "Error Message "+sb.toString());
        }
        return null;
    }


    /**
     * Method that handles Get request
     *
     * @return String - response from API, null if failed
     */
    public static String sendGet(String url, String accessToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        con.setRequestProperty("cache-control", "no-cache");
        con.setRequestProperty("X-Restli-Protocol-Version", "2.0.0");
        int responseCode = con.getResponseCode();
        System.out.println("Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
            BufferedReader in = new BufferedReader(new InputStreamReader( con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {

            BufferedReader err=new BufferedReader( new InputStreamReader(con.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line="";
            while((line = err.readLine()) != null) {
                sb.append(line);
                break;
            }
            err.close();
            Log.e(TAG, "Error Code "+con.getResponseCode());
            Log.e(TAG, "Error Message "+sb.toString());

            return null;
        }
    }


    /**
     * Method that encodes parameters
     *
     * @return String - url encoded string
     */
    private static String encodeParams(JSONObject params) throws UnsupportedEncodingException, JSONException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
