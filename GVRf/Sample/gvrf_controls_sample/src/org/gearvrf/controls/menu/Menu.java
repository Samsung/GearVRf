
package org.gearvrf.controls.menu;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.focus.FocusListener;
import org.gearvrf.controls.util.Util;

public class Menu extends ControlSceneObject implements FocusListener {

    private static final float Z_HOVER_ANIMATION_OFFSET = 0.5f;
    private static final float Z_HOVER_ANIMATION_TIME = 0.5f;

    private static final float MENU_WIDTH = 6.0f;
    private static final float MENU_HEIGHT = 0.7f;

    private float[] originalPosition = null;
    private MenuItem menuItem;

    public Menu(GVRContext gvrContext) {
        super(gvrContext, MENU_WIDTH, MENU_HEIGHT, Util.transparentTexture(gvrContext));

        attachEyePointeeHolder();
        this.focusListener = this;

        menuItem = new MenuItem(gvrContext);
        
        addChildObject(menuItem);
    }

    public void setOriginalPosition() {
        originalPosition = new float[3];
        originalPosition[0] = this.getTransform().getPositionX();
        originalPosition[1] = this.getTransform().getPositionY();
        originalPosition[2] = this.getTransform().getPositionZ();
    }

    private void bringMenuToFront() {
        if (originalPosition == null)
            setOriginalPosition();

        new GVRPositionAnimation(this.getTransform(),
                Z_HOVER_ANIMATION_TIME, originalPosition[0], originalPosition[1],
                originalPosition[2]
                        + Z_HOVER_ANIMATION_OFFSET)
                .start(getGVRContext().getAnimationEngine());

    }

    private void sendMenuToBack() {
        if (originalPosition == null)
            setOriginalPosition();

        new GVRPositionAnimation(this.getTransform(),
                Z_HOVER_ANIMATION_TIME, originalPosition[0], originalPosition[1],
                originalPosition[2])
                .start(getGVRContext().getAnimationEngine());
    }

    @Override
    public void gainedFocus(GVRSceneObject object) {
        bringMenuToFront();
    }

    @Override
    public void lostFocus(GVRSceneObject object) {
        sendMenuToBack();
    }

    @Override
    public void inFocus(GVRSceneObject object) {
    }
}
