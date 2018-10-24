/* Copyright 2018 Samsung Electronics Co., LTD
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
package org.gearvrf.animation;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Component that animates a mesh based on a set of bones.
 * <p>
 * This class provides a component that implements skinning on the GPU.
 * It is based on a {@link GVRSkeleton} representing the bones of a skeleton.
 * The {@link GVRSkin} designates which bones of the skeleton animate a particular mesh.
 * Only these bones are sent to the GPU when the mesh is skinned.
 * Each mesh can use a different set of bones from the skeleton.
 * The skin should be attached to the scene object that owns the mesh.
 * <p>
 * When an asset containing a skinned mesh is loaded by the {@link org.gearvrf.GVRAssetLoader},
 * the {@link GVRSkeleton} and the {@link GVRSkin} components for each mesh
 * are automatically generated.
 * @see GVRSkeleton
 * @see GVRPose
 * @see org.gearvrf.animation.keyframe.GVRSkeletonAnimation
 */
public class GVRSkin extends GVRComponent implements PrettyPrint
{
    private static final String TAG = Log.tag(GVRSkin.class);
    protected int[] mBoneMap = null;
    protected GVRSkeleton mSkeleton;

    static public long getComponentType()
    {
        return NativeSkin.getComponentType();
    }

    /**
     * Create a skin based on the given skeleton.
     * @param skel {@link GVRSkeleton} whose bones drive this skin.
     */
    public GVRSkin(GVRSkeleton skel)
    {
        super(skel.getGVRContext(), NativeSkin.ctor(skel.getNative()));
        mType = getComponentType();
        mSkeleton = skel;
    }

    /**
     * Gets the number of bones used by this mesh.
     * <p>
     * The number of bones is established when the bone map is
     * set and cannot be changed without changing the bone map.
     * @see #setBoneMap(int[])
     */
    public int getNumBones()
    {
        return mBoneMap.length;
    }

    /**
     * Set the bone map which indicates which bone in the {@GVRSkeleton}
     * drives which bone in the {@link org.gearvrf.GVRVertexBuffer}.
     * <p>
     * The vertex buffer has up to four bone indices and corresponding
     * bone weights for each vertex. The bone indices in the vertex
     * buffer do not need to correspond to the bone indices in the
     * {@link GVRSkeleton} that drives the mesh. The bone map
     * establishes the correspondence between them.
     * @param boneMap   index of skeleton bones for each mesh bones.
     */
    public void setBoneMap(int[] boneMap)
    {
        mBoneMap = boneMap;
        NativeSkin.setBoneMap(getNative(), boneMap);
    }

    /**
     * Change the skeleton which contains the bones that
     * control this mesh.
     * <p>
     * The new skeleton must have corresponding bones for
     * the skin or an exception is thrown.
     * @param newSkel   new skeleton to use
     */
    public void setSkeleton(GVRSkeleton newSkel)
    {
        if (mSkeleton == newSkel)
        {
            return;
        }
        int[] newMap = new int[newSkel.getNumBones()];
        for (int i = 0; i < mBoneMap.length; ++i)
        {
            int oldIndex = mBoneMap[i];
            String boneName = mSkeleton.getBoneName(oldIndex);
            int newIndex = newSkel.getBoneIndex(boneName);
            if (newIndex >= 0)
            {
                newMap[i] = newIndex;
            }
            else
            {
                throw new IllegalArgumentException("Destination skeleton does not have bone " + boneName);
            }
        }
        mBoneMap = newMap;
        mSkeleton = newSkel;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(getClass().getSimpleName());
        sb.append(System.lineSeparator());
        sb.append(Log.getSpaces(indent) + 2);
        sb.append("numBones = " + Integer.toString(getNumBones()));
        sb.append(System.lineSeparator());
        for (int i = 0; i < getNumBones(); ++i)
        {
            int boneId = mBoneMap[i];
            String boneName = mSkeleton.getBoneName(boneId);
            sb.append(Log.getSpaces(indent) + 4);
            sb.append(Integer.toString(i));
            sb.append(": ");
            sb.append(boneName);
        }
    }
}

class NativeSkin
{
    static native long ctor(long skeleton);
    static native long getComponentType();
    static native boolean setBoneMap(long object, int[] boneMap);
}
