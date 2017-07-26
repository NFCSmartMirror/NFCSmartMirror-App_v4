package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.IOException;

/**
 * This is the same like UploadResourceTask, but this one is for the HTML upload
 */

public class UploadBytesTask extends AsyncTask<String, Void, String> {
    private byte[] bytes;
    private String urlBasePath;
    private final StaticResourceUploader resourceUploader;
    private StaticResourceUploader.ResourceRegistrationConfig config;
    private AsyncTask nextTask;
    private final Activity context;
    private String[] nextTaskParams;



    UploadBytesTask(final StaticResourceUploader uploader,final Activity context, final byte[] bytes,  final String urlBasePath, final String appViewID) {
        this.bytes = bytes;
        this.context = context;
        this.urlBasePath = urlBasePath;
        this.resourceUploader = uploader;
        this.config = new StaticResourceUploader.ResourceRegistrationConfig(appViewID,true,false);


    }

    public void setNextTask(final AsyncTask task, final String... params) {
        this.nextTask = task;
        this.nextTaskParams = params;
    }

    @Override
    protected String doInBackground(String... params){
        String result = null;
        try {
            result = this.resourceUploader.uploadResource(this.bytes, this.urlBasePath, this.config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(nextTask != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nextTask.execute(nextTaskParams);
                }
            });
        }
        return result;
    }

}
