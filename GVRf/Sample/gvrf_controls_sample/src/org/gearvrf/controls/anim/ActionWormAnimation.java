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

package org.gearvrf.controls.anim;

import android.content.res.Resources;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.GamepadTouchImpl;
import org.gearvrf.controls.focus.TouchAndGestureImpl;
import org.gearvrf.controls.input.GamepadMap;

public class ActionWormAnimation extends GVRSceneObject {

    private static final float CLEARBUTTON_POSITIONY = -0.3f;
    private AnimButtonPlay playButton;
    private AnimCleanButton cleanButton;
    private boolean playbuttonbIsHidden = true;
    private boolean cleanbuttonbIsHidden = true;

    private final float MINIMUM_Y_POSITION = 0.65f;
    private final float MAXIMUM_Y_POSITION = 1f;
    private final float MINIMUM_SCALE = 0.4f;
    private final float MAXIMUM_SCALE = 1.2f;

    private boolean animColorPlaying = false;
    private boolean animScalePlaying = false;

    public ActionWormAnimation(GVRContext gvrContext) {
        super(gvrContext);

        Resources res = gvrContext.getContext().getResources();
        String clearButtonText = res.getString(R.string.clear_button);

        playButton = new AnimButtonPlay(gvrContext);
        cleanButton = new AnimCleanButton(gvrContext, clearButtonText);

        playButton.getTransform().setPosition(0, 0, 0);
        playButton.getTransform().setRotationByAxis(16, 0, 1, 0);

        cleanButton.getTransform().setPosition(0, CLEARBUTTON_POSITIONY, 0);
        cleanButton.getTransform().setRotationByAxis(5f, 0, 1, 0);

        getTransform().setPositionY(MINIMUM_Y_POSITION);

        attachActionButtons();
    }

    private void attachActionButtons() {

        playButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                ScaleWorm.playAnim();

                playAnimations();
            }
        });

        playButton.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    ScaleWorm.playAnim();

                    playAnimations();
                }
            }
        });

        cleanButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                cleanControls();
            }
        });

        cleanButton.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    cleanControls();
                }
            }
        });
    }

    private void cleanControls() {

        removeChildObject(playButton);
        removeChildObject(cleanButton);

        playbuttonbIsHidden = true;
        cleanbuttonbIsHidden = true;

        animScalePlaying = false;
        animColorPlaying = false;
    }

    public void resetAnimationState() {

        removeChildObject(cleanButton);

        playbuttonbIsHidden = true;
        cleanbuttonbIsHidden = true;

        showPlayButton();
    }

    private void playAnimations() {

        animPlaying();

        if (ColorWorm.lastColor != null) {

            animColorPlaying = true;

            MainScript.worm.resetColor(ColorWorm.lastColor);
            MainScript.worm.changeColor(ColorWorm.currentColor);
        }

        if (ScaleWorm.scaleAnimIsEnable()) {

            animScalePlaying = true;

            float factor = ScaleWorm.getWorm().getHead().getTransform().getScaleX()
                    - ScaleWorm.getLastSize()[0];
            ScaleWorm.animPlaying = true;

            resetPositionParts();

            WormApplyTransformAnims.resetScaleWorm(ScaleWorm.getLastSize());
            WormApplyTransformAnims.scaleWorm(getGVRContext(), factor);
        }
    }

    private void animPlaying() {

        removeChildObject(playButton);
        removeChildObject(cleanButton);

        getGVRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {

                addChildObject(playButton);
                addChildObject(cleanButton);
                ScaleWorm.animPlaying = false;

                getTransform().setPositionY(calculateNewYPosition());
            }

        }, getMajorDelayAnim());
    }

    private float calculateNewYPosition() {
        float currentScale = MainScript.worm.getHead().getTransform().getScaleY();
        float position = (((currentScale - MINIMUM_SCALE) / (MAXIMUM_SCALE - MINIMUM_SCALE)) * (MAXIMUM_Y_POSITION - MINIMUM_Y_POSITION))
                + MINIMUM_Y_POSITION;
        return position;
    }

    private float getMajorDelayAnim() {

        if (animColorPlaying && animScalePlaying) {

            float maxTime = AnimationsTime.getChangeColorTime() > AnimationsTime.getScaleTime()
                    ? AnimationsTime.getChangeColorTime() : AnimationsTime.getScaleTime();

            return maxTime;

        } else if (animColorPlaying && !animScalePlaying) {

            return AnimationsTime.getChangeColorTime();

        } else {

            return AnimationsTime.getScaleTime();
        }
    }

    private void resetPositionParts() {

        GVRRelativeMotionAnimation headAnimation = WormApplyTransformAnims.moveWormPartReset(
                MainScript.worm.getHead().getParent(), MainScript.worm.getMiddle());
        headAnimation.start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {

                WormApplyTransformAnims.moveWormPartToClose(getGVRContext(), MainScript.worm
                        .getHead().getParent(), MainScript.worm.getMiddle());
            }
        });

        GVRRelativeMotionAnimation endAnimation = WormApplyTransformAnims.moveWormPartReset(
                MainScript.worm.getEnd(), MainScript.worm.getMiddle());
        endAnimation.start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                WormApplyTransformAnims.moveWormPartToClose(getGVRContext(),
                        MainScript.worm.getEnd(), MainScript.worm.getMiddle());
            }
        });
    }

    public void showPlayButton() {

        if (playbuttonbIsHidden) {
            playbuttonbIsHidden = false;
            addChildObject(playButton);
        }
    }

    public void showCleanButton() {

        if (cleanbuttonbIsHidden) {

            cleanbuttonbIsHidden = false;
            addChildObject(cleanButton);
        }
    }
}
