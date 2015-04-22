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

/** GVRF version strings. */
public class GVRVersion {
    /** Final HQ version. */
    public static final String V_1_5_0 = "1.5.0";

    /**
     * San Jose version, with a cleaner package structure.
     * 
     * Future versions will be backward compatible with 1.6.0.
     */
    public static final String V_1_6_0 = "1.6.0";

    /** Adds asynchronous texture loading */
    public static final String V_1_6_1 = "1.6.1";

    /** Adds asynchronous mesh loading */
    public static final String V_1_6_2 = "1.6.2";

    /** Adds float point textures, and texture updating */
    public static final String V_1_6_3 = "1.6.3";

    /** Splash screen */
    public static final String V_1_6_4 = "1.6.4";

    /**
     * Remove deprecated loadMesh() methods; add blocking
     * {@link GVRContext#loadTexture(GVRAndroidResource)} method.
     */
    public static final String V_1_6_5 = "1.6.5";

    /**
     * Add high-level
     * {@link GVRPicker#findObjects(GVRScene, float, float, float, float, float, float)}
     * method.
     */
    public static final String V_1_6_6 = "1.6.6";

    /** Simpler asynch */
    public static final String V_1_6_7 = "1.6.7";

    public static final String CURRENT = V_1_6_7;
}
