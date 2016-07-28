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

import org.gearvrf.GVRActivityBase.DockListener;
import org.gearvrf.utility.Log;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Wrapper class for rotation-related sensors. Combines handling a device's
 * internal {@link Sensor#TYPE_ROTATION_VECTOR rotation sensor} with the
 * GearVR's {@link KSensor}. When the {@link KSensor} is available, sensor
 * readings from it will be used; otherwise, {@link RotationSensor} will fall
 * back to the device's internal sensor.
 */
class RotationSensor {
    private final RotationSensorListener mListener;

    private GVRInternalSensorListener mInternalSensorListener;
    private final Context mApplicationContext;
    private boolean mUsingInternalSensor = true;

    /**
     * Constructor.
     * 
     * @param context
     *            A {@link Context}.
     * @param listener
     *            A {@link RotationSensorListener} implementation to receive
     *            rotation data.
     */
    RotationSensor(GVRActivity activity, RotationSensorListener listener) {
        mListener = listener;
        mApplicationContext = activity.getApplicationContext();

        activity.addDockListener(new DockListener() {
            @Override
            public void onUndock() {
                startInternalSensor();
                mUsingInternalSensor = true;
            }
            @Override
            public void onDock() {
                stopInternalSensor();
                mUsingInternalSensor = false;
            }
        });
    }

    /**
     * Resumes listening for sensor data. Must be called from
     * {@link Activity#onResume()}.
     */
    void onResume() {
        if (mUsingInternalSensor) {
            startInternalSensor();
        }
    }

    /**
     * Pauses listening for sensor data. Must be called from
     * {@link Activity#onPause()}.
     */
    void onPause() {
        if (mUsingInternalSensor) {
            stopInternalSensor();
        }
    }

    /**
     * Releases connection to {@link KSensor}. Must be called from
     * {@link Activity#onDestroy()}.
     */
    void onDestroy() {
        stopInternalSensor();
    }

    /**
     * Implementation detail. Handles data from device's internal rotation
     * sensor. See
     * {@link RotationSensorListener#onRotationSensor(long, float, float, float, float, float, float, float)
     * RotationSensorListener.onRotationSensor()}.
     */
    void onInternalRotationSensor(long timeStamp, float w, float x, float y,
            float z, float gyroX, float gyroY, float gyroZ) {
        mListener.onRotationSensor(timeStamp, w, x, y, z, gyroX, gyroY, gyroZ);
    }

    /**
     * Chooses the sensor to call onRotationSensor().
     */
    void onRotationSensorChanged(int currentSensor) {
    }

    private void startInternalSensor() {
        if (null == mInternalSensorListener) {
            final SensorManager sensorManager = (SensorManager)mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor internalSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            if (internalSensor == null) {
                Log.e("RotationSensor", "This phone does not have a rotation sensor - it cannot run GearVRF applications");
            }
            mInternalSensorListener = new GVRInternalSensorListener(this);
            sensorManager.registerListener(mInternalSensorListener, internalSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void stopInternalSensor() {
        if (null != mInternalSensorListener) {
            final SensorManager sensorManager = (SensorManager)mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(mInternalSensorListener);
            mInternalSensorListener = null;
        }
    }

}
