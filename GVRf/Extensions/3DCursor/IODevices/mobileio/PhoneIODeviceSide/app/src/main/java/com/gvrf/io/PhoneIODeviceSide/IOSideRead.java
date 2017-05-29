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

/*
  This Class continously reads on the InputStream on the Client side.
  If 1 is recieved, It enables vibration on the iophonedevice.
 */

package com.gvrf.io.PhoneIODeviceSide;



import android.util.Log;

import com.gvrf.io.PhoneIODeviceSide.MainActivity.ServerPack;

import java.io.IOException;

public class IOSideRead implements Runnable {
	String TAG = "IOSideRead";
	private ServerPack serverInfo;
	public IOSideRead(ServerPack serverInfo) {
		this.serverInfo = serverInfo;
	}
	@Override
	public void run() {
		
		while(true)
		{
			if(serverInfo.in_stream != null)
			{
				byte[] bytes = new byte[1];
				try {
					serverInfo.in_stream.read(bytes);
				} catch (IOException e) {
					Log.e(TAG,e.getMessage());
				}

				int input_code = 1; //Used to describe the code to vibrate
				int vibrate_time = 300; //Used to change the time if vibration in ms
				if(bytes != null)
				{
					if(bytes[0] == (byte )input_code)
					{
						serverInfo.v.vibrate(vibrate_time);
					}
				}
			}
		}
	}
}
