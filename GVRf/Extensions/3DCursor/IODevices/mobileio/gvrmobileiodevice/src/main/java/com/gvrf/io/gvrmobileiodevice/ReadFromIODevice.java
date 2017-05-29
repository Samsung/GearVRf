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
 *This thread remains through the lifetime of the app and continously
 *listens to the data coming from the client
 * After reading it parses the data in the following order
 * Bytestream to quaternion
 * quaternion to RPY
 * RPY to distance if necessary based ont he implementation of the server
 * And sets either the orientation or position of the cursor as required
 */

package com.gvrf.io.gvrmobileiodevice;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;

public class ReadFromIODevice implements Runnable {

    GVRMobileIoDevice.ServerInfo serverInfo = null;
    String TAG = "ReadFromIODevice";
    float tmp_value;
    ReadFromIODevice(GVRMobileIoDevice.ServerInfo serverInfo)
    {
        this.serverInfo = serverInfo;
    }

    @Override
    public void run()
    {
        byte[] bytes = new byte[257]; //256 bytes to accommodate quaternion + 1 byte to set the rotation/position flag
        int mode_byte_value = 1;
        while(true)
        {
            if(serverInfo.inputStream != null)
            {
                try {
                    serverInfo.inputStream.read(bytes);
                    if(bytes != null)
                    {
                         if(bytes[256] == (byte)mode_byte_value)    // 1 is the integer value of byte used to switch the mode
                         {
                             if(serverInfo.position_mode == GVRMobileIoDevice.MotionModes.TRANSLATION) {
                                 serverInfo.position_mode = GVRMobileIoDevice.MotionModes.ROTATION;
                             }
                             else
                             {
                                 serverInfo.position_mode = GVRMobileIoDevice.MotionModes.TRANSLATION;
                             }
                         }
                           float[] quat = new float[4];
                           getQuatfromBytes(bytes, quat);

                           float[] rpy = new float[3];
                           float[] pos = new float[3];

                           if(serverInfo.position_mode == GVRMobileIoDevice.MotionModes.TRANSLATION)
                           {
                               quat2rpy(quat, rpy);
                               rpy2position(rpy, pos);
                               if(!Float.isNaN(pos[2]))
                               {
                                   serverInfo.gvrMobileIoDevice.setPosition(pos[0], pos[1], pos[2]);
                               }
                           }
                           else
                           {
                               serverInfo.gvrMobileIoDevice.setRotation(quat[0], quat[1], quat[2], quat[3]);
                           }
                    }
                } catch (IOException e) {
                    Log.e(TAG,Log.getStackTraceString(e));
                }

            }
        }
    }

    /**@brief function to convert rpy to pos based on
     *        the limits specified
     * TODO Eventually this fuction will be replaced by a call to accelerometers
     *      to obtain the values of translation/Position
     */
    private void rpy2position(float[] rpy, float[] pos)
    {

        final float minDegForZAxis = -45.0f;   //These values were set for the current application
        final float minValInZDirection = -30.0f;//These values can be changed as per the application
        final float maxDegForZAxis = 45.0f;
        final float maxValInZDirection = -4.0f;

        /* Limits for Pitch:rpy [1] degrees to depth
         * Current Limits are set to - pitch - [-45.0, 45.0]
         * */
        if(rpy[1] > maxDegForZAxis)
        {
            rpy[1] = maxDegForZAxis;

        }

        if(rpy[1] < minDegForZAxis)
        {
            rpy[1] = minDegForZAxis;
        }


        //Value of Depth:Z:pos[2]
        pos[2] = minValInZDirection + (maxValInZDirection - minValInZDirection)*(rpy[1] - minDegForZAxis)/(maxDegForZAxis - minDegForZAxis);

        /* Limits for Roll:rpy[0] degree to lateral
        *  Current Limits are identified as - if roll in anticlockwise,Limits - [90,179.99]
        *                                        roll in clockwise Limits - [-179.99, 90]
        *  */
        float minDegAntiClockwiseForX = 90.0f;
        float maxDegAntiCloclwiseForX = 179.99f;
        float minValInNegativeXDirection = -10.0f;
        float maxValInNegativeXDirection = 0.0f;

        float minDegClockwiseForX = -179.99f;
        float maxDegCloclwiseForX = -90.0f;
        float minValInPositiveXDirection = 0.0f;
        float maxValInPositiveXDirection = 10.0f;

        if(rpy[0] < minDegAntiClockwiseForX && rpy[0] > 0)
        {
            rpy[0] = minDegAntiClockwiseForX;
        }

        if(rpy[0] > maxDegCloclwiseForX && rpy[0] < 0 )
        {
            rpy[0] = maxDegCloclwiseForX;
        }

        if(rpy[0] > minDegAntiClockwiseForX-1 && rpy[0] < maxDegAntiCloclwiseForX+1)
        {
            pos[0] = (rpy[0] - minDegAntiClockwiseForX)/(maxDegAntiCloclwiseForX - minDegAntiClockwiseForX)*(maxValInNegativeXDirection - minValInNegativeXDirection) + minValInNegativeXDirection;
        }


        if(rpy[0] < maxDegCloclwiseForX+1 && rpy[0] > minDegClockwiseForX-1)
        {
            pos[0] = (rpy[0] - minDegClockwiseForX)/(maxDegCloclwiseForX - minDegClockwiseForX)*(maxValInPositiveXDirection - minValInPositiveXDirection) + minValInPositiveXDirection;
        }

        /* THE FOLLOWING LINES CAN BE UNCOMMENTED TO OBTAIN THE MOVEMENT OF THE CURSOR IN
        *  THE DIRECTION OF Y */
        /* Limits for rpy[2] height limits from 135 to left to 90 on the right*/
      /*  if(rpy[2] > 135.0f)
        {
            rpy[2] = 135.0f;
        }
        if(rpy[2] < 90.0f)
        {
            rpy[2] = 90.0f;
        }

        pos[1] = 2.0f/9.0f*(rpy[2] - 90.0f) - 5.0f; */

    }

    /**@brief Method to convert byte array to Quaternion
     *        Each Quaternion is represented by 4 byte Arrays
     *        The following method sends each array to conversionFuncInv
     *        to be converted a component of byte array
    */
    private void getQuatfromBytes(byte[] bytes,float[] quat) {
        int tmp_index = 0;
        byte[] tmp = new byte[64];
        int quat_index = 0;
        for(int i = 0; i < bytes.length; i++)
        {
            tmp[tmp_index++] = bytes[i];
            if((i+1)%64 == 0)
            {
                quat[quat_index++] = conversionFuncInv(tmp);
                tmp_index = 0;
                tmp = new byte[64];
            }
        }
    }

    /**@brief Method to convert individual byte arrays to quaternion vals
     */
    private float conversionFuncInv(byte[] tmp) {
        int constant2scalelong = 10000;
        ByteBuffer buffer1 = ByteBuffer.allocate(Long.SIZE);
        buffer1.put(tmp);
        buffer1.flip();
        float value = ((float)buffer1.getLong()/(float)constant2scalelong);
        return value;
    }

    /**@brief Method to convert quaternion to rpy
     */
    public void quat2rpy(float[] quat, float[] rpy) {
        double sqw = quat[0]*quat[0];
        double sqx = quat[1]*quat[1];
        double sqy = quat[2]*quat[2];
        double sqz = quat[3]*quat[3];
        double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
        double test = quat[1]*quat[2] + quat[3]*quat[0];

        if (test > 0.499*unit) { // singularity at north pole
           /* rpy[0] = (float)(2 * atan2(quat[1],quat[0]));
            rpy[1] = (float)Math.PI/2;
            rpy[2] = 0;*/
            return;
        }
        if (test < -0.499*unit) { // singularity at south pole
           /* rpy[0] = (float)(-2 * atan2(quat[1],quat[0]));
            rpy[1] = (float)-Math.PI/2;
            rpy[2] = 0;*/
            return;
        }
        rpy[0] = (float)Math.toDegrees(atan2(2*quat[2]*quat[0]-2*quat[1]*quat[3] , sqx - sqy - sqz + sqw));
        rpy[1] = (float)Math.toDegrees(asin(2*test/unit));
        rpy[2] = (float)Math.toDegrees(atan2(2*quat[1]*quat[0]-2*quat[2]*quat[3] , -sqx + sqy - sqz + sqw));

    }
}
