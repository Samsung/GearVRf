package org.gearvrf.animation.keyframe;

import java.util.HashMap;
import java.util.Map;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSkeleton;
import org.joml.Matrix4f;

/**
 * Controls skeletal animation (skinning).
 */
public class GVRSkinningController extends GVRAnimationController {
    private GVRSkeleton GVRSkeleton;
    private Map<Integer, GVRSceneObject> channelToNode;

    /**
     * Constructs the GVRSkeleton for a list of {@link GVRSceneObject}.
     *
     * @param sceneRoot The scene root.
     * @param animation The animation object.
     */

    public GVRSkinningController(GVRSceneObject sceneRoot, GVRKeyFrameAnimation animation) {
        super(animation);
        GVRSkeleton = new GVRSkeleton(sceneRoot.getGVRContext());
        sceneRoot.attachComponent(GVRSkeleton);
        channelToNode = new HashMap<>();
        parseTree(sceneRoot);
    }

    void parseTree(GVRSceneObject sceneObject) {
        int channelId = animation.findChannel(sceneObject.getName());
        if (channelId != -1) {
            channelToNode.put(channelId, sceneObject);
            int index = GVRSkeleton.getBoneIndex(sceneObject.getName());
            if (index != -1) {
                GVRSkeleton.setBoneOptions(index, GVRSkeleton.BONE_ANIMATE);
            }
        }
        for (GVRSceneObject child : sceneObject.getChildren()) {
            parseTree(child);
        }
    }

    @Override
    protected void animateImpl(float animationTick) {
        int i = 0;
        for (GVRAnimationChannel channel : animation.mChannels) {
            GVRSceneObject sceneObject = channelToNode.get(i++);
            if (sceneObject == null) {
                continue;
            }

            Matrix4f matrix4f = channel.animate(animationTick);
            sceneObject.getTransform().setModelMatrix(matrix4f);
        }
        GVRSkeleton.update();
    }
}