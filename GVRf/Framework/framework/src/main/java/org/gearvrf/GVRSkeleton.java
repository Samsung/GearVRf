package org.gearvrf;

import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GVRSkeleton extends GVRBehavior {
    public static final int BONE_LOCK_ROTATION = 1;// lock bone rotation
    public static final int BONE_ANIMATE = 4;    // keyframe bone animation
    public static final int BONE_PHYSICS = 2;    // use physics to compute bone motion

    private static final String TAG = GVRSkeleton.class.getSimpleName();
    static private long TYPE_SKELETON = newComponentType(GVRSkeleton.class);

    private GVRPose GVRPose;
    private final Matrix4f scratchGlobalInverse;
    private final Matrix4f finalMatrix;

    private int[] mBoneOptions;
    private Map<String, Integer> boneIndexMap;

    private int mNumBones;
    private int boneIndex = 0;

    private Map<String, List<GVRBone>> boneMap;

    public GVRSkeleton(GVRContext context) {
        super(context);
        boneMap = new HashMap<String, List<GVRBone>>();
        GVRPose = new GVRPose(this);
        scratchGlobalInverse = new Matrix4f();
        finalMatrix = new Matrix4f();
        boneIndexMap = new HashMap<String, Integer>();
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);
        //find bones on attach
        findBones();
    }

    public GVRPose getPose() {
        return GVRPose;
    }

    static public long getComponentType() {
        return TYPE_SKELETON;
    }

    /**
     * Examines the child engines for the skeleton looking for Transformers which match the
     * bones in the skeleton. Use this function instead of AttachBones if you have already created
     * the bone Transformer hierarchy and want to attach it to the skeleton.
     */
    private void findBones() {
        GVRSceneObject owner = getOwnerObject();

        if (owner == null) {
            return;
        }

        GVRSceneObject.SceneVisitor visitor = new GVRSceneObject.SceneVisitor() {
            @Override
            public boolean visit(GVRSceneObject sceneObject) {
                if (boneMap.get(sceneObject.getName()) != null) {
                    // this is a bone
                    boneIndexMap.put(sceneObject.getName(), boneIndex);
                    GVRPose.visit(sceneObject, boneIndex);
                    boneIndex++;
                }
                setupBone(sceneObject);
                return true;
            }
        };

        owner.forAllDescendants(visitor);

        mNumBones = boneIndex;
        mBoneOptions = new int[mNumBones];
    }

    public int getBoneIndex(String name) {
        Integer index = boneIndexMap.get(name);
        if (index == null) {
            index = -1;
        }
        return index;
    }

    protected void setupBone(GVRSceneObject node) {
        GVRMesh mesh;
        if (node.getRenderData() != null && (mesh = node.getRenderData().getMesh()) != null) {
            Log.v(TAG, "setupBone checking mesh with %d vertices", mesh.getVertices().length / 3);
            for (GVRBone bone : mesh.getBones()) {
                bone.setSceneObject(node);
                // Create look-up table for bones
                List<GVRBone> boneList = boneMap.get(bone.getName());
                if (boneList == null) {
                    boneList = new ArrayList<GVRBone>();
                    boneMap.put(bone.getName(), boneList);
                }
                boneList.add(bone);
            }
        }
    }

    /**
     * Use this call to update all the bone matrices once the new transforms have been set.
     *
     * Future: Could do this during onDrawFrame()
     */
    public void update() {
        /**
         * Update the GVRPose based on bone animations.
         * If any of the bones are animated, they will be marked as changed.
         */
        Quaternionf q = new Quaternionf();
        Vector3f p = new Vector3f();
        for (int i = 0; i < mNumBones; ++i) {

            GVRTransform trans = GVRPose.getSceneObject(i).getTransform();
            if ((mBoneOptions[i] & BONE_ANIMATE) != 0) {
                q.set(trans.getRotationX(), trans.getRotationY(), trans.getRotationZ(), trans
                        .getRotationW());
                Matrix4f matrix4f = trans.getLocalModelMatrix4f();
                matrix4f.getTranslation(p);
                GVRPose.setLocalRotation(i, q);
            }
        }

        // GVRPose sync
        GVRPose.sync();

        for (Map.Entry<String, List<GVRBone>> ent : boneMap.entrySet()) {
            // Transform all bone splits (a bone can be split into multiple instances if they
            // influence different meshes)
            for (GVRBone bone : ent.getValue()) {
                updateBoneMatrices(bone);
            }
        }
    }

    /**
     * Get rotation and physics options for this bone
     */
    public int getBoneOptions(int boneIndex) {
        if (boneIndex < 0) {
            Log.d(TAG, "No bone found for " + boneIndex);
            return -1;
        }
        return mBoneOptions[boneIndex];
    }

    /**
     * Set rotation and physics options for this bone
     *
     * @param options options to control how this bone moves
     *                BONE_PHYSICS will use physics (rigid body dynamics) to calculate
     *                the motion of the bone
     *                BONE_LOCK_ROTATION will lock the bone rotation, freezing its current
     *                local rotation
     */
    public void setBoneOptions(int boneIndex, int options) {
        if (boneIndex < 0) {
            Log.d(TAG, "No bone found for " + boneIndex);
            return;
        }
        mBoneOptions[boneIndex] = options;
    }

    /**
     * Determine if given bone is locked or not.
     *
     * @param boneIndex 0 based bone index
     */
    public boolean isLocked(int boneIndex) {
        return (mBoneOptions[boneIndex] & BONE_LOCK_ROTATION) != 0;
    }

    protected void updateBoneMatrices(GVRBone bone) {
        finalMatrix.set(bone.getOffsetMatrixFloatArray());
        Matrix4f globalTransform = GVRPose.getWorldMatrix(boneIndexMap.get(bone.getName()));
        globalTransform.mul(finalMatrix, finalMatrix);
        scratchGlobalInverse.set(bone.getSceneObject().getTransform().getModelMatrix());
        scratchGlobalInverse.invert();
        scratchGlobalInverse.mul(finalMatrix, finalMatrix);
        bone.setFinalTransformMatrix(finalMatrix);
    }
}
