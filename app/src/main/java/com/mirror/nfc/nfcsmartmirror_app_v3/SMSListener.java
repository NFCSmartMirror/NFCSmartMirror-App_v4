package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
    // Look up for the number: From https://stackoverflow.com/questions/3079365/android-retrieve-contact-name-from-phone-number
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
//        String contactName = null;
        String contactName = phoneNumber;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

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

                    Log.i("SMS_Receiver", "phoneNumber: " + getContactName(context,phoneNumber) + "; message: " + message);


                    //display toast in app
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "phone number: " + getContactName(context,phoneNumber) + ", message: " + message, duration);
                    toast.show();
                    Intent msgIntent = new Intent("Msg");
                    //add additional info to toast
                    msgIntent.putExtra("package", "");
                    msgIntent.putExtra("ticker", phoneNumber);
                    msgIntent.putExtra("title", phoneNumber);
                    msgIntent.putExtra("text", message);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
/*
                    The creation of our HMTL code happens here
*/
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
//                            "\t\t\t\t<footer><cite title=\"Source Title\">" + phoneNumber + "</cite></footer>\n" +
                           "\t\t\t\t<footer><cite title=\"Source Title\">" + getContactName(context,phoneNumber) + "</cite></footer>\n" +
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
                    //this.mirrors.put("DUMMY", "http://10.0.2.2:2534/api");

                    //Does the code actually go here
                    Log.i("SMS", "hallo wird ausgeführt");
                    try {
                        //neue Instanz des StaticResourceUploaders ausführen
                        this.staticResourceUploader = new StaticResourceUploader(mirrors.get("DUMMY"), "Messages", "ASP");
                        UploadResourceTask iconUploadTask = new UploadResourceTask(this.staticResourceUploader, SettingsActivity.thisActivity, R.raw.sms, "sms.png", appViewID, false,true);
                        UploadBytesTask mainPageUploadTask = new UploadBytesTask(this.staticResourceUploader, SettingsActivity.thisActivity, htmlString.getBytes(), "test1.html",appViewID);
                        HttpPostRequest publisherTask = new HttpPostRequest();
//                        mainPageUploadTask.setNextTask(publisherTask, "http://192.168.1.178:2534/api");
                        mainPageUploadTask.setNextTask(publisherTask,AppCompatPreferenceActivity.mirrorIPRU);
                        iconUploadTask.setNextTask(mainPageUploadTask, null);
                        iconUploadTask.execute();
                    } catch (MalformedURLException e) {
                        this.staticResourceUploader = null;
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SMS_Error", "Exception smsReceiver" + e);
        }
    }

    // Hash
    private final Map<String, String> mirrors = new HashMap<>();
    private StaticResourceUploader staticResourceUploader;
    final String appID = null;



}