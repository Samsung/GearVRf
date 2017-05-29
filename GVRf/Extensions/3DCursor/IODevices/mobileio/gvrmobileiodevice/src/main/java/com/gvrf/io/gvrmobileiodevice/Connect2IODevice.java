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
 * Class to connect to the client, listens to the client, accepts to the connection
 * and sets the input and output streams to read write the data
 * The thread runs till the Server hears and accpets the client socket connection
 * indicated by serverinfo.flag = true
 * The class then spins of the read from server thread
 */

package com.gvrf.io.gvrmobileiodevice;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;


public class Connect2IODevice implements Runnable {
    GVRMobileIoDevice.ServerInfo serverinfo = null;
    String TAG = "Connect2IODevice";
    Connect2IODevice(GVRMobileIoDevice.ServerInfo sInfo) throws IOException {
        serverinfo = sInfo;
        try {
            serverinfo.serversocket = new ServerSocket(serverinfo.port);

        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }
    @Override
    public void run() {
        while(serverinfo.flag) {
            try {
                if(serverinfo.serversocket != null) {
                    serverinfo.client = serverinfo.serversocket.accept();
                }
                if (serverinfo.client != null)
                {
                    serverinfo.flag = false;
                    Log.d(TAG, "Socket connection extablished");
                }
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }
        try {
            serverinfo.inputStream = serverinfo.client.getInputStream();
            if(serverinfo.inputStream != null) {
                Log.d(TAG, "InputStream on Vr device has been established");
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
        }

        try {
            serverinfo.outputStream = serverinfo.client.getOutputStream();
            if(serverinfo.outputStream != null)
            {
                Log.d(TAG,"OutputStream on Vr device has been established");
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
        }

        //Spin off the thread to start reading from the server
        if(!serverinfo.flag)
        {
            new Thread(new ReadFromIODevice(serverinfo)).start();
        }
    }

}
