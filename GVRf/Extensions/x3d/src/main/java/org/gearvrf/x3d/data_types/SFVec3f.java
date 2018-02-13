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

import org.joml.Vector3f;

/**
 * Defines the X3D SFVec3f data type
 */
public class SFVec3f extends Vector3f {

    public SFVec3f() {
        set(0, 0, 0);
    }

    public SFVec3f(float x, float y, float z) {
        set(x, y, z);
    }

    public void setValue(float[] vec) {
        set(vec[0], vec[1], vec[2]);
    }

    public void setValue(float x, float y, float z) {
        set(x, y, z);
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

    public float[] getValue() {
        float[] vec = {x, y, z};
        return vec;
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


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.x);
        buf.append(' ');
        buf.append(this.y);
        buf.append(' ');
        buf.append(this.z);
        return buf.toString();
    }

}



