package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRTransformAnimation;
import org.json.JSONException;
import org.json.JSONObject;

import org.gearvrf.widgetlib.widget.Widget;

public class SinglePositionAnimation extends TransformAnimation {

    public SinglePositionAnimation(final Widget target, final float duration,
                                   final float translateBy, final float xFactor, final float yFactor, final float zFactor) {
        super(target);
        mTargetX = getCurrentX() + xFactor * translateBy;
        mTargetY = getCurrentY() + yFactor * translateBy;
        mTargetZ = getCurrentZ() + zFactor * translateBy;

        float startPosition = 0;
        float targetPosition = 0;
        if (xFactor != 0) {
            startPosition = getCurrentX();
            targetPosition = mTargetX;
        } else if (yFactor != 0) {
            startPosition = getCurrentY();
            targetPosition = mTargetY;
        } else if (zFactor != 0) {
            startPosition = getCurrentZ();
            targetPosition = mTargetZ;
        }

        mAdapter = new Adapter(target, duration, targetPosition, startPosition, xFactor, yFactor, zFactor);
    }

    public SinglePositionAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        this(target, (float) parameters.getDouble("duration"),
                 (float) parameters.getDouble("position"), //
                (float) parameters.getDouble("xFactor"), //
                (float) parameters.getDouble("yFactor"), //
                (float) parameters.getDouble("zFactor"));
    }

    public float getX() {
        return mTargetX;
    }

    public float getY() {
        return mTargetY;
    }

    public float getZ() {
        return mTargetZ;
    }

    public float getCurrentX() {
        return getTarget().getPositionX();
    }

    public float getCurrentY() {
        return getTarget().getPositionY();
    }

    public float getCurrentZ() {
        return getTarget().getPositionZ();
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
        target.checkTransformChanged();
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRTransformAnimation implements
            Animation.AnimationAdapter {
        private final float mDelta;
        private final float mStart;
        private final float mFactorX, mFactorY, mFactorZ;
        private float mTranslatedBy = 0;

        public Adapter(Widget target, float duration,
               final float position, final float startPosition,
               final float xFactor, final float yFactor, final float zFactor) {
            super(target.getTransform(), duration);
            mStart = startPosition;
            mDelta = position - mStart;

            mFactorX = xFactor;
            mFactorY = yFactor;
            mFactorZ = zFactor;
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            float x = 0;
            float y = 0;
            float z = 0;
            float delta = ratio * mDelta - mTranslatedBy;
            if (mFactorX != 0) {
                x = delta;
            } else if (mFactorY != 0) {
                y = delta;
            } else if (mFactorZ != 0) {
                z = delta;
            }
            mTransform.translate(x, y, z);
            mTranslatedBy = ratio * mDelta;
        }
    }

    private final Adapter mAdapter;
    private final float mTargetX;
    private final float mTargetY;
    private final float mTargetZ;
}
