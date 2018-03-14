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

import org.joml.Vector2f;

/**
 * Defines the X3D SFVec2f data type
 */
public class SFVec2f extends Vector2f {

    public SFVec2f() {
        set(0, 0);
    }

    /**
     * Assign a new value to this field.
     * Warning: newValue array length must correspond to tuple size for base type SFVec2f tuple size of 2.
     * @param x
     * @param y
     */
    public SFVec2f(float x, float y) {
        set(x, y);
    }

    /**
     * Assign a new value to this field.
     * Warning: newValue array length must correspond to tuple size for base type SFVec2f tuple size of 2.
     * @param newValue  is replacement value array to assign
     */
    public void setValue(float[] newValue) {
        set(newValue[0], newValue[1]);
    }

    public void setValue(SFVec2f newValue) {
        set(newValue.getX(), newValue.getY());
    }

    public void setValue(float x, float y) {
        set(x, y);
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination The array to be filled in with current field values.
     */
    public void getValue(float[] valueDestination) {
        valueDestination[0] = this.getX();
        valueDestination[1] = this.getY();
    }

    public float[] getValue() {
        float[] vec = {x, y};
        return vec;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.x);
        buf.append(' ');
        buf.append(this.y);
        return buf.toString();
    }

}



