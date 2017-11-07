package org.gearvrf.physics;

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

import android.os.Handler;
import android.os.HandlerThread;

/**
 * This class represents the Physics context
 * with its own main loop.
 */
public class GVRPhysicsContext {
    private static final GVRPhysicsContext mInstance = new GVRPhysicsContext();
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    public static GVRPhysicsContext getInstance() {
        return mInstance;
    }

    private GVRPhysicsContext() {
        mHandlerThread = new HandlerThread("gvrf-physics");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public boolean runOnPhysicsThread(Runnable r) {
        return mHandler.post(r);
    }

    public boolean runDelayedOnPhysicsThread(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    public boolean runAtTimeOnPhysicsThread(Runnable r, long uptimeMillis) {
        return mHandler.postAtTime(r, uptimeMillis);
    }

    public void removeTask(Runnable r) {
        mHandler.removeCallbacks(r);
    }
}
