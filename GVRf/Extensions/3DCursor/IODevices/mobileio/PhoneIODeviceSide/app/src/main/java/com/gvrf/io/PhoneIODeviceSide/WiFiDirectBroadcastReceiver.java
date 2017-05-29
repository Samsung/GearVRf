/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.gvrf.io.PhoneIODeviceSide;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    protected static final String TAG = "WifiBroadcastReceiver";
	private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
	private List peers = new ArrayList();

	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
    		MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();;
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) 
        {
        	String message = " ";
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        	if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
        	{
        		message = "wifi is enabled";
				Log.d(TAG, message);
        	}
        	else
        	{
        		message = "wifi is not enabled";
				Log.d(TAG, message);
        	}
        	Toast toast = Toast.makeText(mActivity.getApplicationContext(), message, Toast.LENGTH_LONG);
        	toast.show();
        } 
        
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) 
        {
        	if(mManager != null)
            {
            	mManager.requestPeers(mChannel, new PeerListListener() {
					

					@Override
					public void onPeersAvailable(WifiP2pDeviceList devicelist) {
						Activity activity = (Activity)mActivity;
						View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
						ListView listView = (ListView) rootView.findViewById(R.id.listView);
						if(!peers.isEmpty())
						{
							peers.clear();
						}
						for(WifiP2pDevice device : devicelist.getDeviceList())
						{
							peers.add(device.deviceName);
						}
						ArrayAdapter adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, peers);
						listView.setAdapter(adapter);
					}
				});
            } 
        	
        } 
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) 
        {
            // Respond to new connection or disconnections
        } 
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) 
        {
            // Respond to this device's wifi state changing
        }
    }

	public void connect2Device(String s)
	{
		for(int i = 0; i < peers.size(); ++i)
		{
			if(peers.get(i) == s)
			{
				connectDevice(s);
			}
		}
	}

	protected void connectDevice(final String deviceAddress2) {
		WifiP2pDevice device;
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = deviceAddress2;
		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Connection to "+deviceAddress2+" is established");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "Connection to "+deviceAddress2+" has failed");
			}
		});
	}

}

