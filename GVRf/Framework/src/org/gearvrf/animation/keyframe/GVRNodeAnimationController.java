package org.gearvrf.animation.keyframe;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;

/**
 * Controls node animation.
 */
public class GVRNodeAnimationController extends GVRAnimationController {
    private static final String TAG = GVRNodeAnimationController.class.getSimpleName();
    protected GVRSceneObject sceneRoot;

    protected class AnimationItem {
        GVRSceneObject target;
        int channelId;

        AnimationItem(GVRSceneObject target, int channelId) {
            this.target = target;
            this.channelId = channelId;
        }
    }

    protected List<AnimationItem> animatedNodes;

    /**
     * Constructs a list of animated {@link GVRSceneObject}.
     *
     * @param gvrContext The GVR context.
     * @param sceneRoot The scene root.
     * @param animation The animation object.
     */
    public GVRNodeAnimationController(GVRSceneObject sceneRoot, GVRKeyFrameAnimation animation) {
        super(animation);
        this.sceneRoot = sceneRoot;

        animatedNodes = new ArrayList<AnimationItem>();
        if (animation != null) {
            scanTree(sceneRoot);
        }
    }

    /* Returns true if subtree contains renderables */
    protected boolean scanTree(GVRSceneObject node) {
        boolean containsRenderable = node.getRenderData() != null;

        for (GVRSceneObject child : node.getChildren()) {
            containsRenderable |= scanTree(child);
        }

        // Find channel Id
        int channelId = animation.findChannel(node.getName());
        if (channelId != -1 && containsRenderable) {
            animatedNodes.add(new AnimationItem(node, channelId));
        }

        return containsRenderable;
    }

    /**
     * Update node transforms to a tick.
     * @param animationTick
     *         The tick to animate to.
     */
    @Override
    protected void animateImpl(float animationTick) {
        Matrix4f[] animationTransform = animation.getTransforms(animationTick);

        for (AnimationItem item : animatedNodes) {
            item.target.getTransform().setModelMatrix(animationTransform[item.channelId]);
        }
    }
}