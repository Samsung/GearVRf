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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial.GVRShaderType;

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
public class GVRShaderTemplate
{
    protected class ShaderVariant
    {
        String  FragmentShaderSource;
        String  VertexShaderSource;
        GVRCustomMaterialShaderId ShaderID;
    };


    public GVRShaderTemplate(String descriptor)
    {
        mUniformDescriptor = descriptor;
        mShaderSegments = new HashMap<String, String>();
    }

    /**
     * Get the designated shader segment.
     * 
     * @param name string name of shader segment
     * @return source code for segment or null if none exists.
     * {@link setSegment}
     */
    public String getSegment(String name)
    {
        return mShaderSegments.get(name);
    }

    /**
     * Attach a named shader segment.
     * 
     * Shader segment names should start with either "Vertex" or
     * "Fragment" to designate which type of shader the code
     * belongs with. Both vertex and fragment shaders can have
     * more than one code segment contribute to the final shader.
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
     * Get the string describing the shader uniforms.
     * 
     * Each uniform is a fixed number of integer or float values.
     * It is described with the type ("int" or "float") immediately
     * followed by the size (a small integer) a space and then the
     * name of uniform in the shader (e.g. "int enabled, float3 color")
     * Spaces, commas, and other punctuation are ignored.
     * 
     * @return String with descriptor.
     * {@link GVRLight.getUniformDescriptor }
     */
    public String getUniformDescriptor() {
        return mUniformDescriptor;
    }
    
    /**
     * Create a unique signature for this shader variant.
     * 
     * @param defined   list of names to be defined for this shader
     * @return string signature for shader
     */
    public String generateSignature(Set<String> defined, GVRLight[] lightlist)
    {
        String sig = getClass().getSimpleName() + ":";

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
    private Set<String> generateVariantDefines(GVRMesh mesh, GVRMaterial material)
    {
        Set<String> definedNames = new HashSet<String>();
        Set<String> texNames = material.getTextureNames();
     
         for (String name : mShaderDefines)
         {
             if (mesh.hasAttribute(name) ||
                 material.hasUniform(name) ||
                 texNames.contains(name))
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
            throw new IllegalArgumentException(type + "Template segment missing - cannot make shader");
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
                combinedSource = combinedSource.replace("@ShaderName", getClass().getSimpleName());
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
    
    public GVRMaterialShaderId bindShader(GVRContext context, GVRRenderData rdata, GVRLight[] lightlist)
    {
        GVRMesh mesh = rdata.getMesh();
        GVRMaterial material = rdata.getMaterial();
        
        if ((mesh == null) ||
            (material == null))
        {
            return null;
        }
        if (!rdata.isLightEnabled()) {
            lightlist = null;
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
            GVRMaterialShaderManager shaderManager = context.getMaterialShaderManager();
            Log.d("vertex", variant.VertexShaderSource);
            Log.d("fragment", variant.FragmentShaderSource);
            variant.ShaderID = shaderManager.addShader(variant.VertexShaderSource, variant.FragmentShaderSource);
            GVRMaterialMap materialMap = shaderManager.getShaderMap(variant.ShaderID);
            makeMaterialMap(material, materialMap);
        }
        material.setShaderType(variant.ShaderID);
        return variant.ShaderID;
    }

   
    protected void makeMaterialMap(GVRMaterial material, GVRMaterialMap map)
    {
        Pattern pattern = Pattern.compile("[ ]*([fFiI][loatn]+)([0-9]*)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Matcher matcher = pattern.matcher(mUniformDescriptor);        
        Set<String> texNames = material.getTextureNames();
        
        for (String s : texNames)
        {
            map.addTextureKey(s, s);
        }
        while (matcher.find())
        {
            String name = matcher.group(3);
            String size = matcher.group(2);
            int nfloats = 1;
            
            if (size.length() > 0) {
                nfloats = Integer.parseInt(size);
            }
            switch (nfloats)
            {
                case 1: map.addUniformFloatKey(name,  name); break;
                case 2: map.addUniformVec2Key(name, name); break;
                case 3: map.addUniformVec3Key(name, name); break;
                case 4: map.addUniformVec4Key(name, name); break;
                default:
                // TODO: throw exception
                break;
            }
         }
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
        String lightFunction = "vec3 LightPixel(Surface s) {\n" + "   vec3 color = vec3(0.0, 0.0, 0.0);\n   float enable;\n";
        String lightDefs = "";
        Map<String, Integer> classNames = new HashMap<String, Integer>();
        int lightindex = 0;
        
        for (GVRLight light : lightlist)
        {
            String lightShader = light.getShaderSource();
            String lightClassName = light.getClass().getSimpleName();
            String lightid = light.getLightID();

            
            if ((lightShader == null) || lightid.isEmpty())
            {
                continue;
            }
            Integer n = classNames.get(lightClassName);
            if (n != null)
            {
                classNames.put(lightClassName, ++n);
            }
            else
            {
                n = 0;
                classNames.put(lightClassName, 0);
                lightDefs += light.getShaderStruct();
                lightShader = lightShader.replace("@LightType", lightClassName);
                lightDefs += lightShader;
            }
            lightFunction += "   Radiance r" + lightindex + " = " + lightClassName +
                             "(s, " + lightid + ");\n";
            lightFunction += "   enable = " + lightid + ".enabled;"; 
            lightFunction += "   color += vec3(enable, enable, enable) * AddLight(s, r" + lightindex + ");\n";
            ++lightindex;
        }
        String lightSources = "";
        for (Map.Entry<String, Integer> entry : classNames.entrySet())
        {
            int n = entry.getValue() + 1;
            lightSources += "uniform Struct" + entry.getKey() + " Data" + entry.getKey() + "[" + n + "];\n";            
        }
        lightFunction += "   return color; }\n";
        return lightDefs + lightSources + lightFunction;
    }

    protected Map<String, String> mShaderSegments;
    protected Map<String, ShaderVariant> mShaderVariants;
    protected Set<String> mShaderDefines;
    protected String mUniformDescriptor;
}
