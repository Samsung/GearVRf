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

package org.gearvrf.immersivepedia.focus;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.immersivepedia.R;

public class SwipeIndicator extends GVRSceneObject {

    private GVRContext gvrContext;
    private GVRSceneObject swipeIndicator;
    private GVRSceneObject hand;
    private FocusableSceneObject dino;
    private static final float HAND_X = -.2f;
    private static final float HAND_Y = -.15f;
    private static final float HAND_Z = 0.1f;
    private long currentSecond;
    private boolean isStoping;

    public SwipeIndicator(GVRContext gvrContext, FocusableSceneObject dino) {
        super(gvrContext);
        this.gvrContext = gvrContext;

        this.dino = dino;
        swipeIndicator = new GVRSceneObject(gvrContext, .6f, .1f,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.swipe_trace)));

        hand = new GVRSceneObject(gvrContext, gvrContext.createQuad(.2f, .3f), gvrContext.loadTexture(new GVRAndroidResource(
                gvrContext, R.drawable.swipe_hand)));

        setAttribute();

    }

    public void init() {
        dino.attachEyePointeeHolder();
        addChildObject(swipeIndicator);
        addChildObject(hand);
        dino.focusListener = new FocusListener() {

            
            @Override
            public void lostFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub

            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                currentSecond = System.currentTimeMillis() / 1000;
                if (!isStoping) {
                    if (currentSecond % 5 == 0) {
                       restartSwipeIndicator();
                    }
                }

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {

            }
        };

    }

    private void restartSwipeIndicator() {
        swipeIndicator.getRenderData().getMaterial().setOpacity(1);
        hand.getRenderData().getMaterial().setOpacity(1);
        hand.getTransform().setPosition(HAND_X, HAND_Y, HAND_Z);
  
        swipeAnimation();
    }

    private void setAttribute() {
        hand.getRenderData().setRenderingOrder(1000008);
        hand.getTransform().setPosition(HAND_X, HAND_Y, HAND_Z);
    }

    private void swipeAnimation() {

        GVRPositionAnimation positionAnimationHand = new
                GVRPositionAnimation(hand, 1f, swipeIndicator.getTransform().getPositionX() + .3f, hand.getTransform().getPositionY()
                        , hand.getTransform().getPositionZ());

        positionAnimationHand.setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                new GVROpacityAnimation(swipeIndicator, 1, 0).start(gvrContext.getAnimationEngine());
                new GVROpacityAnimation(hand, 1, 0).start(gvrContext.getAnimationEngine());
              //  gvrContext.getMainScene().removeSceneObject(SwipeIndicator.this);

            }
        }).start(gvrContext.getAnimationEngine());

    }

    public void setStop(boolean isStoping) {
        this.isStoping = isStoping;
    }

}
