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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.R;
import org.gearvrf.controls.Worm;
import org.gearvrf.controls.focus.GamepadTouchImpl;
import org.gearvrf.controls.focus.TouchAndGestureImpl;
import org.gearvrf.controls.input.GamepadMap;

import java.util.ArrayList;

public class ScaleMenu extends MenuWindow {

    private static final float WORM_SCALE_FACTOR = 0.1f;
    private static final float ARROW_X_POSITION = 1.35f;
    private static final float ARROW_Y_POSITION = -0.7f;
    private static final float WORM_Y_POSITION = -1.0f;
    private final float WORM_HEAD_X_POSITION = -0.2f;
    private final float WORM_MIDDLE_X_POSITION = 0f;
    private final float WORM_END_X_POSITION = 0.16f;
    private Worm worm;
    private TouchableButton minusSign;
    private TouchableButton plusSign;

    public ScaleMenu(GVRContext gvrContext) {
        super(gvrContext);
        addWorm(gvrContext);

        addScaleSigns(gvrContext);
    }

    private void addWorm(GVRContext gvrContext) {
        worm = new Worm(gvrContext);
        worm.getTransform().setPositionZ(2.7f);
        worm.getTransform().setPositionY(0.22f);
//        worm.getTransform().setPositionX(-0.1f);

        worm.getHead().getParent().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getMiddle().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getEnd().getTransform().setPositionY(WORM_Y_POSITION);

        worm.getHead().getTransform().setPositionX(WORM_HEAD_X_POSITION);
        worm.getHead().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getMiddle().getTransform().setPositionX(WORM_MIDDLE_X_POSITION);
        worm.getMiddle().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getEnd().getTransform().setPositionX(WORM_END_X_POSITION);
        worm.getEnd().getTransform().rotateByAxis(90, 0, 1, 0);
    }

    private void addScaleSigns(GVRContext gvrContext) {
        addMinusSign(gvrContext);
        addPlusSign(gvrContext);
    }

    private void addMinusSign(GVRContext gvrContext) {
        final ArrayList<GVRTexture> minusSignTextures = createTextureList(getGVRContext(),
                R.drawable.scale_less_idle, R.drawable.scale_less_hover,
                R.drawable.scale_less_pressed);
        minusSign = new TouchableButton(gvrContext, minusSignTextures);
        addMinusSignTouchListener(gvrContext);
        addMinusSignGamepadListener();
        minusSign.getTransform().setPositionX(-ARROW_X_POSITION);
        minusSign.getTransform().setPositionY(ARROW_Y_POSITION);
    }

    private void addMinusSignTouchListener(GVRContext gvrContext) {
        minusSign.setTouchAndGesturelistener(new TouchAndGestureImpl() {
            @Override
            public void pressed() {
                super.pressed();
                minusSign.pressButton();
               
                if (canScaleLess()){
                
                    scaleWorms(-WORM_SCALE_FACTOR);
                
                    new GVRRelativeMotionAnimation(worm, .1f, 0, 0.01f, 0).start(getGVRContext()
                        .getAnimationEngine());
                }
            }

            @Override
            public void up() {
                super.up();
                minusSign.unPressButton();
            }
        });
    }

    private void addMinusSignGamepadListener() {
        minusSign.setGamepadTouchListener(new GamepadTouchImpl() {
            @Override
            public void pressed(Integer code) {
                super.pressed(code);
                if (isActionButton(code)) {
                   
                    minusSign.pressButton();
                    
                    if (canScaleLess()){
                       
                        scaleWorms(-WORM_SCALE_FACTOR);
                        
                        new GVRRelativeMotionAnimation(worm, .1f, 0, 0.01f, 0).start(getGVRContext()
                            .getAnimationEngine());
                    }
                }
            }

            @Override
            public void up(Integer code) {
                super.up(code);
                if (isActionButton(code)) {
                    minusSign.unPressButton();
                }
            }
        });
    }

    private void addPlusSign(GVRContext gvrContext) {
        final ArrayList<GVRTexture> plusSignTextures = createTextureList(getGVRContext(),
                R.drawable.scale_more_idle, R.drawable.scale_more_hover,
                R.drawable.scale_more_pressed);
        plusSign = new TouchableButton(gvrContext, plusSignTextures);

        addPlusSignTouchListener();
        addPlusSignGamepadListener();
        plusSign.getTransform().setPositionX(ARROW_X_POSITION);
        plusSign.getTransform().setPositionY(ARROW_Y_POSITION);
    }

    private void addPlusSignTouchListener() {
        plusSign.setTouchAndGesturelistener(new TouchAndGestureImpl() {
            @Override
            public void pressed() {
                super.pressed();
                plusSign.pressButton();
                
                if (canScaleMore()){
                    
                    scaleWorms(WORM_SCALE_FACTOR);
                    
                    new GVRRelativeMotionAnimation(worm, .1f, 0, -0.01f, 0).start(getGVRContext()
                            .getAnimationEngine());
                }
            }

            @Override
            public void up() {
                super.up();
                plusSign.unPressButton();
            }
        });
    }

    private void addPlusSignGamepadListener() {
        plusSign.setGamepadTouchListener(new GamepadTouchImpl() {
            @Override
            public void pressed(Integer code) {
                super.pressed(code);
               
                if (isActionButton(code)) {
                    plusSign.pressButton();
                    
                    if (canScaleMore()){
                       
                        scaleWorms(WORM_SCALE_FACTOR);
                        
                        new GVRRelativeMotionAnimation(worm, .1f, 0, -0.01f, 0).start(getGVRContext()
                                .getAnimationEngine());
                    }
                }
            }

            @Override
            public void up(Integer code) {
                super.up(code);
                if (isActionButton(code)) {
                    plusSign.unPressButton();
                }
            }
        });
    }

    private ArrayList<GVRTexture> createTextureList(GVRContext gvrContext, int res1, int res2,
            int res3) {
        ArrayList<GVRTexture> textureList = new ArrayList<GVRTexture>();
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res1)));
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res2)));
        textureList.add(gvrContext.loadTexture(new GVRAndroidResource(getGVRContext(), res3)));
        return textureList;
    }

    @Override
    protected void show() {
        removeChildObject(worm);
        addChildObject(worm);

        removeChildObject(minusSign);
        addChildObject(minusSign);

        removeChildObject(plusSign);
        addChildObject(plusSign);
    }

    @Override
    protected void hide() {
        removeChildObject(worm);
        removeChildObject(minusSign);
        removeChildObject(plusSign);
    }

    private void scaleWorms(float scaleFactor) {

        worm.moveWorm(scaleFactor);
        worm.scaleWorm(scaleFactor);
        
        MainScript.worm.scaleWorm(scaleFactor);
    }

    private boolean canScaleLess() {
        return worm.getHead().getTransform().getScaleX() > .4f;
    }

    private boolean canScaleMore() {
        return worm.getHead().getTransform().getScaleX() < 1.2f;
    }

    private boolean isActionButton(Integer code) {
        return code == GamepadMap.KEYCODE_BUTTON_A || code == GamepadMap.KEYCODE_BUTTON_B
                || code == GamepadMap.KEYCODE_BUTTON_X
                || code == GamepadMap.KEYCODE_BUTTON_Y;
    }
}