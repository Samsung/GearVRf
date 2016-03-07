package org.gearvrf.animation.keyframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

/**
 * Controls skeletal animation (skinning). 
 */
public class GVRSkinningController extends GVRAnimationController {
    private static final String TAG = GVRSkinningController.class.getSimpleName();

    protected GVRContext gvrContext;
    protected GVRSceneObject sceneRoot;

    protected SceneAnimNode animRoot;
    protected Map<String, SceneAnimNode> nodeByName;
    protected Map<GVRSceneObject, List<GVRBone>> boneMap;

    protected class SceneAnimNode {
        GVRSceneObject sceneObject;
        SceneAnimNode parent;
        List<SceneAnimNode> children;
        Matrix4f localTransform;
        Matrix4f globalTransform;
        int channelId;

        SceneAnimNode(GVRSceneObject sceneObject, SceneAnimNode parent) {
            this.sceneObject = sceneObject;
            this.parent = parent;
            children = new ArrayList<SceneAnimNode>();
            localTransform = new Matrix4f();
            globalTransform = new Matrix4f();
            channelId = -1;
        }
    }

    /**
     * Constructs the skeleton for a list of {@link GVRSceneObject}.
     *
     * @param gvrContext The GVR context.
     * @param sceneRoot The scene root.
     * @param animation The animation object.
     */
    public GVRSkinningController(GVRSceneObject sceneRoot, GVRKeyFrameAnimation animation) {
        super(animation);
        this.sceneRoot = sceneRoot;

        nodeByName = new TreeMap<String, SceneAnimNode>();
        boneMap = new HashMap<GVRSceneObject, List<GVRBone>>();

        animRoot = createAnimationTree(sceneRoot, null);
        pruneTree(animRoot);
    }

    protected SceneAnimNode createAnimationTree(GVRSceneObject node, SceneAnimNode parent) {
        SceneAnimNode internalNode = new SceneAnimNode(node, parent);
        nodeByName.put(node.getName(), internalNode);

        // Bind-pose local transform
        internalNode.localTransform.set(node.getTransform().getLocalModelMatrix4f());
        internalNode.globalTransform.set(internalNode.localTransform);

        // Global transform
        if (parent != null) {
            parent.globalTransform.mul(internalNode.globalTransform, internalNode.globalTransform);
        }

        // Find channel Id
        if (animation != null) {
            internalNode.channelId = animation.findChannel(node.getName());
        }

        setupBone(node, internalNode);

        for (GVRSceneObject child : node.getChildren()) {
            SceneAnimNode animChild = createAnimationTree(child, internalNode);
            internalNode.children.add(animChild);
        }

        return internalNode;
    }

    protected void setupBone(GVRSceneObject node, SceneAnimNode internalNode) {
        GVRMesh mesh;
        if (node.getRenderData() != null && (mesh = node.getRenderData().getMesh()) != null) {
            Log.v(TAG, "setupBone checking mesh with %d vertices", mesh.getVertices().length / 3);
            for (GVRBone bone : mesh.getBones()) {
                bone.setSceneObject(node);

                GVRSceneObject skeletalNode = sceneRoot.getSceneObjectByName(bone.getName());
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
     * Update bone transforms for the specified tick.
     */
    @Override
    protected void animateImpl(float animationTick) {
        Matrix4f[] animationTransform = animation.getTransforms(animationTick);

        updateTransforms(animRoot, new Matrix4f(), animationTransform);

        for (Entry<GVRSceneObject, List<GVRBone>> ent : boneMap.entrySet()) {
            // Transform all bone splits (a bone can be split into multiple instances if they influence
            // different meshes)
            SceneAnimNode node = nodeByName.get(ent.getKey().getName());
            for (GVRBone bone : ent.getValue()) {
                updateBoneMatrices(bone, node);
            }
        }
    }

    protected void updateTransforms(SceneAnimNode node, Matrix4f parentTransform, Matrix4f[] animationTransform) {
        if (node.channelId != -1) {
            node.localTransform.set(animationTransform[node.channelId]);
        } else {
            // Default local transform
            node.localTransform.set(node.sceneObject.getTransform().getLocalModelMatrix4f());
        }

        parentTransform.mul(node.localTransform, node.globalTransform);

        for (SceneAnimNode child : node.children) {
            updateTransforms(child, node.globalTransform, animationTransform);
        }
    }

    protected void updateBoneMatrices(GVRBone bone, SceneAnimNode node) {
        Matrix4f finalMatrix = new Matrix4f().set(bone.getOffsetMatrix());

        node.globalTransform.mul(finalMatrix, finalMatrix);

        Matrix4f globalInverse = new Matrix4f().set(bone.getSceneObject().getTransform().getModelMatrix4f()).invert();
        globalInverse.mul(finalMatrix, finalMatrix);

        bone.setFinalTransformMatrix(finalMatrix);
    }

    /* Returns true if the subtree should be kept */
    protected boolean pruneTree(SceneAnimNode node) {
        boolean keep = node.channelId != -1;
        if (keep) {
            return keep;
        }

        Iterator<SceneAnimNode> iter = node.children.iterator();
        while (iter.hasNext()) {
            SceneAnimNode child = iter.next();
            boolean keepChild = pruneTree(child);
            keep |= keepChild;
            if (!keepChild) {
                iter.remove();
            }
        }

        return keep;
    }
}