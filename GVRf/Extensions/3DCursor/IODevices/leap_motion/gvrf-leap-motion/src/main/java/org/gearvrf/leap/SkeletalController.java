package org.gearvrf.leap;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Use this class to control the skeletal structure of a loaded model.
 */
class SkeletalController {
    private static final String TAG = SkeletalController.class.getSimpleName();
    private static int UPDATED_GLOBAL_POSITION = 1 << 0;
    private static int UPDATED_GLOBAL_ROTATION = 1 << 1;
    private static int UPDATED_LOCAL_TRANSFORM = 1 << 2;
    protected GVRContext gvrContext;
    protected GVRSceneObject rootSceneObject;
    protected SceneNode rootSceneNode;
    private Map<String, SceneNode> nodeByName;
    protected Map<GVRSceneObject, List<GVRBone>> boneMap;

    // temp objects to reduce GC cycles
    private final Vector3f scratchRotationVector;
    private final Matrix4f scratchGlobalInverse;
    private final Matrix4f finalMatrix;

    public class SceneNode {
        private int updateStatus;
        public GVRSceneObject sceneObject;
        private SceneNode parent;
        private List<SceneNode> children;
        public Matrix4f localTransform;
        private Matrix4f globalTransform;
        private boolean isValid;
        private Vector3f globalPosition;
        private Quaternionf globalRotation;

        SceneNode(GVRSceneObject sceneObject, SceneNode parent) {
            this.sceneObject = sceneObject;
            this.parent = parent;
            children = new ArrayList<SceneNode>();
            localTransform = new Matrix4f();
            globalTransform = new Matrix4f();
            isValid = true;
            globalPosition = new Vector3f();
            globalRotation = new Quaternionf();
        }

        void setIsValid(boolean isValid) {
            this.isValid = isValid;
        }
    }

    /**
     * Constructs the skeletal controller for the provided {@link GVRSceneObject}
     *
     * @param sceneObject The corresponding scene object.
     */
    public SkeletalController(GVRSceneObject sceneObject) {
        this.rootSceneObject = sceneObject;
        nodeByName = new TreeMap<String, SceneNode>();
        boneMap = new HashMap<GVRSceneObject, List<GVRBone>>();
        scratchRotationVector = new Vector3f();
        scratchGlobalInverse = new Matrix4f();
        finalMatrix = new Matrix4f();
        this.rootSceneNode = createTree(sceneObject, null);
    }

    protected SceneNode createTree(GVRSceneObject node, SceneNode parent) {

        SceneNode internalNode = new SceneNode(node, parent);
        nodeByName.put(node.getName(), internalNode);

        // Bind-pose local transform
        internalNode.localTransform.set(node.getTransform().getLocalModelMatrix4f());
        internalNode.globalTransform.set(internalNode.localTransform);

        // Global transform
        if (parent != null) {
            parent.globalTransform.mul(internalNode.globalTransform, internalNode.globalTransform);
        }

        setupBone(node);

        for (GVRSceneObject child : node.getChildren()) {
            SceneNode sceneNode = createTree(child, internalNode);
            internalNode.children.add(sceneNode);
        }

        return internalNode;
    }

    protected void setupBone(GVRSceneObject node) {
        GVRMesh mesh;
        if (node.getRenderData() != null && (mesh = node.getRenderData().getMesh()) != null) {
            Log.v(TAG, "setupBone checking mesh with %d vertices", mesh.getVertices().length / 3);
            for (GVRBone bone : mesh.getBones()) {
                bone.setSceneObject(node);

                GVRSceneObject skeletalNode = rootSceneObject.getSceneObjectByName(bone.getName());
                if (skeletalNode == null) {
                    Log.w(TAG, "what? cannot find the skeletal node for bone: %s", bone.toString());
                    continue;
                }

                // Create look-up table for bones
                List<GVRBone> boneList = boneMap.get(skeletalNode);
                if (boneList == null) {
                    boneList = new ArrayList<GVRBone>();
                    boneMap.put(skeletalNode, boneList);
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
        updateTransforms(rootSceneNode, new Matrix4f());

        for (Entry<GVRSceneObject, List<GVRBone>> ent : boneMap.entrySet()) {
            // Transform all bone splits (a bone can be split into multiple instances if they
            // influence different meshes)
            SceneNode node = nodeByName.get(ent.getKey().getName());
            for (GVRBone bone : ent.getValue()) {
                updateBoneMatrices(bone, node);
            }
        }
    }

    /**
     * Update the global/world position of the provided bone.
     *
     * @param bone     the corresponding {@link GVRBone} object
     * @param position the new position in world coordinates.
     */
    public void updateGlobalPosition(GVRBone bone, Vector3f position) {
        SceneNode node = nodeByName.get(bone.getName());
        node.updateStatus = node.updateStatus | UPDATED_GLOBAL_POSITION;
        node.globalPosition.set(position);
    }

    /**
     * Update the global/world rotation of the provided bone.
     *
     * @param bone     the corresponding {@link GVRBone} object
     * @param rotation the new rotation in world coordinates.
     */
    public void updateGlobalRotation(GVRBone bone, Quaternionf rotation) {
        SceneNode node = nodeByName.get(bone.getName());
        node.updateStatus = node.updateStatus | UPDATED_GLOBAL_ROTATION;
        node.globalRotation.set(rotation);
    }

    /**
     * Update the local matrix transform of the provided {@link GVRSceneObject}
     *
     * @param sceneObject the corresponding {@link GVRSceneObject}
     * @param matrix4f    the new local transform matrix
     */
    public void updateLocalMatrix(GVRSceneObject sceneObject, Matrix4f matrix4f) {
        SceneNode node = nodeByName.get(sceneObject.getName());
        node.updateStatus = node.updateStatus | UPDATED_LOCAL_TRANSFORM;
        node.localTransform.set(matrix4f);
    }

    /**
     * Update the local matrix transform of the provided {@link GVRSceneObject}
     *
     * @param bone     the corresponding {@link GVRSceneObject}
     * @param matrix4f the new local transform matrix
     */
    public void updateLocalMatrix(GVRBone bone, Matrix4f matrix4f) {
        SceneNode node = nodeByName.get(bone.getName());
        node.updateStatus = node.updateStatus | UPDATED_LOCAL_TRANSFORM;
        node.localTransform.set(matrix4f);
    }

    public SceneNode getSceneNode(GVRBone bone) {
        return nodeByName.get(bone.getName());
    }

    protected void updateTransforms(SceneNode node, Matrix4f parentTransform) {

        if ((node.updateStatus & UPDATED_LOCAL_TRANSFORM) != 0) {
            //do nothing
        } else {
            node.localTransform.set(node.sceneObject.getTransform().getLocalModelMatrix4f());
        }

        parentTransform.mul(node.localTransform, node.globalTransform);

        if ((node.updateStatus & UPDATED_GLOBAL_ROTATION) != 0) {
            node.globalRotation.getEulerAnglesXYZ(scratchRotationVector);
            node.globalTransform.setRotationXYZ(scratchRotationVector.x, scratchRotationVector.y,
                    scratchRotationVector.z);
        }

        if ((node.updateStatus & UPDATED_GLOBAL_POSITION) != 0) {
            node.globalTransform.setTranslation(node.globalPosition.x, node.globalPosition.y,
                    node.globalPosition.z);
        }

        // reset the update status
        node.updateStatus = 0;

        for (SceneNode child : node.children) {
            updateTransforms(child, node.globalTransform);
        }
    }

    protected void updateBoneMatrices(GVRBone bone, SceneNode node) {
        finalMatrix.set(bone.getOffsetMatrix());

        node.globalTransform.mul(finalMatrix, finalMatrix);

        scratchGlobalInverse.set(bone.getSceneObject().getTransform().getModelMatrix());
        scratchGlobalInverse.invert();
        scratchGlobalInverse.mul(finalMatrix, finalMatrix);

        bone.setFinalTransformMatrix(finalMatrix);
    }

    /**
     * Prune the tree to remove nodes that may be considered invalid using the
     * {@link #setInvalid(GVRSceneObject)} call.
     */
    public void pruneTree() {
        pruneTree(rootSceneNode);
    }

    /**
     * This is a part of the skeletal controller optimization.
     *
     * Select parts of the model hierarchy as invalid to prune the
     * tree of nodes that do not impact the skeletal transform.
     *
     * Nodes that are marked as invalid using this call will be <b>considered</b> for pruning
     * when the call to {@link #pruneTree()} is made.
     *
     * @param sceneObject the {@link GVRSceneObject} to be marked invalid.
     */
    public void setInvalid(GVRSceneObject sceneObject) {
        SceneNode node = nodeByName.get(sceneObject.getName());
        node.setIsValid(false);
    }

    /* Returns true if the subtree should be kept */
    protected boolean pruneTree(SceneNode node) {
        boolean keep = node.isValid;

        if (keep) {
            return keep;
        }

        Iterator<SceneNode> iter = node.children.iterator();
        while (iter.hasNext()) {
            SceneNode child = iter.next();

            boolean keepChild = pruneTree(child);
            keep |= keepChild;
            if (!keepChild) {
                iter.remove();
            }
        }

        return keep;
    }
}