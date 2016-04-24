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

import java.util.Arrays;

import org.gearvrf.utility.TextFile;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import android.util.Log;

/**
 * Illuminates object in the scene with a directional light source
 * that casts shadows on scene objects.
 * 
 * The direction of the light is the forward orientation of the scene object
 * the light is attached to. Light is emitted in that direction
 * from infinitely far away.
 *
 * The intensity of the light remains constant and does not fall
 * off with distance from the light.
 *
 * GearVRF automatically calculates a shadow map from the point
 * of view of this light and employs it during rendering.
 *
 * Direct shadow light uniforms:
 * {@literal
 *   enabled               1 = light is enabled, 0 = light is disabled
 *   world_direction       direction of light in world coordinates
 *                         derived from scene object orientation
 *   ambient_intensity     intensity of ambient light emitted
 *   diffuse_intensity     intensity of diffuse light emitted
 *   specular_intensity    intensity of specular light emitted
 *   shadow_matrix		   shadow mapping matrix
 * }
 * @see GVRDirectLight
 * @see GVRLightBase
 */
public class GVRDirectShadowLight extends GVRDirectLight {
    private static String shaderSource = null;
    
    public GVRDirectShadowLight(GVRContext gvrContext) {
        this(gvrContext, null);
     }

    public GVRDirectShadowLight(GVRContext gvrContext, GVRSceneObject parent) {
        super(gvrContext, parent);
        uniformDescriptor += " float shadow_map_index mat4 shadow_matrix";
         if (shaderSource == null) {
            shaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.directshadowlight);
        }
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setFloat("shadow_map_index", -1.0f);
        fragmentShaderSource = shaderSource;
        castShadow(gvrContext);
    }
    
    /**
     * Updates the position, direction and shadow matrix
     * of this light from the transform of scene object that owns it.
     * The shadow matrix is the model/view/projection matrix
     * from the point of view of the light.
     */
    public void onDrawFrame(float frameTime)
    {
        GVRSceneObject parent = owner;
        float[] odir = getVec3("world_direction");
        float[] opos = getVec3("world_position");
        boolean changed = false;
        Matrix4f worldmtx = parent.getTransform().getModelMatrix4f();
        Matrix4f lightrot = new Matrix4f();
        Vector3f olddir = new Vector3f(odir[0], odir[1], odir[2]);
        Vector3f oldpos = new Vector3f(opos[0], opos[1], opos[2]);
        Vector3f newdir = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f newpos = new Vector3f();
       
        defaultDir.get(lightrot);
        worldmtx.getTranslation(newpos);
        worldmtx.mul(lightrot);
        worldmtx.transformDirection(newdir);
         if ((olddir.x != newdir.x) || (olddir.y != newdir.y) || (olddir.z != newdir.z))
        {
            changed = true;
            setVec3("world_direction", newdir.x, newdir.y, newdir.z);
        }
        if ((oldpos.x != newpos.x) || (oldpos.y != newpos.y) || (oldpos.z != newpos.z))
        {
            changed = true;
            setVec3("world_position", newpos.x, newpos.y, newpos.z);
        }
        if(changed){

            Matrix4f proj = new Matrix4f();
            proj.perspective((float)Math.toRadians(100), 1, 0.1f, 1000);
            worldmtx.invert();
            proj.mul(worldmtx);
            setMat4("shadow_matrix", proj);
        }
    }
}
