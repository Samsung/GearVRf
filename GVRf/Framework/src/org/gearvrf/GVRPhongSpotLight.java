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

import org.gearvrf.utility.TextFile;

public class GVRPhongSpotLight extends GVRPhongLight
{
    protected static String mSpotLightShaderSource = null;
    public GVRPhongSpotLight(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, owner);
        uniformDescriptor += " float inner_cone_angle; float outer_cone_angle; ";
        if (mSpotLightShaderSource == null) {
            mSpotLightShaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotlight);
        }
        setShaderSource(mSpotLightShaderSource);
        setFloat("inner_cone_angle", 90.0f);
        setFloat("outer_cone_angle", 90.0f);
    }
    
    public GVRPhongSpotLight(GVRContext gvrContext) {
        this(gvrContext, null);
    }

}
