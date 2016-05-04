package org.gearvrf.animation.keyframe;

public abstract class GVRAnimationController {
    private static final float TOL = 1e-6f;
    protected final GVRKeyFrameAnimation animation;

    public GVRAnimationController(GVRKeyFrameAnimation animation) {
        this.animation = animation;
    }

    /**
     * Update animation to {@code timeInSeconds}. This function converts
     * time to ticks and invokes {@link #animateImpl}.
     */
    public void animate(float timeInSeconds) {
        float ticksPerSecond;
        float timeInTicks;

        if (animation.mTicksPerSecond != 0) {
            ticksPerSecond = (float) animation.mTicksPerSecond;
        } else {
            ticksPerSecond = 25.0f;
        }
        timeInTicks = timeInSeconds * ticksPerSecond;

        float animationTick = timeInTicks % (animation.mDurationTicks + TOL); // auto-repeat
        animateImpl(animationTick);
    }

    /**
     * Animate to a tick in the timeline.
     * @param animationTick
     *         The tick to animate to.
     */
    protected abstract void animateImpl(float animationTick);
}
