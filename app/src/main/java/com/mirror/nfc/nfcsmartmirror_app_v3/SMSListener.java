package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * retrieves incoming SMS. These are logged together with the phone number of the message's sender.
 * The app then shows a toast alert (mainly for testing reasons) and processes the information in an html file.
 */
public class SMSListener extends BroadcastReceiver {

    public static String appViewID = "Messages";
    public void onReceive(Context context, Intent intent) {
        //map of all extras previously added with putExtra(), or null if none have been added.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                //longer SMS are split into more than one protocol data units (PDUs)
                //therefore store them in an array
                final Object[] pduArray = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pduArray.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "phoneNumber: " + phoneNumber + "; message: " + message);


                    //display toast in app
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "phone number: " + phoneNumber + ", message: " + message, duration);
                    toast.show();
                    Intent msgIntent = new Intent("Msg");
                    //add additional info to toast
                    msgIntent.putExtra("package", "");
                    msgIntent.putExtra("ticker", phoneNumber);
                    msgIntent.putExtra("title", phoneNumber);
                    msgIntent.putExtra("text", message);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);

                    //////////////////////////////////////////////////
                    // Ab hier neuer HTML erstellugscode
                    //////////////////////////////////////////////////

                   String htmlString = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                            "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                            "\n" +
                            "<html>\n" +
                           "<body bgcolor=\"#000000\">\n"+
                           "<font color=\"#FFFFFF\">\n"+
                           "<font size=\"20\">\n"+
                           "<font face=\"verdana\">\n"+
                            "\n" +
                            "<head>\n" +
                            "\t<meta charset=\"utf-8\">\n" +
                            "\t<!-- Bootstrap Core CSS -->\n" +
                            " <link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">\n" +
                            " <link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">\n" +
                            "</head>\n" +
                            "\n" +
                            "<body>\n" +
                            "\n" +
                            " <div id=\"content\" class=\"centered-text\">\n" +
                            "\t\t<div class=\"quote\">\n" +
                            "\t\t\t<blockquote class=\"quote-size\">\n" +
                            "\t\t\t\t<p>" + message + "</p>\n" +
                            "\t\t\t\t<footer><cite title=\"Source Title\">" + phoneNumber + "</cite></footer>\n" +
                            "\t\t\t</blockquote>\n" +
                            "\t\t</div>\n" +
                            "\t</div>\n" +
                            "\n" +
                            "\n" +
                            " <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>\n" +
                            " <!-- Bootstrap Core JavaScript -->\n" +
                            " <script src=\"js/bootstrap.min.js\"></script>\n" +
                            " <!-- Custom JavaScript -->\n" +
                            " <script src=\"js/alignment.js\"></script>\n" +
                            "</body>\n" +
                            "\n" +
                            "</html>\n";

                    Log.i("HTML", htmlString);
                    this.mirrors.put("DUMMY", "http://192.168.1.178:2534/api");
                    //this.mirrors.put("DUMMY", AppCompatPreferenceActivity.mirrorIPRU);
                    //Log.i("NSD_STRING_Transmission",AppCompatPreferenceActivity.mirrorIPRU);
                    //this.mirrors.put("DUMMY", "http://10.0.2.2:2534/api");

                    //Kontrolle, ob Methode ausgeführt wird
                    Log.i("ASP", "hallo wird ausgeführt");
                    try {
                        //neue Instanz des StaticResourceUploaders ausführen
                        this.staticResourceUploader = new StaticResourceUploader(mirrors.get("DUMMY"), "Messages", "ASP");
                        UploadResourceTask iconUploadTask = new UploadResourceTask(this.staticResourceUploader, SettingsActivity.thisActivity, R.raw.sms, "sms.png", appViewID, false,true);
                        UploadBytesTask mainPageUploadTask = new UploadBytesTask(this.staticResourceUploader, SettingsActivity.thisActivity, htmlString.getBytes(), "test1.html",appViewID);
                        HttpPostRequest publisherTask = new HttpPostRequest();
                        mainPageUploadTask.setNextTask(publisherTask, "http://192.168.1.178:2534/api");
                        iconUploadTask.setNextTask(mainPageUploadTask, null);
                        iconUploadTask.execute();
                        Log.i("Error_Upload1", "Error_Upload1111");
                        //while(!AsyncTask.Status.FINISHED.equals(iconUploadTask.getStatus())) {
                          //  AsyncTask.Status blub = iconUploadTask.getStatus();
                            // wait
                        //}
                        //mainPageUploadTask.execute();
                        //while(!AsyncTask.Status.FINISHED.equals(mainPageUploadTask.getStatus())) {
                            // wait
                        //}
                        //publisherTask.execute("http://192.168.1.171:2534/api");
                    } catch (MalformedURLException e) {
                        this.staticResourceUploader = null;
                        Log.i("Error_Upload2", "Error_Upload222 innerhalb catch");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
        /* Prepare a char-Array that will
         * hold the chars we read back in. */
                  /*  char[] inputBuffer = new char[message.length()];

                    // Fill the Buffer with data from the file
                    isr.read(inputBuffer);

                    // Transform the chars to a String
                    String readString = new String(inputBuffer);

                    // Check if we read back the same chars that we had written out
                    boolean isTheSame = message.equals(readString);

                    Log.i("File Reading stuff", "success = " + isTheSame);
                    */

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    // Hash
    private final Map<String, String> mirrors = new HashMap<>();
    private StaticResourceUploader staticResourceUploader;
    final String appID = null;



}