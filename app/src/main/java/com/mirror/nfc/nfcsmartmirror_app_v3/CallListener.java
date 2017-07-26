package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * retrieves incoming calls and logs them together with the incoming phone number and call duration.
 */


public class CallListener extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        //create instance of TelephonyManager to retrieve calls in application context
        TelephonyManager callManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        callManager.listen(new PhoneStateListener() {
            @Override
            /**
             * when incoming call registered, show toast notification and create html file with the same contents
             */
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                System.out.println("incomingNumber : " + incomingNumber);


                //map of all extras previously added with putExtra(), or null if none have been added.
//                final Bundle bundle = intent.getExtras();

                try {
                    Date dat = new Date();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                    String time = sdf.format(dat);

                    String message = " called at: " + time;
                    Log.i("Call_Error_Time", time);
                    String phoneNumber = incomingNumber;


                    Log.i("CallReceiver", "phoneNumber: " + SMSListener.getContactName(context, phoneNumber) + "; message: " + message);


                    //display toast in app
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "phone number: " + SMSListener.getContactName(context, phoneNumber) + ", message: " + message, duration);
                    toast.show();

/*
                    The creation of our HMTL code happens here
*/
                    String htmlString = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                            "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                            "\n" +
                            "<html>\n" +
                            "<body bgcolor=\"#000000\">\n" +
                            "<font color=\"#FFFFFF\">\n" +
                            "<font size=\"20\">\n" +
                            "<font face=\"verdana\">\n" +
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
                            "\t\t\t\t<p>" + SMSListener.getContactName(context, incomingNumber) + message + "</p>" +
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
                    this.mirrors.put("DUMMY", AppCompatPreferenceActivity.mirrorIPRU);
                    //Does the code actually go here
                    Log.i("Call_test", "Call came in");
                    try {
                        //create new instance of StaticResourceUploaders
                        this.staticResourceUploader = new StaticResourceUploader(mirrors.get("DUMMY"), "Call", "ASP");
                        UploadResourceTask iconUploadTask = new UploadResourceTask(this.staticResourceUploader, SettingsActivity.thisActivity, R.raw.call, "call.png", appViewID, false, true);
                        UploadBytesTask mainPageUploadTask = new UploadBytesTask(this.staticResourceUploader, SettingsActivity.thisActivity, htmlString.getBytes(), "call.html", appViewID);
                        HttpPostRequestCall publisherTask = new HttpPostRequestCall();
                        mainPageUploadTask.setNextTask(publisherTask, AppCompatPreferenceActivity.mirrorIPRU);
                        iconUploadTask.setNextTask(mainPageUploadTask, null);
                        iconUploadTask.execute();
                        Log.i("Call_Listener", "Call is executed");
                    } catch (MalformedURLException e) {
                        this.staticResourceUploader = null;
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (Exception e) {
                    Log.e("CALL_Error", "Exception CallListener" + e);
                }
            }

            private final Map<String, String> mirrors = new HashMap<>();
            private StaticResourceUploader staticResourceUploader;
            final String appID = null;
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }


    public static String appViewID = "Call";


}