package org.gearvrf.widgetlib.widget.animation;

import org.gearvrf.widgetlib.widget.Widget;

import org.json.JSONException;
import org.json.JSONObject;

public class RelativeScaleAnimation extends ScaleAnimation {
    public RelativeScaleAnimation(final Widget target, float duration,
                                  float scaleFactor) {
        super(target, duration, scaleFactor);
    }

    public RelativeScaleAnimation(final Widget target, float duration,
                                  float scaleFactorX, float scaleFactorY, float scaleFactorZ) {
        super(target, duration, scaleFactorX, scaleFactorY, scaleFactorZ);
    }

    public RelativeScaleAnimation(final Widget target, final JSONObject params)
            throws JSONException {
        super(target, params);
    }

    @Override
    protected void init(float duration, float scaleX, float scaleY, float scaleZ) {
        super.init(duration,
                getCurrentScaleX() * scaleX,
                getCurrentScaleY() * scaleY,
                getCurrentScaleZ() * scaleZ);
    }
}