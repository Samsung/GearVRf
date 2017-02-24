package org.gearvrf;

import android.util.SparseArray;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class to control the skeletal structure of
 * a loaded model.
 */
public class GVRPose {
    private static final String TAG = GVRPose.class.getSimpleName();
    private static int UPDATED_GLOBAL_POSITION = 1 << 0;
    private static int UPDATED_GLOBAL_ROTATION = 1 << 1;
    private static int UPDATED_LOCAL_TRANSFORM = 1 << 2;
    protected GVRContext gvrContext;

    private SparseArray<Bone> boneByName;

    // temp objects to reduce GC cycles
    private final Vector3f scratchRotationVector;
    private GVRSkeleton GVRSkeleton;

    private class Bone {
        private int updateStatus;
        private GVRSceneObject sceneObject;
        private Bone parent;
        private List<Bone> children;
        private int parentIndex;

        // following matrices reduce load on GC
        private Matrix4f localTransform;
        private Matrix4f globalTransform;
        private Vector3f globalPosition;
        private Quaternionf globalRotation;
        private Vector3f localPosition;
        private Quaternionf localRotation;

        Bone(GVRSceneObject sceneObject, Bone parent, int parentIndex) {
            this.sceneObject = sceneObject;
            this.parent = parent;
            this.parentIndex = parentIndex;
            children = new ArrayList<Bone>();
            localTransform = new Matrix4f();
            globalTransform = new Matrix4f();
            globalPosition = new Vector3f();
            globalRotation = new Quaternionf();
            localPosition = new Vector3f();
            localRotation = new Quaternionf();
        }
    }

    /**
     * Constructs the skeletal controller for the provided {@link GVRSkeleton}
     */
    public GVRPose(GVRSkeleton GVRSkeleton) {
        this.GVRSkeleton = GVRSkeleton;
        boneByName = new SparseArray<Bone>();
        scratchRotationVector = new Vector3f();
    }

    void visit(GVRSceneObject node, int boneIndex) {
        GVRSceneObject parentNode = node.getParent();
        Bone parent = null;
        int parentIndex = -1;
        if (parentNode != null) {
            parentIndex = GVRSkeleton.getBoneIndex(parentNode.getName());
            if (parentIndex != -1) {
                parent = boneByName.get(parentIndex);
            }
        }

        Bone internalNode = new Bone(node, parent, parentIndex);
        boneByName.put(boneIndex, internalNode);

        // Bind-pose local transform
        internalNode.localTransform.set(node.getTransform().getLocalModelMatrix4f());
        internalNode.globalTransform.set(node.getTransform().getModelMatrix4f());

        internalNode.localTransform.getUnnormalizedRotation(internalNode.localRotation);
        internalNode.localTransform.getTranslation(internalNode.localPosition);

        // Global transform
        if (parent != null) {
            parent.children.add(internalNode);
        }

        internalNode.globalTransform.getUnnormalizedRotation(internalNode.globalRotation);
        internalNode.globalTransform.getTranslation(internalNode.globalPosition);
    }

    /**
     * Sync the bone transformations
     */
    public void sync() {

        for (int i = 0; i < boneByName.size(); i++) {
            Bone node = boneByName.get(i);
            int parentIndex = node.parentIndex;
            Matrix4f parentTransform;

            if (parentIndex == -1) {
                // no parent bone
                parentTransform = node.sceneObject.getParent().getTransform().getModelMatrix4f();
            } else {
                //has a parent bone
                parentTransform = node.parent.globalTransform;
            }

            parentTransform.mul(node.localTransform, node.globalTransform);

            if ((node.updateStatus & UPDATED_GLOBAL_ROTATION) != 0) {
                node.globalRotation.getEulerAnglesXYZ(scratchRotationVector);
                node.globalTransform.setRotationXYZ(scratchRotationVector.x,
                        scratchRotationVector.y, scratchRotationVector.z);
            }

            if ((node.updateStatus & UPDATED_GLOBAL_POSITION) != 0) {
                node.globalTransform.setTranslation(node.globalPosition.x, node.globalPosition.y,
                        node.globalPosition.z);
            }

            // reset the update status
            node.updateStatus = 0;
        }
    }

    GVRSceneObject getSceneObject(int boneIndex) {
        Bone node = boneByName.get(boneIndex);
        if (node != null) {
            return node.sceneObject;
        }

        return null;
    }

    /**
     * Update the global/world position of the provided bone.
     *
     * @param position the new position in world coordinates.
     */
    public void setWorldPosition(int boneIndex, Vector3f position) {
        Bone node = boneByName.get(boneIndex);
        if (node == null) {
            throw new IllegalArgumentException("Bone not found");
        }
        node.updateStatus = node.updateStatus | UPDATED_GLOBAL_POSITION;
        node.globalPosition.set(position);
    }

    /**
     * Update the global/world rotation of the provided bone.
     *
     * @param rotation the new rotation in world coordinates.
     */
    public void setWorldRotation(int boneIndex, Quaternionf rotation) {
        if (GVRSkeleton.isLocked(boneIndex)) {
            return;
        }

        Bone node = boneByName.get(boneIndex);
        if (node == null) {
            throw new IllegalArgumentException("Bone not found");
        }
        node.updateStatus = node.updateStatus | UPDATED_GLOBAL_ROTATION;
        node.globalRotation.set(rotation);
    }

    /**
     * Update the local matrix transform of the provided bone
     *
     * @param matrix4f the new local transform matrix
     */
    public void setLocalMatrix(int boneIndex, Matrix4f matrix4f) {
        Bone node = boneByName.get(boneIndex);

        if (node == null) {
            throw new IllegalArgumentException("Bone not found");
        }

        node.updateStatus = node.updateStatus | UPDATED_LOCAL_TRANSFORM;
        node.localTransform.set(matrix4f);
    }

    /**
     * Update the local matrix transform of the provided bone
     *
     * @param rotation the new local rotation
     */
    public void setLocalRotation(int boneIndex, Quaternionf rotation) {
        if (GVRSkeleton.isLocked(boneIndex)) {
            return;
        }

        Bone node = boneByName.get(boneIndex);
        if (node == null) {
            throw new IllegalArgumentException(String.format("Bone not found"));
        }
        node.updateStatus = node.updateStatus | UPDATED_LOCAL_TRANSFORM;

        node.localTransform.identity();
        node.localTransform.rotate(rotation);
        node.localTransform.setTranslation(node.localPosition);
    }

    /**
     * Get the local matrix transform of the provided bone
     *
     * @param boneIndex the index of the corresponding bone object
     */
    public Matrix4f getLocalMatrix(int boneIndex) {
        Bone node = boneByName.get(boneIndex);
        if (node == null) {
            throw new IllegalArgumentException(String.format("Bone not found"));
        }
        return node.localTransform;
    }

    /**
     * Get the world matrix transform of the provided bone
     *
     * @param boneIndex the index of the corresponding bone object
     */
    public Matrix4f getWorldMatrix(int boneIndex) {
        Bone node = boneByName.get(boneIndex);
        if (node == null) {
            throw new IllegalArgumentException(String.format("Bone %d not found", boneIndex));
        }
        return node.globalTransform;
    }
}