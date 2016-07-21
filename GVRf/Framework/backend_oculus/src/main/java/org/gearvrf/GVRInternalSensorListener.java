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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;

import org.joml.Quaternionf;

/** A listener for a TYPE_ROTATION_VECTOR type sensor. */
class GVRInternalSensorListener implements SensorEventListener {
    private static final float SQRT_OF_HALF = (float)Math.sqrt(0.5);
    private static final Quaternionf COORDINATE_QUATERNION = new Quaternionf(0.0f, 0.0f, -SQRT_OF_HALF, SQRT_OF_HALF);
    private static final Quaternionf OFFSET_QUATERNION = new Quaternionf(0.0f, SQRT_OF_HALF, 0.0f, SQRT_OF_HALF);
    private static final Quaternionf CONSTANT_EXPRESSION = new Quaternionf().set(COORDINATE_QUATERNION).invert()
            .mul(OFFSET_QUATERNION);

    private RotationSensor mSensor;
    private final Quaternionf mQuaternion = new Quaternionf(); 

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
            w = getQuaternionW(event.values[0], event.values[1], event.values[2]);
        } else {
            w = event.values[3];
        }

        mQuaternion.set(x, y, z, w);
        CONSTANT_EXPRESSION.mul(mQuaternion, mQuaternion);
        mQuaternion.mul(COORDINATE_QUATERNION);

        mSensor.onInternalRotationSensor(GVRTime.getCurrentTime(), mQuaternion.w, mQuaternion.x, mQuaternion.y,
                mQuaternion.z, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Finds the missing value. Seems to lose a degree of freedom, but it
     * doesn't. That degree of freedom is already lost by the sensor.
     */
    private float getQuaternionW(float x, float y, float z) {
        return (float) Math.cos(Math.asin(Math.sqrt(x * x + y * y + z * z)));
    }
}
