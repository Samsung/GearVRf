
package org.gearvrf.controls.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;


public class ControlSceneObject extends GVRSceneObject {

    private boolean focus = false;
    public FocusListener focusListener = null;

    public ControlSceneObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public ControlSceneObject(GVRContext gvrContext, float f, float g, GVRTexture t) {
        super(gvrContext, f, g, t);
    }

    public void dispatchGainedFocus() {
        if (this.focusListener != null) {
            this.focusListener.gainedFocus(this);
        }
    }

    public void dispatchLostFocus() {
        if (this.focusListener != null) {
            focusListener.lostFocus(this);
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false) {
            focus = true;
            this.dispatchGainedFocus();
            return;
        }

        if (state == false && focus == true) {
            focus = false;
            this.dispatchLostFocus();
            return;
        }
    }

    public void dispatchInFocus() {
        if (this.focusListener != null) {
            this.focusListener.inFocus(this);
        }
    }

    public static boolean hasFocusMethods(GVRSceneObject obj) {
        if (obj instanceof ControlSceneObject) {
            return true;
        }
        return false;
    }
}
