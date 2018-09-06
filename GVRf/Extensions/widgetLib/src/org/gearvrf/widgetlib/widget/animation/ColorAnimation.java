package org.gearvrf.widgetlib.widget.animation;

import java.util.Arrays;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRColorAnimation;
import org.gearvrf.utility.Colors;

import org.json.JSONException;
import org.json.JSONObject;

import org.gearvrf.widgetlib.widget.Widget;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getJSONColorGl;

public class ColorAnimation extends MaterialAnimation {

    public enum Properties { color }

    public ColorAnimation(final Widget target, final float duration,
                          final int color) {
        this(target, duration, Colors.toColors(color));
    }

    public ColorAnimation(final Widget target, final float duration,
            final float[] rgb) {
        super(target);
        mTargetColor = Arrays.copyOf(rgb, rgb.length);
        mAdapter = new Adapter(target, duration, rgb);
    }

    public ColorAnimation(final Widget target, final JSONObject parameters)
            throws JSONException {
        this(target, getFloat(parameters, Animation.Properties.duration), //
                getJSONColorGl(parameters, Properties.color));
    }

    public float[] getColor() {
        return Arrays.copyOf(mTargetColor, mTargetColor.length);
    }

    @Override
    protected void animate(Widget target, float ratio) {
        mAdapter.superAnimate(target, ratio);
    }

    @Override
    Animation.AnimationAdapter getAnimation() {
        return mAdapter;
    }

    private class Adapter extends GVRColorAnimation implements
            Animation.AnimationAdapter {

        public Adapter(Widget target, float duration, float[] rgb) {
            super(target.getSceneObject(), duration, rgb);
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

    private final float[] mTargetColor;
}
