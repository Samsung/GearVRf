/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


/**
 * Created by v.nidamarthy, d.knaan, p.mehta, r.rudhradevan on 8/26/16.
 *
 * Server - mobile with app, client - mobile/iodevice
 * This class encodes the reading and writing of data to the app over Wifi,  //TODO adding bluetooth communication option
 * WifiDirectBroadcastReceiver establishes connection over WiFi
 * Connect2IODevice listens to the client and establishes connection and
 * also the Input and Outputstreams
 * Write2IODevice writes data/feedback to the client
 * ReadFromIODevice reads the incoming data i.e the orientation
 *
 */

package com.gvrf.io.gvrmobileiodevice;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

import org.gearvrf.GVRContext;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.OutputEvent;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class GVRMobileIoDevice extends IoDevice{
    public static final String DEVICE_ID = "gvrmobile";
    public static final int VENDOR_ID = 525;
    public static final int PRODUCT_ID = 0;
    public static final String VENDOR_NAME = "Samsung";
    public static final String DEVICE_NAME = "GVR Mobile Device";
    private static final String TAG = GVRMobileIoDevice.class.getSimpleName();
    private final ServerInfo serverInfo;
    private Context androidContext;

    /* Fields for sensors and sockets */
    private GVRContext mGVRContext;

    /* Broadcast receiver side*/
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mIntentFilter;
    public final static int PORT_ = 8888;
    private WifiDirectBroadcastReceiver mReceiver;

    public enum Actions {ACTION_VIBRATE}   //enum to set feedback actions to the device
    public enum MotionModes {ROTATION, TRANSLATION};

    @Override
    public void handleOutputEvent(OutputEvent event) {

        try {
            if (event.action == Actions.ACTION_VIBRATE.ordinal())
            {
                //Write a single bit to give haptic feedback, vibration to the client/mobile device
                if(serverInfo.outputStream != null && serverInfo.client.getInetAddress().isReachable(100)) {
                    new Thread(new Write2IODevice(serverInfo)).start();
                }
            }
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    //ServerInfo class to handle the input and output streams and other flags
    public class ServerInfo
    {
        public Socket client = null; //client acept socket
        public ServerSocket serversocket; //serversocket
        public InputStream inputStream;
        public OutputStream outputStream;
        public GVRMobileIoDevice gvrMobileIoDevice; //class reference to call functions from outside
        int port;
        boolean flag;
        MotionModes position_mode;   //flag to change modes between position and rotation
        public ServerInfo()      //TODO eventually will be using IMU to perform this function
        {
            port = PORT_;   // port can be changed
            flag = true;
            position_mode = MotionModes.TRANSLATION;
        }

    }

    /**@brief Constructor for the class
     *
     * @param gvr context
     */
    public GVRMobileIoDevice(GVRContext gvrContext) {
        super(DEVICE_ID, VENDOR_ID, PRODUCT_ID, DEVICE_NAME, VENDOR_NAME, true);
        mGVRContext = gvrContext;
        /* code to get sensor values */
        androidContext = gvrContext.getActivity().getApplicationContext();
        registerWithWifiBroadcastReceiverAndConnect();
        setEnable(true);

        //Initial position of the Cursor
        setPosition(-0.5f,0,-5.0f);
        serverInfo = new ServerInfo();
        serverInfo.gvrMobileIoDevice = this;

        //start the thread to estableish connection and stream info
        try {
            new Thread(new Connect2IODevice(serverInfo)).start();
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    /**@brief Register and establiosh params for WiFi connection
     *
     */

    private void registerWithWifiBroadcastReceiverAndConnect() {
        mManager = (WifiP2pManager)androidContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(androidContext, androidContext.getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, androidContext);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void setEnable(boolean enable) {
        super.setEnable(enable);
        /* register for wifi listener and discover and connect devices*/
        androidContext.registerReceiver(mReceiver, mIntentFilter);
        discoverWifiDevices();
    }

    @Override
    public void setRotation(float w, float x, float y, float z) {
        super.setRotation(w, x, y, z);
    }

    @Override
    public void setPosition(float x, float y, float z){
        super.setPosition(x,y,z);
    }

    private void discoverWifiDevices() {
        mManager.discoverPeers(mChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Discovered devices");

            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Could not discover any devices because "+ Integer.toString(reasonCode));
            }
        });
    }


    public void onDestroy()
    {
        //close output and input stream when the app closes
        if(serverInfo.outputStream != null)
        {
            try {
                serverInfo.outputStream.close();
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }
        if(serverInfo.inputStream != null)
        {
            try {
                serverInfo.inputStream.close();
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }

    }
}
