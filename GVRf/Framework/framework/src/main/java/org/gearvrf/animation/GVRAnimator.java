package org.gearvrf.animation;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Group of animations that can be collectively manipulated.
 *
 * Typically the animations belong to a particular model and
 * represent a sequence of poses for the model over time.
 * This class allows you to start, stop and set animation modes
 * for all the animations in the group at once.
 * An asset which has animations will have this component
 * attached to collect the animations for the asset.
 *
 * @see org.gearvrf.GVRAssetLoader
 * @see org.gearvrf.GVRExternalScene
 * @see GVRAnimator
 * @see GVRAnimationEngine
 */
public class GVRAnimator extends GVRBehavior
{
    private static final String TAG = Log.tag(GVRAnimator.class);
    static private long TYPE_ANIMATOR = newComponentType(GVRAnimator.class);
    protected List<GVRAnimation> mAnimations;
    protected boolean mAutoStart;

    /**
     * Make an instance of the GVRAnimator component.
     * Auto-start is not enabled - a call to start() is
     * required to run the animations.
     *
     * @param ctx GVRContext for this animator
     */
    public GVRAnimator(GVRContext ctx)
    {
        super(ctx);
        mType = getComponentType();
        mAutoStart = false;
        mAnimations = new ArrayList<GVRAnimation>();
    }

    /**
     * Make an instance of the GVRAnimator component.
     * If auto start is enabled the animations should automatically
     * be started. Otherwise an explicit call to start() is
     * required to start them.
     *
     * @param ctx       GVRContext for this animator
     * @param autoStart true to automatically start animations.
     */
    public GVRAnimator(GVRContext ctx, boolean autoStart)
    {
        super(ctx);
        mType = getComponentType();
        mAutoStart = autoStart;
        mAnimations = new ArrayList<GVRAnimation>();
    }

    static public long getComponentType() { return TYPE_ANIMATOR; }

    /**
     * Determine if this animator should start all the animations
     * automatically or require an explicit call to start().
     * @return true if auto start is enabled, false if not
     */
    public boolean autoStart()
    {
        return mAutoStart;
    }

    /**
     * Adds an animation to this animator.
     * <p>
     * This animation will participate in any subsequent operations
     * but it's state will not be changed when added. For example,
     * if the existing animations in this animator are already running
     * the new one will not be started.
     *
     * @param anim animation to add
     * @see GVRAnimator#removeAnimation(GVRAnimation)
     * @see GVRAnimator#clear()
     */
    public void addAnimation(GVRAnimation anim)
    {
        mAnimations.add(anim);
    }

    /**
     * Removes an animation from this animator.
     * <p>
     * This animation will not participate in any subsequent operations
     * but it's state will not be changed when removed. For example,
     * if the animation is already running it will not be stopped.
     *
     * @param anim animation to add
     * @see GVRAnimator#addAnimation(GVRAnimation)
     * @see GVRAnimator#clear()
     */
    public void removeAnimation(GVRAnimation anim)
    {
        mAnimations.remove(anim);
    }

    /**
     * Removes all the animations from this animator.
     * <p>
     * The state of the animations are not changed when removed. For example,
     * if the animations are already running they are not be stopped.
     *
     * @see GVRAnimator#removeAnimation(GVRAnimation)
     * @see GVRAnimator#addAnimation(GVRAnimation)
     */
    public void clear()
    {
        mAnimations.clear();
    }

    /**
     * Sets the repeat mode for all the animations in this animator.
     * The number of times an animation is repeated is controlled
     * by the repeat count.
     *
     * @param repeatMode Value from GVRRepeatMode
     *                   ONCE - run the animations once
     *                   REPEATED - repeat the animation
     *                   PINGPONG - run forward, run reverse, repeat
     * @see GVRAnimator#setRepeatCount(int)
     */
    public void setRepeatMode(int repeatMode)
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.setRepeatMode(repeatMode);
        }
    }

    /**
     * Sets the repeat count for all the animations in this animator.
     * This establishes the number of times the animations are repeated
     * if the repeat mode is not set to ONCE.
     *
     * @param repeatCount number of times to repeat the animation
     *                    -1 indicates repeat endlessly
     *                    0 indicates animation will stop after current cycle

     * @see GVRAnimator#setRepeatMode(int)
     */
    public void setRepeatCount(int repeatCount)
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.setRepeatCount(repeatCount);
        }
    }

    /**
     * Starts all of the animations.
     * @see GVRAnimator#reset()
     * @see GVRAnimationEngine#start(GVRAnimation)
     */
    public void start()
    {
        for (GVRAnimation anim : mAnimations)
        {
            getGVRContext().getAnimationEngine().start(anim);
        }
    }


    /**
     * Stops all of the animations associated with this animator.
     * @see GVRAnimator#start()
     * @see GVRAnimationEngine#stop(GVRAnimation)
     */
    public void stop()
    {
        for (GVRAnimation anim : mAnimations)
        {
            getGVRContext().getAnimationEngine().stop(anim);
        }
    }

    /**
     * Resets all animations to their initial state.
     * <p>
     * If the animations are running, they will start again
     * at the beginning.
     * @see GVRAnimation#reset()
     * @see GVRAnimator#start()
     */
    public void reset()
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.reset();
        }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);
        this.stop();
    }
}
