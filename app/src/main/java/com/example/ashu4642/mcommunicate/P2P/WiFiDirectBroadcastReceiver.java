package com.example.ashu4642.mcommunicate.P2P;

/**
 * Created by ashu4642 on 10/16/2014.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.example.ashu4642.mcommunicate.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();
        private WiFiDirectActivity mActivity;
    public List<WifiP2pDevice> peersToCheck = new ArrayList<WifiP2pDevice>();

    /**
     * @param Manager WifiP2pManager system service
     * @param Channel Wifi p2p mChannel
     * @param Activity mActivity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager Manager, Channel Channel,
                                       WiFiDirectActivity Activity) {
        super();
        this.mManager = Manager;
        this.mChannel = Channel;
        this.mActivity = Activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                mActivity.setIsWifiP2pEnabled(true);
            } else {
                mActivity.setIsWifiP2pEnabled(false);
                mActivity.resetData();

            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p mManager. This is an
            // asynchronous call and the calling mActivity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, (PeerListListener) mActivity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP

                DeviceDetailFragment fragment = (DeviceDetailFragment) mActivity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                mManager.requestConnectionInfo(mChannel, fragment);
            } else {
                // It's a disconnect
                mActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) mActivity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }

    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        Log.d("Service", "requesting peers");
        Log.d("Service", "device list size: " + peerList.getDeviceList().size());


        List<WifiP2pDevice> oldPeers = new ArrayList<WifiP2pDevice>(mPeers);

        mPeers.clear();
        mPeers.addAll(peerList.getDeviceList());

        List<WifiP2pDevice> newPeers = new ArrayList<WifiP2pDevice>(mPeers);

        List<WifiP2pDevice> outgoing = new ArrayList<WifiP2pDevice>();
        List<WifiP2pDevice> incoming = new ArrayList<WifiP2pDevice>();

        for(int i = 0; i < oldPeers.size(); i++) {
            WifiP2pDevice device = oldPeers.get(i);
            if(!newPeers.contains(device)) {
                Log.d("outgoing", device.deviceName);
                outgoing.add(device);
            }
        }

        for(int i = 0; i < newPeers.size(); i++) {
            WifiP2pDevice device = newPeers.get(i);
            if(!oldPeers.contains(device)) {
                Log.d("incoming", device.deviceName);
                incoming.add(device);
            }
        }

        for(int i = 0; i < peersToCheck.size(); i++) {
            Log.d("peers2check before", peersToCheck.get(i).deviceName);
        }

        peersToCheck.removeAll(outgoing);
        peersToCheck.addAll(incoming);

        for(int i = 0; i < peersToCheck.size(); i++) {
            Log.d("peers2check after", peersToCheck.get(i).deviceName);
        }
    }
}


