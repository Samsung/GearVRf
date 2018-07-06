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

import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES30.GL_RGB32F;

public class GVRMeshMorph extends GVRBehavior
{
    static private long TYPE_MESHMORPH = newComponentType(GVRMeshMorph.class);

    final protected int mNumBlendShapes;
    final protected boolean mMorphNormals;
    protected int mFloatsPerVertex;
    protected int mTexWidth;
    protected int mNumVerts;
    protected float[] mWeights;
    protected float[] mBlendShapeDiffs;
    protected float[] mBaseBlendShape;

    public GVRMeshMorph(GVRContext ctx, int numBlendShapes, boolean morphNormals)
    {
        super(ctx, 0);
        mType = getComponentType();
        mNumBlendShapes = numBlendShapes;
        mMorphNormals = morphNormals;
        if (numBlendShapes <= 0)
        {
            throw new IllegalArgumentException("Number of blend shapes must be positive");
        }
        mFloatsPerVertex = 3;
        mTexWidth = numBlendShapes * 3; // 3 floats for position
        if (morphNormals)
        {
            mTexWidth *= 2;             // 3 more for normal
            mFloatsPerVertex *= 3;
        }
    }

    static public long getComponentType() { return TYPE_MESHMORPH; }

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
        mNumVerts = baseShape.getVertexCount();
        if (mNumVerts <= 0)
        {
            throw new IllegalArgumentException("Base shape has no vertices");
        }
        mBaseBlendShape = new float[mFloatsPerVertex * mNumVerts];
        mWeights = new float[mNumBlendShapes];
        mBlendShapeDiffs = new float[mTexWidth * mNumVerts];
        float[] vec3data = baseShape.getFloatArray("a_position");
        for (int i = 0; i < mNumVerts; ++i)
        {
            int t = i * mFloatsPerVertex;
            mBaseBlendShape[t] = vec3data[i * 3];
            mBaseBlendShape[t + 1] = vec3data[i * 3 + 1];
            mBaseBlendShape[t + 2] = vec3data[i * 3 + 2];
        }
        if (mMorphNormals)
        {
            vec3data = baseShape.getFloatArray("a_normal");
            for (int i = 0; i < mNumVerts; ++i)
            {
                int t = i * mFloatsPerVertex + 3;
                mBaseBlendShape[t] = vec3data[i * 3];
                mBaseBlendShape[t + 1] = vec3data[i * 3 + 1];
                mBaseBlendShape[t + 2] = vec3data[i * 3 + 2];
            }
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
        copyBlendShape(index * mFloatsPerVertex, 0, vbuf.getFloatArray("a_position"));
        if (mMorphNormals)
        {
            copyBlendShape(index * mFloatsPerVertex + 3, 3, vbuf.getFloatArray("a_normal"));
        }
    }

    public void setBlendPositions(int index, float[] vec3data)
    {
        copyBlendShape(index * mFloatsPerVertex, 0, vec3data);
    }

    public void setBlendNormals(int index, float[] vec3data)
    {
        copyBlendShape(index * mFloatsPerVertex + 3, 3, vec3data);
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

    public boolean update()
    {
        GVRTexture blendshapeTex;
        GVRFloatImage blendshapeImage;
        GVRMaterial mtl = getMaterial();

        if ((mBlendShapeDiffs == null) || (mtl == null))
        {
            return false;
        }
//        Log.d("MORPH", dumpDiffs());
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

    private String dumpDiffs()
    {
        String s = "";
        for (int i = 0; i < mNumVerts; ++i)
        {
            for (int j = 0; j < mTexWidth; ++j)
            {
                Float f = mBlendShapeDiffs[i * mFloatsPerVertex + j];
                s += " " + f.toString();
            }
            s += "\n";
        }
        return s;
    }
}
