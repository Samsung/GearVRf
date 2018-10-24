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
package org.gearvrf.animation;

import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.BitSet;
import java.util.Formatter;
import java.util.Locale;

import static org.gearvrf.animation.GVRPose.Bone.LOCAL_ROT;
import static org.gearvrf.animation.GVRPose.Bone.WORLD_POS;
import static org.gearvrf.animation.GVRPose.Bone.WORLD_ROT;

/*!
 * Set of transformations on the bones of a skeleton.
 * p>
 * A pose is associated with a specific skeleton.
 * It contains a matrix for every bone in that skeleton.
 * You can view a pose in local space where each bone matrix is relative to its parent bone.
 * You can also view it in world space where each bone matrix gives the world space joint orientation
 * and the world space joint position.
 * <p>
 * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
 * The pose orients and positions each bone in the skeleton with respect to this initial state.
 * Usually the bones are in a hierarchy and transformations on a parent bone apply to the
 * child bones as well.
 * <p>
 * Each skeleton has a current pose. Usually the current pose of a skeleton is used to
 * drive a skinned animation.
 *
 * @see GVRTransform
 * @see GVRSkeleton
 * @see GVRSkeletonAnimation
 */
public class GVRPose implements PrettyPrint
{
    static final float EPSILON = Float.intBitsToFloat(1);
    protected GVRSkeleton mSkeleton;
    private boolean	      mNeedSync;
    private Bone[]        mBones;
    private final Quaternionf mTempQuat = new Quaternionf();
    public static final Matrix4f mTempMtxA = new Matrix4f();
    public static final Matrix4f mTempMtxB = new Matrix4f();
    private static boolean sDebug = false;

    /**
     * The pose space designates how the world matrices
     * of the pose relate to one another.
     */
    public enum PoseSpace
    {
        /**
         * world positions and orientations are relative to the bind pose of the skeleton.
         */
        BIND_POSE_RELATIVE,

        /**
         * world positions and orientations are relative to the root bone of the skeleton.
         */
        SKELETON,

        /*
         * pose only contains local rotations
         */
        ROTATION_ONLY,
    };

    /**
     * Constructs a pose based on the specified skeleton.
     * Initially all of the bone matrices are identity.
     * @param skel	skeleton associated with the pose.
     * @see GVRSkeleton#setPose(GVRPose)
     * @see GVRSkeleton#applyPose(GVRPose, int)
     */
    public GVRPose(GVRSkeleton skel)
    {
        mSkeleton = skel;
        mBones = new Bone[skel.getNumBones()];
        for (int i = 0; i < mBones.length; ++i)
        {
            mBones[i] = new Bone();
        }
    }


    /**
     * Makes a copy of the input pose.
     * <p>
     * The poses will share the same skeleton but their bones will
     * be distinct (changes to the bones of the clone will not
     * affect the original).
     * @param src	pose to clone.
     */
    public GVRPose(GVRPose src)
    {
        mSkeleton = src.getSkeleton();
        mBones = new Bone[mSkeleton.getNumBones()];
        for (int i = 0; i < mBones.length; ++i)
        {
            mBones[i] = new Bone(src.getBone(i));
        }
    }


   /**
    * @return number of bones in the skeleton associated with this pose.
    * If there is no skeleton associated with the pose, 0 is returned.
    */

    public int          getNumBones() { return mSkeleton.getNumBones(); }


    /**
     * Get the skeleton for this pose.
     * <p>
     * The skeleton is established when the pose is created
     * and cannot be modified.
     * @return skeleton the pose applies to.
     */
    public GVRSkeleton	getSkeleton() { return mSkeleton; }


    public Bone		getBone(int boneindex) { return mBones[boneindex]; }


    /**
     * Get the world position of the given bone (relative to skeleton root).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton. This function gets the world space position of the designated bone.
      * @param boneindex index of bone whose position is wanted.
     * @return world position for the given bone.
     *
     * @see #setWorldPositions
     */

    public void     getWorldPosition(int boneindex, Vector3f pos) 
    {
        Bone bone = mBones[boneindex];
        int boneParent = mSkeleton.getParentBoneIndex(boneindex);

        if ((boneParent >= 0) && ((bone.Changed & LOCAL_ROT) == LOCAL_ROT))
        {
            calcWorld(bone, boneParent);
        }
        pos.x = bone.WorldMatrix.m30();
        pos.y = bone.WorldMatrix.m31();
        pos.z = bone.WorldMatrix.m32();
    }

    /**
     * Get the world positions of all the bones in this pose (relative to skeleton root).
     * @param dest	destination array to get world space joint positions
     * <p>
     * The world space positions for each bone are copied into the
     * destination array as vectors in the order of their bone index.
     * The array must be as large as three times the number of bones in the skeleton
     * (which can be obtained by calling {@link #getNumBones}).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton. This function returns the world space bone positions
     * as an array of vectors.
     *
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see #setWorldPositions
     */
    public void	getWorldPositions(float[] dest)
    {
        if (dest.length != mBones.length * 3)
        {
            throw new IllegalArgumentException("Destination array is the wrong size");
        }
        sync();
        for (int i = 0; i < mBones.length; ++i)
        {
            int t = i * 3;
            dest[t] = mBones[i].WorldMatrix.m30();
            dest[t + 1] = mBones[i].WorldMatrix.m31();
            dest[t + 2] = mBones[i].WorldMatrix.m32();
        }
    }

    /**
     * Get the world matrices of all the bones in this pose (relative to skeleton root).
     * <p>
     * The world space matrices for each bone are copied into the
     * destination array as vectors in the order of their bone index.
     * The array must be as large as 16 times the number of bones in the skeleton
     * (which can be obtained by calling {@link #getNumBones}).
     * @param dest	destination array to get world space matrices.
     *
     * @see #getWorldRotations
     * @see #getWorldMatrix
     * @see #getWorldPositions
     */
    public void	getWorldMatrices(float[] dest)
    {
        if (dest.length != mBones.length * 16)
        {
            throw new IllegalArgumentException("Destination array is the wrong size");
        }
        sync();
        for (int i = 0; i < mBones.length; ++i)
        {
            mBones[i].WorldMatrix.get(dest, i * 16);
        }
    }

    /**
     * Set the world positions for the bones in this pose (relative to skeleton root).
     * <p>
     * The world space positions for each bone are copied from the
     * source array of vectors in the order of their bone index.
     * The array must be as large as the number of bones in the skeleton
     * (which can be obtained by calling {@link #getNumBones}).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton. This function sets the world space bone positions
     * from an array of vectors. The bone orientations are unaffected and it is up to the
     * caller to make sure these positions are compatible with the current pose rotations.
     * @param positions	array with the positions in world coordinates.
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see #getWorldPositions
     */
    public void	setWorldPositions(float[] positions)
    {
        if (positions.length != mBones.length * 3)
        {
            throw new IllegalArgumentException("Destination array is the wrong size");
        }
        mNeedSync = true;
        for (int i = 0; i < mBones.length; ++i)
        {
            Bone bone = mBones[i];
            int t = i * 3;

            bone.setWorldPosition(positions[t], positions[t + 1], positions[t + 2]);
            bone.Changed = WORLD_POS;
            if (sDebug)
            {
                Log.d("BONE", "setWorldPosition: %s %s", mSkeleton.getBoneName(i), bone.toString());
            }
        }
        sync();
    }

    /**
     * Sets the world rotations for all the bones in this pose (relative to skeleton root).
     * <p>
     * The world space joint rotations for each bone are copied from the
     * source array of quaterions in the order of their bone index.
     * The order of the bones in the array must follow the order in the skeleton for this pose.
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton. This function sets the world space bone rotations
     * from an array of quaternions.The bone positions are unaffected.
     * @param rotations	array with quaternions in world coordinates.
     *
     * @see #setLocalRotations
     * @see #setWorldMatrix
     * @see #getWorldRotations
     * @see #setWorldPositions
     * @see GVRSkeleton#setBoneAxis
     * @see #getNumBones
     */
    public void setWorldRotations(float[] rotations)
    {
        if (rotations.length != mBones.length * 4)
        {
            throw new IllegalArgumentException("Source array is the wrong size");
        }
        mNeedSync = true;
        for (int i = 0; i < mBones.length; ++i)
        {
            Bone bone = mBones[i];
            int t = i * 4;

            bone.setWorldRotation(rotations[t], rotations[t + 1], rotations[t + 2], rotations[t + 3]);
            bone.Changed |= WORLD_ROT;

            calcLocal(bone, mSkeleton.getParentBoneIndex(i));

            if (sDebug)
            {
                Log.d("BONE", "setWorldRotation: %s %s", mSkeleton.getBoneName(i), bone.toString());
            }
        }
    }

    /**
     * Get the world matrix for this bone (relative to skeleton root).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton.
     * @param boneindex	zero based index of bone to get matrix for.
     * @param mtx		where to store bone matrix.
     * @return world matrix for the designated bone.
     *
     * @see #getWorldRotation
     * @see #getLocalRotation
     * @see #setWorldMatrix
     * @see GVRSkeleton#setBoneAxis
     */
    public void getWorldMatrix(int boneindex, Matrix4f mtx)
    {
        mBones[boneindex].getWorldMatrix(mtx);
    }

    /**
     * Set the world matrix for this bone (relative to skeleton root).
     * <p>
     * Sets the world matrix for the designated bone.
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton.
     * @param boneindex	zero based index of bone to set matrix for.
     * @param mtx		new bone matrix.
     * @see #getWorldRotation
     * @see #setLocalRotation
     * @see #getWorldMatrix
     * @see #getWorldPositions
     * @see GVRSkeleton#setBoneAxis
     */
    public void setWorldMatrix(int boneindex, Matrix4f mtx)
    {
        Bone	  bone = mBones[boneindex];

        bone.WorldMatrix.set(mtx);
        if (mSkeleton.getParentBoneIndex(boneindex) >= 0)
        {
            calcLocal(bone, mSkeleton.getParentBoneIndex(boneindex));
        }
        else
        {
            bone.LocalMatrix.set(mtx);
        }
        mNeedSync = true;
        bone.Changed = Bone.WORLD_POS | Bone.WORLD_ROT;
        if (sDebug)
        {
            Log.d("BONE", "setWorldMatrix: %s %s", mSkeleton.getBoneName(boneindex), bone.toString());
        }
    }

    /**
     * Get the world rotations for all the bones in this pose.
     * <p>
     * The world space rotations for each bone are copied into the
     * destination array as quaterions in the order of their bone index.
     * The array must be as large as the number of bones in the skeleton
     * (which can be obtained by calling {@link #getNumBones}).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The world bone matrix expresses the orientation and position of the bone relative
     * to the root of the skeleton. This function returns the world space bone rotations
     * as an array of quaternions.
     * @param rotations	destination array to get world space joint rotations.
     * @see #setWorldRotations
     * @see #getWorldRotation
     * @see #getWorldMatrix
     * @see #getNumBones
     * @see GVRSkeleton#setBoneAxis
     */
    public void getWorldRotations(float[] rotations)
    {
        if (rotations.length != mBones.length * 4)
        {
            throw new IllegalArgumentException("Destination array is the wrong size");
        }
        sync();
        for (int i = 0; i < mBones.length; i++)
        {
            Bone bone = mBones[i];
            int t = i * 4;

            bone.WorldMatrix.getUnnormalizedRotation(mTempQuat);
            mTempQuat.normalize();

            rotations[t++] = mTempQuat.x;
            rotations[t++] = mTempQuat.y;
            rotations[t++] = mTempQuat.z;
            rotations[t] = mTempQuat.w;
        }
    }

    /**
     * Gets the world location of a bone (relative to hierarchy root).
     * <p>
     * @param boneindex	zero based index of bone whose rotation is wanted.
     * @return world rotation for the designated bone as a quaternion.
     *
     * @see #setWorldRotation
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see GVRSkeleton#setBoneAxis
     */
    public void	getWorldRotation(int boneindex, Quaternionf q)
    {
        Bone bone = mBones[boneindex];

        if ((bone.Changed & LOCAL_ROT) == LOCAL_ROT)
        {
            calcWorld(bone, mSkeleton.getParentBoneIndex(boneindex));
        }
        bone.WorldMatrix.getUnnormalizedRotation(q);
        q.normalize();
    }

    /**
     * Sets the world rotation for the designated bone.
     * <p>
     * This function recomputes the local bone rotation and the world bone position.
     * @param boneindex	zero based index of bone to set matrix for.
     * @param x,y,z,w   quaternion with new rotation.
     * @see #getWorldRotation
     * @see #setLocalRotation
     * @see #getWorldMatrix
     * @see #getWorldPositions
     */
    public boolean setWorldRotation(int boneindex, float x, float y, float z, float w)
    {
        if (mSkeleton.isLocked(boneindex))
        {
            return false;
        }

        Bone	bone = mBones[boneindex];
        bone.setWorldRotation(x, y, z, w);
        bone.Changed |= WORLD_ROT;
        if (mSkeleton.getParentBoneIndex(boneindex) < 0)
        {
            bone.LocalMatrix.set3x3(bone.WorldMatrix);
        }
        else
        {
            mNeedSync = true;
        }
        if (sDebug)
        {
            Log.d("BONE", "%s WorldRotation: rot = (%f, %f, %f, %f)",
                    mSkeleton.getBoneName(boneindex), x, y, z, w);
        }
        return true;
    }

    /**
     * Get the local rotation matrix for this bone (relative to parent).
     * @param mtx		where to store bone matrix.
     * @param boneindex	zero based index of bone to get matrix for.
     * @return local matrix for the designated bone.
     *
     * @see #getWorldRotation
     * @see #getLocalRotation
     * @see GVRSkeleton#setBoneAxis
     */
    public void getLocalMatrix(int boneindex, Matrix4f mtx)
    {
        Bone bone = mBones[boneindex];

        if ((bone.Changed & (WORLD_ROT | WORLD_POS)) != 0)
        {
            calcLocal(bone, mSkeleton.getParentBoneIndex(boneindex));
        }
        bone.getLocalMatrix(mtx);
    }

    /**
     * Set the local matrix for this bone (relative to parent bone).
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The local bone matrix expresses the orientation and position of the bone relative
     * to the parent of this bone.
     * @param boneindex	zero based index of bone to set matrix for.
     * @param mtx		new bone matrix.
     * @see #getLocalRotation
     * @see #setWorldRotation
     * @see #getWorldMatrix
     */
    public void setLocalMatrix(int boneindex, Matrix4f mtx)
    {
        Bone	  bone = mBones[boneindex];
        int		  parentid = mSkeleton.getParentBoneIndex(boneindex);

        bone.LocalMatrix.set(mtx);
        bone.Changed = Bone.LOCAL_ROT;
        if (parentid < 0)
        {
            bone.WorldMatrix.set(bone.LocalMatrix);
        }
        else
        {
            mNeedSync = true;
        }
        if (sDebug)
        {

            Log.d("BONE",
                  "setLocalMatrix: %s %s",
                  mSkeleton.getBoneName(boneindex),
                  bone.toString());
        }
    }

    /**
     * Sets the local rotations for all the bones in this pose.
     * <p>
     * The local space rotations for each bone are copied from the
     * source array of quaterions in the order of their bone index.
     * The order of the bones in the array must follow the order in the skeleton for this pose.
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The local bone matrix expresses the orientation and position of the bone relative
     * to it's parent. This function sets the local orientations of all the bones from an
     * array of quaternions. The position of the bones are unaffected.
     * @param rotations array with the rotations for each bone.
     *					the angles are in the bone's local coordinate system.
     * @see #setLocalRotation
     * @see #getNumBones
     * @see GVRSkeleton#setBoneAxis
     * @see #setWorldRotations
     * @see #setWorldMatrix
     */
    public void setLocalRotations(float[] rotations)
    {
        mNeedSync = true;
        for (int i = 0; i < mBones.length; ++i)
        {
            int t = i * 4;
            if (!mSkeleton.isLocked(i))
            {
                Bone bone = mBones[i];

                bone.setLocalRotation(rotations[t], rotations[t + 1], rotations[t + 2], rotations[t + 3]);
                bone.Changed = LOCAL_ROT;
                if (sDebug)
                {
                    Log.d("BONE", "setLocalRotation: %s %s", mSkeleton.getBoneName(i), bone.toString());
                }
            }
        }
    }

    /**
     * Gets the local rotation for a bone given its index.
     * @param boneindex	zero based index of bone whose rotation is wanted.
     * @return local rotation for the designated bone as a quaternion.
     *
     * @see #setLocalRotation
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see GVRSkeleton#setBoneAxis
     */
    public void getLocalRotation(int boneindex, Quaternionf q)
    {
        Bone bone = mBones[boneindex];

        if ((bone.Changed & (WORLD_POS | WORLD_ROT)) != 0)
        {
            calcLocal(bone, mSkeleton.getParentBoneIndex(boneindex));
        }
        bone.LocalMatrix.getUnnormalizedRotation(q);
        q.normalize();
    }

    /**
     * Sets the local rotation for the designated bone.
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The local bone matrix expresses the orientation and position of the bone relative
     * to it's parent. This function sets the rotation component of that matrix from a quaternion.
     * The position of the bone is unaffected.
     * @param boneindex	zero based index of bone to rotate.
     * @param x,y,z,w	quaternion with the rotation for the named bone.
     * @see #setLocalRotations
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see GVRSkeleton#setBoneAxis
     */
    public boolean setLocalRotation(int boneindex, float x, float y, float z, float w)
    {
        if (mSkeleton.isLocked(boneindex))
            return false;

        Bone bone = mBones[boneindex];

        bone.setLocalRotation(x, y, z, w);
        if (mSkeleton.getParentBoneIndex(boneindex) < 0)
        {
            bone.WorldMatrix.set(bone.LocalMatrix);
        }
        else
        {
            mNeedSync = true;
        }
        bone.Changed = LOCAL_ROT;
        if (sDebug)
        {
            Log.d("BONE", "setLocalRotation: %s %s", mSkeleton.getBoneName(boneindex), bone.toString());
        }
        return true;
    }

    /**
     * Gets the local position (relative to the parent) of a bone.
     * <p>
     * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
     * The pose orients and positions each bone in the skeleton with respect to this initial state.
     * The local bone matrix expresses the orientation and position of the bone relative
     * to it's parent. This function returns the translation component of that matrix.
     * @param boneindex	zero based index of bone whose position is wanted.
     * @return local translation for the designated bone.
     *
     * @see #setLocalRotation
     * @see #setWorldRotations
     * @see #setWorldMatrix
     * @see GVRSkeleton#setBoneAxis
     */
    public void     getLocalPosition(int boneindex, Vector3f pos)
    {
        pos.x = mBones[boneindex].LocalMatrix.m30();
        pos.y = mBones[boneindex].LocalMatrix.m31();
        pos.z = mBones[boneindex].LocalMatrix.m32();
    }

    public void getLocalScale(int boneindex, Vector3f scale)
    {
        mBones[boneindex].getScale(scale);
    }

    public void setLocalPosition(int boneindex, float x, float y, float z)
    {
        Bone bone = mBones[boneindex];
        bone.setLocalPosition(x, y, z);

        if (mSkeleton.getParentBoneIndex(boneindex) < 0)
        {
            bone.WorldMatrix.set(bone.LocalMatrix);
        }
        else
        {
            mNeedSync = true;
        }
        bone.Changed = LOCAL_ROT;
        if (sDebug)
        {
            Log.d("BONE", "setLocalPosition: %s %s", mSkeleton.getBoneName(boneindex), bone.toString());
        }
    }

    /**
     * Transform the root bone of the pose by the given matrix.
     * @param trans matrix to transform the pose by.
     */
    public void transformPose(Matrix4f trans)
    {
        Bone bone = mBones[0];

        bone.LocalMatrix.set(trans);
        bone.WorldMatrix.set(trans);
        bone.Changed = WORLD_POS | WORLD_ROT;
        mNeedSync = true;
        sync();
    }

    /**
     * Compares two poses to see if they are the same.
     * @param src pose to compare with.
     * @return true if both objects represent the same pose.
     */
    public boolean  equals(GVRPose src)
    {
        int		    numbones = getNumBones();
        boolean	    same = true;
        float       tolerance = 3 * EPSILON;

        if (numbones != src.getNumBones())
            return false;
        sync();
        for (int i = 0; i < numbones; ++i)
        {
            if (!mBones[i].equals(src.getBone(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the contents of the input pose into this pose.
     * <p>
     * The bones of the two poses will be distinct after the copy,
     * changes to one pose will not affect the other.
     * They must both share the same skeleton.
     * @param src pose to copy.
     */
    public void  copy(GVRPose src)
    {
        int numbones = getNumBones();

        if (getSkeleton() != src.getSkeleton())
            throw new IllegalArgumentException("GVRPose.copy: input pose does not have same skeleton as this pose");
        src.sync();
        for (int i = 0; i < numbones; ++i)
        {
            mBones[i].copy(src.getBone(i));
        }
    }

    /**
     * Combines the input pose with this pose.
     * <p>
     * The matrices of each pose are multiplied together
     * to produce the combined pose.
     * @param src pose to combine.
     */
    public void  combine(GVRPose src)
    {
        int numbones = getNumBones();

        if (getSkeleton() != src.getSkeleton())
            throw new IllegalArgumentException("GVRPose.copy: input pose is incompatible with this pose");
        mNeedSync = false;
        src.sync();
        for (int i = 0; i < numbones; ++i)
        {
            Bone bone = mBones[i];

            bone.WorldMatrix.mul(src.mBones[i].WorldMatrix);
            calcLocal(bone, mSkeleton.getParentBoneIndex(i));
            if (sDebug)
            {
                Log.d("BONE", "combine: %s %s", mSkeleton.getBoneName(i), bone.toString());
            }
            bone.Changed = 0;
        }
        sync();
    }

    /**
     * Clear the rotations in this pose.
     * <p>
     * Positions are not affected.
     */
    public void  clearRotations()
    {
        int numbones = getNumBones();

        mNeedSync = true;
        for (int i = 0; i < numbones; ++i)
        {
            Bone bone = mBones[i];

            bone.clearRotation();
            bone.Changed = 0;
        }
    }

    /**
     * Makes this pose the inverse of the input pose.
     * @param src pose to invert.
     */
    public void  inverse(GVRPose src)
    {
        if (getSkeleton() != src.getSkeleton())
            throw new IllegalArgumentException("GVRPose.copy: input pose is incompatible with this pose");
        src.sync();
        int numbones = getNumBones();
        Bone srcBone = src.mBones[0];
        Bone dstBone = mBones[0];

        mNeedSync = true;
        srcBone.WorldMatrix.invertAffine(dstBone.WorldMatrix);
        srcBone.LocalMatrix.set(dstBone.WorldMatrix);
        if (sDebug)
        {
            Log.d("BONE", "invert: %s %s", mSkeleton.getBoneName(0), dstBone.toString());

        }
        for (int i = 1; i < numbones; ++i)
        {
            srcBone = src.mBones[i];
            dstBone = mBones[i];
            srcBone.WorldMatrix.invertAffine(dstBone.WorldMatrix);
            dstBone.Changed = WORLD_ROT | WORLD_POS;
            if (sDebug)
            {
                Log.d("BONE", "invert: %s %s", mSkeleton.getBoneName(i), dstBone.toString());
            }
        }
        sync();
    }

    /**
     * Sets the world position of a root bone and propagates to all children.
     * <p>
     * This has the effect of moving the overall skeleton to a new position
     * without affecting the orientation of it's bones.
     * @param x,y,z	new world position of root bone.
     * @return true if world position set, false if bone is not a root bone.
     *
     * @see #setWorldPositions
     * @see #getWorldPosition
     */
    public boolean	setPosition(float x, float y, float z)
    {
        Bone bone = mBones[0];
        float dx = x - bone.WorldMatrix.m30();
        float dy = y - bone.WorldMatrix.m31();
        float dz = z - bone.WorldMatrix.m32();

        sync();
        bone.LocalMatrix.setTranslation(x, y, z);
        for (int i = 0; i < mBones.length; ++i)
        {
            bone = mBones[i];
            bone.WorldMatrix.m30(bone.WorldMatrix.m30() + dx);
            bone.WorldMatrix.m31(bone.WorldMatrix.m31() + dy);
            bone.WorldMatrix.m32(bone.WorldMatrix.m32() + dz);
        }
        if (sDebug)
        {
            Log.d("BONE", "setWorldPosition: %s ", mSkeleton.getBoneName(0), bone.toString());
        }
        return true;
    }

    public boolean	setScale(float sx, float sy, float sz)
    {
        Bone bone = mBones[0];
        Vector3f v = new Vector3f();

        bone.getScale(v);
        v.x /= sx;
        v.y /= sy;
        v.z /= sz;
        bone.WorldMatrix.scale(v.x, v.y, v.z);
        bone.LocalMatrix.scale(1 / v.x, 1 / v.y, 1 / v.z);
        bone.Changed = WORLD_ROT | WORLD_POS;
        for (int i = 1; i < mBones.length; ++i)
        {
            bone = mBones[i];
            bone.WorldMatrix.scale(v.x, v.y, v.z);
            bone.Changed = WORLD_ROT | WORLD_POS;
        }
        if (sDebug)
        {
            Log.d("BONE", "setWorldScale: %s ", mSkeleton.getBoneName(0), bone.toString());
        }
        mNeedSync = true;
        sync();
        return true;
    }

    /**
     * Synchronize the state of the pose.
     * <p>
     * The local and world rotations are not automatically kept in sync.
     * When a pose is updated, the updates do not take complete
     * effect until you call this function.
     */
    public boolean	sync()
    {
        if (!mNeedSync)
            return false;
        mNeedSync = false;
        for (int i = 0; i < mBones.length; ++i)
        {
            Bone 	bone = mBones[i];
            int		pid = mSkeleton.getParentBoneIndex(i);
            boolean	update;

            if (pid < 0)							        // root bone?
                continue;
            update = (mBones[pid].Changed & (WORLD_ROT | LOCAL_ROT)) != 0;
            if (!mSkeleton.isLocked(i))				        // bone not locked?
            {
                if ((bone.Changed == WORLD_ROT) ||
                    ((bone.Changed & Bone.WORLD_POS) != 0))	// world matrix changed?
                {
                    calcLocal(bone, pid);					// calculate local rotation and position
                    if (sDebug)
                    {
                        Log.d("BONE", "sync: %s %s", mSkeleton.getBoneName(i), bone.toString());
                    }
                    continue;
                }
            }
            if (update ||								    // use local pos & rot?
                (bone.Changed & (LOCAL_ROT | WORLD_ROT)) != 0)
            {
                bone.Changed = LOCAL_ROT;
                calcWorld(bone, pid);				        // update world rotation & position
                if (sDebug)
                {
                    Log.d("BONE", "sync: %s %s", mSkeleton.getBoneName(i), bone.toString());
                }
            }
        }
        for (int i = 0; i < mBones.length; ++i)
        {
            mBones[i].Changed = 0;
        }
        return true;
    }

    /**
     * Calculates the world matrix based on the local matrix.
     */
    protected void		calcWorld(Bone bone, int parentId)
    {
        getWorldMatrix(parentId, mTempMtxB);   // WorldMatrix (parent) TempMtxB
        mTempMtxB.mul(bone.LocalMatrix);       // WorldMatrix = WorldMatrix(parent) * LocalMatrix
        bone.WorldMatrix.set(mTempMtxB);
     }

    /**
     * Calculates the local translation and rotation for a bone.
     * Assumes WorldRot and WorldPos have been calculated for the bone.
     */
    protected void		calcLocal(Bone bone, int parentId)
    {
        if (parentId < 0)
        {
            bone.LocalMatrix.set(bone.WorldMatrix);
            return;
        }
	/*
	 * WorldMatrix = WorldMatrix(parent) * LocalMatrix
	 * LocalMatrix = INVERSE[ WorldMatrix(parent) ] * WorldMatrix
	 */
        getWorldMatrix(parentId, mTempMtxA);	// WorldMatrix(par)
        mTempMtxA.invert();					    // INVERSE[ WorldMatrix(parent) ]
        mTempMtxA.mul(bone.WorldMatrix, bone.LocalMatrix);  // LocalMatrix = INVERSE[ WorldMatrix(parent) ] * WorldMatrix
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        GVRSkeleton skel = mSkeleton;
        int numBones = skel.getNumBones();
        sb.append(Log.getSpaces(indent));
        sb.append(getClass().getSimpleName());
        sb.append(System.lineSeparator());
        for (int i = 0; i < numBones; ++i)
        {
            String boneName = skel.getBoneName(i);
            Bone bone = mBones[i];
            sb.append(Log.getSpaces(indent) + 2);
            sb.append(boneName);
            sb.append(bone.toString());
        }
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }


/**
 * Internal structure used to maintain information about each bone.
 */
static class Bone
{
    public int Changed;             // WORLD_ROT, LOCAL_ROT, WORLD_POS
    public Matrix4f LocalMatrix;    // local transformations (relative to parent)
    public Matrix4f WorldMatrix;    // world transformations (relative to root of skeleton)

    public static final int LOCAL_ROT = 1;
    public static final int WORLD_ROT = 2;
    public static final int WORLD_POS = 4;
    private static final Quaternionf mTempQuat = new Quaternionf();

    public Bone()
    {
        LocalMatrix = new Matrix4f();
        WorldMatrix = new Matrix4f();
    }

    public Bone(Bone src)
    {
        LocalMatrix = new Matrix4f();
        WorldMatrix = new Matrix4f();
        copy(src);
    }

    public void copy(Bone src)
    {
        LocalMatrix.set(src.LocalMatrix);
        WorldMatrix.set(src.WorldMatrix);
        Changed = src.Changed;
    }

    public void clearRotation()
    {
        setLocalRotation(0, 0, 0, 1);
    }

    public void getWorldMatrix(Matrix4f mtx)
    {
        mtx.set(WorldMatrix);
    }

    public void getLocalMatrix(Matrix4f mtx)
    {
        mtx.set(LocalMatrix);
    }

    public void setWorldPosition(float x, float y, float z)
    {
        WorldMatrix.setTranslation(x, y, z);
    }

    public void setLocalPosition(float x, float y, float z)
    {
        LocalMatrix.setTranslation(x, y, z);
    }

    public void getScale(Vector3f scale)
    {
        scale.x = (float) Math.sqrt( LocalMatrix.m00() *  LocalMatrix.m00() +
                LocalMatrix.m01() *  LocalMatrix.m01() +
                LocalMatrix.m02() *  LocalMatrix.m02());
        scale.y = (float) Math.sqrt( LocalMatrix.m10() *  LocalMatrix.m10() +
                LocalMatrix.m11() *  LocalMatrix.m11() +
                LocalMatrix.m12() *  LocalMatrix.m12());
        scale.z = (float) Math.sqrt( LocalMatrix.m20() *  LocalMatrix.m20() +
                LocalMatrix.m21() *  LocalMatrix.m21() +
                LocalMatrix.m22() *  LocalMatrix.m22());
    }

    public void setLocalRotation(float x, float y, float z, float w)
    {
        float posx = LocalMatrix.m30();
        float posy = LocalMatrix.m31();
        float posz = LocalMatrix.m32();
        float scalex = (float) Math.sqrt( LocalMatrix.m00() *  LocalMatrix.m00() +
                                            LocalMatrix.m01() *  LocalMatrix.m01() +
                                            LocalMatrix.m02() *  LocalMatrix.m02());
        float scaley = (float) Math.sqrt( LocalMatrix.m10() *  LocalMatrix.m10() +
                                        LocalMatrix.m11() *  LocalMatrix.m11() +
                                        LocalMatrix.m12() *  LocalMatrix.m12());
        float scalez = (float) Math.sqrt( LocalMatrix.m20() *  LocalMatrix.m20() +
                LocalMatrix.m21() *  LocalMatrix.m21() +
                LocalMatrix.m22() *  LocalMatrix.m22());

        LocalMatrix.translationRotateScale(posx, posy, posz, x, y, z, w, scalex, scaley, scalez);
    }

    public void setWorldRotation(float x, float y, float z, float w)
    {
        float posx = WorldMatrix.m30();
        float posy = WorldMatrix.m31();
        float posz = WorldMatrix.m32();
        float scalex = (float) Math.sqrt( WorldMatrix.m00() *  WorldMatrix.m00() +
                WorldMatrix.m01() *  WorldMatrix.m01() +
                WorldMatrix.m02() *  WorldMatrix.m02());
        float scaley = (float) Math.sqrt( WorldMatrix.m10() *  WorldMatrix.m10() +
                WorldMatrix.m11() *  WorldMatrix.m11() +
                WorldMatrix.m12() *  WorldMatrix.m12());
        float scalez = (float) Math.sqrt( WorldMatrix.m20() *  WorldMatrix.m20() +
                WorldMatrix.m21() *  WorldMatrix.m21() +
                WorldMatrix.m22() *  WorldMatrix.m22());

        WorldMatrix.translationRotateScale(posx, posy, posz, x, y, z, w, scalex, scaley, scalez);
    }

    public void setWorldRotation(Quaternionf q)
    {
        setWorldRotation(q.z, q.y, q.z, q.w);
    }

    @Override
    public String toString()
    {
        Vector3f scale = new Vector3f();
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.US);

        LocalMatrix.getScale(scale);
        format.format(" wpos (%.2f %.2f %.2f)",
                WorldMatrix.m30(), WorldMatrix.m31(), WorldMatrix.m32());
        mTempQuat.setFromUnnormalized(WorldMatrix);
        mTempQuat.normalize();
        format.format(" wrot (%.2f %.2f %.2f %.2f)",
                mTempQuat.x, mTempQuat.y, mTempQuat.z, mTempQuat.w);
        format.format(" lpos (%.2f %.2f %.2f)",
                LocalMatrix.m30(), LocalMatrix.m31(), LocalMatrix.m32());
        mTempQuat.setFromUnnormalized(LocalMatrix);
        mTempQuat.normalize();
        format.format(" lrot (%.2f %.2f %.2f %.2f)",
                mTempQuat.x, mTempQuat.y, mTempQuat.z, mTempQuat.w);
        format.format(" lscale (%.2f %.2f %.2f)", scale.x, scale.y, scale.z);
        return sb.toString();
    }
}

};




