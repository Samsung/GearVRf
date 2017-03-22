/* Copyright 2016 Samsung Electronics Co., LTD
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
package org.gearvrf.x3d.data_types;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


/**
 * Defines the X3D SFRotation data type in AxisAngle format
 * Angles in radians
 */
public class SFRotation extends AxisAngle4f {

    public SFRotation() {
        set(0, 0, 1, 0); // angle, x, y, z
    }

    public SFRotation(float x, float y, float z, float angle) {
        set(angle, x, y, z);
    }

    public SFRotation(SFVec3f axis, float angle) {
        set(angle, axis.x, axis.y, axis.z);
    }

    public SFRotation(SFVec3f axis, double angle) {
        set( (float)angle, axis.x, axis.y, axis.z);
    }

    public SFRotation(SFVec3f fromVector, SFVec3f toVector) {
        fromVector.normalize();
        toVector.normalize();
        float angle = fromVector.dot(toVector);
        Vector3f axis = fromVector.cross(toVector);
        this.set(angle, axis.x, axis.y, axis.z);
    }

    /**
     * @param axisAngle
     */
    public void setValue(float[] axisAngle) {
        set(axisAngle[3], axisAngle[0], axisAngle[1], axisAngle[2]);
    }

    /**
     * x, y, z should be a unit value such that
     * x*x + y*y + z*z = 1;
     * angle is in radians
     *
     * @param angle
     * @param x
     * @param y
     * @param z
     */
    public void setValue(float angle, float x, float y, float z) {
        set(angle, x, y, z);
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    /**
     * angle in radians
     *
     * @return
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * returns float[4] x, y, z, angle in radians
     *
     * @return
     */
    public float[] getValue() {
        float[] axisAngle = new float[4];
        axisAngle[0] = this.x;
        axisAngle[1] = this.y;
        axisAngle[2] = this.z;
        axisAngle[3] = this.angle;
        return axisAngle;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    /**
     * angle in radians
     *
     * @return
     */
    public float getAngle() {
        return this.angle;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.x);
        buf.append(' ');
        buf.append(this.y);
        buf.append(' ');
        buf.append(this.z);
        buf.append(' ');
        buf.append(this.angle);
        return buf.toString();
    }

}



