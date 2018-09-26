package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRScaleAnimation;
import org.joml.Vector3f;
import org.json.JSONException;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.hasFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.hasVector3f;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optVector3f;

public class ScaleAnimation extends TransformAnimation {
    public enum Properties { scale }

    public ScaleAnimation(final Widget widget, float duration, float scale) {
        super(widget);
        init(duration, scale, scale, scale);
    }

    public ScaleAnimation(final Widget widget, float duration, float scaleX,
                          float scaleY, float scaleZ) {
        super(widget);
        init(duration, scaleX, scaleY, scaleZ);
    }

    public ScaleAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target);
        float duration = (float) params.getDouble("duration");

        if (hasFloat(params, Properties.scale)) {
            float scale = (float) params.getDouble("scale");
            init(duration, scale, scale, scale);
        } else if (hasVector3f(params, Properties.scale)) {
            Vector3f scale = optVector3f(params, Properties.scale);
            init(duration, scale.x, scale.y, scale.z);
        } else {
            throw new JSONException("Bad parameters; expected 'scale' (float or Vector3f), got " + params);
        }
    }

    protected void init(float duration, float scaleX, float scaleY, float scaleZ) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mScaleZ = scaleZ;

        if (mScaleX == mScaleY &&  mScaleY == mScaleZ) {
            mAdapter = new Adapter(getTarget(), duration, mScaleX);
        } else {
            mAdapter = new Adapter(getTarget(), duration, scaleX, scaleY, scaleZ);
        }
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getScaleZ() {
        return mScaleZ;
    }

    public float getCurrentScaleX() {
        return getTarget().getScaleX();
    }

    public float getCurrentScaleY() {
        return getTarget().getScaleY();
    }

    public float getCurrentScaleZ() {
        return getTarget().getScaleZ();
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

    private class Adapter extends GVRScaleAnimation implements
            Animation.AnimationAdapter {
        Adapter(Widget widget, float duration, float scale) {
            super(widget.getSceneObject(), duration, scale);
        }

        Adapter(Widget widget, float duration, float scaleX, float scaleY,
                float scaleZ) {
            super(widget.getSceneObject(), duration, scaleX, scaleY, scaleZ);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            super.animate(target.getTransform(), ratio);
        }
    }

    private Adapter mAdapter;
    private float mScaleX;
    private float mScaleY;
    private float mScaleZ;
}
