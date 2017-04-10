/* Copyright 2015 Samsung Electronics Co., LTD
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;

/**
 * This class runs {@linkplain GVRAnimation animations}.
 * 
 * You can animate changes in just about any property of a
 * {@linkplain GVRSceneObject scene object}.
 * 
 * <p>
 * The animation engine is an optional part of GVRF: to use it, you must call
 * {@link #getInstance(GVRContext)} to lazy-create the singleton.
 * 
 * <p>
 * You can {@link #stop(GVRAnimation)} a running animation at any time, but
 * usually you will either
 * <ul>
 * 
 * <li>Use {@link GVRAnimation#setRepeatCount(int) setRepeatCount(0)} to
 * 'schedule' termination at the end of the current repetition, or
 * 
 * <li>{@linkplain GVRAnimation#setOnFinish(GVROnFinish) Set} a
 * {@linkplain GVROnRepeat callback,} which allows you to terminate the
 * animation before the next loop.
 * </ul>
 */
public class GVRAnimationEngine {

    private static GVRAnimationEngine sInstance = null;

    static {
        GVRContext.addResetOnRestartHandler(new Runnable() {

            @Override
            public void run() {
                sInstance = null;
            }
        });
    }

    private final List<GVRAnimation> mAnimations = new CopyOnWriteArrayList<GVRAnimation>();
    private final GVRDrawFrameListener mOnDrawFrame = new DrawFrame();

    protected GVRAnimationEngine(GVRContext gvrContext) {
        gvrContext.registerDrawFrameListener(mOnDrawFrame);
    }

    /**
     * The animation engine is an optional part of GVRF: You do have to call
     * {@code getInstance()} to lazy-create the singleton.
     * 
     * @param gvrContext
     *            current GVR context
     */
    public static synchronized GVRAnimationEngine getInstance(
            GVRContext gvrContext) {
        if (sInstance == null) {
            sInstance = new GVRAnimationEngine(gvrContext);
        }
        return sInstance;
    }

    /**
     * Registers an animation with the engine: It will start running
     * immediately.
     * 
     * You will usually use {@link GVRAnimation#start(GVRAnimationEngine)}
     * instead of this method:
     * 
     * <pre>
     * 
     * new GVRSomeAnimation(object, duration, parameter) //
     *         .setOnFinish(handler) //
     *         .start(animationEngine);
     * </pre>
     * 
     * reads better than
     * 
     * <pre>
     * 
     * animationEngine.start( //
     *         new GVRSomeAnimation(object, duration, parameter) //
     *                 .setOnFinish(handler) //
     *         );
     * </pre>
     * 
     * @param animation
     *            an animation
     * @return The animation that was passed in.
     */
    public GVRAnimation start(GVRAnimation animation) {
        if (animation.getRepeatCount() != 0) {
            animation.reset();
            mAnimations.add(animation);
        }
        return animation;
    }

    /**
     * Stop the animation, even if it is still running: the animated object will
     * be left in its current state, not reset to the start or end values.
     * 
     * This is probably not what you want to do! Usually you will either
     * <ul>
     * <li>Use {@link GVRAnimation#setRepeatCount(int) setRepeatCount(0)} to
     * 'schedule' termination at the end of the current repetition, or
     * <li>{@linkplain GVRAnimation#setOnFinish(GVROnFinish) Set} a
     * {@linkplain GVROnRepeat callback,} which allows you to terminate the
     * animation before the next loop.
     * </ul>
     * You <em>may</em> want to {@code stop()} an animation if you are also
     * removing the animated object the same time. For example, you may be
     * spinning some sort of In Progress object. In a case like this, stopping
     * in mid-animation is harmless.
     * 
     * @param animation
     *            an animation
     */
    public void stop(GVRAnimation animation) {
        mAnimations.remove(animation);
    }

    private final class DrawFrame implements GVRDrawFrameListener {

        @Override
        public void onDrawFrame(float frameTime) {
            for (GVRAnimation animation : mAnimations) {
                if (animation.onDrawFrame(frameTime) == false) {
                    mAnimations.remove(animation);
                }
            }
        }
    }
}
