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

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

/**
 * Animate a {@link GVRTransform}.
 * 
 * The constructors cast their {@code target} parameter to a
 * {@code protected final GVRTransform mTransform} field.
 */
public abstract class GVRTransformAnimation extends GVRAnimation {

    private final static Class<?>[] SUPPORTED = { GVRTransform.class,
            GVRSceneObject.class };

    protected final GVRTransform mTransform;

    /**
     * Sets the {@code protected final GVRTransform mTransform} field.
     * 
     * @param target
     *            May be a {@link GVRTransform} or a {@link GVRSceneObject} -
     *            does runtime checks.
     * @param duration
     *            The animation duration, in seconds.
     * @throws IllegalArgumentException
     *             If {@code target} is neither a {@link GVRTransform} nor a
     *             {@link GVRSceneObject}
     * @deprecated Using this overload reduces 'constructor fan-out' and thus
     *             makes your life a bit easier - but at the cost of replacing
     *             compile-time type checking with run-time type checking, which
     *             is more expensive <em>and</em> can miss errors in code if you
     *             don't test every path through your code.
     */
    protected GVRTransformAnimation(GVRHybridObject target, float duration) {
        super(target, duration);
        Class<?> type = checkTarget(target, SUPPORTED);
        if (type == GVRTransform.class) {
            mTransform = (GVRTransform) target;
        } else {
            GVRSceneObject sceneObject = (GVRSceneObject) target;
            mTransform = sceneObject.getTransform();
        }
    }

    /**
     * 'Knows how' to get a transform from a scene object - protects you from
     * any changes (however unlikely) in the object hierarchy.
     */
    protected static GVRTransform getTransform(GVRSceneObject sceneObject) {
        return sceneObject.getTransform();
    }

    /**
     * Sets the {@code protected final GVRTransform mTransform} field without
     * doing any runtime checks.
     * 
     * @param target
     *            {@link GVRTransform} to animate.
     * @param duration
     *            The animation duration, in seconds.
     */
    protected GVRTransformAnimation(GVRTransform target, float duration) {
        super(target, duration);
        mTransform = target;
    }

    /**
     * Sets the {@code protected final GVRTransform mTransform} field without
     * doing any runtime checks.
     * 
     * <p>
     * This constructor is included to be orthogonal ;-) but you probably won't
     * use it, as most derived classes will have final fields of their own to
     * set. Rather than replicate the final field setting code, the best pattern
     * is to write a 'master' constructor, and call it <i>via</i>
     * {@code this(getTransform(target), duration), ...);} - see
     * {@link GVRRotationByAxisAnimation#GVRRotationByAxisAnimation(GVRSceneObject, float, float, float, float, float)}
     * for an example.
     * 
     * @param target
     *            {@link GVRSceneObject} containing a {@link GVRTransform}
     * @param duration
     *            The animation duration, in seconds.
     */
    protected GVRTransformAnimation(GVRSceneObject target, float duration) {
        super(target, duration);
        mTransform = getTransform(target);
    }

    /** Latch/restore starting orientation */
    protected class Orientation {
        private final float w, x, y, z;

        private Orientation(float w, float x, float y, float z) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /** Copies {@link #mTransform}'s orientation */
        protected Orientation() {
            this( //
                    mTransform.getRotationW(), //
                    mTransform.getRotationX(), //
                    mTransform.getRotationY(), //
                    mTransform.getRotationZ());
        }

        /** Restores {@link #mTransform}'s starting orientation */
        protected void setOrientation() {
            mTransform.setRotation(w, x, y, z);
        }
    }

    /** Latch/restore starting position */
    protected class Position {
        private final float x, y, z;

        private Position(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /** Copies {@link #mTransform}'s position */
        protected Position() {
            this( //
                    mTransform.getPositionX(), //
                    mTransform.getPositionY(), //
                    mTransform.getPositionZ() //
            );
        }

        /** Restores {@link #mTransform}'s starting position */
        protected void setPosition() {
            mTransform.setPosition(x, y, z);
        }
    }
}
