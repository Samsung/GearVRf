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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.controls.focus.GamepadTouchImpl;
import org.gearvrf.controls.focus.TouchAndGestureImpl;
import org.gearvrf.controls.input.GamepadMap;

public class MenuBox extends GVRSceneObject {

    public static final float FRAME_INITITAL_POSITION_Y = -0.8f;
    private static final float MENU_BOX_Y = 0.5f;
    private static final float MENU_BOX_Z = -7f;

    private MenuHeader mMenu;
    private MenuFrame frameWrapper;
    private MenuCloseButton closeButton;

    public MenuBox(GVRContext gvrContext) {
        super(gvrContext);

        getTransform().setPosition(0, MENU_BOX_Y, MENU_BOX_Z);

        attachMenuHeader();
        attachEmptyFrame();

        createCloseButton();
    }

    private void createCloseButton() {

        closeButton = new MenuCloseButton(getGVRContext());
        closeButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {
            @Override
            public void singleTap() {
                super.singleTap();
                closeMenu();
            }
        });

        closeButton.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    closeMenu();
                }
            }
        });

        closeButton.getTransform().setPositionY(0.45f);
    }

    private void closeMenu() {
        if (frameWrapper.isOpen()) {

            frameWrapper.collapseFrame();

            mMenu.hideContent();
        }
        removeChildObject(closeButton);
    }

    private void attachMenuHeader() {

        mMenu = new MenuHeader(this.getGVRContext(), new FrameListener() {

            @Override
            public void show() {
                frameWrapper.expandFrame(mMenu);
                addChildObject(closeButton);
            }
        });

        mMenu.getTransform().setPosition(0f, 0.1f, 0f);

        this.addChildObject(mMenu);
    }

    private void attachEmptyFrame() {
        
        GVRSceneObject wrapper = new GVRSceneObject(getGVRContext());

        frameWrapper = new MenuFrame(getGVRContext());
        frameWrapper.getTransform().setPosition(0f, FRAME_INITITAL_POSITION_Y, 0f);
        
        wrapper.addChildObject(frameWrapper);
        
        addChildObject(wrapper);
    }
}
