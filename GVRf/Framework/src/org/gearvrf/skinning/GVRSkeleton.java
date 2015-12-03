package org.gearvrf.skinning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRBoneWeight;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRVertexBoneData;
import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Cell;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GVRSkeleton implements PrettyPrint {
    private static final String TAG = GVRSkeleton.class.getSimpleName();

    protected GVRContext gvrContext;
    protected GVRSceneObject sceneRoot;
    protected GVRSceneObject animatedNode;
    protected GVRSceneObject[] skeleton;
    protected Matrix4f[] boneOffsetInverse;
    protected Map<GVRSceneObject, List<GVRBone>> boneMap;

    /**
     * Constructs the skeleton for a list of {@link GVRSceneObject}.
     *
     * @param sceneRoot The scene root.
     * @param animatedNodes The list of scene objects to animate.
     */
    public GVRSkeleton(GVRContext gvrContext, GVRSceneObject sceneRoot, GVRSceneObject animatedNode) {
        this.gvrContext = gvrContext;
        this.sceneRoot = sceneRoot;
        this.animatedNode = animatedNode;

        Set<GVRSceneObject> skeletalNodes = new HashSet<GVRSceneObject>();
        boneMap = new HashMap<GVRSceneObject, List<GVRBone>>();

        prepareSkeleton(animatedNode, skeletalNodes);

        skeleton = new GVRSceneObject[skeletalNodes.size()];
        boneOffsetInverse = new Matrix4f[skeletalNodes.size()];

        Cell<Integer> boneIdx = new Cell<Integer>(0);
        sortBones(skeletalNodes, sceneRoot, -1, boneIdx);
        if (boneIdx.get() != skeletalNodes.size()) {
            Log.w(TAG, "Skeleton cannot be fully constructed (number of bones = %d, bone array size = %d)",
                    skeletalNodes.size(), boneIdx.get());
        } else {
            Log.v(TAG, "Skeleton is fully constructed with %d bones", boneIdx.get());
        }
    }

    /**
     * Update bone transform at each animation step.
     */
    public void animate(GVRKeyFrameAnimation animation, float animationTime) {
        // Visit nodes in topological order
        for (int nodeId = 0; nodeId < skeleton.length; ++nodeId) {
            GVRSceneObject node = skeleton[nodeId];

            // This node is not for a bone, so no need to compute its transform.
            // Bone transform is relative to the holding scene object's frame.
            if (boneMap.get(node) == null)
                continue;

            Matrix4f animationTransform;
            if (animation != null) {
                // update nodeTransform
                animationTransform = animation.getTransform(node.getName(), animationTime);
                if (animationTransform == null) {
                    animationTransform = new Matrix4f();
                }
            } else {
                // Debug: blank animation (z-axis rotation)
                animationTransform = new Matrix4f();
                animationTransform.rotation((float)(animationTime / 180 * Math.PI),
                                            new Vector3f(1f, 0, 0));
            }

            Matrix4f boneTransform = new Matrix4f();

            // Apply animation to bone
            boneTransform.mul(boneOffsetInverse[nodeId]);
            boneTransform.mul(animationTransform);

            // Transform all bone splits (a bone can be split into multiple instances if they influence
            // different meshes)
            for (GVRBone bone : boneMap.get(node)) {
                Matrix4f newBoneTransform = new Matrix4f(boneTransform);
                newBoneTransform.mul(bone.getOffsetMatrix());

                // Apply parent bone's transform (if any)
                GVRSceneObject parentNode = node.getParent();
                if (parentNode!= null && boneMap.get(parentNode) != null) {
                    newBoneTransform.mul(boneMap.get(parentNode).get(0).getFinalTransformMatrix());
                }

                bone.setFinalTransformMatrix(newBoneTransform);
            }
        }
    }

    /**
     * Prepares the minimal skeleton for animation.
     */
    protected void prepareSkeleton(GVRSceneObject animatedNode, Set<GVRSceneObject> skeletalNodes) {
        GVRMesh mesh;
        if (animatedNode.getRenderData() != null && (mesh = animatedNode.getRenderData().getMesh()) != null) {
            Log.v(TAG, "prepareSkeleton checking mesh with %d vertices", mesh.getVertices().length);

            GVRVertexBoneData vertexBoneData = mesh.getVertexBoneData();
            for (GVRBone bone : mesh.getBones()) {
                // Calculate vertex bone weights
                List<GVRBoneWeight> boneWeights = bone.getBoneWeights();
                int boneId = 0;
                for (GVRBoneWeight weight : boneWeights) {
                    int vid = weight.getVertexId();
                    int boneSlot = vertexBoneData.getFreeBoneSlot(vid);
                    if (boneSlot >= 0) {
                        vertexBoneData.setVertexBoneWeight(vid, boneSlot, boneId, weight.getWeight());
                    } else {
                        Log.w(TAG, "Vertex %d (total %d) in node %s has too many bones", vid,
                                mesh.getVertices().length, animatedNode.getName());
                    }
                    boneId++;
                }

                GVRSceneObject skeletalNode = sceneRoot.findChildByName(bone.getName());
                if (skeletalNode == null) {
                    Log.w(TAG, "what? cannot find the skeletal node for bone: %s", bone.toString());
                    continue;
                }

                // Link them for faster look-up
                bone.setSceneObject(skeletalNode);
                List<GVRBone> boneList = boneMap.get(skeletalNode);
                if (boneList == null) {
                    boneList = new ArrayList<GVRBone>();
                    boneMap.put(skeletalNode, boneList);
                }
                boneList.add(bone);

                // Follow parent chain until it is merged into the skeleton.
                // Invariant: skeletalNodes is always a tree
                GVRSceneObject sceneObject = skeletalNode;
                while (sceneObject != null && !skeletalNodes.contains(sceneObject)) {
                    skeletalNodes.add(sceneObject);
                    sceneObject = sceneObject.getParent();
                }
            }

            // Normalize vertex
            vertexBoneData.normalizeWeights();
        }

        // Traverse children recursively
        for (GVRSceneObject child : animatedNode.getChildren()) {
            prepareSkeleton(child, skeletalNodes);
        }
    }

    protected void sortBones(Set<GVRSceneObject> skeletalNodes, GVRSceneObject node, int parentId,
            Cell<Integer> boneIndex) {
        if (!skeletalNodes.contains(node))
            return;

        int myId = boneIndex.get();
        boneIndex.set(boneIndex.get() + 1);

        skeleton[myId] = node;
        List<GVRBone> boneList = boneMap.get(node);
        if (boneList != null) {
            boneOffsetInverse[myId] = new Matrix4f(boneList.get(0).getOffsetMatrix()).invert();
        }

        for (GVRSceneObject child : node.getChildren()) {
            sortBones(skeletalNodes, child, myId, boneIndex);
        }
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(GVRSkeleton.class.getSimpleName());
        sb.append(System.lineSeparator());

        sb.append(Log.getSpaces(indent + 2));
        sb.append("animatedNode: " + animatedNode);
        sb.append(System.lineSeparator());
        sb.append("skeleton: ");
        sb.append(System.lineSeparator());
        for (GVRSceneObject sceneObject : skeleton) {
            if (sceneObject != null) {
                sceneObject.prettyPrint(sb, indent + 4);
            } else {
                // Shouldn't happen unless there is a bug in the importer.
                sb.append("null");
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }
}