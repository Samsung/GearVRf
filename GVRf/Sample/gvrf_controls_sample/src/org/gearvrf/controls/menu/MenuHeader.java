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

package org.gearvrf.controls.menu;

import android.content.res.Resources;

import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.focus.GamepadTouchImpl;
import org.gearvrf.controls.input.GamepadMap;
import org.gearvrf.controls.menu.MenuHeaderItem.headerType;
import org.gearvrf.controls.menu.color.ColorsMenu;
import org.gearvrf.controls.menu.motion.MotionMenu;
import org.gearvrf.controls.menu.rotation.RotationMenu;
import org.gearvrf.controls.menu.scale.ScaleMenu;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MenuHeader extends ControlSceneObject implements ItemSelectedListener {

    private static final int MENU_WINDOW_INDEX = 4;
    private static final float Z_HOVER_ANIMATION_OFFSET = 0.25f;
    private static final float Z_HOVER_ANIMATION_TIME = 0.5f;

    private static final float MENU_WIDTH = 3.45f;
    private static final float MENU_HEIGHT = 0.24f;

    private static final float X_OFFSET = -1.27f;
    private static final float Y_OFFSET = 0f;
    private static final float Z_OFFSET = 0.15f;
    private static final float SPACING = .85f;

    private float[] originalPosition = null;

    private MenuHeaderItem motion;
    private MenuHeaderItem color;
    private MenuHeaderItem scale;
    private MenuHeaderItem rotation;

    private List<MenuWindow> menus = new ArrayList<MenuWindow>();

    private FrameListener frameListener;
    private MenuHeaderItem itemSelected;
    private Resources res;
    private boolean isSentToFront = false;

    public MenuHeader(GVRContext gvrContext, FrameListener frameListener) {
        super(gvrContext, MENU_WIDTH, MENU_HEIGHT, Util.whiteTexture(gvrContext));

        res = gvrContext.getContext().getResources();

        this.frameListener = frameListener;

        attachEyePointeeHolder();

        createMenuTypes();
        createMenuItems(gvrContext);
        organizeItens();
        attachMenuItens();
        setOriginalPosition();

        getRenderData().setRenderingOrder(RenderingOrder.MENU_HEADER);
    }

    private void createMenuTypes() {
        menus.add(new MotionMenu(getGVRContext()));
        menus.add(new ColorsMenu(getGVRContext()));
        menus.add(new ScaleMenu(getGVRContext()));
        menus.add(new RotationMenu(getGVRContext()));
    }

    private void createMenuItems(GVRContext gvrContext) {

        motion = new MenuHeaderItem(gvrContext, res.getString(R.string.motion), headerType.MOTION, this);
        color = new MenuHeaderItem(gvrContext, res.getString(R.string.color), headerType.COLOR, this);
        scale = new MenuHeaderItem(gvrContext, res.getString(R.string.scale), headerType.SCALE, this);
        rotation = new MenuHeaderItem(gvrContext, res.getString(R.string.rotation), headerType.ROTATION, this);

        setGamePadListener(motion);
        setGamePadListener(color);
        setGamePadListener(scale);
        setGamePadListener(rotation);
    }

    public void setGamePadListener(final MenuHeaderItem item) {

        item.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    resetMenuState(item);
                    item.select();
                }
            }
        });
    }

    private void attachMenuItens() {
        addChildObject(motion);
        addChildObject(color);
        addChildObject(scale);
        addChildObject(rotation);
    }

    private void organizeItens() {
        float itemPositionX = 0f;
        float itemPositionY = 0f;
        float itemPositionZ = 0f;

        motion.getTransform().setPosition(itemPositionX + X_OFFSET, itemPositionY + Y_OFFSET,
                itemPositionZ + Z_OFFSET);

        color.getTransform().setPosition(motion.getTransform().getPositionX() + SPACING,
                itemPositionY, Z_OFFSET);

        scale.getTransform().setPosition(color.getTransform().getPositionX() + SPACING,
                itemPositionY, Z_OFFSET);

        rotation.getTransform().setPosition(scale.getTransform().getPositionX() + SPACING,
                itemPositionY, Z_OFFSET);
    }

    private void resetMenuState(ControlSceneObject object) {

        if (itemSelected != null) {
            if (itemSelected != object) {
                hideContent();
            }
        }

        itemSelected = (MenuHeaderItem) object;

        addChildObject(menus.get(itemSelected.getHeaderType().ordinal()));

        frameListener.show();
    }

    public void show() {
        menus.get(itemSelected.getHeaderType().ordinal()).show();
    }

    public void hideContent() {
        itemSelected.unselect();
        menus.get(itemSelected.getHeaderType().ordinal()).hide();

        if (getChildrenCount() > 3) {
            itemSelected = null;
            removeChildObject(getChildByIndex(MENU_WINDOW_INDEX));
        }
    }

    private void setOriginalPosition() {
        originalPosition = new float[3];
        originalPosition[0] = this.getTransform().getPositionX();
        originalPosition[1] = this.getTransform().getPositionY();
        originalPosition[2] = this.getTransform().getPositionZ();
    }

    private void bringMenuToFront() {

        if (!isSentToFront) {

            new GVRPositionAnimation(this.getTransform(),
                    Z_HOVER_ANIMATION_TIME,
                    originalPosition[0],
                    originalPosition[1],
                    originalPosition[2] + Z_HOVER_ANIMATION_OFFSET).start(getGVRContext()
                    .getAnimationEngine());

            isSentToFront = true;
        }
    }

    private void sendMenuToBack() {

        if (isSentToFront) {

            new GVRPositionAnimation(this.getTransform(),
                    Z_HOVER_ANIMATION_TIME, originalPosition[0], originalPosition[1],
                    originalPosition[2])
                    .start(getGVRContext().getAnimationEngine());

            isSentToFront = false;
        }
    }

    private boolean canClose() {

        if (!motion.isSelected() && !color.isSelected() && !scale.isSelected()
                && !rotation.isSelected()) {
            return true;
        }

        return false;
    }

    @Override
    public void gainedFocus() {
        bringMenuToFront();
    }

    @Override
    public void lostFocus() {

        if (canClose()) {
            sendMenuToBack();
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();
    }

    @Override
    public void selected(ControlSceneObject object) {
        resetMenuState(object);
    }
}
