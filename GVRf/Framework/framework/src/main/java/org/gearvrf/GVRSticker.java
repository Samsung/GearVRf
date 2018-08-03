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

package org.gearvrf;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.utility.Threads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class takes screenshots at a specified interval and writes the output PNG files to GearVRFScreenshots directory.
 * The capturing can be stopped by calling {@link GVRSticker#stop()}
 * The images can be used to create Stickers (a GIF animation)
 */

public class GVRSticker {
    private final GVRContext mGVRContext;
    private final String mTag;
    private final String mDirectory;
    private final long mInterval;
    private boolean mCaptureFlag = true;
    private final static String TAG = "GVRSticker";

    /**
     * Constructor takes the following parameters
     * @param gvrContext
     * @param tag
     *           Name for outputting PNG's to the SD Card along with appended frame ID
     * @param interval
     *           Time in milliseconds to take screenshot at that interval
     */
    public GVRSticker(GVRContext gvrContext, String tag, long interval){
        mGVRContext = gvrContext;
        mTag = tag;
        mInterval = interval;

        File sdcard = Environment.getExternalStorageDirectory();
        mDirectory = sdcard.getAbsolutePath() + "/GearVRFScreenshots/";
        File d = new File(mDirectory);
        d.mkdirs();
    }

    private boolean lastScreenshotFinished[] = {true, true, true};
    private int pboIndex = 0;

    /**
     * Initiate's the screenshot capturing
     * Captures images every {@link GVRSticker#mInterval} milliseconds
     */
    public void startCapturing()
    {
        mCaptureFlag = true;
        Threads.spawn(new Runnable()
        {
            public void run()
            {
                int frame = 0;
                boolean lastStickerCall;
                while(mCaptureFlag) {
                    if (lastScreenshotFinished[pboIndex]) {
                        lastStickerCall = mGVRContext
                                .captureSticker(newScreenshotCallback(frame, pboIndex), pboIndex);
                        if(lastStickerCall) {
                            lastScreenshotFinished[pboIndex] = false;
                            pboIndex = (pboIndex + 1) % 3;
                            frame++;
                        }
                    }
                    else{
                        Log.e(TAG, "Sticker skipped since previous read is not completed");
                    }
                    try {
                        Thread.sleep(mInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Stop's the screenshot thread
     */
    public void stopCapturing(){
        mCaptureFlag = false;
    }

    private GVRScreenshotCallback newScreenshotCallback(final int frame, final int currentPboIndex)
    {
        return new GVRScreenshotCallback()
        {
            @Override
            public void onScreenCaptured(Bitmap bitmap)
            {
                if (bitmap != null)
                {
                    File file = new File(mDirectory + mTag +"_"+ frame +"_" + ".png");
                    FileOutputStream outputStream = null;
                    try
                    {
                        outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            outputStream.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    Log.e(TAG, "Returned Bitmap is null for frame " + frame);
                }

                // enable next screenshot
                lastScreenshotFinished[currentPboIndex] = true;
            }
        };
    }
}
