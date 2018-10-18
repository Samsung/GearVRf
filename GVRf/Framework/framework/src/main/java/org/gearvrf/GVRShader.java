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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;

import org.joml.Matrix4f;

/**
 * Generates a vertex and fragment shader from the sources provided.
 * <p>
 * This class allows you to define your own custom shader and introduce
 * it into GearVRF.
 * Each shader generated has a unique signature so that the same shader
 * will not be generated twice. For shaders derived directly from
 * GVRShader, the signature is the simple Java class name.
 * <p>
 * The shader also defines descriptors that define the
 * names and types of all the uniforms, textures and vertex attributes
 * used by the shader. For uniforms and attributes, each entry is a
 * float or integer type, an optional count and the name of the
 * uniform (e.g. "float3 diffuse_color, float specular_exponent, int is_enabled".
 * For textures, the descriptor contains the sampler type followed by the name:
 * (e.g. "sampler2D u_texture; samplerCube u_cubemap")
 * <p>
 * Shaders derived from GVRShader cannot have variants and they ignore
 * light sources in the scene. To generate a more complex shader which
 * has multiple variants depending on what meshes, materials and
 * light sources it is used with, you can derive from @{link GVRShaderTemplate}
 *
 * @see GVRShaderTemplate
 */
public class GVRShader
{
    protected boolean mWriteShadersToDisk = true;
    protected GLSLESVersion mGLSLVersion = GLSLESVersion.V100;
    protected boolean mHasVariants = false;
    protected boolean mUsesLights = false;
    protected String mUniformDescriptor;
    protected String mVertexDescriptor;
    protected String mTextureDescriptor;
    protected int mOutputBufferSize = 0;
    protected Map<String, String> mShaderSegments;
    protected static String sBonesDescriptor = "mat4 u_bone_matrix[" + GVRMesh.MAX_BONES + "]";

    protected static String sTransformUBOCode =
            "#ifdef HAS_MULTIVIEW\n"
            + "   #define u_projection u_matrices[0]\n"
            + "   #define u_view u_matrices[gl_ViewID_OVR + uint(1)]\n"
            + "   #define u_view_i u_matrices[gl_ViewID_OVR + uint(3)]\n"
            + "   #define u_mvp u_matrices[u_matrix_offset + gl_ViewID_OVR]\n"
            + "#else\n"
            + "   #define u_projection u_matrices[0]\n"
            + "   #define u_view u_matrices[u_right + uint(1)]\n"
            + "   #define u_view_i u_matrices[u_right + uint(3)]\n"
            + "   #define u_mvp u_matrices[u_matrix_offset + u_right]\n"
            + "#endif\n"
            + "#define u_model u_matrices[u_matrix_offset + uint(2)]\n"
            + "uniform uint u_matrix_offset;\n"
            + "uniform uint u_right;\n"
            + "uniform uint u_render_mask;\n"
            + "uniform float u_proj_offset;\n"
            + "layout (std140) uniform Transform_ubo\n{\n"
            + "     mat4 u_matrices[64];\n"
            + "};\n";

    protected static String sTransformCode =
            "#define u_mvp u_matrices[0]\n"
            + "uniform uint u_matrix_offset;\n"
            + "uniform uint u_right;\n"
            + "uniform uint u_render_mask;\n"
            + "uniform float u_proj_offset;\n"
            + "uniform mat4 u_matrices[1];\n";

    protected static String sTransformVkUBOCode =
            "#ifdef HAS_MULTIVIEW\n"
            + "   #define u_projection u_matrices[0]\n"
            + "   #define u_view u_matrices[gl_ViewID_OVR + uint(1)]\n"
            + "   #define u_view_i u_matrices[gl_ViewID_OVR + uint(3)]\n"
            + "   #define u_mvp u_matrices[u_matrix_offset + gl_ViewID_OVR + uint(1)]\n"
            + "#else\n"
            + "   #define u_projection u_matrices[0]\n"
            + "   #define u_view u_matrices[u_right + uint(1)]\n"
            + "   #define u_view_i u_matrices[u_right + uint(3)]\n"
            + "   #define u_mvp u_matrices[u_matrix_offset + u_right]\n"
            + "#endif\n"
            + "#define u_model u_matrices[u_matrix_offset]\n"
            + "layout (std140, set = 0, binding = 0) uniform MatrixUniforms {\n "
            + "     uint u_matrix_offset;\n"
            + "     uint u_right;\n"
            + "     uint u_render_mask;\n"
            + "     float u_proj_offset;\n"
            + "};\n"
            + "layout (std140, set = 0, binding = 0) uniform Transform_ubo {\n "
            + "     mat4 u_matrices[64];\n"
            + "};\n";


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
     * Construct a shader using specified GLSL version
     *
     * @param uniformDescriptor string describing uniform names and types
     *                          e.g. "float4 diffuse_color, float4 specular_color, float specular_exponent"
     * @param textureDescriptor string describing texture names and types
     *                          e.g. "sampler2D diffuseTexture, sampler2D specularTexture"
     * @param vertexDescriptor  string describing vertex attributes and types
     *                          e.g. "float3 a_position, float2 a_texcoord"
     *            string describing uniform names and types
     * @param glslVersion
     *            GLSL version (e.g. GLSLESVersion.V300)
     */
    public GVRShader(String uniformDescriptor, String textureDescriptor, String vertexDescriptor, GLSLESVersion glslVersion)
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
     * Establish space for output matrices for this shader.
     * By default GearVRF will provide the model, view and
     * projection natrices as well as the concatenation of them
     * (projection * view * model). If your shader variant
     * needs additional matrices to be computed, this
     * function defines the number of output matrices
     * computed by the shader. These output matrices will
     * be stored in the u_matrices uniform (an array of
     * matrices). The uniform u_matrix_offset gives
     * the offset of the model matrix. The matrices
     * calculated by this shader will immediately follow
     * in whatever order they are stored in the output buffer.
     * <p>
     * To compute the matrices, your shader must override
     * @{code String getMatrixCalc(boolean usesLights); }
     * The function should return a string with a set of
     * expressions to calculate the matrices needed by the shader.
     * These matrix expressios will be calculated at run time
     * for every object that uses this shader.
     * @param n number of output matrices
     */
    public void setOutputMatrixCount(int n)
    {
        mOutputBufferSize = n * 16 * 4;
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
     *         {@link GVRLight#getUniformDescriptor()}  }
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
     * {@link GVRLight#getVertexDescriptor()}  }
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
    public String generateSignature(HashMap<String, Integer> defined, GVRLight[] lightlist)
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

    public String getMatrixCalc(boolean usesLights)
    {
        return null;
    }

    private int addShader(GVRShaderManager shaderManager, String signature, GVRShaderData material)
    {
        StringBuilder vertexShaderSource = new StringBuilder();
        StringBuilder fragmentShaderSource = new StringBuilder();
        boolean useLights = signature.contains("$LIGHTSOURCES");
        vertexShaderSource.append("#version " + mGLSLVersion.toString() + "\n");
        fragmentShaderSource.append("#version " + mGLSLVersion.toString() + " \n");
        String vshader = replaceTransforms(getSegment("VertexTemplate"), useLights);
        String fshader = replaceTransforms(getSegment("FragmentTemplate"), useLights);
        if (material != null)
        {
            String mtlLayout = material.makeShaderLayout();
            vshader = vshader.replace("@MATERIAL_UNIFORMS", mtlLayout);
            fshader = fshader.replace("@MATERIAL_UNIFORMS", mtlLayout);
        }
        vshader = vshader.replace("@BONES_UNIFORMS", GVRShaderManager.makeLayout(sBonesDescriptor, "Bones_ubo", true));
        vertexShaderSource.append(vshader);
        fragmentShaderSource.append(fshader);
        String frag = fragmentShaderSource.toString();
        String vert = vertexShaderSource.toString();
        int nativeShader = shaderManager.addShader(signature,
                                                   mUniformDescriptor,
                                                   mTextureDescriptor,
                                                   mVertexDescriptor,
                                                   vert, frag,
                                                   getMatrixCalc(useLights));
        if (mWriteShadersToDisk)
        {
            writeShader("V-" + signature + ".glsl", vert);
            writeShader("F-" + signature + ".glsl", frag);
        }
        return nativeShader;
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
     *            renderable entity with mesh and rendering options
     * @param scene
     *            list of light sources
     */
    public int bindShader(GVRContext context, IRenderable rdata, GVRScene scene, boolean isMultiview)
    {
        String signature = getClass().getSimpleName();
        GVRShaderManager shaderManager = context.getShaderManager();
        GVRMaterial mtl = rdata.getMaterial();
        synchronized (shaderManager)
        {
            int nativeShader = shaderManager.getShader(signature);

            if (nativeShader == 0)
            {
                nativeShader = addShader(shaderManager, signature, mtl);
            }
            if (nativeShader > 0)
            {
                rdata.setShader(nativeShader, isMultiview);
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
    public int bindShader(GVRContext context, GVRShaderData material, String vertexDesc)
    {
        String signature = getClass().getSimpleName();
        GVRShaderManager shaderManager = context.getShaderManager();

        synchronized (shaderManager)
        {
            int nativeShader = shaderManager.getShader(signature);
            if (nativeShader == 0)
            {
                nativeShader = addShader(shaderManager, signature, material);
            }
            return nativeShader;
        }
    }


    /**
     * Replaces @MATRIX_UNIFORMS in shader source with the
     * proper transform uniform declarations.
     * @param code          shader source code
     * @param usesLights    true if shader uses light sources, else false
     * @return shader source with transform uniform declarations added
     */
    protected String replaceTransforms(String code, boolean usesLights)
    {
        if (!code.contains("@MATRIX_UNIFORMS"))
        {
            return code;
        }
        if (isVulkanInstance())
        {
            return code.replace("@MATRIX_UNIFORMS", sTransformVkUBOCode);
        }
        if (getMatrixCalc(usesLights) != null)
        {
            return code.replace("@MATRIX_UNIFORMS", sTransformUBOCode);
        }
        return code.replace("@MATRIX_UNIFORMS", sTransformCode);
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
    }

    protected void writeShader( String fileName, String sourceCode)
    {
        try
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File file = new File(sdCard.getAbsolutePath() + "/GearVRF/" + fileName);
            OutputStreamWriter stream = new FileWriter(file);
            stream.append(sourceCode);
            stream.close();
        }
        catch (IOException ex)
        {
            org.gearvrf.utility.Log.e("GVRShaderTemplate", "Cannot write shader file " + fileName);
        }

    }

    public static native boolean isVulkanInstance();

    public enum GLSLESVersion {
        V100("100 es"),
        V300("300 es"),
        V310("310 es"),
        V320("320 es"),
        VULKAN("400"); // HACK: OK with Vulkan; gl_shader.c will replace with "300 es"

        private GLSLESVersion(String name) {
            this.name = name;
        }
        private final String name;

        public String toString() {
            return name;
        }
    }
}
