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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.keyframe.BVHImporter;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Component that animates a skeleton based on a set of bones.
 * <p>
 * This class provides a common infrastructure for skeletal animation.
 * It can construct an animation hierarchy representing the bones of a skeleton
 * which can be used to animate a skinned character made up of multiple meshes.
 * <p>
 * You can set the bone names in the skeleton
 * to designate which scene objects in your hierarchy represent which bones.
 * If you attach the {@link GVRSkeleton} component to the root of a hierarchy of scene objects,
 * it will link the skeleton bones to the scene objects so that, when you change the
 * skeleton, the scene objects will reflect that change.
 * <p>
 * The asset loader constructs the {@link GVRSkeleton} and attaches the bones when you export a rigged character.
 * It also makes a {@link org.gearvrf.animation.keyframe.GVRSkeletonAnimation} class
 * which associates the animations in the asset with the appropriate skeleton bones.
 * <p>
 * {@link GVRSkeleton} relies on the {@link GVRPose} class to represent the position and orientation of its bones.
 * All bones in the skeleton start out at the origin oriented along the bone axis (usually 0,0,1).
 * The pose orients and positions each bone in the skeleton respect to this initial state by maintaining
 * a matrix for each bone. The root bone which is the parent of all the other bones in the
 * hierarchy should be the first bone (with index 0).
 * <p>
 * The <i>bind pose</i> of the skeleton defines the neutral position of the bones before any
 * external transformations are applied. Usually it represents the pose of the skeleton
 * matching the meshes which will be skinned from the skeleton. The bone transformations
 * used for skinning the mesh are relative to the bind pose.
 * <p>
 * The <i>current pose</i> of the skeleton defines the current orientation and position of the bones.
 * It uses the same conventions as the bind pose, relative to bones at the origin along the bone axis
 * It is <b>not</b> relative to the bind pose, instead it is relative to the root of the skeleton.
 * When the current pose is updated, the skeleton modifies the matrices
 * attached to the models in the hierarchy and the bone matrices.
 * <p>
 *  Each mesh that is skinned by the skeleton bones has a {@link GVRSkin} component attached
 *  which tracks which bones in the skeleton affect the mesh. The skin is responsible
 *  for updating the matrices in the GPU which drive skinning.
 * </p>
 * @see GVRSkin
 * @see GVRPose
 * @see org.gearvrf.animation.keyframe.GVRSkeletonAnimation
 */
public class GVRSkeleton extends GVRComponent implements PrettyPrint
{
    /**
     * The pose space designates how the world matrices
     * of the pose relate to one another.
     */
    public static final int BIND_POSE_RELATIVE = 1;

    /**
     * world positions and orientations are relative to the root bone of the skeleton.
     */
    public static final int SKELETON_ROOT = 0;

    /*
     * pose only contains local rotations
     */
    public static final int ROTATION_ONLY = 4;

    /*
     * Bone rotation is locked
     */
    public static final int BONE_LOCK_ROTATION = 1;

    /*
     * Bone driven by animation
     */
    public static final int BONE_ANIMATE = 4;
    /*
     * Bone driven by physics
     */
    public static final int BONE_PHYSICS = 2;

    private static final String TAG = Log.tag(GVRSkeleton.class);
    protected GVRSceneObject[] mBones;
    protected int[] mParentBones;
    protected int[] mBoneOptions;
    final private Quaternionf mTempQuatA = new Quaternionf();
    final private Quaternionf mTempQuatB = new Quaternionf();
    final private Matrix4f mTempMtx = new Matrix4f();
    private static int[] sTempBoneParents;
    private GVRSceneObject sTempRoot;

    protected String[] mBoneNames;
    protected Vector3f mRootOffset;     // offset for root bone animations
    protected Vector3f mBoneAxis;       // axis of bone, defines bone coordinate system
    protected GVRPose mBindPose;        // bind pose for this skeleton
    protected GVRPose mInverseBindPose; // inverse bind pose for this skeleton
    protected GVRPose mPose;            // current pose for this skeleton
    protected GVRPose mSkinPose;        // current pose for the skin
    protected float[] mPoseMatrices;

    static public long getComponentType()
    {
        return NativeSkeleton.getComponentType();
    }

    /**
     * Construct a skeleton given a bone hierarchy
     * @param ctx           {@link GVRContext} to associate the skeleton with
     * @param parentBones   Array of integers describing the bone hierarchy.
     *                      Each bone has an index, with bone 0 being the root bone.
     *                      Parent bones must appear in the array before their children.
     *                      Each entry in the array gives the index of that bone's
     *                      parent, -1 indicates the root bone (no parent).
     * @see #setBoneName(int, String)
     * @see #getBoneIndex(String)
     * @see #getParentBoneIndex(int)
     */
    public GVRSkeleton(GVRContext ctx, int[] parentBones)
    {
        super(ctx, NativeSkeleton.ctor(parentBones));
        mType = getComponentType();
        mParentBones = parentBones;
        mBoneAxis = new Vector3f(0, 0, 1);
        mRootOffset = new Vector3f(0, 0, 0);
        mBoneOptions = new int[parentBones.length];
        mBoneNames = new String[parentBones.length];
        mBones = new GVRSceneObject[parentBones.length];
        mPose = new GVRPose(this);
        mBindPose = new GVRPose(this);
        mPoseMatrices =  new float[parentBones.length * 16];
    }

    public GVRSkeleton(GVRSceneObject root, List<String> boneNames)
    {
        super(root.getGVRContext(), 0);
        setNative(NativeSkeleton.ctor(makeParentBoneIds(root, boneNames)));
        int numBones = boneNames.size();
        mType = getComponentType();
        mParentBones = sTempBoneParents;
        mBoneAxis = new Vector3f(0, 0, 1);
        mRootOffset = new Vector3f(0, 0, 0);
        mBoneOptions = new int[numBones];
        mBoneNames = new String[numBones];
        mBones = new GVRSceneObject[numBones];
        mPoseMatrices =  new float[numBones * 16];
        mPose = new GVRPose(this);
        mBindPose = new GVRPose(this);
        for (int boneId = 0; boneId < numBones; ++boneId)
        {
            String boneName = boneNames.get(boneId);
            setBoneName(boneId, boneName);
        }
        if (sTempRoot != null)
        {
            sTempRoot.attachComponent(this);
        }
    }

    protected int[] makeParentBoneIds(GVRSceneObject root, List<String> boneNames)
    {
        int numBones = boneNames.size();
        int[] parentBones = new int[numBones];
        int numRoots = 0;
        sTempRoot = null;
        Arrays.fill(parentBones, -1);
        for (int boneId = 0; boneId < numBones; ++boneId)
        {
            String boneName = boneNames.get(boneId);
            GVRSceneObject obj = root.getSceneObjectByName(boneName);

            if (obj == null)
            {
                Log.e("BONE", "bone %s not found in scene", boneName);
                continue;
            }
            GVRSceneObject parent = obj.getParent();
            int parBoneId = -1;

            if (parent != null)
            {
                String nodeName = parent.getName();
                parBoneId = boneNames.indexOf(nodeName);
            }
            if ((parBoneId < 0) && (sTempRoot == null))
            {
                sTempRoot = obj;
                Log.d("BONE", "Skeleton root %d is %s", numRoots, boneNames.get(boneId));
                ++numRoots;
            }
            parentBones[boneId] = parBoneId;
        }
        sTempBoneParents = parentBones;
        return parentBones;
    }

    /**
     * Get world space position of skeleton (position of the root bone, index 0).
     * <p>
     * The world space position of the skeleton is the translation of
     * the root bone (index 0) of the current pose.
     */
    public void getPosition(Vector3f pos)
    {
        mPose.getWorldPosition(0, pos);
    }

    /**
     * Sets the world space position of the skeleton.
     * <p>
     * @param pos world space position of the skeleton.
     *            <p>
     *            The world space position of the skeleton is the translation of
     *            the root bone (index 0) of the current pose. Setting the skeleton position
     *            directly updates both the root bone position and the translation
     *            on the target model of the skeleton.
     * @see GVRPose#setWorldPositions
     */
    public void setPosition(Vector3f pos)
    {
        mPose.setPosition(pos.x, pos.y, pos.z);
    }

    /**
     * Gets the offset of the root bone when animated.
     * <p>
     * This offset is useful for moving the starting point of animations.
     * For example, you can make a zero-based animation relative to the
     * current position of the character by setting this offset to that position.
     * By default, this offset is zero.
     *
     * @see #setRootOffset(Vector3f)
     * @see #poseFromBones()
     */
    public Vector3f getRootOffset()
    {
        return mRootOffset;
    }

    /**
     * Sets the offset of the root bone when animated.
     * <p>
     * This offset is useful for moving the starting point of animations.
     * For example, you can make a zero-based animation relative to the
     * current position of the character by setting this offset to that position.
     * By default, this offset is zero.
     * <p>
     * The root bone offset only affects animations. It does not affect any
     * updates to the current pose.
     * @param ofs vector with new root bone offset.
     * @see #getRootOffset()
     * @see #poseFromBones()
     */
    public void setRootOffset(Vector3f ofs)
    {
        mRootOffset = ofs;
    }

    /**
     * Set the bind pose of the skeleton to describe the initial position and
     * orientation of the bones when the skinned meshes are not modified.
     * <p>
     * The <i>bind pose</i> of the skeleton defines the position and orientation of the bones before any
     * animations are applied. Usually it represents the pose that matches
     * the source vertices of the meshes driven by the skeleton.
     * You can restore the skeleton to it's bind pose with {@link #restoreBindPose()}
     * <p>
      * The bind pose is maintained as a {link GVRPose} object internal to the skeleton
     * and cannot be shared across skeletons. Setting the bind pose copies the
     * value of the input pose into the skeleton's bind pose. Subsequent
     * updates to the input pose are not reflected in the skeleton's bind pose.
     * @param pose {@link GVRPose} containing rotations and positions for the bind pose.
     * @see #setPose(GVRPose)
     * @see #getBindPose()
     * @see #restoreBindPose()
     * @see {@link GVRPose}
     */
    public void setBindPose(GVRPose pose)
    {
        if (pose != mBindPose)
        {
            pose.sync();
            mBindPose.copy(pose);
        }
        if (mInverseBindPose == null)
        {
            mInverseBindPose = new GVRPose(this);
        }
        mInverseBindPose.inverse(mBindPose);
        mPose.copy(mBindPose);
        updateBonePose();
    }

    /**
     * Set the bind pose of the skeleton to describe the initial position and
     * orientation of the bones when the skinned meshes are not modified.
     * <p>
     * The <i>bind pose</i> of the skeleton defines the position and orientation of the bones before any
     * animations are applied. Usually it represents the pose that matches
     * the source vertices of the meshes driven by the skeleton.
     * You can restore the skeleton to it's bind pose with {@link #restoreBindPose()}
     * <p>
     * The bind pose is maintained as a {link GVRPose} object internal to the skeleton
     * and cannot be shared across skeletons. Setting the bind pose copies the
     * value of the input pose into the skeleton's bind pose. Subsequent
     * updates to the input pose are not reflected in the skeleton's bind pose.
     * @param rotations new bind pose rotations
     * @param positions new bind pose positions
     *                  Both arrays must have enough entries for all the bones in the skeleton.
     *                  The positions and orientation are all relative to the root
     *                  bone of the skeleton (not the parent bone).
     * @see #setPose(GVRPose)
     * @see #getBindPose()
     * @see #restoreBindPose()
     * @see {@link GVRPose}
     */
    public void setBindPose(float[] rotations, float[] positions)
    {
        mBindPose.setWorldRotations(rotations);
        mBindPose.setWorldPositions(positions);
        if (mInverseBindPose == null)
        {
            mInverseBindPose = new GVRPose(this);
        }
        mInverseBindPose.inverse(mBindPose);
        mPose.copy(mBindPose);
        updateBonePose();
    }


    /**
     * The bind pose is the pose the skeleton is in when no rotations are
     * applied to it's joints. This is a reference to an internal {@GVRPose}
     * which cannot be shared across multiple skeletons.
     * <p>
     * Do not modify the bind pose directly - consider it a read only pose.
     * The skeleton maintains the inverse bind pose internally so
     * you should always call {@link #setBindPose(GVRPose)} to
     * change it.
     * </p>
     *
     * @see #getNumBones
     * @see #setBindPose
     * @see #getPose
     * @see GVRPose#setWorldRotations
     * @see GVRPose#setWorldPositions
     */
    public final GVRPose getBindPose()
    {
        return mBindPose;
    }

    /**
     * The inverse bind pose transforms a pose relative to the
     * skeleton root into a pose that can be applied to the
     * mesh being skinned. This is a reference to an internal {@GVRPose}
     * which cannot be shared across multiple skeletons.
     * <p>
     * Do not modify the inverse bind pose directly - consider it a read only pose.
     * The skeleton maintains the inverse bind pose internally so
     * you should always call {@link #setBindPose(GVRPose)} to
     * change it.
     * </p>
     *
     * @see #getNumBones
     * @see #setBindPose
     * @see #getPose
     * @see GVRPose#setWorldRotations
     * @see GVRPose#setWorldPositions
     */
    public final GVRPose getInverseBindPose()
    {
        return mInverseBindPose;
    }

    /**
     * Get the current skeleton pose.
     * <p>
     * The<i>current pose</i> is the pose the skeleton is currently in. It contains the
     * location and orientation of each bone relative to the skeleton root bone.
     * The current pose is a reference to an internal {@link GVRPose} that changes dynamically.
     * Modifications made to this pose will affect the skeleton.
     *
     * @see GVRPose
     * @see #setPose
     * @see GVRPose#setWorldRotations
     * @see GVRPose#setWorldPositions
     */
    public GVRPose getPose()
    {
        return mPose;
    }

    /**
     * Sets the the current pose for this skeleton.
     * <p>
     * The given {@link GVRPose} object becomes linked to the skeleton
     * so that changes to this pose change the bones of the skeleton
     * and any skinned meshes driven by it.
     * The current pose contains the current orientation and position of the bones in the skeleton,
     * relative to bones at the origin along the bone axis.
     * The "world" matrices of the bind pose are relative to the root bone
     * of the skeleton.
     *
     * @param pose {@link @GVRPose} containing rotations and positions relative
     *                              to the root bone of the skeleton.
     * @see #getPose
     * @see #restoreBindPose
     * @see #setBindPose
     */
    public void setPose(GVRPose pose)
    {
        GVRSkeleton skel = pose.getSkeleton();
        pose.sync();
        if (pose == mPose)
            return;
        if (skel != this)
            throw new IllegalArgumentException("setPose: input pose has incompatible skeleton");
        mPose.copy(pose);
        updateBonePose();
    }

    /**
     * Updates the current pose of a skeleton without replacing it.
     * <p>
     * This function does not require the input pose to be relative to the root bone,
     * it will also accept poses relative to the bind pose of the skeleton.
     * It does not replace the current pose with the new pose, it updates the
     * current pose in place. If a bone is locked, it's input is ignored.
     *
     * @param newpose new pose to apply.
     * @param poseSpace indicates the coordinate space of the input pose.
     * <TABLE>
     * <TR><TD>SKELETON_ROOT</TD>
     * <TD>Indicates the world matrices in the pose are relative to the root of the skeleton.</TD>
     * </TR>
     * <TR><TD>BIND_POSE_RELATIVE</TD>
     * <TD>Indicates the local rotations in the pose are relative to the bind pose the skeleton.
     * Translations are ignored.</TD>
     * </TR>
     * <TR><TD>ROTATION_ONLY</TD>
     * <TD>Indicates the local rotations in the pose are relative to the root of the skeleton.
     * Translations are ignored.</TD>
     * </TR>
     * </TABLE>
     * @see #setPose
     * @see GVRPose
     */
    public void applyPose(GVRPose newpose, int poseSpace)
    {
        int numbones = getNumBones();

        /*
         * Apply input pose that is relative to the bind pose.
         * Multiply the input rotations by the bind pose
         * rotations to get rotations relative to the
         * skeleton root.
         */
        if (poseSpace == BIND_POSE_RELATIVE)
        {
            for (int i = 0; i < numbones; ++i)
            {
                GVRPose.Bone srcBone = newpose.getBone(i);

                if ((srcBone.Changed != 0) && !isLocked(i))
                {
                    mBindPose.getLocalMatrix(i, mTempMtx);
                    mTempMtx.mul(srcBone.LocalMatrix);
                    mTempMtx.getUnnormalizedRotation(mTempQuatA);
                    mPose.setLocalRotation(i, mTempQuatA.x, mTempQuatA.y, mTempQuatA.z, mTempQuatA.w);
                    srcBone.Changed = 0;
                }
            }
        }
        /*
         * Apply input pose that contains only local rotations.
         */
        else if (poseSpace == ROTATION_ONLY)
        {
            for (int i = 0; i < numbones; ++i)
            {
                GVRPose.Bone srcBone = newpose.getBone(i);

                if ((srcBone.Changed != 0) && !isLocked(i))
                {
                    newpose.getLocalRotation(i, mTempQuatA);
                    mPose.setLocalRotation(i, mTempQuatA.x, mTempQuatA.y, mTempQuatA.z, mTempQuatA.w);
                    srcBone.Changed = 0;
                }
            }
        }
        /*
         * Apply the world rotations from the input pose to
         * the skeleton's current pose. The rotations are
         * already relative to the skeleton root.
         */
        else
        {
            newpose.sync();
            if (mPose != newpose)
            {
                for (int i = 0; i < numbones; ++i)
                {
                    if (!isLocked(i))
                    {
                        GVRPose.Bone srcBone = newpose.getBone(i);
                        srcBone.getLocalMatrix(mTempMtx);
                        mPose.setLocalMatrix(i, mTempMtx);
                    }
                }
            }
        }
        mPose.sync();
        updateBonePose();
    }

    /**
     * Updates the current pose of a skeleton and applies the
     * transform of the {@link GVRSkeleton} owner.
     * <p>
     * This function does not require the input pose to be relative to the root bone,
     * it will also accept poses relative to the bind pose of the skeleton.
     * It does not replace the current pose with the new pose, it updates the
     * current pose in place. If a bone is locked, it's input is ignored.
     *
     * @param newpose new pose to apply, supports all pose spaces.
     * @see #setPose
     * @see GVRPose
     */
    public void transformPose(GVRPose newpose)
    {
        int numbones = getNumBones();
        Matrix4f rootMtx = owner.getTransform().getModelMatrix4f();

        rootMtx.getUnnormalizedRotation(mTempQuatB);
        mTempQuatB.conjugate();
        newpose.sync();
        for (int i = 0; i < numbones; ++i)
        {
            GVRPose.Bone srcBone = newpose.getBone(i);

            if ((mBoneOptions[i] & BONE_LOCK_ROTATION) == 0)
            {
                rootMtx.mul(mTempMtx, mTempMtx);
                mTempMtx.mul(srcBone.LocalMatrix, mTempMtx);
                mPose.setWorldMatrix(i, mTempMtx);
            }
        }
        updateBonePose();
    }

    /**
     * Restore the skeleton to the bind pose.
     * <p>
     * The bind pose is the default pose of the skeleton when not being animated.
     * It is initialized as the pose the skeleton is when it is first attached
     * to the Skeleton but you can change it with {@link #setBindPose(GVRPose)}
     *
     * @see #setBindPose
     * @see #getBindPose
     * @see GVRPose
     */
    public void restoreBindPose()
    {
        mInverseBindPose.inverse(mBindPose);
        mPose.copy(mBindPose);
        updateBonePose();
    }

    /**
     * Gets the bone index for the parent of the specified bone.
     *
     * @param boneindex index of bone whose parent to set.
     * @return parent index or -1 for root bone
     * @see #getBoneIndex
     */
    public int getParentBoneIndex(int boneindex)
    {
        return mParentBones[boneindex];
    }

    /**
     * Get the bone index for the given scene object.
     *
     * @param bone GVRSceneObject bone to search for
     * @return bone index or -1 for root bone.
     * @see #getParentBoneIndex
     */
    public int getBoneIndex(GVRSceneObject bone)
    {
        for (int i = 0; i < getNumBones(); ++i)
            if (mBones[i] == bone)
                return i;
        return -1;
    }

    /**
     * Get the bone index for the bone with the given name.
     *
     * @param bonename string identifying the bone whose index you want
     * @return 0 based bone index or -1 if bone with that name is not found.
     */
    public int getBoneIndex(String bonename)
    {
        for (int i = 0; i < getNumBones(); ++i)

            if (mBoneNames[i].equals(bonename))
                return i;
        return -1;
    }

    /**
     * Gets the number of bones in the skeleton.
     * <p>
     * The number of bones is established when the skeleton is first created
     * and cannot be subsquently changed.
     */
    public int getNumBones()
    {
        return mParentBones.length;
    }

    /**
     * Get the bone Z axis which defines the bone coordinate system.
     * <p>
     * The default bone coordinate system has the Z axis of
     * the bone at (0, 0, 1).
     */
    public Vector3f getBoneAxis()
    {
        return mBoneAxis;
    }

    /**
     * Set the bone Z axis which defines the bone coordinate system.
     * The default bone coordinate system has the Z axis of
     * the bone at (0, 0, 1).
     */
    public void setBoneAxis(Vector3f boneAxis)
    {
        mBoneAxis = boneAxis;
    }

    /**
     * Sets the name of the designated bone.
     *
     * @param boneindex zero based index of bone to rotate.
     * @param bonename  string with name of bone.
.     * @see #getBoneName
     */
    public void setBoneName(int boneindex, String bonename)
    {
        mBoneNames[boneindex] = bonename;
    }

    /**
     * Get the name of a bone given it's index.
     *.
     * @param boneindex index of bone to get
     * @return name of bone or null if not found.
     */
    public String getBoneName(int boneindex)
    {
        return mBoneNames[boneindex];
    }

    /**
     * Get the list of bone names in the order of bone indices.
     * @return bone name list
     */
    public final String[] getBoneNames() { return mBoneNames; }

    /**
     * Set the bone names for all of the bones from an array.
     * This array will now be owned by the skeleton,
     * it is not copied. This function throws an exception
     * if the length of the list does not match the number
     * of bones in the skeleton.
     * @see #getBoneNames()
     * @see #getBoneName(int)
     */
    public void setBoneNames(String[] boneNames)
    {
        if (getNumBones() != boneNames.length)
        {
            throw new IllegalArgumentException("Bone names array has wrong length");
        }
        mBoneNames = boneNames;
    }

    /**
     * Get the scene object driven by the given bone.
     *
     * @param boneindex index of bone to get.
     * @return GVRSceneObject whose transform represents the bone.
     */
    public GVRSceneObject getBone(int boneindex)
    {
        return mBones[boneindex];
    }

    public void setBone(int boneindex, GVRSceneObject bone)
    {
        mBones[boneindex] = bone;
    }

    /**
     * Set rotation and physics options for this bone.
     *
     * @param boneindex 0 based bone index.
     * @param options   Options to control how this bone moves.
     *                  {@link #BONE_PHYSICS} will use physics (rigid body dynamics) to calculate
     *                  the motion of the bone.
     *                  {@link #BONE_LOCK_ROTATION} will lock the bone rotation, freezing its current
     *                  local rotation.
     *                  {@link #BONE_ANIMATE} uses animation to drive the bone.
     * @see GVRPose#setLocalRotations
     * @see GVRPose#setWorldRotations
     * @see #poseFromBones()
     * @see #poseToBones()
     */
    public void setBoneOptions(int boneindex, int options)
    {
        mBoneOptions[boneindex] = options;
    }

    /**
     * Get rotation and physics options for this bone.
     *
     * @param boneindex 0 based bone index.
     * @see #setBoneOptions
     */
    public int getBoneOptions(int boneindex)
    {
        return mBoneOptions[boneindex];
    }

    /**
     * Determine if given bone is locked or not.
     *
     * @param boneindex 0 based bone index.
     */
    public boolean isLocked(int boneindex)
    {
        return (mBoneOptions[boneindex] & BONE_LOCK_ROTATION) != 0;
    }

    public void onAttach(GVRSceneObject root)
    {
        super.onAttach(root);
        if (mInverseBindPose == null)
        {
            attachBones(mBindPose);
            mBindPose.sync();
            mPose.copy(mBindPose);
            mInverseBindPose = new GVRPose(this);
            mInverseBindPose.inverse(mBindPose);
            updateBonePose();
        }
        else
        {
            attachBones(null);
            restoreBindPose();
        }
    }

    public void onDetach(GVRSceneObject root)
    {
        super.onDetach(root);
        for (int i = 0; i < mBones.length; ++i)
        {
            mBones[i] = null;
        }
    }

    /**
     * Links the bones in the skeleton with scene nodes in the owner's hierarchy.
     * <p>
     * Examines the descendants of the skeleton owner looking for nodes whose names match the
     * bones in the skeleton. This function assumes the hierarchy below the owner contains
     * the nodes to animate to control the skeleton in the scene.
     * The input pose will have the positions and rotations of the nodes.
     */
    public void attachBones(final GVRPose savepose)
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner == null)
        {
            return;
        }
        GVRSceneObject.SceneVisitor visitor = new GVRSceneObject.SceneVisitor()
        {
            @Override
            public boolean visit(GVRSceneObject newbone)
            {
                String bonename = newbone.getName();

                if (bonename.isEmpty())            // ignore nodes without names
                    return true;
                int boneindex = getBoneIndex(bonename);
                if (boneindex >= 0)                // a bone we recognize?
                {
                    mBones[boneindex] = newbone;
                    if (savepose != null)
                    {
                        savepose.setWorldMatrix(boneindex, newbone.getTransform().getModelMatrix4f());
                    }
                }
                return true;
            }
        };
        owner.forAllDescendants(visitor);
    }

    /**
     * Applies the matrices computed from the scene object's
     * linked to the skeleton bones to the current pose.
     * @see #applyPose(GVRPose, int)
     * @see #setPose(GVRPose)
     */
    public void poseFromBones()
    {
        for (int i = 0; i < getNumBones(); ++i)
        {
            GVRSceneObject bone = mBones[i];
            if (bone == null)
            {
                continue;
            }
            if ((mBoneOptions[i] & BONE_LOCK_ROTATION) != 0)
            {
                continue;
            }
            GVRTransform trans = bone.getTransform();
            mPose.setLocalMatrix(i, trans.getLocalModelMatrix4f());
        }
        mPose.sync();
        updateBonePose();
    }

    /**
     * Applies the matrices computed from the scene object's
     * linked to the skeleton bones to the current pose.
     * @param boneOptions   Only update bones with the given options
     *                      (BONE_PHYSICS or BONE_ANIMATE)
     * @see #applyPose(GVRPose, int)
     * @see #setPose(GVRPose)
     */
    public void poseFromBones(int boneOptions)
    {
        for (int i = 0; i < getNumBones(); ++i)
        {
            GVRSceneObject bone = mBones[i];
            if ((bone != null) && (mBoneOptions[i] & boneOptions) != 0)
            {
                GVRTransform trans = bone.getTransform();
                mPose.setLocalMatrix(i, trans.getLocalModelMatrix4f());
            }
        }
        mPose.sync();
        updateBonePose();
    }

    /**
     * Applies the matrices from the skeleton's current pose
     * to the scene objects associated with each bone.
     * <p>
     * The {@link org.gearvrf.animation.keyframe.GVRSkeletonAnimation} class
     * does this as a part of skeletal animation. It does not occur
     * automatically when the current pose is updated.
     * @see #applyPose(GVRPose, int)
     * @see #setPose(GVRPose)
     */
    public void poseToBones()
    {
        mPose.sync();
        for (int i = 0; i < getNumBones(); ++i)
        {
            GVRSceneObject bone = mBones[i];
            if ((bone != null) &&
               ((mBoneOptions[i] & BONE_LOCK_ROTATION) == 0))
            {
                mPose.getLocalMatrix(i, mTempMtx);
                bone.getTransform().setModelMatrix(mTempMtx);
            }
        }
    }


    /*
     * Compute the skinning matrices from the current pose.
     * <p>
     * The skin pose is relative to the untransformed mesh.
     * It will be used to transform the vertices before
     * overall translate, rotation and scale are performed.
     * It is the current pose relative to the bind pose
     * of the mesh.
     * <p>
     * Each skinned mesh has an associated @{link GVRSkin} component
     * which manages the bones that affect that mesh.
     */
    public GVRPose computeSkinPose()
    {
        if (mInverseBindPose == null)
        {
            return null;
        }
        mPose.sync();
        if (mSkinPose == null)
        {
            mSkinPose = new GVRPose(mPose);
        }
        else
        {
            mSkinPose.copy(mPose);
        }
        mSkinPose.combine(mInverseBindPose);
        return mSkinPose;
    }

    /*
     * Compute the skinning matrices from the current pose.
     * <p>
     * The skin pose is relative to the untransformed mesh.
     * It will be used to transform the vertices before
     * overall translate, rotation and scale are performed.
     * It is the current pose relative to the bind pose
     * of the mesh.
     * <p>
     * Each skinned mesh has an associated @{link GVRSkin} component
     * which manages the bones that affect that mesh.
     * This function updates the GPU skinning matrices each frame.
     * @see GVRSkin
     */
    public void updateSkinPose()
    {
        GVRPose skinPose = computeSkinPose();

        skinPose.getWorldMatrices(mPoseMatrices);
        NativeSkeleton.setSkinPose(getNative(), mPoseMatrices);
    }

    /*
     * Compute the skinning matrices from the current pose.
     * <p>
     * The skin pose is relative to the untransformed mesh.
     * It will be used to transform the vertices before
     * overall translate, rotation and scale are performed.
     * It is the current pose relative to the bind pose
     * of the mesh.
     * <p>
     * Each skinned mesh has an associated @{link GVRSkin} component
     * which manages the bones that affect that mesh.
     * This function updates the GPU skinning matrices each frame.
     * @see GVRSkin
     */
    public void updateBonePose()
    {
        GVRPose pose = getPose();
        int t = 0;

        for (int i = 0; i < mParentBones.length; ++i)
        {
            pose.getLocalMatrix(i, mTempMtx);
            mTempMtx.get(mPoseMatrices, t);
            t += 16;
        }
        NativeSkeleton.setPose(getNative(), mPoseMatrices);
    }

    /**
     * Merge the source skeleton with this one.
     * The result will be that this skeleton has all of its
     * original bones and all the bones in the new skeleton.
     *
     * @param newSkel skeleton to merge with this one
     */
    public void merge(GVRSkeleton newSkel)
    {
        int numBones = getNumBones();
        List<Integer> parentBoneIds = new ArrayList<Integer>(numBones + newSkel.getNumBones());
        List<String> newBoneNames = new ArrayList<String>(newSkel.getNumBones());
        List<Matrix4f> newMatrices = new ArrayList<Matrix4f>(newSkel.getNumBones());
        GVRPose oldBindPose = getBindPose();
        GVRPose curPose = getPose();

        for (int i = 0; i < numBones; ++i)
        {
            parentBoneIds.add(mParentBones[i]);
        }
        for (int j = 0; j < newSkel.getNumBones(); ++j)
        {
            String boneName = newSkel.getBoneName(j);
            int boneId = getBoneIndex(boneName);
            if (boneId < 0)
            {
                int parentId = newSkel.getParentBoneIndex(j);
                Matrix4f m = new Matrix4f();

                newSkel.getBindPose().getLocalMatrix(j, m);
                newMatrices.add(m);
                newBoneNames.add(boneName);
                if (parentId >= 0)
                {
                    boneName = newSkel.getBoneName(parentId);
                    parentId = getBoneIndex(boneName);
                }
                parentBoneIds.add(parentId);
            }
        }
        if (parentBoneIds.size() == numBones)
        {
            return;
        }
        int n = numBones +  parentBoneIds.size();
        int[] parentIds = Arrays.copyOf(mParentBones, n);
        int[] boneOptions = Arrays.copyOf(mBoneOptions, n);
        String[] boneNames = Arrays.copyOf(mBoneNames, n);

        mBones = Arrays.copyOf(mBones, n);
        mPoseMatrices =  new float[n * 16];
        for (int i = 0; i < parentBoneIds.size(); ++i)
        {
            n = numBones + i;
            parentIds[n] = parentBoneIds.get(i);
            boneNames[n] = newBoneNames.get(i);
            boneOptions[n] = BONE_ANIMATE;
        }
        mBoneOptions = boneOptions;
        mBoneNames = boneNames;
        mParentBones = parentIds;
        mPose = new GVRPose(this);
        mBindPose = new GVRPose(this);
        mBindPose.copy(oldBindPose);
        mPose.copy(curPose);
        mBindPose.sync();
        for (int j = 0; j < newSkel.getNumBones(); ++j)
        {
            mBindPose.setLocalMatrix(numBones + j, newMatrices.get(j));
            mPose.setLocalMatrix(numBones + j, newMatrices.get(j));
        }
        setBindPose(mBindPose);
        mPose.sync();
        updateBonePose();
    }

    public GVRSceneObject createSkeletonGeometry(GVRSceneObject parent)
    {
        GeometryHelper helper = new GeometryHelper(this);
        return helper.createSkeletonGeometry(parent);
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(getClass().getSimpleName());
        sb.append(System.lineSeparator());
        sb.append(Log.getSpaces(indent) + 2);
        sb.append("numBones = " + Integer.toString(getNumBones()));
        sb.append(System.lineSeparator());
        for (int i = 0; i < getNumBones(); ++i)
        {
            String boneName = getBoneName(i);
            sb.append(Log.getSpaces(indent) + 4);
            sb.append(Integer.toString(i));
            sb.append(": ");
            sb.append(boneName);
        }
    }

    private static class GeometryHelper
    {
        private GVRSceneObject mSphereProto;
        private GVRSceneObject mCylProto;
        private final GVRSkeleton mSkeleton;

        public GeometryHelper(GVRSkeleton skel)
        {
            mSkeleton = skel;
        }

        public GVRSceneObject createSkeletonGeometry(GVRSceneObject root)
        {
            GVRContext ctx = mSkeleton.getGVRContext();
            GVRMaterial flatMaterialSphr = new GVRMaterial(ctx);
            GVRMaterial flatMaterialCyl = new GVRMaterial(ctx);

            flatMaterialSphr.setColor(1f, 1f, 0f);
            flatMaterialCyl.setColor(1f, 0f, 0f);
            mCylProto =  new GVRCylinderSceneObject(ctx, 0.2f, 0.2f, 1f, 2, 8, true);
            mSphereProto =  new GVRSphereSceneObject(ctx, true, flatMaterialSphr, 0.5f);
            mCylProto.getRenderData().setMaterial(flatMaterialCyl);
            GVRSceneObject rootGeo = makeSpheres();

            if (rootGeo.getParent() == null)
            {
                root.addChildObject(rootGeo);
            }
            mSkeleton.poseToBones();
            return root;
        }

        private GVRSceneObject makeSpheres()
        {
            GVRContext ctx = mSkeleton.getGVRContext();
            GVRPose bindPose = mSkeleton.getBindPose();
            Vector3f childDir = new Vector3f(0,0,0);

            for (int j = 0; j < mSkeleton.getNumBones(); j++)
            {
                GVRSceneObject boneGeo = mSkeleton.getBone(j);
                int parentIndex = mSkeleton.getParentBoneIndex(j);
                String boneName = mSkeleton.getBoneName(j);

                if (boneGeo == null)
                {
                    boneGeo = new GVRSceneObject(ctx, mSphereProto.getRenderData().getMesh(),
                                                mSphereProto.getRenderData().getMaterial());
                    mSkeleton.setBone(j, boneGeo);
                    boneGeo.setName(boneName);
                }
                else if (boneGeo.getRenderData() != null)
                {
                    continue;
                }
                else
                {
                    GVRRenderData rdata = new GVRRenderData(ctx);
                    rdata.setMesh(mSphereProto.getRenderData().getMesh());
                    rdata.setMaterial(mSphereProto.getRenderData().getMaterial());
                    boneGeo.attachRenderData(rdata);
                }
                if (parentIndex >= 0)
                {
                    GVRSceneObject parent = mSkeleton.getBone(parentIndex);
                    if (parent != null)
                    {
                        parent.addChildObject(boneGeo);
                        float height = calcCylHeight(j);
                        bindPose.getLocalPosition(j, childDir);

                        GVRSceneObject cyl = createBoneGeo(childDir, height);
                        cyl.setName(mSkeleton.getBoneName(parentIndex) + "_" + boneName);
                        parent.addChildObject(cyl);
                    }
                }
            }
            return mSkeleton.getBone(0);
        }

        private float calcCylHeight(int boneIndex)
        {
            Vector3f p = new Vector3f(0,0,0);

            mSkeleton.getBindPose().getLocalPosition(boneIndex, p);
            return (float) p.length();
        }

        private GVRSceneObject createBoneGeo(Vector3f boneDir, float height)
        {
            GVRContext ctx = mCylProto.getGVRContext();
            Vector3f downNormal = new Vector3f(0,-1,0);
            Quaternionf q = new Quaternionf(0,0,0,1);
            Quaternionf quatRot = q.rotateTo(downNormal, boneDir);
            GVRMesh oldMesh = mCylProto.getRenderData().getMesh();
            GVRVertexBuffer oldVerts = oldMesh.getVertexBuffer();
            GVRMesh newMesh = new GVRMesh(ctx, oldVerts.getDescriptor());
            float[] verts = oldMesh.getVertices();

            quatRot.normalize();
            for (int t = 0; t < verts.length; t += 3)
            {
                Vector3f dest = new Vector3f(0,0,0);
                quatRot.transform(verts[t], (verts[t + 1] - 0.5f) * height, verts[t + 2], dest);
                verts[t] = dest.x();
                verts[t + 1] = dest.y();
                verts[t + 2] = dest.z();
            }
            newMesh.setIndexBuffer(oldMesh.getIndexBuffer());
            newMesh.setNormals(oldMesh.getNormals());
            newMesh.setVertices(verts);
            return new GVRSceneObject(mCylProto.getGVRContext(), newMesh, mCylProto.getRenderData().getMaterial());
        }
    }
}

class NativeSkeleton
{
    static native long ctor(int[] boneParents);
    static native long getComponentType();
    static native boolean setPose(long object, float[] matrices);
    static native boolean setSkinPose(long object, float[] matrices);
}
