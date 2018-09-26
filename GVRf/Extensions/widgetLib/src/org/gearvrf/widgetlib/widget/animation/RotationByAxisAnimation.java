package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.json.JSONException;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getFloat;

public class RotationByAxisAnimation extends TransformAnimation {

    public enum Properties { angle, x, y, z }

    public RotationByAxisAnimation(final Widget target, float duration,
                                   float angle, float x, float y, float z) {
        super(target);
        mAngle = angle;
        mAxisX = x;
        mAxisY = y;
        mAxisZ = z;
        mAdapter = new Adapter(target, duration, angle, x, y, z);
    }

    public RotationByAxisAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target);
        float duration = getFloat(params, Animation.Properties.duration);
        mAngle = getFloat(params, Properties.angle);
        mAxisX = getFloat(params, Properties.x);
        mAxisY = getFloat(params, Properties.y);
        mAxisZ = getFloat(params, Properties.z);
        mAdapter = new Adapter(target, duration, mAngle, mAxisX, mAxisY, mAxisZ);
    }

    public float getAngle() {
        return mAngle;
    }

    public float getAxisX() {
        return mAxisX;
    }

    public float getAxisY() {
        return mAxisY;
    }

    public float getAxisZ() {
        return mAxisZ;
    }

    public float getCurrentAngle() {
        return mCurrentAngle;
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
        target.checkTransformChanged();
        mCurrentAngle = ratio * mAngle;
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRRotationByAxisAnimation implements
            Animation.AnimationAdapter {
        Adapter(Widget target, float duration, float angle, float x, float y,
                float z) {
            super(target.getSceneObject(), duration, angle, x, y, z);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            super.animate(target.getTransform(), ratio);
        }
    }

    private final Adapter mAdapter;
    private final float mAngle;
    private final float mAxisX;
    private final float mAxisY;
    private final float mAxisZ;
    private float mCurrentAngle;
}
