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

/** Created by v.nidamarthy
 *  Activity to launch the app that sends the orientation data of the phone
 *  to control the cursor in the app present in the Mobile providing
 *  the content on the headset
 *
 *  MainActivity starts the app, establishes the Wifi connection
 *  Server - Phone the headset Client-device in hand/iodevice
 *  ConnectVRDevice connects to the head set phone and Establishes the input and outputstreams
 *  IOSideReading listens continously to server and reads the feedback if any
 *  IOSideWrite is called when the sensor listener gets the quaternion
 *
 *	Select the name of the headset device from the list,
 *	enter the ip of the device on the headset in the text box and click the
 *  Connect Button to establish connection and communication
 *
 *  If this app gets closed, both the VR app and the current app need to be restarted
 * */

package com.gvrf.io.PhoneIODeviceSide;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements SensorEventListener {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private WiFiDirectBroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;
	String TAG = "MainActivity";
	private ListView listView;
	private static int port;

	/**@brief class to hold server and client objects along with the
	 *        input and outputstreams, flags and changeMode
	 * */
	public class ServerPack
	{
		public Socket client_;
		public ServerSocket serversocket_;
		public InputStream in_stream;
		public OutputStream out_stream;
		public int port;
		public InetAddress host;
		public boolean pollflag = true;
		public long[] data;
		public Vibrator v;
		public boolean changeMode;
		public boolean is_connection_obtained;

		ServerPack(String ip, int port) throws IOException
		{
			this.port = port;
			client_ = new Socket();
			serversocket_ = new ServerSocket(port);
			in_stream = null;
			out_stream = null;
			data = new long[4];
			changeMode = false;
			is_connection_obtained = false;
			v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			try {
				host = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				Log.e(TAG,e.getMessage());
			}
		}
	}
	
	private static ServerPack serverpack;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private boolean connection_flag =  false;

	private static String deviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		
		mIntentFilter = new IntentFilter();
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object obj = listView.getItemAtPosition(position);
				setDeviceName(obj.toString());
			}
		});

	    mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
		port = 8888;
	}
	
	/**@brief Button callback to start the threads to connect
	 * 	      to the server and reading from server
	 */
	public void searchAndConnect(View view) 
	{
		EditText etext = (EditText)findViewById(R.id.textView);
		if(TextUtils.isEmpty(etext.getText().toString()))
		{
			Toast.makeText(this.getApplicationContext(), "Please select a valid device name and enter a valid IP address!", Toast.LENGTH_LONG).show();
			return;
		}
		if(deviceName == null)
		{
			Toast.makeText(this.getApplicationContext(), "Please select a valid device name and enter a valid IP address!", Toast.LENGTH_LONG).show();
			return;

		}

		//connect to the selected device
		(new Thread(new Connect2Device(deviceName))).start();

		//get the ip of the selected device
		String ip = etext.getText().toString();

		//open connection to the server using the ip address obtained
		try {
			serverpack = new ServerPack(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		new ConnectVRDevice(this, serverpack).execute(serverpack);
		connection_flag  = true;
		new Thread(new IOSideRead(serverpack)).start();
	}

	public class Connect2Device implements Runnable {
		String deviceName;
		Connect2Device(String deviceName)
		{
			this.deviceName = deviceName;
		}
		@Override
		public void run() {
			mReceiver.connect2Device(deviceName);
		}
	}
	/**@brief Button callback to switch modes between
	 *        setPosition and setRotation
	 * */
	public void switchMode(View view)
	{
		serverpack.changeMode = true;
	}
	
	
	private void discoverWifiDevices() 
	{
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

	/**@brief Registrer to receiver, discover and connect to the
	 *        wifi of the device
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
		discoverWifiDevices();
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	/**@brief Unregister the wifi receiver whent he app pauses or stops
	 * */
	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	/** Close all the sockets and input and outstream when the app closes
	 * 
	 */
	@Override
	protected void onDestroy() {
		if(serverpack == null)
		{
			super.onDestroy();
			return;
		}
		serverpack.pollflag = false;
		
		if(serverpack.out_stream != null)
		{
			new Thread(){
				public void run()
				{
					try {
							serverpack.out_stream.close();
							Log.d(TAG, "Client side output stream is closed");
						} catch (IOException e) {
						Log.e(TAG,e.getMessage());
						}
				}
			}.start();
		}
		
		if(serverpack.in_stream != null)
		{
			new Thread()
			{
				public void run() {
						try {
							serverpack.in_stream.close();
							Log.d(TAG, "Client side output stream is closed");
						} catch (IOException e) {
							Log.e(TAG,e.getMessage());
						}
				}
			}.start();
		}
		
		if(serverpack.client_ != null)
		{
			new Thread()
			{
				public void run()
				{
					if(serverpack.client_.isConnected())
					{
						try {
							serverpack.client_.close();
							Log.d(TAG, "Client socket closed");
					} catch (IOException e) {
							Log.e(TAG,e.getMessage());
						}
					}
				}
			}.start();
		}
		
		if(serverpack.serversocket_ != null)
		{
			new Thread()
			{
				public void run()
				{
					{
						if(!serverpack.serversocket_.isClosed())
						{
							try {
								serverpack.serversocket_.close();
								Log.d(TAG, "Serversocket closed");
							} catch (IOException e) {
								Log.e(TAG,e.getMessage());
							}
						}
					}
				}
			}.start();
		}
		super.onDestroy();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	

	float[] mGravity;
	float[] mGeomagnetic;
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
		  mGravity = event.values;
			if(connection_flag)
			{
			//Multiplying the data by 10,000 to not lose any post decimal vals
				serverpack.data[0] = (long)(mGravity[0]*10000);
				serverpack.data[1] = (long)(mGravity[1]*10000);
				serverpack.data[2] = (long)(mGravity[2]*10000);
				serverpack.data[3] = (long)(mGravity[3]*10000);
				new IOSideWrite().execute(serverpack);
			}
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		  mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null)
		{
		  float R[] = new float[9];
		  float I[] = new float[9];
		  boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		  if (success) {
			float orientation[] = new float[3];
			SensorManager.getOrientation(R, orientation);
			serverpack.data[0] = (long) (orientation[0]*100); // orientation contains: azimut, pitch and roll
			serverpack.data[1] = (long) (orientation[1]*100);
			serverpack.data[2] = (long) (orientation[2]*100);

			if(connection_flag)
			{
				new IOSideWrite().execute(serverpack);
			}
		  }
		}
	}

	public void setDeviceName(String dName)
	{
		deviceName = dName;
	}

}

