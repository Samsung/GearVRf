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

import org.gearvrf.utility.Log;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES30.GL_RGB32F;

/**
 * Component which morphs a mesh based on a set of blend shapes.
 * <p>
 * The mesh being morphed is not changed - it's vertices
 * always contain the base shape being morphed.
 * Each blend shape is represented as a {@link GVRVertexBuffer}
 * with vertices in the same order as the base shape
 * but in different positions. A blend shape vertex
 * buffer may contain normals, tangents and bitangents
 * as well. The only restriction is that, if the attribute
 * is present in the base shape, the blend shapes must
 * also have it (applies only to a_position, a_normal,
 * a_tangent and a_bitangent vertex attributes).
 * <p>
 * The {@link GVRMeshMorph} component should be attached
 * to the {@link GVRSceneObject} which owns the base mesh.
 * The {@link GVRMaterial} used to render the mesh must
 * have a shader that supports morphing.
 * </p>
 * In addition to blend shapes, the morph component
 * has a set of blend weights which indicate what
 * proportion of each blend shape is used to
 * morph the mesh. After all the blend shapes
 * have been added and the weights are set,
 * the blend shape information is converted into
 * a {@link GVRFloatImage} where each RGB ixel is
 * actually three 32 bit floats representing a 3D
 * vector or position. Each row of the texture represents
 * a vertex, each column all the blend shape information
 * for just that vertex.
 * <p>
 * The blend shape texture is put in the <b>blendShapeTexture</b> sampler
 * in the vertex shader. The blend weights are in the <b>u_blendweights</b> uniform.
 * </p>
 */
public class GVRMeshMorph extends GVRBehavior
{
    static private long TYPE_MESHMORPH = newComponentType(GVRMeshMorph.class);
    static final int HAS_NORMAL = 1;
    static final int HAS_TANGENT = 2;
    protected int mDescriptorFlags = 0;

    final protected int mNumBlendShapes;
    //final protected boolean mMorphNormals;
    protected int mFloatsPerVertex;
    protected int mTexWidth;
    protected int mNumVerts;
    protected boolean desc;
    protected float[] mWeights;
    protected float[] mBlendShapeDiffs;
    protected String[] descriptors = new String[2];
    protected float[] mBaseBlendShape;
    protected GVRVertexBuffer mbaseShape;

    /**
     * Construct a morph to a scene object with a base mesh.
     * @param ctx  The current GVRF context.
     * @param numBlendShapes number of blend shapes to be set.
     */
    public GVRMeshMorph(GVRContext ctx, int numBlendShapes)
    {
        super(ctx, 0);

        mType = getComponentType();
        mNumBlendShapes = numBlendShapes;
        if (numBlendShapes <= 0)
        {
            throw new IllegalArgumentException("Number of blend shapes must be positive");
        }
        mFloatsPerVertex = 0;
        mTexWidth = 0; // 3 floats for position
    }


    static public long getComponentType() { return TYPE_MESHMORPH; }

    /**
     * Attaches a morph to scene object with a base mesh
     * @param sceneObj is the base mesh.
     * @throws IllegalStateException if component is null
     * @throws IllegalStateException if mesh is null
     * @throws IllegalStateException if material is null
     */
    public void onAttach(GVRSceneObject sceneObj)
    {
        super.onAttach(sceneObj);
        GVRComponent comp = getComponent(GVRRenderData.getComponentType());

        if (comp == null)
        {
            throw new IllegalStateException("Cannot attach a morph to a scene object without a base mesh");
        }

        GVRMesh mesh = ((GVRRenderData) comp).getMesh();
        if (mesh == null)
        {
            throw new IllegalStateException("Cannot attach a morph to a scene object without a base mesh");
        }
        GVRShaderData mtl = getMaterial();

        if ((mtl == null) ||
            !mtl.getTextureDescriptor().contains("blendshapeTexture"))
        {
            throw new IllegalStateException("Scene object shader does not support morphing");
        }
        copyBaseShape(mesh.getVertexBuffer());
        mtl.setInt("u_numblendshapes", mNumBlendShapes);
        mtl.setFloatArray("u_blendweights", mWeights);
    }

    public void onDetach(GVRSceneObject sceneObj)
    {
        mBlendShapeDiffs = null;
        mBaseBlendShape = null;
        mNumVerts = 0;
    }

    protected void copyBaseShape(GVRVertexBuffer baseShape)
    {
        String baseDescriptor = baseShape.getDescriptor();

        mFloatsPerVertex = 3;
        if (baseDescriptor.contains("a_normal"))
        {
            mDescriptorFlags |= HAS_NORMAL;
            mFloatsPerVertex += 3;
        }
        if (baseDescriptor.contains("a_tangent"))
        {
            mDescriptorFlags |= HAS_TANGENT;
            mFloatsPerVertex += 6;
        }
        mbaseShape = baseShape;
        mNumVerts = baseShape.getVertexCount();
        if (mNumVerts <= 0)
        {
            throw new IllegalArgumentException("Base shape has no vertices");
        }
        mTexWidth = mFloatsPerVertex*mNumBlendShapes;
        mBaseBlendShape = new float[mFloatsPerVertex * mNumVerts];
        mWeights = new float[mNumBlendShapes];
        mBlendShapeDiffs = new float[mTexWidth * mNumVerts];

        copyBaseAttribute(baseShape, "a_position", 0);
        if ((mDescriptorFlags & HAS_NORMAL) != 0)
        {
            copyBaseAttribute(baseShape, "a_normal", 3);
        }
        if ((mDescriptorFlags & HAS_TANGENT) != 0)
        {
            copyBaseAttribute(baseShape, "a_tangent", 6);
            copyBaseAttribute(baseShape, "a_bitangent", 9);
        }
    }


    protected void copyBaseAttribute(GVRVertexBuffer baseShape, String attrName, int baseOfs)
    {
        float[] vec3data = baseShape.getFloatArray(attrName);
        for (int i = 0; i < mNumVerts; ++i)
        {
            int t = i * mFloatsPerVertex + baseOfs;
            mBaseBlendShape[t] = vec3data[i * 3];
            mBaseBlendShape[t + 1] = vec3data[i * 3 + 1];
            mBaseBlendShape[t + 2] = vec3data[i * 3 + 2];
        }
    }

    protected void copyBlendShape(int shapeofs, int baseofs, float[] vec3data)
    {
        if (mBaseBlendShape == null)
        {
            throw new IllegalStateException("Must be attached to a scene object to set blend shapes");
        }
        if (mNumVerts != vec3data.length / 3)
        {
            throw new IllegalArgumentException("All blend shapes must have the same number of vertices");
        }
        for (int i = 0; i < mNumVerts; ++i)
        {
            int b = i * mFloatsPerVertex + baseofs;
            int s = i * mTexWidth + shapeofs;
            mBlendShapeDiffs[s] = (vec3data[i * 3] - mBaseBlendShape[b]);
            mBlendShapeDiffs[s + 1] = (vec3data[i * 3 + 1] - mBaseBlendShape[b + 1]);
            mBlendShapeDiffs[s + 2] = (vec3data[i * 3 + 2] - mBaseBlendShape[b + 2]);
        }
    }

    public float getWeight(int index)
    {
        return mWeights[index];
    }

    public final float[] getWeights()
    {
        return mWeights;
    }

    public void setWeights(float[] weights)
    {
        GVRMaterial mtl = getMaterial();
        System.arraycopy(weights, 0, mWeights, 0, mWeights.length);
        if (mtl != null)
        {
            mtl.setFloatArray("u_blendweights", mWeights);
        }
    }

    public void setBlendShape(int index, GVRSceneObject obj)
    {
        GVRRenderData rdata = obj.getRenderData();
        GVRMesh mesh;
        GVRVertexBuffer vbuf;

        if ((rdata == null) ||
            ((mesh = rdata.getMesh()) == null) ||
            ((vbuf = mesh.getVertexBuffer()) == null))
        {
            throw new IllegalArgumentException("Scene object must have a mesh to be used as a blend shape");
        }
        setBlendShape(index, vbuf);
    }

    public void setBlendShape(int index, GVRVertexBuffer vbuf)
    {
        int shapeDescriptorFlags = 0;
        String shapeDescriptor = vbuf.getDescriptor();

        copyBlendShape(index * mFloatsPerVertex, 0, vbuf.getFloatArray("a_position"));
        if (shapeDescriptor.contains("a_normal"))
        {
            shapeDescriptorFlags |= HAS_NORMAL;
        }
        if (shapeDescriptor.contains("a_tangent"))
        {
            shapeDescriptorFlags |= HAS_TANGENT;
        }
        if (shapeDescriptorFlags != mDescriptorFlags)
        {
            throw new IllegalArgumentException("Blend shapes descriptors are inconsistent");
        }
        if ((shapeDescriptorFlags & HAS_NORMAL) != 0)
        {
            copyBlendShape(index * mFloatsPerVertex + 3, 3, vbuf.getFloatArray("a_normal"));
        }
        if ((shapeDescriptorFlags & HAS_TANGENT) != 0)
        {
            copyBlendShape(index * mFloatsPerVertex + 6, 6, vbuf.getFloatArray("a_tangent"));
        }
    }


    private GVRMaterial getMaterial()
    {
        GVRComponent comp = getComponent(GVRRenderData.getComponentType());
        if (comp == null)
        {
            return null;
        }
        return ((GVRRenderData) comp).getMaterial();
    }

    public int getBlendShapeCount()
    {
        return mNumBlendShapes;
    }

    public boolean update()
    {
        GVRTexture blendshapeTex;
        GVRFloatImage blendshapeImage;
        GVRMaterial mtl = getMaterial();

        if ((mBlendShapeDiffs == null) || (mtl == null))
        {
            return false;
        }
        if (mtl.hasTexture("blendshapeTexture"))
        {
            blendshapeTex = mtl.getTexture("blendshapeTexture");
            blendshapeImage = (GVRFloatImage) blendshapeTex.getImage();
        }
        else
        {
            GVRTextureParameters texparams = new GVRTextureParameters(getGVRContext());
            texparams.setMinFilterType(GVRTextureParameters.TextureFilterType.GL_NEAREST);
            texparams.setMagFilterType(GVRTextureParameters.TextureFilterType.GL_NEAREST);
            blendshapeImage = new GVRFloatImage(getGVRContext(), GL_RGB);
            blendshapeTex = new GVRTexture(getGVRContext(), texparams);
            blendshapeTex.setImage(blendshapeImage);
            mtl.setTexture("blendshapeTexture", blendshapeTex);
        }
        blendshapeImage.update(mTexWidth / 3, mNumVerts, mBlendShapeDiffs);
        return true;
    }

    private String dumpDiffs(int numVerts)
    {
        String s = "";
        for (int i = 0; i < numVerts; ++i)
        {
            for (int j = 0; j < mTexWidth; ++j)
            {
                Float f = mBlendShapeDiffs[i * mTexWidth + j];
                s += " " + f.toString();
            }
            s += "\n";
        }
        return s;
    }
}