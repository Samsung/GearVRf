package org.gearvrf.animation.keyframe;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

public class GVRKeyFrameAnimation extends GVRAnimation implements PrettyPrint {
    protected String mName;
    protected float mTicksPerSecond;
    protected float mDurationTicks;
    protected List<GVRAnimationChannel> mChannels;

    protected GVRNodeAnimationController mNodeAnimationController;
    protected GVRSkinningController mSkinningController;

    protected GVRSceneObject mTarget;
    protected Matrix4f[] mTransforms;

    public GVRKeyFrameAnimation(String name, GVRSceneObject target, float durationTicks, float ticksPerSecond) {
    	super(target, durationTicks / ticksPerSecond);
        mName = name;
        mDurationTicks = durationTicks;
        mTicksPerSecond = ticksPerSecond;
        mChannels = new ArrayList<GVRAnimationChannel>();

        mNodeAnimationController = null;
        mSkinningController = null;

        mTarget = target;
    }

    /**
     * Add a channel to the animation.
     * @param channel The animation channel.
     */
    public void addChannel(GVRAnimationChannel channel) {
        mChannels.add(channel);
    }

    /**
     * Must be called after adding all channels.
     */
    public void prepare() {
        mNodeAnimationController = new GVRNodeAnimationController(mTarget, this);

        mSkinningController = new GVRSkinningController(mTarget, this);
        mTransforms = new Matrix4f[mChannels.size()];
        for (int i = 0; i < mTransforms.length; ++i) {
            mTransforms[i] = new Matrix4f();
        }
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRKeyFrameAnimation.class.getSimpleName());
        sb.append("[name=" + mName + ", ticksPerSecond="
                + mTicksPerSecond + ", duration=" + mDurationTicks + ", "
                + mChannels.size() + " channels]");
        sb.append(System.lineSeparator());

        for (GVRAnimationChannel nodeAnim : mChannels) {
            nodeAnim.prettyPrint(sb, indent + 2);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    public int findChannel(String nodeName) {
        int i = 0;
        for (GVRAnimationChannel channel : mChannels) {
            if (nodeName != null && nodeName.equals(channel.getNodeName())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        if (mTarget != target) {
            return;
        }

        if (mNodeAnimationController == null || mSkinningController == null) {
            throw new RuntimeException("Animation is not prepared. Call prepare() before starting.");
        }

        mNodeAnimationController.animate(getDuration() * ratio);

        mSkinningController.animate(getDuration() * ratio);
    }

    protected Matrix4f[] getTransforms(float animationTime) {
        int i = 0;
        for (GVRAnimationChannel channel : mChannels) {
            mTransforms[i++].set(channel.animate(animationTime));
        }
        return mTransforms;
    }
}
