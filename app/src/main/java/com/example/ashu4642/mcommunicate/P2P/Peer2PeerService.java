package com.example.ashu4642.mcommunicate.P2P;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Peer2PeerService extends Service {
    private static final int SERVER_PORT = 5252;
    private WifiP2pManager mManager;
    private Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver;
    private WiFiDirectActivity mActivity;
    private IntentFilter mIntentFilter;
    private String mAlias;
    private HashMap<String, HashMap<String, String>> mPeople = new HashMap<String, HashMap<String, String>>();

    public Peer2PeerService()
    {
        this.mAlias = LoginActivity.currentUser;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "started");

        Toast guy = Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_LONG);
        guy.show();

        // get the wifip2p manager
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        // get the channel from the manager
        mChannel = mManager.initialize(this, getMainLooper(), null);

        // instantiate a new receiver with the given manager and channel
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, mActivity);

        // set up the intent filter
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // register the broadcast receiver
        registerReceiver(mReceiver, mIntentFilter);

        // register this service for discovery
        startRegistration();

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new ActionListener() {
            @Override
            public void onSuccess()
            {
                // success
            }

            @Override
            public void onFailure(int code)
            {
                // check error
            }
        });

        mManager.discoverServices(mChannel, new ActionListener() {
            @Override
            public void onSuccess()
            {
                // success
            }

            @Override
            public void onFailure(int code)
            {
                // figure out the error
                if(code == WifiP2pManager.P2P_UNSUPPORTED)
                {
                    Log.d("test", "p2p is not supported on this device");
                }
            }
        });

        discoverService();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d("Service", "binded");

        Toast guy = Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_LONG);
        guy.show();

        // get the wifip2p manager
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        // get the channel from the manager
        mChannel = mManager.initialize(this, getMainLooper(), null);

        // instantiate a new receiver with the given manager and channel
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, mActivity);

        // set up the intent filter
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // register the broadcast receiver
        registerReceiver(mReceiver, mIntentFilter);

        // register this service for discovery
        startRegistration();

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new ActionListener() {
            @Override
            public void onSuccess()
            {
                // success
            }

            @Override
            public void onFailure(int code)
            {
                // check error
            }
        });

        mManager.discoverServices(mChannel, new ActionListener() {
            @Override
            public void onSuccess()
            {
                // success
            }

            @Override
            public void onFailure(int code)
            {
                // figure out the error
                if(code == WifiP2pManager.P2P_UNSUPPORTED)
                {
                    Log.d("test", "p2p is not supported on this device");
                }
            }
        });

        discoverService();

        return null;
    }

    private void startRegistration()
    {
        // create a hash map for information about this service
        Map<String, String> info = new HashMap<String, String>();
        info.put("listenport", String.valueOf(SERVER_PORT));
        info.put("alias", mAlias);
        info.put("location", "VSR");

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_mCommunicate", "_presence._tcp", info);

        // add the local service
        mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
            @Override
            public void onSuccess()
            {
                // don't need to do anything here
            }

            @Override
            public void onFailure(int arg0)
            {
                // check what the error was
            }
        });
    }

    private void discoverService()
    {
        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain,
                                                  Map<String, String> info, WifiP2pDevice device) {
                Log.d("test", "DnsSdTxtRecord available -" + info.toString());
                //mPeople.put(device.deviceAddress, (HashMap<String, String>) info);
                // I don't think we need to do anything here
            }
        };

        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {

            @Override
            public void onDnsSdServiceAvailable(String instanceName,
                                                String registrationType, WifiP2pDevice resourceType) {

                // check if the device advertising this service is in the list to check
                Collection<WifiP2pDevice> peers = mReceiver.peersToCheck;
                for(WifiP2pDevice device : peers)
                {
                    if(device.equals(resourceType))
                    {
                        // log a touch event
                        Log.d("test", "touched " + device.deviceName);
                        Toast.makeText(getApplicationContext(), "touched " + device.deviceName, Toast.LENGTH_SHORT).show();
                        // remove this device from the list to check
                        mReceiver.peersToCheck.remove(device);
                    }
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
    }

    public class Peer2PeerBinder extends Binder {
        Peer2PeerService getService() {
            return Peer2PeerService.this;
        }
    }

}