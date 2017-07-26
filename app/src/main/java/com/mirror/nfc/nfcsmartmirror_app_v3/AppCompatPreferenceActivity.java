package com.mirror.nfc.nfcsmartmirror_app_v3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;

import static com.mirror.nfc.nfcsmartmirror_app_v3.SettingsActivity.thisActivity;

/**
 * Defining layout of the Mirror Connect App
 * A {@link android.preference.PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public abstract class AppCompatPreferenceActivity extends PreferenceActivity {

    private static int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private AppCompatDelegate mDelegate;
    //NFC
    private TextView mTextView;
    private NfcAdapter mNfcAdapter;

    public static final String SERVICE_TYPE = "_mirror._tcp.";
    public static final String TAG = "NSD_Service";
//    Please enter here your mirrors IP if you are not using the showrooms mirror and are using a virtual device
    public static String mirrorIPRU = "http://192.168.1.91:2534/api";
    private NsdManager mNsdManager;
    public static boolean connectionEstablished = false;

    //Here starts a part of the NetworkServiceDiscovery

    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    //public static final String SERVICE_TYPE = "_http._tcp.";

//  the discovery listener construct was taken from https://android.googlesource.com/platform/development/+/master/samples/training/NsdChat/src/com/example/android/nsdchat/NsdHelper.java
//   And modified to fit our app

    /**
     * Starts the discovery of devices in the network in our case (_mirror._tcp.), if found it is initialized
     */
    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }


            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //     mNsdManager.stopServiceDiscovery(this);
            }


            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
//                If a service of our type is found (_mirror._tcp.), we hand over our service to the resolveService method.
                if (service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Service Type: " + service.getServiceType());
                    mNsdManager.resolveService(service, initializeResolveListener());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

        };
    }

    /**
     * This function is started as soon as the app is installed on a device.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        //Beginning of  NFC Tag handling
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        handleIntent(getIntent());

//        If you are not using a virtual device, comment out the line above and use the lines below
//        if (mNfcAdapter == null) {
//            Toast.makeText(this, " this device doesnt support NFC", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        if (!mNfcAdapter.isEnabled()) {
//            mTextView.setText("NFC is disabled");
//        }
//        handleIntent (getIntent());

    }


    //NFC Tag handling

    /**
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {

        //Call of the discovery function, will always be executed if a message comes in
        //If a connection is not yet established, the following function will be called, as soon as a NFC-Tag is used
        if ( connectionEstablished == false) {

            this.mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            initializeDiscoveryListener();
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            Log.i("NFC_handling", "Next try");
            
            String action = intent.getAction();
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                String type = intent.getType();
                Toast.makeText(this, "You used an NFC Tag", Toast.LENGTH_SHORT).show();
                Log.i("NFC_Use","A NFC Tag was used");
            }
        }else{
//            The app will be closed if a NFC-Tag is being used and it was already connected to a mirror.
            finish();
        }
    }

    /**
     * This function is for resolving our service, if a service is found, it can be used, to get the IP and port of the service.
     * @return resolved NsdManager
     */
    public NsdManager.ResolveListener initializeResolveListener() {
        return new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                NsdServiceInfo mService = serviceInfo;
                InetAddress host = mService.getHost();
                String mirrorHostname = serviceInfo.getServiceName();
                String mirrorPort = String.valueOf(serviceInfo.getPort());
                String mirrorIP = host.getHostAddress();

                Log.i("NSD_Test", String.valueOf(serviceInfo));
                if (host == null) {
                    Log.i("Host", "Host is null");
                    return;
                }
                //Creation of ipv4 address of mirror
                if (host instanceof Inet4Address) {
                    String inetAdressv4Mirror = "http://" + mirrorIP + ":" + mirrorPort + "/api";
                    Log.i("NSD_IP4", inetAdressv4Mirror);
//                    If you use a virtual device, it will never overwrite the mirrorIPRU, because you cannot connect
//                    a virtual device to the the wireless network. Because of that it can never find a service in the network.
//                    This is the reason why the serviceadress is hard coded in the top part.
                    connectionEstablished = true;
                    mirrorIPRU = inetAdressv4Mirror;
                    Toast.makeText(getApplicationContext(), "IPv4! Mirror successfully saved!", Toast.LENGTH_SHORT).show();
                } else {
                    //Creation of ipv6 address of mirror
//                    IP v6 are not possible to being used right now in this app.
                    String inetAdressv6Mirror = "http://[" + mirrorIP + "]:" + mirrorPort + "/api";
                    Log.i("NSD_IP6", inetAdressv6Mirror);
                    Toast.makeText(getApplicationContext(), "IPv6! Please use tag again!", Toast.LENGTH_SHORT).show();
                }
                Log.i("NSD_Host", host.getHostAddress());
            }
        };
    }


    //The following code was created by our app template, does not add functionality for the smart mirror
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    //    Permissions given
    @Override
    public void onStart() {
        super.onStart();
        // Here, thisActivity is the current activity
        // ask for CONTACTS permission
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(thisActivity,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }

    }


}
