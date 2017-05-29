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

import android.os.AsyncTask;
import android.util.Log;

import com.gvrf.io.PhoneIODeviceSide.MainActivity.ServerPack;

import java.io.IOException;
import java.nio.ByteBuffer;


public class IOSideWrite extends AsyncTask<ServerPack, Void, Void>{
	String TAG = "IOSideWrite";
	byte[] q_bytes = new byte[257];
	
	@Override
	protected Void doInBackground(ServerPack... params) {
		{
			byte[] bytes_w, bytes_x, bytes_y, bytes_z;

			bytes_w = conversionFunc(params[0].data[0]);
			bytes_x = conversionFunc(params[0].data[1]);
			bytes_y = conversionFunc(params[0].data[2]);
			bytes_z = conversionFunc(params[0].data[3]);

			arrayfyBytes(bytes_w, bytes_x, bytes_y, bytes_z, q_bytes);
			arrafiedToLong(q_bytes);

			int modeVariable = 0;
			q_bytes[256] = (byte)modeVariable;
			if (params[0].changeMode)
			{
				modeVariable = 1;
				q_bytes[256] = (byte)modeVariable;
				params[0].changeMode = false;
			}
			if(params[0].out_stream != null) {
				try {
					params[0].out_stream.write(q_bytes);
				} catch (IOException e) {
					Log.e(TAG,e.getMessage());
				}
			}
		}
		return null;
	}


	private void arrafiedToLong(byte[] q_bytes2) {
		
		int tmp_index = 0;
		byte[] tmp = new byte[64];
		for(int i = 0; i < q_bytes2.length; i++)
		{
			tmp[tmp_index++] = q_bytes2[i];
			if((i+1)%64 == 0)
			{
				tmp_index = 0;
				tmp = new byte[64];
			}
		}
	}

	/**@brief This method takes as inputs individual bytearrays and arranges them into
	 *        a single byte array which is later parsed into quaternion on the
	 *        ServerSide
	* */
	private void arrayfyBytes(byte[] bytes_w, byte[] bytes_x, byte[] bytes_y, byte[] bytes_z, byte[] q_bytes2) {
		int count = bytes_w.length+bytes_x.length+bytes_y.length+bytes_z.length;

		int count_var = 0;
		for(int i = 0; i < bytes_w.length; i++)
		{
			q_bytes2[count_var++] = bytes_w[i];
			
		}
		for(int i = 0; i < bytes_x.length; i++)
		{
			q_bytes2[count_var++] = bytes_x[i];
		}
		for(int i = 0; i < bytes_y.length; i++)
		{
			q_bytes2[count_var++] = bytes_y[i];
		}
		for(int i = 0; i < bytes_z.length; i++)
		{
			q_bytes2[count_var++] = bytes_z[i];
		}
		
	}

	private byte[] conversionFunc(long l) {

		long tmp_before = l;
		ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
	    buffer.putLong(tmp_before);
	    return buffer.array();
	}

	
}
