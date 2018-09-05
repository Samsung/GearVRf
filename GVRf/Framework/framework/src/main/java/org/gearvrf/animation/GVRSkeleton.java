package org.gearvrf.animation;

;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    final protected GVRSceneObject[] mBones;
    final protected int[] mParentBones;
    final protected int[] mBoneOptions;
    final private Quaternionf mTempQuatA = new Quaternionf();
    final private Quaternionf mTempQuatB = new Quaternionf();
    final private Matrix4f mTempMtx = new Matrix4f();

    protected String[] mBoneNames;
    protected Vector3f mRootOffset;     // offset for root bone animations
    protected Vector3f mBoneAxis;       // axis of bone, defines bone coordinate system
    protected GVRPose mBindPose;        // bind pose for this skeleton
    protected GVRPose mInverseBindPose; // inverse bind pose for this skeleton
    protected GVRPose mPose;            // current pose for this skeleton
    protected GVRPose mSkinPose;        // current pose for the skin
    protected final float[] mPoseMatrices;

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
        super(ctx, NativeSkeleton.ctor(parentBones.length));
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
        super(root.getGVRContext(), NativeSkeleton.ctor(boneNames.size()));
        int numBones = boneNames.size();
        mType = getComponentType();
        mParentBones = new int[numBones];
        mBoneAxis = new Vector3f(0, 0, 1);
        mRootOffset = new Vector3f(0, 0, 0);
        mBoneOptions = new int[numBones];
        mBoneNames = new String[numBones];
        mBones = new GVRSceneObject[numBones];
        mPoseMatrices =  new float[numBones * 16];
        int numRoots = 0;
        GVRSceneObject skelRoot = null;

        Arrays.fill(mParentBones, -1);
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

            setBoneName(boneId, boneName);
            if (parent != null)
            {
                String nodeName = parent.getName();
                parBoneId = boneNames.indexOf(nodeName);
            }
            if ((parBoneId < 0) && (skelRoot == null))
            {
                skelRoot = obj;
                Log.d("BONE", "Skeleton root %d is %s", numRoots, boneNames.get(boneId));
                ++numRoots;
            }
            mParentBones[boneId] = parBoneId;
        }
        mPose = new GVRPose(this);
        mBindPose = new GVRPose(this);
        if (skelRoot != null)
        {
            skelRoot.attachComponent(this);
        }
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
     * and connot be shared across skeletons. Setting the bind pose copies the
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
        mBindPose.copy(pose);
        mBindPose.sync();
        if (mInverseBindPose == null)
        {
            mInverseBindPose = new GVRPose(this);
        }
        mInverseBindPose.inverse(mBindPose);
        mPose.copy(mBindPose);
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
     * and connot be shared across skeletons. Setting the bind pose copies the
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
            Vector3f v = new Vector3f();
            newpose.getWorldPosition(0, v);
            mPose.setPosition(v.x, v.y, v.z);
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
                        srcBone.getWorldMatrix(mTempMtx);
                        mPose.setWorldMatrix(i, mTempMtx);
                    }
                }
            }
        }
        mPose.sync();
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
     *
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
        NativeSkeleton.setPose(getNative(), mPoseMatrices);
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
}

class NativeSkeleton
{
    static native long ctor(int numbones);
    static native long getComponentType();
    static native boolean setPose(long object, float[] matrices);
}
