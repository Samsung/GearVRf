
/* Copyright 2018 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    protected boolean mIsRunning;
    protected String mName;

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
        mIsRunning = false;
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
        mIsRunning = false;
        mAnimations = new ArrayList<GVRAnimation>();
    }

    static public long getComponentType() { return TYPE_ANIMATOR; }

    /**
     * Get the name of this animator.
     * <p>
     * The name is optional and may be set with {@link #setName(String) }
     * @returns string with name of animator, may be null
     * @see #setName(String)
     */
    public String getName() { return mName; }

    /**
     * Set the name of this animator.
     * @param name string with name of animator, may be null
     * @see #getName()
     */
    public void setName(String name) { mName = name; }

    /**
     * Determine if this animator is running (has been started).
     */
    public boolean isRunning() { return mIsRunning; }

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
     * Query the number of animations owned by this animator.
     * @return number of animations added to this animator
     */
    public int getAnimationCount() { return mAnimations.size(); }

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
     * Gets an animation from this animator.
     *
     * @param index index of animation to get
     * @see GVRAnimator#addAnimation(GVRAnimation)
     */
    public GVRAnimation getAnimation(int index)
    {
        return mAnimations.get(index);
    }

    /**
     * Removes an animation from this animator.
     * <p>
     * This animation will not participate in any subsequent operations
     * but it's state will not be changed when removed. For example,
     * if the animation is already running it will not be stopped.
     *
     * @param anim animation to remove
     * @see GVRAnimator#addAnimation(GVRAnimation)
     * @see GVRAnimator#clear()
     */
    public void removeAnimation(GVRAnimation anim)
    {
        mAnimations.remove(anim);
    }

    /**
     * Find the index of this animation if it is in this animator.
     *
     * @param findme    {@link GVRAnimation} to find.
     * @returns 0 based index of animation or -1 if not found
     * @see GVRAnimator#addAnimation(GVRAnimation)
     */
    public int findAnimation(GVRAnimation findme)
    {
        int index = 0;
        for (GVRAnimation anim : mAnimations)
        {
            if (anim == findme)
            {
                return index;
            }
            ++index;
        }
        return -1;
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
     * Sets the offset for the all animations in this animator.
     *
     * @param startOffset animation will start at the specified offset value
     *
     * @see GVRAnimation#setOffset(float)
     */
    public void setOffset(float startOffset)
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.setOffset(startOffset);
        }
    }
    /**
     * Sets the speed for the all animations in this animator.
     *
     * @param speed values from between 0 to 1 displays animation in slow mode
     *              values from 1 displays in fast mode
     *
     * @see GVRAnimation#setSpeed(float)
     */
    public void setSpeed(float speed)
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.setSpeed(speed);
        }
    }
    /**
     * Sets the duration for the animations in this animator.
     *
     * @param start the animation will start playing from the specified time
     * @param end the animation will stop playing at the specified time
     *
     * @see GVRAnimation#setDuration(float, float)
     */
    public void setDuration(float start, float end)
    {
        for (GVRAnimation anim : mAnimations)
        {
            anim.setDuration(start,end);
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
     * Starts all of the animations in this animator.
     * @see GVRAnimator#reset()
     * @see GVRAnimationEngine#start(GVRAnimation)
     */
    public void start()
    {
        if (mAnimations.size() == 0)
        {
            return;
        }
        mIsRunning = true;
        for (GVRAnimation anim : mAnimations)
        {
            anim.start(getGVRContext().getAnimationEngine());
        }
    }

    /**
     * Starts all of the animations in this animator.
     * @see GVRAnimator#reset()
     * @see GVRAnimationEngine#start(GVRAnimation)
     */
    public void start(GVROnFinish finishCallback)
    {
        if (mAnimations.size() == 0)
        {
            return;
        }
        mIsRunning = true;
        for (int i = 0; i < mAnimations.size(); ++i)
        {
            GVRAnimation anim = mAnimations.get(i);
            anim.reset();
            if (i == 0)
            {
                anim.setOnFinish(finishCallback);
            }
            else
            {
                anim.setOnFinish(null);
            }
            anim.start(getGVRContext().getAnimationEngine());
        }
    }

    public void animate(float timeInSec)
    {
        if (mAnimations.size() > 0)
        {
            for (int i = 0; i < mAnimations.size(); ++i)
            {
                GVRAnimation anim = mAnimations.get(i);
                anim.animate(timeInSec);
            }
        }
    }

    /**
     * Stops all of the animations associated with this animator.
     * @see GVRAnimator#start()
     * @see GVRAnimationEngine#stop(GVRAnimation)
     */
    public void stop()
    {
        if (!mIsRunning || (mAnimations.size() == 0))
        {
            return;
        }
        GVRAnimation anim = mAnimations.get(0);
        mIsRunning = false;
        anim.setOnFinish(null);
        for (int i = 0; i < mAnimations.size(); ++i)
        {
            anim = mAnimations.get(i);
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
