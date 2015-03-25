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

    private static abstract class SensorType {
        public static final int INTERNAL = 0;
        public static final int KSENSOR = 1;
    }

    private static final int DEFAULT_SENSOR = SensorType.INTERNAL;

    private int mCurrentSensor = DEFAULT_SENSOR;

    private final RotationSensorListener mListener;

    private final KSensor mKSensor;
    private final KSensorListener mKSensorListener;

    private final Sensor mInternalSensor;
    private final SensorManager mInternalSensorManager;
    private final GVRInternalSensorListener mInternalSensorListener;

    /**
     * Constructor.
     * 
     * @param context
     *            A {@link Context}.
     * @param listener
     *            A {@link RotationSensorListener} implementation to receive
     *            rotation data.
     */
    RotationSensor(Context context, RotationSensorListener listener) {
        mListener = listener;

        mInternalSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mInternalSensor = mInternalSensorManager
                .getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mInternalSensorListener = new GVRInternalSensorListener(this);

        mKSensor = new KSensor();
        mKSensorListener = new KSensorListener(this);
        mKSensor.registerListener(mKSensorListener);
    }

    /**
     * Resumes listening for sensor data. Must be called from
     * {@link Activity#onResume()}.
     */
    void onResume() {
        mInternalSensorManager.registerListener(mInternalSensorListener,
                mInternalSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mKSensor.resume();
    }

    /**
     * Pauses listening for sensor data. Must be called from
     * {@link Activity#onPause()}.
     */
    void onPause() {
        mInternalSensorManager.unregisterListener(mInternalSensorListener);
        mKSensor.pause();
    }

    /**
     * Releases connection to {@link KSensor}. Must be called from
     * {@link Activity#onDestroy()}.
     */
    void onDestroy() {
        mKSensor.close();
    }

    /**
     * Implementation detail. Handles data from device's internal rotation
     * sensor. See
     * {@link RotationSensorListener#onRotationSensor(long, float, float, float, float, float, float, float)
     * RotationSensorListener.onRotationSensor()}.
     */
    void onInternalRotationSensor(long timeStamp, float w, float x, float y,
            float z, float gyroX, float gyroY, float gyroZ) {
        if (mCurrentSensor == SensorType.INTERNAL) {
            mListener.onRotationSensor(timeStamp, w, x, y, z, gyroX, gyroY,
                    gyroZ);
        }
    }

    /**
     * Implementation detail. Handles data from {@link KSensor}. See
     * {@link RotationSensorListener#onRotationSensor(long, float, float, float, float, float, float, float)
     * RotationSensorListener.onRotationSensor()}.
     */
    void onKSensor(long timeStamp, float w, float x, float y, float z,
            float gyroX, float gyroY, float gyroZ) {
        mCurrentSensor = SensorType.KSENSOR;
        mListener.onRotationSensor(timeStamp, w, x, y, z, gyroX, gyroY, gyroZ);
    }

    /**
     * Implementation detail. Switches to device's internal sensor.
     */
    void onKSensorError() {
        mCurrentSensor = SensorType.INTERNAL;
    }

    /**
     * Chooses the sensor to call onRotationSensor().
     */
    void onRotationSensorChanged(int currentSensor) {
        mCurrentSensor = currentSensor;
    }
}
