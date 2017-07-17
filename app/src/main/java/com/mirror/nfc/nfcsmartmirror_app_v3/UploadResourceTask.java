package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Julian on 15.07.2017.
 */

public class UploadResourceTask extends AsyncTask<String, Void, String> {
    private final StaticResourceUploader resourceUploader;
    private StaticResourceUploader.InputStreamSupplier supplier;
    private String urlBasePath;
    private static StaticResourceUploader.ResourceRegistrationConfig config;
    private AsyncTask nextTask;
    private final Activity context;
    private String[] nextTaskParams;



    UploadResourceTask(final StaticResourceUploader uploader,final Activity context, final int resource, final String urlBasePath) {
        this(uploader,context,resource,urlBasePath,null,false,false);

    }

    public void setNextTask(final AsyncTask<String, Void, String> task, final String... params) {
        this.nextTask = task;
        this.nextTaskParams = params;
    }

//  Our constructor with all the arguments
    UploadResourceTask(final StaticResourceUploader uploader, final Activity context, final int resource, final String urlBasePath, final String appViewID, final boolean isMainpage, final boolean isIcon) {
        this.resourceUploader = uploader;
        this.urlBasePath = urlBasePath;
        this.context = context;
        this.config = new StaticResourceUploader.ResourceRegistrationConfig(appViewID, isMainpage, isIcon);

        this.supplier = new StaticResourceUploader.InputStreamSupplier() {
            @Override
            public InputStream get() throws IOException {
                return context.getResources().openRawResource(resource);
            }
        };

    }

    @Override
    protected String doInBackground(String... params){
        Log.i("WTF?", "Verarschen?");
        String result = null;
        try {
            result = this.resourceUploader.uploadResource(this.supplier, this.urlBasePath, config);
            Log.i("WTF?", "Minion");
        } catch (IOException e) {
            Log.i("WTF?", "Banane");
            e.printStackTrace();
        }
        Log.i("TASK", "Upload resource to "+urlBasePath+" finished");
        if(nextTask != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nextTask.execute(params);
                }
            });
        }
        return result;
    }

}


