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
import org.gearvrf.debug.GVRStatsLine;
import org.mozilla.javascript.NativeGenerator.GeneratorClosedException;

import android.util.Log;

/**
 * Generates a vertex and fragment shader from the sources provided.
 *
 * This class allows you to define your own custom shader and introduce
 * it into GearVRF.
 *
 * Each shader generated has a unique signature so that the same shader
 * will not be generated twice. For shaders derived directly from
 * GVRShader, the signature is the simple Java class name.
 *
 * The shader also defines descriptors that define the
 * names and types of all the uniforms, textures and vertex attributes
 * used by the shader. For uniforms and attributes, each entry is a
 * float or integer type, an optional count and the name of the
 * uniform (e.g. "float3 diffuse_color, float specular_exponent, int is_enabled".
 * For textures, the descriptor contains the sampler type followed by the name:
 * (e.g. "sampler2D u_texture; samplerCube u_cubemap")
 *
 * Shaders derived from GVRShader cannot have variants and they ignore
 * light sources in the scene. To generate a more complex shader which
 * has multiple variants depending on what meshes, materials and
 * light sources it is used with, you can derive from @{link GVRShaderTemplate}
 *
 * @see GVRShaderTemplate
 */
public class GVRShader
{
    protected Integer mGLSLVersion = 100;
    protected boolean mHasVariants = false;
    protected boolean mUsesLights = false;
    protected String mUniformDescriptor;
    protected String mVertexDescriptor;
    protected String mTextureDescriptor;
    protected Map<String, String> mShaderSegments;


    /**
     * Construct a shader using GLSL version 100.
     * To make a shader for another version use the other form of the constructor.
     *
     * @param uniformDescriptor string describing uniform names and types
     *                          e.g. "float4 diffuse_color, float4 specular_color, float specular_exponent"
     * @param textureDescriptor string describing texture names and types
     *                          e.g. "sampler2D diffuseTexture, sampler2D specularTexture"
     * @param vertexDescriptor  string describing vertex attributes and types
     *                          e.g. "float3 a_position, float2 a_texcoord"
     */
    public GVRShader(String uniformDescriptor, String textureDescriptor, String vertexDescriptor)
    {
        mUniformDescriptor = uniformDescriptor;
        mVertexDescriptor = vertexDescriptor;
        mTextureDescriptor = textureDescriptor;
        mShaderSegments = new HashMap<String, String>();
    }

    /**
     * Construct a shader using GLSL version 300
     *
     * @param uniformDescriptor string describing uniform names and types
     *                          e.g. "float4 diffuse_color, float4 specular_color, float specular_exponent"
     * @param textureDescriptor string describing texture names and types
     *                          e.g. "sampler2D diffuseTexture, sampler2D specularTexture"
     * @param vertexDescriptor  string describing vertex attributes and types
     *                          e.g. "float3 a_position, float2 a_texcoord"
     *            string describing uniform names and types
     * @param glslVersion
     *            integer giving GLSL version (e.g. 300)
     */
    public GVRShader(String uniformDescriptor, String textureDescriptor, String vertexDescriptor, int glslVersion)
    {
        mUniformDescriptor = uniformDescriptor;
        mVertexDescriptor = vertexDescriptor;
        mTextureDescriptor = textureDescriptor;
        mShaderSegments = new HashMap<String, String>();
        mGLSLVersion = glslVersion;
    }

    /**
     * Check if this shader template generates variants.
     *
     * If a shader template generates variants, the specific vertex and
     * fragment shader to use cannot be determined until
     * the mesh, the material and lights are known. If the shader
     * template only makes a single shader, the vertex and fragment
     * shader programs can be generated immediately.
     *
     * @return true if this template generates variants,
     *         false if only a single shader can be generated.
     * @see GVRRenderData
     */
    public boolean hasVariants() { return mHasVariants; }


    /**
     * Check if this shader template uses light sources.
     *
     * If a shader template uses light sources, the specific vertex
     * and fragment shader to use cannot be generated until all
     * light sources are known. If the shader ignores lighting,
     * it will not need to be regenerated if lights are added
     * or removed from the scene.
     * @see GVRShader#hasVariants()
     */
    public boolean usesLights() { return mUsesLights; }

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
     *         {@link GVRLightBase#getUniformDescriptor()}  }
     */
    public String getUniformDescriptor()
    {
        return mUniformDescriptor;
    }

    /**
     * Get the string describing the vertex attributes used by this shader.
     *
     * Each vertex attribute represents a channel of float or int vectors.
     * It is described with the type ("int" or "float") immediately followed by the
     * size (a small integer) a space and then the name of the vertex attribute in the shader
     * (e.g. "float3 a_position, float23 a_texcoord") Spaces, commas, and other punctuation
     * are ignored.
     *
     * @return String with uniform descriptor.
     * {@link GVRLightBase#getVertexDescriptor()}  }
     */
    public String getVertexDescriptor()
    {
        return mVertexDescriptor;
    }

    /**
     * Get the string describing the textures  used by this shader.
     *
     * A texture is described with the sampler type (e.g. "sampler2D" or "samplerCube")
     * a space and then the name of the sampler in the shader.
     * (e.g. "sampler2D u_texture") Spaces, commas, and other punctuation are ignored.
     *
     * @return String with uniform descriptor.
     */
    public String getTextureDescriptor()
    {
        return mTextureDescriptor;
    }

    /**
     * Create a unique signature for this shader.
     * The signature for simple shaders is just the class name.
     * For the more complex shaders generated by GVRShaderTemplate
     * the signature includes information about the vertex attributes,
     * uniforms, textures and lights used by the shader variant.
     *
     * @param defined
     *            names to be defined for this shader
     * @return string signature for shader
     * @see GVRShaderTemplate
     */
    public String generateSignature(HashMap<String, Integer> defined, GVRLightBase[] lightlist)
    {
        return getClass().getSimpleName();
    }

    /**
     * Sets the default values for material data used by the shader.
     * Subclasses can override this function to provide values for
     * uniforms used by the material that are required by this shader.
     * This function is called whenever a GVRMaterial is created
     * that uses this shader class.
     *
     * @param material the material whose values should be set
     * @see GVRMaterial
     */
    protected void setMaterialDefaults(GVRShaderData material) { }

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
     *            renderable entity with mesh and rendering options
     * @param scene
     *            list of light sources
     */
    public int bindShader(GVRContext context, IRenderable rdata, GVRScene scene)
    {
        String signature = getClass().getSimpleName();
        GVRMaterialShaderManager shaderManager = context.getMaterialShaderManager();
        synchronized (shaderManager)
        {
            int nativeShader = shaderManager.getShader(signature);
            if (nativeShader == 0)
            {
                String vertexShaderSource = "";
                String fragmentShaderSource = "";
                if (mGLSLVersion > 100)
                {
                    vertexShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                    fragmentShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                }
                vertexShaderSource += getSegment("VertexTemplate");
                fragmentShaderSource += getSegment("FragmentTemplate");
                nativeShader = context.getMaterialShaderManager().
                        addShader(signature, mUniformDescriptor, mTextureDescriptor,
                                  mVertexDescriptor, vertexShaderSource, fragmentShaderSource);
            }
            if (nativeShader > 0)
            {
                rdata.setShader(nativeShader);
            }
            return nativeShader;
        }
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
    public int bindShader(GVRContext context, GVRShaderData material)
    {
        String signature = getClass().getSimpleName();
        GVRShaderManager shaderManager = context.getMaterialShaderManager();

        synchronized (shaderManager)
        {
            int nativeShader = shaderManager.getShader(signature);
            if (nativeShader == 0)
            {
                String vertexShaderSource = "";
                String fragmentShaderSource = "";
                if (mGLSLVersion > 100)
                {
                    vertexShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                    fragmentShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                }
                vertexShaderSource += getSegment("VertexTemplate");
                fragmentShaderSource += getSegment("FragmentTemplate");
                nativeShader = context.getMaterialShaderManager().addShader(signature,
                        mUniformDescriptor, mTextureDescriptor, mVertexDescriptor, vertexShaderSource, fragmentShaderSource);
            }
            return nativeShader;
        }
    }

    /**
     * Select the specific vertex and fragment shader to use with a shader
     * template that does not generate variants.
     *
     * This is the only way to bind a post-effect shader (because it
     * does not have a material).
     *
     * @param context
     *            GVRContext
     * @param shaderManager
     *          shader manager to use
     * @return ID of vertex/fragment shader set or 0 if shader template has variants.
     *
     * @see GVRContext#getPostEffectShaderManager()
     * @see GVRContext#getMaterialShaderManager()
     */
    public int bindShader(GVRContext context, GVRShaderManager shaderManager)
    {
        String signature = getClass().getSimpleName();
        int nativeShader = shaderManager.getShader(signature);

        synchronized (shaderManager)
        {
            if (nativeShader == 0)
            {
                String vertexShaderSource = "";
                String fragmentShaderSource = "";
                if (mGLSLVersion > 100)
                {
                    vertexShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                    fragmentShaderSource = "#version " + mGLSLVersion.toString() + " es\n";
                }
                vertexShaderSource += getSegment("VertexTemplate");
                fragmentShaderSource += getSegment("FragmentTemplate");
                nativeShader = shaderManager.addShader(signature,
                                                       mUniformDescriptor, mTextureDescriptor,
                                                       mVertexDescriptor, vertexShaderSource,
                                                       fragmentShaderSource);
            }
            return nativeShader;
        }
    }

    /**
     * Get the designated shader segment.
     *
     * @param name
     *            string name of shader segment
     * @return source code for segment or null if none exists.
     *         {@link GVRShaderTemplate#setSegment(String, String)}
     */
    protected String getSegment(String name)
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
    protected void setSegment(String segmentName, String shaderSource)
    {
        mShaderSegments.put(segmentName, shaderSource);
        if (shaderSource == null)
            throw new java.lang.IllegalArgumentException("Shader source is null for segment " + segmentName + " of shader");
    }
}
