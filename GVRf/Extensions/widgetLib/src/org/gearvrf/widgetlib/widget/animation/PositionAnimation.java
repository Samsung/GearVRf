package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRPositionAnimation;
import org.json.JSONException;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getFloat;

public class PositionAnimation extends TransformAnimation {

    public enum Properties { x, y, z}

    public PositionAnimation(final Widget target, final float duration,
                             final float x, final float y, final float z) {
        super(target);
        mTargetX = x;
        mTargetY = y;
        mTargetZ = z;
        mAdapter = new Adapter(target, duration, x, y, z);
    }

    public PositionAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        this(target, getFloat(parameters, Animation.Properties.duration),
                getFloat(parameters, Properties.x), //
                getFloat(parameters, Properties.y), //
                getFloat(parameters, Properties.z));
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

    private class Adapter extends GVRPositionAnimation implements
            Animation.AnimationAdapter {
        public Adapter(Widget target, float duration, float x, float y, float z) {
            super(target.getSceneObject(), duration, x, y, z);
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
    private final float mTargetX;
    private final float mTargetY;
    private final float mTargetZ;
}
