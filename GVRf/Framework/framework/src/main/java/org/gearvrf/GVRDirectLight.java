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

import static org.gearvrf.utility.Assert.*;

import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.utility.TextFile;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Illuminates object in the scene with a directional light source.
 * 
 * The direction of the light is the forward orientation of the scene object
 * the light is attached to. Light is emitted in that direction
 * from infinitely far away.
 *
 * The intensity of the light remains constant and does not fall
 * off with distance from the light.
 *
 * Dlrect light uniforms:
 * {@literal
 *   world_direction       direction of light in world coordinates
 *                         derived from scene object orientation
 *   ambient_intensity     intensity of ambient light emitted
 *   diffuse_intensity     intensity of diffuse light emitted
 *   specular_intensity    intensity of specular light emitted
 *   sm0                   shadow matrix column 1
 *   sm1                   shadow matrix column 2
 *   sm2                   shadow matrix column 3
 *   sm3                   shadow matrix column 4
 * }
 * 
 * Note: some mobile GPU drivers do not correctly pass a mat4 thru so we currently
 * use 4 vec4's instead.
 * 
 * @see GVRPointLight
 * @see GVRSpotLight
 * @see GVRLightBase
 */
public class GVRDirectLight extends GVRLightBase {
    private static String fragmentShader = null;
    private static String vertexShader = null;
    private boolean useShadowShader = true;
    private Matrix4f biasMatrix = null;
    
    public GVRDirectLight(GVRContext gvrContext) {
        this(gvrContext, null);
     }

    public GVRDirectLight(GVRContext gvrContext, GVRSceneObject parent) {
        super(gvrContext, parent);
        uniformDescriptor += " vec4 diffuse_intensity"
                + " vec4 ambient_intensity"
                + " vec4 specular_intensity"
                + " float shadow_map_index"
                + " vec4 sm0 vec4 sm1 vec4 sm2 vec4 sm3";
         if (useShadowShader)
         {
             if (fragmentShader == null)
                 fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.directshadowlight);
             if (vertexShader == null)
                 vertexShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.vertex_shadow);
             vertexDescriptor = "vec4 shadow_position";
             vertexShaderSource = vertexShader;
             setFloat("shadow_map_index", -1.0f);
         }
         else if (fragmentShader == null)
             fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.directlight);             
         fragmentShaderSource = fragmentShader;
         setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
         setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
         setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * Get the ambient light intensity.
     * 
     * This designates the color of the ambient reflection.
     * It is multiplied by the material ambient color to derive
     * the hue of the ambient reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code ambient_intensity} to control the intensity of ambient light reflected.
     * 
     * @return The current {@code vec4 ambient_intensity} as a four-element array
     */
    public float[] getAmbientIntensity() {
        return getVec4("ambient_intensity");
    }

    /**
     * Set the ambient light intensity.
     * 
     * This designates the color of the ambient reflection.
     * It is multiplied by the material ambient color to derive
     * the hue of the ambient reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code ambient_intensity} to control the intensity of ambient light reflected.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setAmbientIntensity(float r, float g, float b, float a) {
        setVec4("ambient_intensity", r, g, b, a);
    }

    /**
     * Get the diffuse light intensity.
     * 
     * This designates the color of the diffuse reflection.
     * It is multiplied by the material diffuse color to derive
     * the hue of the diffuse reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code diffuse_intensity} to control the intensity of diffuse light reflected.
     * 
     * @return The current {@code vec4 diffuse_intensity} as a four-element
     *         array
     */
    public float[] getDiffuseIntensity() {
        return getVec4("diffuse_intensity");
    }

    /**
     * Set the diffuse light intensity.
     * 
     * This designates the color of the diffuse reflection.
     * It is multiplied by the material diffuse color to derive
     * the hue of the diffuse reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code diffuse_intensity} to control the intensity of diffuse light reflected.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setDiffuseIntensity(float r, float g, float b, float a) {
        setVec4("diffuse_intensity", r, g, b, a);
    }

    /**
     * Get the specular intensity of the light.
     *
     * This designates the color of the specular reflection.
     * It is multiplied by the material specular color to derive
     * the hue of the specular reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code specular_intensity} to control the specular intensity.
     *
     * @return The current {@code vec4 specular_intensity} as a four-element array
     */
    public float[] getSpecularIntensity() {
        return getVec4("specular_intensity");
    }

    /**
     * Set the specular intensity of the light.
     * 
     * This designates the color of the specular reflection.
     * It is multiplied by the material specular color to derive
     * the hue of the specular reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
     * {@code specular_intensity} to control the specular intensity.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setSpecularIntensity(float r, float g, float b, float a) {
        setVec4("specular_intensity", r, g, b, a);
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
        olddir.x = odir[0];
        olddir.y = odir[1];
        olddir.z = odir[2];
        
        oldpos.x = opos[0];
        oldpos.y = opos[1];
        oldpos.z = opos[2];

        newdir.x = 0.0f;
        newdir.y = 0.0f;
        newdir.z = -1.0f;
        
        lightrot.identity();
       
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
        if (getCastShadow() && changed)
        {
            Matrix4f proj = new Matrix4f();
            Vector4f v = new Vector4f();
            if (biasMatrix == null)
            {
                biasMatrix = new Matrix4f();
                biasMatrix.scale(0.5f);
                biasMatrix.setTranslation(0.5f, 0.5f, 0.5f);
            }
            proj.perspective((float) Math.toRadians(100), 1, 0.1f, 1000);
            setMat4("projMatrix", proj);
            biasMatrix.mul(proj, proj);
            worldmtx.invert();
            proj.mul(worldmtx);
            proj.getColumn(0, v);
            setVec4("sm0", v.x, v.y, v.z, v.w);
            proj.getColumn(1, v);
            setVec4("sm1", v.x, v.y, v.z, v.w);
            proj.getColumn(2, v);
            setVec4("sm2", v.x, v.y, v.z, v.w);
            proj.getColumn(3, v);
            setVec4("sm3", v.x, v.y, v.z, v.w);
        }
    }
}
