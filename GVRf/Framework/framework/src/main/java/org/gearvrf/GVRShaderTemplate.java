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

import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.glGetString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.GVRLightBase;
import org.mozilla.javascript.NativeGenerator.GeneratorClosedException;

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

    protected class LightClass
    {
        public LightClass()
        {
            Count = 1;
            FragmentUniforms = "";
            VertexUniforms = null;
            VertexOutputs = null;
            FragmentShader = null;
            VertexShader = null;
        }
        public Integer Count;
        public String FragmentUniforms;
        public String VertexUniforms;
        public String VertexOutputs;
        public String VertexShader;
        public String FragmentShader;
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
        if (shaderSource == null)
            throw new java.lang.IllegalArgumentException("Shader source is null for segment " + segmentName + " of shader");
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
     * @param definedNames
     *            set with defined names for this shader
     * @param mesh
     *            GVRMesh or null if vertex attributes should be ignored
     * @param material
     *            GVRMaterial used with this shader (may not be null)
     * @return shader variable names actually defined by the material and mesh
     */
    private void generateVariantDefines(HashMap<String, Integer> definedNames, GVRMesh mesh, GVRMaterial material)
    {
        Set<String> texNames = material.getTextureNames();
        Set<String> vertNames = null;

        if (mesh != null)
            vertNames = mesh.getAttributeNames();
        for (String name : mShaderDefines)
        {
            if (definedNames.containsKey(name))
                continue;
            if (material.hasUniform(name) || texNames.contains(name))
                definedNames.put(name, 1);
            else if ((vertNames != null) && vertNames.contains(name))
                definedNames.put(name, 1);
        }
    }
    /**
     * Checks  whether device supports OGL_MULTIVIEW extension
     * 
     */    
    private boolean isMultiviewPresent(){
        String extensionString = glGetString(GL_EXTENSIONS);
        if(extensionString.contains("GL_OVR_multiview2"))
            return true;

        return false;
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
    public String generateShaderVariant(boolean isMultiviewSet, String type, HashMap<String, Integer> definedNames, GVRLightBase[] lightlist, Map<String, LightClass> lightClasses)
    {
        String template = getSegment(type + "Template");
        String defines = "";

        if (template == null)
        {
            throw new IllegalArgumentException(
                type + "Template segment missing - cannot make shader");
        }
        String combinedSource = template;
        boolean useLights = (lightlist != null) && (lightlist.length > 0);
        String lightShaderSource = "";
        
        if (definedNames.containsKey("LIGHTSOURCES") &&
            definedNames.get("LIGHTSOURCES") == 0)
            useLights = false;
        if (useLights)
        {
            if (type.equals("Vertex"))
                lightShaderSource = generateLightVertexShader(lightlist, lightClasses);
            else
                lightShaderSource = generateLightFragmentShader(lightlist, lightClasses);
            defines += "#define HAS_LIGHTSOURCES 1\n";
        }

        for (Map.Entry<String, String> entry : mShaderSegments.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(type))
            {
                String segmentSource = entry.getValue();
                if (segmentSource == null)
                    segmentSource = "";
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
        
        if(isMultiviewSet && isMultiviewPresent())
            defines = "#define HAS_MULTIVIEW\n" + defines;
        
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
     * @return ID of vertex/fragment shader set
     */
    public void bindShader(GVRContext context, GVRRenderData rdata, GVRLightBase[] lightlist)
    {
        GVRMesh mesh = rdata.getMesh();
        GVRMaterial material = rdata.getMaterial();

        if (material == null)
        {
            return;
        }
        if (!rdata.isLightEnabled())
        {
            lightlist = null;
        }
        HashMap<String, Integer> variantDefines = getRenderDefines(rdata, lightlist);
        if (mShaderVariants == null)
        {
            mShaderVariants = new HashMap<String, ShaderVariant>();
        }
        generateVariantDefines(variantDefines, mesh, material);
        String signature = generateSignature(variantDefines, lightlist);
        ShaderVariant variant = mShaderVariants.get(signature);
        if (variant != null)
        {
            if (variant.ShaderID != null)
            {
                Log.d("gvrf", "SHADER: Reuse shader #" + variant.ShaderID.ID + " " + signature);
                material.setShaderType(variant.ShaderID);
                return;
            }
        }
        else
        {
            Map<String, LightClass> lightClasses = scanLights(lightlist);
            variant = new ShaderVariant();
            boolean isMultiviewSet = context.getActivity().getAppSettings().isMultiviewSet();
            variant.VertexShaderSource = generateShaderVariant(isMultiviewSet,"Vertex", variantDefines, lightlist, lightClasses);
            variant.FragmentShaderSource = generateShaderVariant(isMultiviewSet,"Fragment", variantDefines, lightlist, lightClasses);
            mShaderVariants.put(signature, variant);            
        }
      
        generateGLShader(context, material, signature);
    }

    /**
     * Select the specific vertex and fragment shader to use with this material.
     * 
     * The shader template is used to generate the sources for the vertex and
     * fragment shader based on the material properties only.
     * It will ignore the mesh attributes and all lights.
     * 
     * @param context
     *            GVRContext
     * @param material
     *            material to use with the shader
     * @return ID of vertex/fragment shader set
     */
    public void bindShader(GVRContext context, GVRMaterial material)
    {
        if (material == null)
        {
            return;
        }
        HashMap<String, Integer> variantDefines = new HashMap<String, Integer>();
        if (mShaderVariants == null)
        {
            mShaderVariants = new HashMap<String, ShaderVariant>();
        }
        generateVariantDefines(variantDefines, null, material);
        String signature = generateSignature(variantDefines, null);
        ShaderVariant variant = mShaderVariants.get(signature);
        if (variant != null)
        {
            if (variant.ShaderID != null)
            {
                Log.d("gvrf", "SHADER: Reuse shader #" + variant.ShaderID.ID + " " + signature);
                material.setShaderType(variant.ShaderID);
                return;
            }
        }
        else
        {
            variant = new ShaderVariant();
            boolean isMultiviewSet = context.getActivity().getAppSettings().isMultiviewSet();
            variant.VertexShaderSource = generateShaderVariant(isMultiviewSet, "Vertex", variantDefines, null, null);
            variant.FragmentShaderSource = generateShaderVariant(isMultiviewSet, "Fragment", variantDefines, null, null);
            mShaderVariants.put(signature, variant);            
        }
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
     * The base implementation LIGHTSOURCES as 0 if lighting is not enabled by the render data,
     * and it defines SHADOWS as 1 if any light source enables shadow casting. 
     * 
     * @param rdata GVRRenderData being used by this shader
     * @param lights list of lights used by this shader
     * @return list of symbols to be defined (value 1) or undefined (value 0) in the shader
     * 
     * @see GVRLightBase.setCastShadow
     */
    public HashMap<String, Integer> getRenderDefines(GVRRenderData rdata, GVRLightBase[] lights) {
        HashMap<String, Integer> defines = new HashMap<String, Integer>();
        int castShadow = 0;

        if (!rdata.isLightEnabled())
        {
            defines.put("LIGHTSOURCES", 0);
            return defines;
        }
        if (lights == null)
            return defines;
        for (GVRLightBase light : lights)
            if (light.getCastShadow())
                castShadow = 1;
        defines.put("SHADOWS", castShadow);
        return defines;
    }
    
    /**
     * Makes the material map for the shader.
     * The material map does not change any names, just maps
     * the keys in the material to themselves.
     */
    protected void makeMaterialMap(GVRMaterial material, GVRMaterialMap map)
    {
        Pattern uniformPattern = Pattern.compile("[ ]*([A-Za-z0-9_]+)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Pattern uniformTypePattern = Pattern.compile("([A-Za-z]+)([0-9_]+)");
        Matcher matcher1 = uniformPattern.matcher(mUniformDescriptor);
        Set<String> texNames = material.getTextureNames();

        for (String s : texNames)
        {
            map.addTextureKey(s, s);
        }
        while (matcher1.find())
        {
            String type = matcher1.group(1);
            String name = matcher1.group(2);
            Matcher matcher2 = uniformTypePattern.matcher(type);
            int nfloats = 1;
            
            if (matcher2.find())
            {
                String size = matcher2.group(2);
                if (size.length() > 0)
                {
                    nfloats = Integer.parseInt(size);
                }
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
     * Generates the shader code to compute fragment lighting for each light source.
     * The fragment shader defines a <LightPixel> function which computes the effect of all
     * light sources on the fragment color. This function calls the
     * <AddLight> function which integrates the light sources. This function
     * is defined in the fragment shader template.
     * 
     * @param lightlist
     *            list of lights in the scene
     * @return string with shader source code for fragment lighting
     */
    protected String generateLightFragmentShader(GVRLightBase[] lightlist, Map<String, LightClass> lightClasses)
    {
        String lightFunction = "vec4 LightPixel(Surface s) {\n"
                + "   vec4 color = vec4(0.0, 0.0, 0.0, 0.0);\n"
                + "   vec4 c;\n"
                + "   float enable;\n"
                + "   Radiance r;\n";
        String lightDefs = "\n";
        String lightSources = "\n";
        Integer index = 0;

        for (GVRLightBase light : lightlist)
        {
            String lightClassName = light.getClass().getSimpleName();
            String uniformId = light.getLightID();
            String vertexId = "v" + uniformId;

            if (light.getFragmentShaderSource() != null)
            {
                String vertDesc = light.getVertexDescriptor();
                if (vertDesc != null)
                {
                    lightFunction += makeVertexCopy(vertDesc, vertexId, vertexId);
                    lightFunction += "   r = " + lightClassName + "(s, " + uniformId + ", " + vertexId;
                    lightFunction += ");\n";
                    lightDefs += makeVertexOutputs(vertDesc, vertexId, "in ");                    
                    lightSources += "Vertex" + lightClassName + " " + vertexId + ";\n";
                }
                else
                    lightFunction += "   r = " + lightClassName + "(s, " + uniformId + ");\n";
                lightFunction += "   enable = " + uniformId + ".enabled;\n";
                lightFunction += "   c = vec4(enable, enable, enable, enable) * AddLight(s, r);\n";
                lightFunction += "   color.xyz += c.xyz;\n";
                lightFunction += "   color.w = c.w;\n";
                lightSources += "\nuniform Uniform" + lightClassName + " " + uniformId + ";\n";
            }
            ++index;
        }
        for (Map.Entry<String, LightClass> entry : lightClasses.entrySet())
        {
            LightClass lclass = entry.getValue();
            
            if (lclass.FragmentShader == null)
                continue;
            lightDefs += "\n" + lclass.FragmentUniforms;            
            if (lclass.VertexOutputs != null)
                lightDefs += "\n" + lclass.VertexOutputs;
            lightDefs += lclass.FragmentShader;
        }
        lightFunction += "   return color; }\n";
        return lightDefs + lightSources + lightFunction;
    }

    /**
     * Generates the shader code to compute vertex lighting for each light source.
     * The vertex shader defines a <LightVertex> function which computes
     * vertex output information for each light
     * 
     * @param lightlist
     *            list of lights in the scene
     * @return string with shader source code for vertex lighting
     */
    protected String generateLightVertexShader(GVRLightBase[] lightlist, Map<String, LightClass> lightClasses)
    {
        String lightSources = "";
        String lightDefs = "";
        String lightFunction = "void LightVertex(Vertex vertex) {\n";
        Integer index = 0;

        for (GVRLightBase light : lightlist)
        {
            String lightShader = light.getVertexShaderSource();
            String lightid = light.getLightID();

            if (lightid.isEmpty())
                continue;
            if (lightShader != null)
            {
                String vertexId = "v" + lightid;
                
                lightShader = lightShader.replace("@LIGHTOUT", vertexId);
                lightShader = lightShader.replace("@LIGHTIN", lightid);
                lightFunction += lightShader;
                lightDefs += makeVertexOutputs(light.getVertexDescriptor(), vertexId, "out ");
                lightSources += "\nuniform Uniform" + light.getClass().getSimpleName() + " " + lightid + ";\n";
            }
            ++index;
        }
        lightFunction += "}\n";
        for (Map.Entry<String, LightClass> entry : lightClasses.entrySet())
        {
            LightClass lclass = entry.getValue();
            
            if (lclass.VertexShader != null)
                lightDefs += lclass.FragmentUniforms;
        }
        return lightDefs + lightSources + lightFunction;
    }

    protected Map<String, LightClass> scanLights(GVRLightBase[] lightlist)
    {
        Map<String, LightClass> lightClasses = new HashMap<String, LightClass>();

        if (lightlist == null)
            return lightClasses;
        for (GVRLightBase light : lightlist)
        {
            String lightClassName = light.getClass().getSimpleName();
            String lightid = light.getLightID();
            String lightShader = light.getFragmentShaderSource();
 
            if ((lightShader == null) || lightid.isEmpty())
                continue;
            LightClass lightClass = lightClasses.get(lightClassName);
            if (lightClass != null)
                ++lightClass.Count;
            else
            {
                lightClass = new LightClass();
                lightClass.FragmentShader = lightShader.replace("@LightType", lightClassName);
                lightClass.FragmentUniforms = makeShaderStruct(light.getUniformDescriptor(), "Uniform" + lightClassName, null);
                if (light.getVertexDescriptor() != null)
                {
                    lightClass.VertexShader = light.getVertexShaderSource().replace("@LightType", lightClassName);
                    lightClass.VertexOutputs = makeShaderStruct(light.getVertexDescriptor(), "Vertex" + lightClassName, lightClass.VertexShader);
                    lightClass.VertexUniforms = makeShaderStruct(light.getUniformDescriptor(), "Uniform" + lightClassName, lightClass.VertexShader);
                }
                lightClasses.put(lightClassName, lightClass);
            }
        }
        return lightClasses;
    }
    
    protected String makeShaderStruct(String descriptor, String structName, String shaderSource)
    {
        Pattern pattern = Pattern.compile("[ ]*([a-zA-Z0-9_]+)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Matcher matcher = pattern.matcher(descriptor);
        String structDesc = "struct " + structName + " {\n";
        while (matcher.find())
        {
            String name = matcher.group(2);
            String type = matcher.group(1);

            if ((shaderSource == null) ||
                shaderSource.contains(name))
                structDesc += "    " + type + " " + name + ";\n";
        }
        structDesc += "};\n";
        return structDesc;
    }
 
    protected String makeVertexOutputs(String descriptor, String baseName, String prefix)
    {
        Pattern pattern = Pattern.compile("[ ]*([a-zA-Z0-9_]+)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Matcher matcher = pattern.matcher(descriptor);
        String desc = "";
        while (matcher.find())
        {
            String name = matcher.group(2);
            String type = matcher.group(1);

            desc += prefix + type + " " + baseName + "_" + name + ";\n";
        }
        return desc;
    }
    
    protected String makeVertexCopy(String descriptor, String inBase, String outBase)
    {
        Pattern pattern = Pattern.compile("[ ]*([a-zA-Z0-9_]+)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Matcher matcher = pattern.matcher(descriptor);
        String desc = "";
        while (matcher.find())
        {
            String name = matcher.group(2);

            desc += "   " + outBase + "." + name + " = " +  inBase + "_" + name + ";\n";
        }
        return desc;
    }
    
    protected Map<String, String> mShaderSegments;
    protected Map<String, ShaderVariant> mShaderVariants;
    protected Set<String> mShaderDefines;
    protected String mUniformDescriptor;
}
