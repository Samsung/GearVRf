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

import org.gearvrf.GVRLightBase;

import android.util.Log;

/**
 * Generates a set of vertex and fragment shaders from the same source template.
 *
 * Each shader template keeps a set of named source code segments which are used
 * to compose the shaders. The code in the "FragmentTemplate" slot is the master
 * template for the fragment shader, the one attached as "VertexTemplate" is the
 * master for the vertex shader. Any name in the fragment template that starts
 * with "@Fragment" is replaced by the shader segment attached in that slot.
 * Similarly, names in the vertex template that start with "@Vertex" are
 * replaced with vertex shader segments. This permits multiple segments to be
 * combined into a single fragment or vertex shader. The segment names are also
 * #defined in the shader for conditional compilation.
 * 
 * To generate different variants from the same source we use #ifdef creatively.
 * Any shader variable name starting with a "HAS_" is assumed to be a #ifdef
 * that affects the generated code. These names typically correspond to
 * uniforms, textures or vertex buffer attributes. If these parameters are
 * defined by the material or mesh, that name will be #defined to 1 in the
 * shader. Otherwise, it will not be defined.
 * 
 * Each shader variant generated has a unique signature so that the same variant
 * will not be generated twice.
 *
 * The shader template also defines a uniform descriptor which contains the
 * names and types of all the uniforms in the material used by the shader. Each
 * entry is a float or integer type, an optional count and the name of the
 * uniform (e.g. "float3 diffuse_color, float specular_exponent, int is_enabled"
 * 
 * Multiple lights are supported by specifying light shader source code segments
 * in GVRLightBase. You can define different light implementations with
 * their own data structures and these will be included in the generated
 * fragment shader.
 * 
 * @see GVRPhongShader
 * @see GVRLightBase
 */
public class GVRShaderTemplate
{
    protected class ShaderVariant
    {
        String FragmentShaderSource;
        String VertexShaderSource;
        GVRCustomMaterialShaderId ShaderID;
    };

    /**
     * Construct a shader template
     * 
     * @param descriptor
     *            string describing uniform names and types
     */
    public GVRShaderTemplate(String descriptor)
    {
        mUniformDescriptor = descriptor;
        mShaderSegments = new HashMap<String, String>();
    }

    /**
     * Get the designated shader segment.
     * 
     * @param name
     *            string name of shader segment
     * @return source code for segment or null if none exists.
     *         {@link setSegment}
     */
    public String getSegment(String name)
    {
        return mShaderSegments.get(name);
    }

    /**
     * Attach a named shader segment.
     * 
     * Shader segment names should start with either "Vertex" or "Fragment" to
     * designate which type of shader the code belongs with. Both vertex and
     * fragment shaders can have more than one code segment contribute to the
     * final shader.
     * 
     * @param segmentName
     *            name associated with shader segment
     * @param shaderSource
     *            String with shader source code
     */
    public void setSegment(String segmentName, String shaderSource)
    {
        mShaderSegments.put(segmentName, shaderSource);
        if (shaderSource == null) return;
        Pattern pattern = Pattern.compile("HAS_([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(shaderSource);
        if (mShaderDefines == null) mShaderDefines = new HashSet<String>();
        int index = 0;
        while (((index = shaderSource.indexOf("HAS_", index)) >= 0) && matcher.find(index))
        {
            String match = matcher.group(1);
            mShaderDefines.add(match);
            index = matcher.end();
        }
    }

    /**
     * Get the string describing the shader uniforms.
     * 
     * Each uniform is a fixed number of integer or float values. It is
     * described with the type ("int" or "float") immediately followed by the
     * size (a small integer) a space and then the name of uniform in the shader
     * (e.g. "int enabled, float3 color") Spaces, commas, and other punctuation
     * are ignored.
     * 
     * @return String with descriptor.
     *         {@link GVRLightBase.getUniformDescriptor }
     */
    public String getUniformDescriptor()
    {
        return mUniformDescriptor;
    }

    /**
     * Create a unique signature for this shader variant.
     * 
     * @param defined
     *            names to be defined for this shader
     * @return string signature for shader
     */
    public String generateSignature(HashMap<String, Integer> defined, GVRLightBase[] lightlist)
    {
        String sig = getClass().getSimpleName() + ":";
        HashMap<Class<? extends GVRLightBase>, Integer> lightCount = new HashMap<Class<? extends GVRLightBase>, Integer>();

        for (HashMap.Entry<String, Integer> entry : defined.entrySet())
        {
            if (entry.getValue() != 0)
                sig += ":" + entry.getKey();
        }
        if (lightlist != null)
        {
            for (GVRLightBase light : lightlist)
            {
                Integer n = lightCount.get(light.getClass());

                if (n == null)
                    lightCount.put(light.getClass(), 1);
                else
                    lightCount.put(light.getClass(), ++n);
            }
            for (Map.Entry<Class<? extends GVRLightBase>, Integer> entry : lightCount.entrySet())
                sig += ":" + entry.getKey().getSimpleName() + entry.getValue().toString();
        }
        return sig;
    }

    /**
     * Generates the set of unique parameter names that make a particular
     * variant of the shader from the source template. Wherever the source
     * template contains "HAS_" followed by a name of a uniform, texture or
     * attribute used in the material or mesh, a "#define" for that name is
     * generated.
     * 
     * @param variantDefines
     *            set of names which are used by material & mesh.
     * @param sourceTemplate
     *            source code template for shader.
     * @return shader variable names actually defined by the material and mesh
     */
    private void generateVariantDefines(HashMap<String, Integer> definedNames, GVRMesh mesh, GVRMaterial material)
    {
        Set<String> texNames = material.getTextureNames();
        Set<String> vertNames = mesh.getAttributeNames();

        for (String name : mShaderDefines)
        {
            if (definedNames.containsKey(name))
                continue;
            if (vertNames.contains(name) || material.hasUniform(name) || texNames.contains(name))
                definedNames.put(name, 1);
        }
    }

    /**
     * Construct the source code for a GL shader based on the input defines. The
     * shader segments attached to slots that start with <type> are combined to
     * form the GL shader. #define statements are added to define compile-time
     * constants to control the code generated.
     * 
     * @param type
     *            "Fragment" or "Vertex" indicating shader type.
     * @param definedNames
     *            set of names to define for this shader.
     * @return GL shader code with parameters substituted.
     */
    public String generateShaderVariant(String type, HashMap<String, Integer> definedNames, GVRLightBase[] lightlist)
    {
        String template = getSegment(type + "Template");
        String defines = "";

        if (template == null) { throw new IllegalArgumentException(
                type + "Template segment missing - cannot make shader"); }
        String combinedSource = template;
        for (Map.Entry<String, String> entry : mShaderSegments.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(type))
            {
                String lightShaderSource = "";
                String segmentSource = entry.getValue();
                boolean useLights = (lightlist != null) && (lightlist.length > 0);
                
                if (definedNames.containsKey("LIGHTSOURCES") &&
                    definedNames.get("LIGHTSOURCES") == 0)
                    useLights = false;
                if (segmentSource == null)
                    segmentSource = "";
                if (useLights)
                {
                    lightShaderSource = generateLightShader(lightlist);
                    defines += "#define HAS_LIGHTSOURCES 1\n";
                }
                if (!definedNames.containsKey(key) ||
                    (definedNames.get(key) != 0)) {
                    defines += "#define HAS_" + key + " 1;\n";
                }
                combinedSource = combinedSource.replace("@" + key, segmentSource);
                combinedSource = combinedSource.replace("@ShaderName", getClass().getSimpleName());
                combinedSource = combinedSource.replace("@LIGHTSOURCES", lightShaderSource);
            }
        }
        for (Map.Entry<String, Integer> entry : definedNames.entrySet())
        {
            if (entry.getValue() != 0)
                defines += "#define HAS_" + entry.getKey() + " 1\n";
        }
        return "#version 300 es\n" + defines + combinedSource;
    }

    /**
     * Select the specific vertex and fragment shader to use.
     * 
     * The shader template is used to generate the sources for the vertex and
     * fragment shader based on the vertex, material and light properties. This
     * function may compile the shader if it does not already exist.
     * 
     * @param context
     *            GVRContext
     * @param rdata
     *            GVRRenderData with mesh and material to use
     * @param lightlist
     *            list of lights illuminating the mesh
     * @param shaderDefines
     *            symbols to define or undefine when creating the shader.
     *            These are shader-specific but permit features to be enabled
     *            or disabled at run time.
     * @return ID of vertex/fragment shader set
     */
    public void bindShader(GVRContext context, GVRRenderData rdata,
                                          GVRLightBase[] lightlist)
    {
        GVRMesh mesh = rdata.getMesh();
        GVRMaterial material = rdata.getMaterial();

        if ((mesh == null) || (material == null))
        {
            return;
        }
        if (!rdata.isLightEnabled())
        {
            lightlist = null;
        }
        HashMap<String, Integer> variantDefines = getRenderDefines(rdata);
        if (mShaderVariants == null)
        {
            mShaderVariants = new HashMap<String, ShaderVariant>();
        }
        generateVariantDefines(variantDefines, mesh, material);
        String signature = generateSignature(variantDefines, lightlist);
        ShaderVariant variant = mShaderVariants.get(signature);
        if ((variant != null) && (variant.ShaderID != null))
        {
            Log.d("gvrf", "SHADER: Reuse shader #" + variant.ShaderID.ID + " " + signature);
            material.setShaderType(variant.ShaderID);
            return;
        }
        variant = new ShaderVariant();
        variant.VertexShaderSource = generateShaderVariant("Vertex", variantDefines, null);
        variant.FragmentShaderSource = generateShaderVariant("Fragment", variantDefines, lightlist);
        mShaderVariants.put(signature, variant);
        generateGLShader(context, material, signature);
    }

    /**
     * Generates a GL shader and adds it to the material.
     * 
     * The shader generation is actually performed by the GL thread.
     * This function spawns a task to generate it.
     * 
     * @param context   GVRContext to use to generate shader
     * @param variant   shader variant with sources
     * @param material  material to use with shader
     */
    private void generateGLShader(final GVRContext context, final GVRMaterial material, final String signature) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                ShaderVariant variant = mShaderVariants.get(signature);
                GVRMaterialShaderManager shaderManager = context.getMaterialShaderManager();
                if (variant.ShaderID == null)
                {
                    variant.ShaderID = shaderManager.addShader(variant.VertexShaderSource, variant.FragmentShaderSource);
                    Log.d("gvrf", "SHADER: Generated shader #" + variant.ShaderID.ID + " " + signature);
                }
                GVRMaterialMap materialMap = shaderManager.getShaderMap(variant.ShaderID);
                makeMaterialMap(material, materialMap);
                material.setShaderType(variant.ShaderID);
            }
        };
        context.runOnGlThread(task);
    }
    
    /**
     * Generate shader-specific defines from the rendering information.
     * You can override this function in your shader class to change which
     * variant is generated depending on the GVRRenderData settings.
     * 
     * @param rdata GVRRenderData being used by this shader
     * @return list of symbols to be defined (value 1) or undefined (value 0) in the shader
     */
    public HashMap<String, Integer> getRenderDefines(GVRRenderData rdata) {
        return new HashMap<String, Integer>();
    }
    
    /**
     * Makes the material map for the shader.
     * The material map does not change any names, just maps
     * the keys in the material to themselves.
     */
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

            if (size.length() > 0)
            {
                nfloats = Integer.parseInt(size);
            }
            switch (nfloats)
            {
            case 1:
                map.addUniformFloatKey(name, name);
                break;
            case 2:
                map.addUniformVec2Key(name, name);
                break;
            case 3:
                map.addUniformVec3Key(name, name);
                break;
            case 4:
                map.addUniformVec4Key(name, name);
                break;
            default:
                throw new UnsupportedOperationException("Vertex attribute size " + nfloats + " unsupported");
            }
        }
    }

    /**
     * Generates the shader code to compute lighting for each light source. The
     * shader defines a <LightPixel> function which computes the effect of all
     * light sources on the fragment color. This function calls the
     * <AddLight> function which integrates the light sources. It must be
     * defined by the fragment shader template.
     * 
     * @param lightlist
     *            list of lights in the scene
     * @return string with shader source code for lighting
     */
    protected String generateLightShader(GVRLightBase[] lightlist)
    {
        String lightFunction = "vec3 LightPixel(Surface s) {\n"
                + "   vec3 color = vec3(0.0, 0.0, 0.0);\n   float enable;\n   Radiance r;\n";
        String lightDefs = "";
        Map<String, Integer> classNames = new HashMap<String, Integer>();
        boolean doLooping = false;

        for (GVRLightBase light : lightlist)
        {
            String lightShader = light.getShaderSource();
            String lightClassName = light.getClass().getSimpleName();
            String lightid = light.getLightID();

            if ((lightShader == null) || lightid.isEmpty()) continue;
            Integer n = classNames.get(lightClassName);
            if (n != null)
                classNames.put(lightClassName, ++n);
            else
            {
                classNames.put(lightClassName, 1);
                lightDefs += light.getShaderStruct();
                lightShader = lightShader.replace("@LightType", lightClassName);
                lightDefs += lightShader;
            }
            if (!doLooping)
            {
                lightFunction += "   r = " + lightClassName + "(s, " + lightid + ");\n";
                lightFunction += "   enable = " + lightid + ".enabled;";
                lightFunction += "   color += vec3(enable, enable, enable) * AddLight(s, r);\n";
            }
        }
        String lightSources = "";
        for (Map.Entry<String, Integer> entry : classNames.entrySet())
        {
            String name = entry.getKey();
            String lightdata = "Data" + name;
            if (doLooping)
            {
                lightSources += "uniform int Count" + name + ";\n";
                lightSources += "uniform Struct" + name + " " + lightdata + "[4];\n";
                lightFunction += "    for (int i = 0; i < Count" + entry.getKey() + "; ++i)\n";
                lightFunction += "    {\n";
                lightFunction += "        r = " + name + "(s, " + lightdata + "[i]);\n";
                lightFunction += "        enable = " + lightdata + "[i].enabled;";
                lightFunction += "        color += vec3(enable, enable, enable) * AddLight(s, r);\n";
                lightFunction += "    };\n";
            }
            else
            {
                Integer count = entry.getValue();
                lightSources += "uniform Struct" + name + " " + lightdata + "[" + count.toString() + "];\n";
            }
        }
        lightFunction += "   return color; }\n";
        return lightDefs + lightSources + lightFunction;
    }

    protected Map<String, String> mShaderSegments;
    protected Map<String, ShaderVariant> mShaderVariants;
    protected Set<String> mShaderDefines;
    protected String mUniformDescriptor;
}
