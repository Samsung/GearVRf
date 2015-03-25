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

import org.apache.commons.math3.complex.Quaternion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;

/** A listener for a TYPE_ROTATION_VECTOR type sensor. */
class GVRInternalSensorListener implements SensorEventListener {
    public static final Quaternion COORDINATE_QUATERNION = new Quaternion(
            Math.sqrt(0.5), 0.0f, 0.0f, -Math.sqrt(0.5));
    public static final Quaternion OFFSET_QUATERNION = new Quaternion(
            Math.sqrt(0.5), 0.0f, Math.sqrt(0.5), 0.0f);

    private RotationSensor mSensor = null;

    public GVRInternalSensorListener(RotationSensor sensor) {
        mSensor = sensor;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float w;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (Build.VERSION.SDK_INT < 18) {
            w = getQuaternionW(event.values[0], event.values[1],
                    event.values[2]);
        } else {
            w = event.values[3];
        }

        Quaternion sensorQuaternion = new Quaternion(w, x, y, z);

        Quaternion quaternion = COORDINATE_QUATERNION.getInverse()
                .multiply(OFFSET_QUATERNION).multiply(sensorQuaternion)
                .multiply(COORDINATE_QUATERNION);

        mSensor.onInternalRotationSensor(GVRTime.getCurrentTime(),
                (float) quaternion.getQ0(), (float) quaternion.getQ1(),
                (float) quaternion.getQ2(), (float) quaternion.getQ3(), 0.0f,
                0.0f, 0.0f);
    }

    /**
     * Finds the missing value. Seems to lose a degree of freedom, but it
     * doesn't. That degree of freedom is already lost by the sensor.
     */
    private float getQuaternionW(float x, float y, float z) {
        return (float) Math.cos(Math.asin(Math.sqrt(x * x + y * y + z * z)));
    }
}
