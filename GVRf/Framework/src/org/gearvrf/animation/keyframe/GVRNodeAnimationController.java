package org.gearvrf.animation.keyframe;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;

/**
 * Controls node animation.
 */
public class GVRNodeAnimationController {
    private static final String TAG = GVRNodeAnimationController.class.getSimpleName();

    protected GVRSceneObject sceneRoot;
    protected GVRKeyFrameAnimation animation;

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
        this.sceneRoot = sceneRoot;
        this.animation = animation;

        animatedNodes = new ArrayList<AnimationItem>();
        if (animation != null) {
            scanTree(sceneRoot);
        }
    }

    protected void scanTree(GVRSceneObject node) {
        // Find channel Id
        int channelId = animation.findChannel(node.getName());
        if (channelId != -1) {
            animatedNodes.add(new AnimationItem(node, channelId));
        }

        for (GVRSceneObject child : node.getChildren()) {
            scanTree(child);
        }
    }

    /**
     * Update node transforms at each animation step.
     */
    public void animate(float timeInSeconds) {
        float ticksPerSecond;
        float timeInTicks;
        Matrix4f[] animationTransform = null;

        if (animation.mTicksPerSecond != 0) {
            ticksPerSecond = (float) animation.mTicksPerSecond;
        } else {
            ticksPerSecond = 25.0f;
        }
        timeInTicks = timeInSeconds * ticksPerSecond;

        float animationTime = timeInTicks % animation.mDurationTicks; // auto-repeat
        animationTransform = animation.getTransforms(animationTime);

        for (AnimationItem item : animatedNodes) {
            item.target.getTransform().setModelMatrix(animationTransform[item.channelId]);
        }
    }
}