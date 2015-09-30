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

package org.gearvrf.controls.menu.rotation;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.R;
import org.gearvrf.controls.anim.AnimationsTime;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.focus.GamepadTouchImpl;
import org.gearvrf.controls.focus.TouchAndGestureImpl;
import org.gearvrf.controls.input.GamepadMap;
import org.gearvrf.controls.menu.ItemSelectedListener;
import org.gearvrf.controls.menu.MenuWindow;
import org.gearvrf.controls.menu.RadioButtonSceneObject;
import org.gearvrf.controls.menu.RadioGrupoSceneObject;
import org.gearvrf.controls.menu.TouchableButton;

import java.util.ArrayList;

public class RotationMenu extends MenuWindow {

    private static final float ROTATION_FACTOR = 10f;
    private static final float BUTTON_X_POSITION = 1.35f;
    private static final float BUTTON_Y_POSITION = -0.7f;
    private static final float GROUP_Y_POSITION = -0.9f;

    private RotationGroup rotationGroup;
    private TouchableButton leftButton;
    private TouchableButton rightButton;
    private RadioGrupoSceneObject radioGroup;

    public RotationMenu(GVRContext gvrContext) {
        super(gvrContext);
        addRotaionGroup(gvrContext);

        addRotationButtons(gvrContext);
        attachRadioGroup();
    }

    private void addRotaionGroup(GVRContext gvrContext) {
        rotationGroup = new RotationGroup(gvrContext);

        rotationGroup.getTransform().setRotationByAxis(10, 1, 0, 0);
        rotationGroup.getTransform().setPositionZ(0.6f);
        rotationGroup.getTransform().setPositionY(GROUP_Y_POSITION);
    }
    
    private void attachRadioGroup() {

        radioGroup =  new RadioGrupoSceneObject(getGVRContext(), new ItemSelectedListener() {
            
            @Override
            public void selected(ControlSceneObject object) {
                
                MainScript.enableAnimationStar();
                
                RadioButtonSceneObject button = (RadioButtonSceneObject)object;
                AnimationsTime.setRotationTime(button.getSecond());
            }
            
        }, 0.2f, 0.5f, 1);
        
        radioGroup.getTransform().setPosition(-.5f, -1.24f, 0f);
        
        addChildObject(radioGroup);
    }

    private void addRotationButtons(GVRContext gvrContext) {
        addLeftButton(gvrContext);
        addRightButton(gvrContext);
    }

    private void addLeftButton(GVRContext gvrContext) {
        final ArrayList<GVRTexture> minusSignTextures =

                createTextureList(getGVRContext(), R.drawable.rotate_left_idle, R.drawable.rotate_left_hover,
                        R.drawable.rotate_left_pressed);
        leftButton = new TouchableButton(gvrContext, minusSignTextures);
        addLeftTouchListener(gvrContext);
        addLeftSignGamepadListener();
        leftButton.getTransform().setPositionX(-BUTTON_X_POSITION);
        leftButton.getTransform().setPositionY(BUTTON_Y_POSITION);
    }

    private void addLeftTouchListener(GVRContext gvrContext) {
        leftButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {
            
            @Override
            public void pressed() {
                super.pressed();
                leftButton.pressButton();
                rotetateGroup(-ROTATION_FACTOR);
            }

            @Override
            public void up() {
                super.up();
                leftButton.unPressButton();
            }
        });
    }

    private void addLeftSignGamepadListener() {
        leftButton.setGamepadTouchListener(new GamepadTouchImpl() {
            @Override
            public void pressed(Integer code) {
                super.pressed(code);
                if (isActionButton(code)) {
                    leftButton.pressButton();
                    rotetateGroup(-ROTATION_FACTOR);
                }
            }

            @Override
            public void up(Integer code) {
                super.up(code);
                if (isActionButton(code)) {
                    leftButton.unPressButton();
                }
            }
        });
    }

    private void addRightButton(GVRContext gvrContext) {
        final ArrayList<GVRTexture> plusSignTextures = createTextureList(getGVRContext(), R.drawable.rotate_right_idle,
                R.drawable.rotate_right_hover,
                R.drawable.rotate_right_pressed);
        rightButton = new TouchableButton(gvrContext, plusSignTextures);

        addRightSignTouchListener();
        addRightSignGamepadListener();

        rightButton.getTransform().setPositionX(BUTTON_X_POSITION);
        rightButton.getTransform().setPositionY(BUTTON_Y_POSITION);
    }

    private void addRightSignTouchListener() {
        rightButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {
            @Override
            public void pressed() {
                super.pressed();
                rightButton.pressButton();
                rotetateGroup(ROTATION_FACTOR);
            }

            @Override
            public void up() {
                super.up();
                rightButton.unPressButton();
            }
        });
    }

    private void addRightSignGamepadListener() {
        rightButton.setGamepadTouchListener(new GamepadTouchImpl() {
            @Override
            public void pressed(Integer code) {
                super.pressed(code);
                if (isActionButton(code)) {
                    rightButton.pressButton();
                    rotetateGroup(ROTATION_FACTOR);
                }
            }

            @Override
            public void up(Integer code) {
                super.up(code);
                if (isActionButton(code)) {
                    rightButton.unPressButton();
                }
            }
        });
    }

    private ArrayList<GVRTexture> createTextureList(GVRContext gvrContext, int res1, int res2, int res3) {
        ArrayList<GVRTexture> textureList = new ArrayList<GVRTexture>();
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res1)));
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res2)));
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res3)));
        return textureList;
    }

    @Override
    protected void show() {
        
        radioGroup.show();
        
        removeChildObject(rotationGroup);
        addChildObject(rotationGroup);

        removeChildObject(leftButton);
        addChildObject(leftButton);

        removeChildObject(rightButton);
        addChildObject(rightButton);
    }

    @Override
    protected void hide() {
        
        radioGroup.hide();
        
        removeChildObject(rotationGroup);
        removeChildObject(leftButton);
        removeChildObject(rightButton);
    }

    private void rotetateGroup(float rotationFactor) {
        rotationGroup.rotate(rotationFactor);
    }

    private boolean isActionButton(Integer code) {
        return code == GamepadMap.KEYCODE_BUTTON_A || code == GamepadMap.KEYCODE_BUTTON_B || code == GamepadMap.KEYCODE_BUTTON_X
                || code == GamepadMap.KEYCODE_BUTTON_Y;
    }
}