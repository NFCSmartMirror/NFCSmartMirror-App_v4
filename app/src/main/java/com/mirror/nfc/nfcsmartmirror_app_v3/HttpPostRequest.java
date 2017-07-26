package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Example from https://stackoverflow.com/questions/2938502/sending-post-data-in-android
 */

public class HttpPostRequest extends AsyncTask<String, String, Void> {
    public HttpPostRequest() {
//set context variables if required
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(String... params) {

        String urlString = params[0]; // URL to call

        String resultToDisplay = "";

        InputStream in = null;
        try {


            URL url = new URL(AppCompatPreferenceActivity.mirrorIPRU + "/rpc");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
//          UserID, AppID, AppViewID
            String requestRPC = "{\"jsonrpc\": \"2.0\", \"method\": \"getOrCreateView\", \"params\": [\"ASP\", \"Messages\", \"" + SMSListener.appViewID + "\"], \"id\": 1}";
            OutputStream outputStreamSend = urlConnection.getOutputStream();
            outputStreamSend.write(requestRPC.getBytes("UTF-8"));
            outputStreamSend.flush();
            outputStreamSend.close();
            in = new BufferedInputStream(urlConnection.getInputStream());


        } catch (Exception e) {

            Log.i("HTTP_Error", e.getMessage());
            return null;

        }

        try {
//            Manipulation of JSON String.
//            Workaround for not using JSON!
//            Setting the public variable of our app on true
            resultToDisplay = IOUtils.toString(in, "UTF-8");
            resultToDisplay = resultToDisplay.replace("false", "true");
            resultToDisplay = resultToDisplay.replace("{\"jsonrpc\":\"2.0\",\"result\":", "");
            resultToDisplay = resultToDisplay.replace(",\"id\":\"1\"}", "");
            Log.i("HTTP_Error",resultToDisplay);
            String updateRequestRPC = "{\"jsonrpc\": \"2.0\", \"method\": \"updateView\", \"params\": ["+resultToDisplay + "], \"id\": 1}";

            URL url = new URL(AppCompatPreferenceActivity.mirrorIPRU + "/rpc");
// URL url = new URL(AppCompatPreferenceActivity.mirrorIPRU + "/rpc");
            HttpURLConnection urlConnectionResend = (HttpURLConnection) url.openConnection();
            urlConnectionResend.setDoInput(true);
            urlConnectionResend.setDoOutput(true);
            urlConnectionResend.setRequestProperty("Content-Type", "application/json");
            urlConnectionResend.setRequestMethod("POST");
            OutputStream outputStreamSend = urlConnectionResend.getOutputStream();

            outputStreamSend.write(updateRequestRPC.getBytes("UTF-8"));
            outputStreamSend.flush();
            outputStreamSend.close();
            urlConnectionResend.getInputStream().close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    protected void onPostExecute(Void result) {
//Update the UI

    }
}