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
 * Class to write the one byte data to send haptic feedback for vibration
 */

package com.gvrf.io.gvrmobileiodevice;

import android.util.Log;

import java.io.IOException;

public class Write2IODevice implements Runnable
{
    String TAG = "Write2IODevice";
    GVRMobileIoDevice.ServerInfo serverInfo = null;
    Write2IODevice(GVRMobileIoDevice.ServerInfo serverinfo)
    {
        serverInfo = serverinfo;
    }
    int input_byte = 1;
    @Override
    public void run() {
        try {
                byte[] bytes = new byte[1];
                bytes[0] = (byte)input_byte;
                serverInfo.outputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}

