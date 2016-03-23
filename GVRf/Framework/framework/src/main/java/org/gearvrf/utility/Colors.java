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

package org.gearvrf.utility;

import android.graphics.Color;

/**
 * RGB <--> GL utilities.
 * 
 * <p>
 * Please note that the {@code org.gearvrf.utility} package contains low-level
 * utility code used in multiple GVRF packages. We can't keep you from using
 * this code in a GVRF application, but we <em>can</em> urge you not to:
 * everything in this package is minimally documented, internal code. We are not
 * making any promises about the behavior or performance of any code in this
 * package; this package is not part of the GVRF API, and future releases may
 * change or remove classes or methods without providing for backward
 * compatibility.
 */
public abstract class Colors {

    public static int glToByte(float zeroToOne) {
        return (int) (zeroToOne * 255f);
    }

    public static float byteToGl(int zeroToFF) {
        return zeroToFF / 255f;
    }

    public static int toColor(float[] rgb) {
        int red = glToByte(rgb[0]);
        int green = glToByte(rgb[1]);
        int blue = glToByte(rgb[2]);
        return Color.rgb(red, green, blue);
    }

    public static float[] toColors(int color) {
        final float red = byteToGl(Color.red(color));
        final float green = byteToGl(Color.green(color));
        final float blue = byteToGl(Color.blue(color));
        return new float[] { red, green, blue };
    }

}
