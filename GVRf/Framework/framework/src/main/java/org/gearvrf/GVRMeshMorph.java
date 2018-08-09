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
 * Attaches a morph to a scene object with a base mesh and sets the blend shapes.
 * <p>
 * When a morph is constructed, number of blend shapes is passed to set for base shape.
 * The differences are determined between base shape and blend shapes vertex descriptors and are assigned to texture.
 * Usually base shape vertex descriptor contains positions. They might also have normals, tangents, bitangents, bone weights and indices.
 * The blend shapes descriptor contain positions. They might also contain normals, tangents, bitangents.
 *</p>
 *@see GVRVertexBuffer
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
        Log.i("componne",""+getComponentType());
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
            throw new IllegalArgumentException("Blend shapes descriptor are to be in consistent");
        }
        if ((shapeDescriptorFlags & HAS_NORMAL) != 0)
        {
            copyBlendShape(index * mFloatsPerVertex + 3, 3, vbuf.getFloatArray("a_normal"));
        }
        if ((shapeDescriptorFlags & HAS_TANGENT) != 0)
        {
            copyBlendShape(index * mFloatsPerVertex + 6, 6, vbuf.getFloatArray("a_tangent"));
            copyBlendShape(index * mFloatsPerVertex + 9, 9, vbuf.getFloatArray("a_bitangent"));
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