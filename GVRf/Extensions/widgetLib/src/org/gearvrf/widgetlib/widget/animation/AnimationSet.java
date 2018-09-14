package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.log.Log;

import java.util.HashSet;
import java.util.Set;
/**
 * A manager for {@link Animation} set. All animations are started simultaneously.
 * The animation set duration is the longest animation duration in the set.
 * {@link Animation.OnFinish} is running when all animations in the set are finished.
 */

public class AnimationSet {
    /**
     * Animation set builder. All animations in the set have to have an identical target.
     * the animations. The Builder can build one AnimationSet only. The AnimationSet cannot be
     * modified after {@link Builder#build()} method is called. {@link IllegalArgumentException}
     * will be thrown for any attempt modify the AnimationSet after building it.
     */
    public static class Builder {
        private Widget mTarget;
        private Set<Animation> mAnimations = new HashSet();
        private AnimationSetImpl mAnimationSetImpl;
        private float mDuration;
        private static final String TAG = Builder.class.getSimpleName();

        /**
         * Builder constructor for specific target.
         * {@link IllegalArgumentException} will be thrown if the target is null
         * @param target
         */
        public Builder(Widget target) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "AnimationSet.Builder is created for target %s", target);
            if (target == null) {
                throw new IllegalArgumentException("Empty target!");
            }
            mTarget = target;
        }

        /**
         * Request to add the Animation to the Animation set
         * {@link IllegalArgumentException} will be thrown if the target is not the same as the
         * builder target or for null animation
         * {@link IllegalArgumentException} will be thrown for any attempt to add new animation
         * to the AnimationSet after building it.
         * @param a animation requested for adding to the set
         */
        public synchronized Builder add(Animation a) {
            if (mAnimationSetImpl == null) {
                if (!mTarget.equals(a.getTarget())) {
                    throw new IllegalArgumentException("Target mistmatch!");
                } else if (a == null) {
                    throw new IllegalArgumentException("Try to add null animation to the set!");
                } else {
                    Log.d(Log.SUBSYSTEM.WIDGET, TAG, "add animation %s", a);
                    mAnimations.add(a);
                    mDuration = Math.max(mDuration, a.getDuration());
                }
            } else {
                throw new IllegalArgumentException("Cannot modify built animation, " +
                        "AnimationSet has been already built!");
            }
            return this;
        }

        /**
         * Request to remove the Animation from the Animation set
         * {@link IllegalArgumentException} will be thrown for any attempt to remove the animation
         * from the AnimationSet after building it.
         * @param a animation requested for removing from the set
         */
        public synchronized Builder remove(Animation a) {
            if (mAnimationSetImpl == null) {
                boolean removed = mAnimations.remove(a);
                if (removed && mDuration == a.getDuration()) {
                    mDuration = 0;
                    for (Animation animation : mAnimations) {
                        mDuration = Math.max(mDuration, animation.getDuration());
                    }
                }
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "Remove animation %s new duration is %f",
                        a, mDuration);
            } else {
                throw new IllegalArgumentException("Cannot modify built animation, " +
                        "AnimationSet has been already built!");
            }
            return this;
        }

        /**
         * Request to build the Animation set with the list of the animations
         * {@link IllegalArgumentException} will be thrown if the list of the animations is empty.
         * If the animation set has been already built, the previous built animation set will be
         * returned.
         */
        public synchronized Animation build() {
            if (mAnimationSetImpl == null) {
                if (mAnimations.isEmpty()) {
                    throw new IllegalArgumentException("Empty set!");
                } else {
                    mAnimationSetImpl = new AnimationSetImpl(mTarget,  mDuration, mAnimations);
                }
            }
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "build animationSet %s",
                    mAnimationSetImpl);
            return mAnimationSetImpl;
        }

        /**
         * return true if the animation list is empty, otherwise --  false.
         */
        public boolean isEmptySet() {
            return mAnimations.isEmpty();
        }
    }

    /**
     * Internal implementation of animation set
     */
    private static class AnimationSetImpl extends Animation {
        private Set<Animation> mAnimations = new HashSet();

        private AnimationSetImpl(Widget target, float duration, Set<Animation> animations) {
            super(target, duration);
            mAnimations = animations;
        }

        @Override
        protected void animate(Widget target, float ratio) {
            for (Animation a: mAnimations) {
                if (!a.isFinished()) {
                    a.animate(target, ratio);
                }
            }
        }
    }
}
