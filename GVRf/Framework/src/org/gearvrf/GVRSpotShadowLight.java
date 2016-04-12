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
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Illuminates object in the scene with a cone shaped beam
* which casts shadows on the scene objects.
* 
* The apex of the cone is at the position of the scene object
* the light is attached to. The direction of the cone is the
* forward direction of that scene object.
* 
* There are two angles for the cone. Beyond the "outer angle"
* no light is emitted. Inside the "inner angle" the light is
* at full intensity. Between the two angles the light linearly
* decreases allowing for soft edges.
* 
* The intensity of the light diminishes with distance from the
* cone apex. Three attenuation factors are provided to specify how
* the intensity of the light falls off with distance:
* {@code I = attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2}
*
* GearVRF automatically computes a shadow map for this light
* and uses it when rendering.
*
* Spot light uniforms:
* {@literal
*   enabled               1 = light is enabled, 0 = light is disabled
*   world_position        position of spot light in world coordinates
*                         derived from scene object position
*   world_direction       direction of spot light in world coordinates
*                         derived from scene object orientation
*   ambient_intensity     intensity of ambient light emitted
*   diffuse_intensity     intensity of diffuse light emitted
*   specular_intensity    intensity of specular light emitted
*   attenuation_constant  constant attenuation factor
*   attenuation_linear    linear attenuation factor
*   attenuation_quadratic quadratic attenuation factor
*   inner_cone_angle      cosine of inner cone angle
*   outer_cone_angle      cosine of outer cone angle
*   shadow_matrix         shadow mapping matrix
* }
* 
* @see GVRPointLight
* @see GVRLightBase
*/
public class GVRSpotShadowLight extends GVRSpotLight {
    private static String shaderSource = null;
    
    public GVRSpotShadowLight(GVRContext gvrContext) {
        this(gvrContext, null);
     }

    public GVRSpotShadowLight(GVRContext gvrContext, GVRSceneObject parent) {
        super(gvrContext, parent);
        uniformDescriptor += " float has_shadow_map mat4 shadow_matrix";
         if (shaderSource == null) {
            shaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotshadowlight);
        }
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setFloat("has_shadow_map", 0);
        setShaderSource(shaderSource);
        NativeLight.castShadow(getNative(), true);
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
        Matrix4f worldmtx = parent.getTransform().getModelMatrix4f();
        Matrix4f lightrot = new Matrix4f();
        Vector3f olddir = new Vector3f(odir[0], odir[1], odir[2]);
        Vector3f oldpos = new Vector3f(opos[0], opos[1], opos[2]);
        Vector3f newdir = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f newpos = new Vector3f();

        defaultDir.get(lightrot);
        worldmtx.getTranslation(newpos);
        worldmtx.mul(lightrot);
        worldmtx.transformDirection(newdir);
        if ((olddir.x != newdir.x) || (olddir.y != newdir.y) || (olddir.z != newdir.z))
        {
            setVec3("world_direction", newdir.x, newdir.y, newdir.z);
        }
        if ((oldpos.x != newpos.x) || (oldpos.y != newpos.y) || (oldpos.z != newpos.z))
        {
            setVec3("world_position", newpos.x, newpos.y, newpos.z);
        }
        worldmtx.lookAt(newpos, newdir, new Vector3f(0, 1, 0));
        worldmtx.perspective(getOuterConeAngle(), 1, 0.1f, 1000.0f);
        setMat4("shadow_matrix", worldmtx);
    }
}

