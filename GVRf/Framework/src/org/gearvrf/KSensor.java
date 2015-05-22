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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Wrapper for GearVR rotation sensor. */
class KSensor {
    private final long mNativePointer = NativeKSensor.ctor();
    private Timer mTimer = null;
    private final List<KSensorListener> mListeners = new ArrayList<KSensorListener>();
    private boolean mConnected = false;

    KSensor() {
    }

    void registerListener(KSensorListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    void pause() {
        mTimer.cancel();
        mTimer.purge();
    }

    void resume() {
        if (mConnected) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new KSensorTask(this), 0, 2);
        } else {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new KSensorTask(this), 0, 500);
        }
    }

    boolean update() {
        return NativeKSensor.update(mNativePointer);
    }

    void dispatchData() {
        float[] data = NativeKSensor.getData(mNativePointer);

        synchronized (mListeners) {
            for (KSensorListener listener : mListeners) {
                listener.onSensorChanged(
                        NativeKSensor.getTimeStamp(mNativePointer), data[0],
                        data[1], data[2], data[3], data[4], data[5], data[6]);
            }
        }
        if (!mConnected) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new KSensorTask(this), 0, 2);
        }
        mConnected = true;
    }

    void dispatchFailure() {
        synchronized (mListeners) {
            for (KSensorListener listener : mListeners) {
                listener.onSensorErrorDetected();
            }
        }
        if (mConnected) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new KSensorTask(this), 0, 500);
        }
        mConnected = false;
    }

    void close() {
        mTimer.cancel();
        mTimer.purge();
        NativeKSensor.close(mNativePointer);
    }
}

class NativeKSensor {
    static native long ctor();

    static native boolean update(long ptr);

    static native long getTimeStamp(long ptr);

    static native float[] getData(long ptr);

    static native void close(long ptr);
}

class KSensorTask extends TimerTask {
    private KSensor mSensor = null;

    public KSensorTask(KSensor sensor) {
        mSensor = sensor;
    }

    @Override
    public void run() {
        if (mSensor.update()) {
            mSensor.dispatchData();
        } else {
            mSensor.dispatchFailure();
        }
    }
}
