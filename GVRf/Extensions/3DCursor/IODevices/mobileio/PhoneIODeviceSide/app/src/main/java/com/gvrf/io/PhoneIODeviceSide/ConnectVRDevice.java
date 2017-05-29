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
	This class defines the procedure to establish connection with the Server,
	And also open input and output Streams.
 */

package com.gvrf.io.PhoneIODeviceSide;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gvrf.io.PhoneIODeviceSide.MainActivity.ServerPack;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ConnectVRDevice extends AsyncTask<ServerPack, Void, Void> {
	String TAG = "ConnectVRDevice";
	private Context mContext;

	public ConnectVRDevice(Context context, ServerPack spack) {
		super();
		mContext = context;
	}
	@Override
	protected Void doInBackground(ServerPack... params) {
		int timeout = 20000;
		try {
			params[0].client_.bind(null);
			params[0].client_.connect((new InetSocketAddress(params[0].host, params[0].port)), timeout);
			params[0].out_stream = params[0].client_.getOutputStream();
			params[0].in_stream = params[0].client_.getInputStream();
		} catch (IOException e) {
			Log.e(TAG,e.getMessage());
		}
		return null;
	}
	
	
}
