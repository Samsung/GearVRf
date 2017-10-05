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

/**
 * Defines the X3D SFColor data type
 */
public class SFColor {

    private float red = 0;
    private float green = 0;
    private float blue = 0;

    public SFColor() {
    }

    public SFColor(float red, float green, float blue) {
        float[] color = {red, green, blue};
        setValue(color);
    }

    public void setValue(float[] color) {
        red = color[0];
        green = color[1];
        blue = color[2];
    }

    public void setValue(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setValue(SFColor color) {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
    }

    public float[] getValue() {
        float[] color = {this.red, this.green, this.blue};
        return color;
    }

    public float getBlue() {
        return blue;
    }

    public float getGreen() {
        return green;
    }

    public float getRed() {
        return red;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setRed(float red) {
        this.red = red;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(red);
        buf.append(' ');
        buf.append(green);
        buf.append(' ');
        buf.append(blue);
        return buf.toString();
    }

}



