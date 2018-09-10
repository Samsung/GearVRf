package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVROpacityAnimation;
import org.json.JSONException;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getFloat;

public class OpacityAnimation extends MaterialAnimation {

    public enum Properties { opacity }

    public OpacityAnimation(final Widget target, final float duration,
                            final float opacity) {
        super(target);
        mTargetOpacity = opacity;
        mAdapter = new Adapter(target, duration, opacity);
    }

    public OpacityAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        this(target, getFloat(parameters, Animation.Properties.duration),
                getFloat(parameters, Properties.opacity));
    }

    public float getOpacity() {
        return mTargetOpacity;
    }

    public float getCurrentOpacity() {
        return getTarget().getOpacity();
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }

    @Override
    AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVROpacityAnimation implements
            Animation.AnimationAdapter {

        public Adapter(Widget target, float duration, float opacity) {
            super(target.getSceneObject(), duration, opacity);
        }

        @Override
        public void animate(GVRHybridObject target, float ratio) {
            doAnimate(ratio);
        }

        void superAnimate(Widget target, float ratio) {
            super.animate(target.getSceneObject(), ratio);
        }
    }

    private final Adapter mAdapter;
    private final float mTargetOpacity;
}
