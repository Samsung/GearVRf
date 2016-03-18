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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.GVRLight;

import android.util.Log;
import android.util.Pair;

/**
 * Manages a set of variants on vertex and fragment shaders from the same source
 * code segments.
 *
 * A shader source code segment is represented as a GVRShaderSegment class.
 * 
 * The GVRShaderSegment attached in the "FragmentTemplate" slot is the master
 * template for the fragment shader, the one attached as "VertexTemplate" is
 * the master for the vertex shader. Any name in the fragment template that
 * starts with "@Fragment" is replaced by the shader segment attached in
 * that slot. Similarly, names in the vertex template that start with
 * "@Vertex" are replaced with vertex shader segments. This permits
 * multiple segments to be combined into a single fragment or vertex shader.
 * The segment names are also #defined in the shader for conditional compilation.
 * 
 * To generate different variants from the same source we use #ifdef creatively.
 * Any shader variable name starting with a "HAS_" is assumed to be a #ifdef
 * that affects the generated code. These names typically correspond to
 * uniforms, textures or vertex buffer attributes. If these parameters
 * are defined by the material or mesh, that name will be #defined to 1
 * in the shader. Otherwise, it will not be defined.
 * 
 * Each shader variant generated has a unique signature so that the
 * same variant will not be made twice.
 *
 *{@link GVRShaderSegment GVRPhongShader}
 */
public class GVRShader
{
    protected class ShaderVariant
    {
        String  FragmentShaderSource;
        String  VertexShaderSource;
        GVRMaterialShaderId ShaderID;
    };


    public GVRShader(String name)
    {
        mName = name;
        mShaderSegments = new HashMap<String, String>();
    }

    /**
     * Get the designated shader segment.
     * 
     * @param name string name of shader segment
     * @return source code for segment or null if none exists.
     */
    public String getSegment(String name)
    {
        return mShaderSegments.get(name);
    }

    /**
     * Attach a named shader segment.
     * 
     * @param segmentName  name associated with shader segment
     * @param shaderSource String with shader source code
     */
    public void setSegment(String segmentName, String shaderSource)
    {
        mShaderSegments.put(segmentName, shaderSource);
        if (shaderSource == null)
            return;
        Pattern pattern = Pattern.compile(".*HAS_([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(shaderSource);
        if (mShaderDefines == null)
            mShaderDefines = new HashSet<String>();             
        while (matcher.find())
        {
            mShaderDefines.add(matcher.group(1));
        }
    }

    /**
     * Create a unique signature for this shader variant.
     * 
     * @param defined   list of names to be defined for this shader
     * @return string signature for shader
     */
    public String generateSignature(Set<String> defined, GVRLight[] lightlist)
    {
        String sig = mName + ":";

        for (String key : defined)
        {
            sig += ":" + key;
        }
        if (lightlist != null)
        {
            for (GVRLight light : lightlist)
            {
                sig += ":" + light.getClass().getSimpleName();
            }
        }
        return sig;
    }

    /**
     * Generates the set of unique parameter names that make
     * a particular variant of the shader from the source template.
     * Wherever the source template contains "HAS_" followed by a
     * name of a uniform, texture or attribute used in the material
     * or mesh, a "#define" for that name is generated.
     * 
     * @param variantDefines set of names which are used by material & mesh.
     * @param sourceTemplate source code template for shader.
     * @return set of shader variable names actually defined by the material and mesh
     */
    public Set<String> generateVariantDefines(GVRMesh mesh, GVRMaterial material)
    {
        Set<String> definedNames = new HashSet<String>();
         for (String name : mShaderDefines)
         {
             if (mesh.hasAttribute(name) ||
                material.hasUniform(name) ||
                material.hasTexture(name))
                {
                    definedNames.add(name);
                }
         }
         return definedNames;
    }
    
    /**
     * Construct the source code for a GL shader based on the input defines.
     * The shader segments attached to slots that start with <type> are
     * combined to form the GL shader. #define statements are added
     * to define compile-time constants to control the code generated.
     * 
     * @param type "Fragment" or "Vertex" indicating shader type.
     * @param definedNames set of names to define for this shader.
     * @return GL shader code with parameters substituted.
     */
    public String generateShaderVariant(String type, Set<String> definedNames, GVRLight[] lightlist)
    {
        String template = getSegment(type + "Template");
        int nsegs = 0;
        String defines = "";
       
        if (template == null)
        {
            // TODO: error vertex template missing
            return null;
        }
        String combinedSource = template;
        for (Map.Entry<String, String> entry : mShaderSegments.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(type))
            {
                String lightShaderSource = "";
                String segmentSource = entry.getValue();
                
                if (segmentSource == null)
                    segmentSource = "";
                if ((lightlist != null) && (lightlist.length > 0))
                {
                    lightShaderSource = generateLightShader(lightlist);
                    defines += "#define HAS_LIGHTSOURCES 1\n";
                }
                combinedSource = combinedSource.replace("@" + key, segmentSource);
                combinedSource = combinedSource.replace("@ShaderName", mName);
                combinedSource = combinedSource.replace("@LIGHTSOURCES", lightShaderSource);
                ++nsegs;
            }
        }
         for (String name : definedNames)
        {
            defines += "#define HAS_" + name + " 1\n";
        }
        return defines + combinedSource;
    }
    
    public GVRMaterialShaderId bindShader(GVRContext ctx, GVRRenderData rdata, GVRLight[] lightlist)
    {
        GVRMesh mesh = rdata.getMesh();
        GVRMaterial material = rdata.getMaterial();
        if ((mesh == null) ||
            (material == null))
        {
            return null;
        }
        Set<String> variantDefines = generateVariantDefines(mesh, material);
        String signature = generateSignature(variantDefines, lightlist);
        if (mShaderVariants == null)
            mShaderVariants = new HashMap<String, ShaderVariant>();
        ShaderVariant variant = mShaderVariants.get(signature);
        if (variant == null)
        {
            variant = new ShaderVariant();
            variant.VertexShaderSource = generateShaderVariant("Vertex", variantDefines, null);
            variant.FragmentShaderSource = generateShaderVariant("Fragment", variantDefines, lightlist);
            mShaderVariants.put(signature, variant);
            GVRMaterialShaderManager shaderManager = ctx.getMaterialShaderManager();
            Log.d("vertex", variant.VertexShaderSource);
            Log.d("fragment", variant.FragmentShaderSource);
            variant.ShaderID = shaderManager.addShader(variant.VertexShaderSource, variant.FragmentShaderSource);
        }
        material.setShaderType(variant.ShaderID);
        return variant.ShaderID;
    }
    
    /**
     * Generates the shader code to compute lighting for each light source.
     * The shader defines a <LightPixel> function which computes the effect
     * of all light sources on the fragment color. This function calls
     * the <AddLight> function which integrates the light sources. It
     * must be defined by the fragment shader template.
     * 
     * @param lightlist
     *            list of lights in the scene
     * @return string with shader source code for lighting
     */
    protected String generateLightShader(GVRLight[] lightlist)
    {
        String lightSources = "";
        String lightFunction = "vec3 LightPixel(Surface s) {\n" + "   vec3 color = vec3(0.0, 0.0, 0.0);\n";
        String lightDefs = "";
        Integer lightid = 0;

        for (GVRLight light : lightlist)
        {
            String lightShader = light.getShaderSource();
            String lightClassName = light.getClass().getSimpleName();
            if (lightShader != null)
            {
                lightSources += "uniform Data" + lightClassName + " light" + lightid.toString() + ";\n";
                lightFunction += "   Radiance r" + lightid.toString() + " = " + lightClassName + ";\n";
                lightFunction += "   color =+ AddLight(s, r" + lightid.toString() + ");\n";
                lightShader = lightShader.replace("@LightType", lightClassName);
                lightDefs += lightShader;
            }
            ++lightid;
        }
        lightFunction += "   return color; }\n";
        return lightDefs + lightSources + lightFunction;
    }

    protected String mName;
    protected Map<String, String> mShaderSegments;
    protected Map<String, ShaderVariant> mShaderVariants;
    protected Set<String> mShaderDefines;
}
