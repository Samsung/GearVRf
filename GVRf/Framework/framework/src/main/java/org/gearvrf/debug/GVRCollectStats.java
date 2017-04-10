/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.debug;

import org.gearvrf.utility.Log;
import org.gearvrf.GVRTime;

public class GVRCollectStats {

    private String mName;
    private int mEntryCnt;

       private long mFrameStartTime;

       private int mFrameStartEntryCnt;
       private int mFpsIndex;

       private long mEnterTime;
       private long mTimeInFunction;
       private long mLeaveTime;
       private long mDeltaTime;

       private boolean mPrintEntry;

       // Optional data to accumulate for each invocation -- reset after each second.
       private float mVal1;
       private float mVal2;
       private float mVal3;
       private boolean mSetValue;

       private final float mIntrevalsInSec = 1000.0f; // Number of timestamp intervals per second.


       /**
        * Class to collect entry, leave, time spent between entry and leave.,
        * time between sequential calls, fps calculation (based on number of entries each second)
        * and log the results.
        *
        * Results will be logged with the name argument appearing in the log.
        * If printEveryEntry is true, every entry/leave pair is logged. If false only fps is logged.
        *
        */
       public GVRCollectStats(String name, boolean printEveryEntry) {
           mName = name;
           mEntryCnt = mFrameStartEntryCnt  = mFpsIndex = 0;
           mPrintEntry = printEveryEntry;
           mFrameStartTime = mLeaveTime = getRelTime();
           mVal1 = mVal2 = mVal3 = 0.0f;
           mSetValue = false;
       }
       
       void setPrintEveryEntry(boolean printEveryEntry)
       {
           mPrintEntry = printEveryEntry;
       }

       long getRelTime()
       {
           return GVRTime.getMilliTime();
       }

       public void enter()
       {
           mEntryCnt++;
           mEnterTime = getRelTime();
           mDeltaTime = mEnterTime - mLeaveTime; // Delta from prev call exit to curr call
       }
       public void leave(int index)
       {
           mLeaveTime = getRelTime();

           long deltaTime = mLeaveTime - mEnterTime;

           if (mPrintEntry)
               Log.d("Entry", "( " + mEntryCnt + " ) " + mName + " (  Wait " + mDeltaTime + " ) " + mEnterTime + " to " +
                       mLeaveTime + " delta -- " + deltaTime + " Idx -- " + index);

           mTimeInFunction += deltaTime;
           if (mLeaveTime - mFrameStartTime > mIntrevalsInSec)
           {
               calculateFPS();
           }
       }
       void calculateFPS()
       {
           mFpsIndex++;
           int numFrames = mEntryCnt - mFrameStartEntryCnt;
           float delta = (mLeaveTime - mFrameStartTime) / mIntrevalsInSec;
           Log.d("FPS", "( " + mFpsIndex + " )" + mName + " " + numFrames / delta
                   + " Time in function " + 1000.0 * mTimeInFunction / (mIntrevalsInSec * delta)
                   + " ms. ( "+ numFrames + " frames in " + delta + " sec)"
                );
           if (mSetValue)
               Log.e("Val", "Data1 -- " + mVal1 + " Data2 -- " + mVal2 + " Data3 -- " + mVal3);
           mFrameStartEntryCnt = mEntryCnt;
           mTimeInFunction = 0;
           mFrameStartTime = mLeaveTime;
           mVal1 = mVal2 = mVal3 = 0.0f;
           mSetValue = false;
       }
       public void setData1(float val)
       {
           mSetValue = true;
           mVal1 += val;
       }

       public void setData2(float val)
       {
           mSetValue = true;
           mVal2 += val;
       }
       public void setData3(float val)
       {
           mSetValue = true;
           mVal3 += val;
       }

}