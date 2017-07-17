package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Example from https://stackoverflow.com/questions/2938502/sending-post-data-in-android
 */

public class HttpPostRequest extends AsyncTask<String, String, String> {
    public HttpPostRequest() {
        //set context variables if required
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String... params) {

        String urlString = params[0]; // URL to call

        String resultToDisplay = "";

        InputStream in = null;
        try {

//            URL url = new URL(urlString);
//            URL url = new URL("http://192.168.1.178:2534/api/rpc");
            URL url = new URL(AppCompatPreferenceActivity.mirrorIPRU+"/rpc");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestMethod("POST");
//          UserID, AppID, AppViewID
            String requestRPC = "{\"jsonrpc\": \"2.0\", \"method\": \"getOrCreateView\", \"params\": [\"ASP\", \"Messages\", \" "+ SMSListener.appViewID +" \"], \"id\": 1}";


            in = new BufferedInputStream(urlConnection.getInputStream());



        } catch (Exception e) {

            Log.i("HTTP_Error",e.getMessage());

            return e.getMessage();

        }

        try {
            resultToDisplay = IOUtils.toString(in, "UTF-8");
            //to [convert][1] byte stream to a string
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultToDisplay;
    }


    @Override
    protected void onPostExecute(String result) {
        //Update the UI

    }
}
