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

package org.gearvrf.controls;

import android.view.MotionEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.anim.ScaleWorm;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.model.Apple;
import org.gearvrf.controls.util.ColorControls;
import org.gearvrf.controls.util.ColorControls.Color;
import org.gearvrf.controls.util.Constants;
import org.gearvrf.controls.util.MathUtils;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class Worm extends GVRSceneObject {

    private static final float SHADOW_END_OFFSET = 0.801f;
    private static final float SHADOW_MIDDLE_OFFSET = 0.8f;
    private static final float SHADOW_HEAD_OFFSET = 0.9f;
    // private static final float MINIMUM_DISTANCE_FACTOR = 0.5f;
    // Chain Data
    private final float CHAIN_DISTANCE_HEAD_MIDDLE = 0.575f;
    private final float CHAIN_DISTANCE_MIDDLE_END = 0.475f;

    private final float CHAIN_SPEED_HEAD_MIDDLE = 0.055f;
    private final float CHAIN_SPEED_MIDDLE_END = 0.065f;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.9f;

    private float DISTANCE_TO_EAT_APPLE = 0.50f;
    private WormBasePart head, middle, end;

    public GVRSceneObject wormParent;
    private boolean isRotatingWorm = false;

    private GVRAnimation wormParentAnimation;

    private MovementDirection wormDirection = MovementDirection.Up;

    private Color color;

    private float[] scaleWorm = new float[] {
            0.4f, 0.4f, 0.4f
    };
    private WormShadow shadowHead;
    private WormShadow shadowMiddle;
    private WormShadow shadowEnd;

    public enum MovementDirection {
        Up, Right, Down, Left
    }

    public Worm(GVRContext gvrContext) {
        super(gvrContext);

        ColorControls gvrColor = new ColorControls(gvrContext.getContext());
        Color color = gvrColor.parseColor(R.color.color10);
        
        this.color = color;

        createWormParts(color);
    }

    public GVRSceneObject getWormParentation() {
        return wormParent;
    }

    public void resetColor(Color color) {
        head.resetColor(color);
        middle.resetColor(color);
        end.resetColor(color);
    }

    private void createWormParts(Color color) {
        
        wormParent = new GVRSceneObject(getGVRContext());
        addChildObject(wormParent);

        head = new WormBasePart(getGVRContext(), R.raw.sphere_head, R.drawable.worm_head_texture, color);
        middle = new WormBasePart(getGVRContext(), R.raw.sphere_body, R.drawable.worm_head_texture, color);
        end = new WormBasePart(getGVRContext(), R.raw.sphere_tail, R.drawable.worm_head_texture, color);

        wormParent.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        head.getTransform().setPosition(0, 0, 0);

        wormParent.addChildObject(head);
        
        addChildObject(middle);
        addChildObject(end);
    }
    
    public void enableShadow() {

        float factor = 3f;
        shadowHead = new WormShadow(getGVRContext(), 0.27f * factor, 0.27f * factor, RenderingOrder.WORM_SHADOW_HEADER);
        shadowMiddle = new WormShadow(getGVRContext(), 0.2f * factor, 0.2f * factor, RenderingOrder.WORM_SHADOW_MIDDLE);
        shadowEnd = new WormShadow(getGVRContext(), 0.18f * factor, 0.18f * factor, RenderingOrder.WORM_SHADOW_END);

        head.addChildObject(shadowHead);
        middle.addChildObject(shadowMiddle);
        end.addChildObject(shadowEnd);
        
        startShadowsPosition();
    }

    private void startShadowsPosition() {
        
        shadowHead.getTransform().setPositionY(shadowHead.getParent().getParent().getTransform().getPositionY() + SHADOW_HEAD_OFFSET);
        shadowMiddle.getTransform().setPositionY(shadowMiddle.getParent().getTransform().getPositionY() + SHADOW_MIDDLE_OFFSET);
        shadowEnd.getTransform().setPositionY(shadowEnd.getParent().getTransform().getPositionY() + SHADOW_END_OFFSET);
    }

    public void changeColor(Color color) {

        this.color = color;

        float[] colorArray = new float[3];
        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();
        
        head.animChangeColor(color);
        middle.animChangeColor(color);
        end.animChangeColor(color);
    }

    public Color getColor() {
        return color;
    }

    public float[] getScaleFactor() {

        this.scaleWorm[0] = getHead().getTransform().getScaleX();
        this.scaleWorm[1] = getMiddle().getTransform().getScaleX();
        this.scaleWorm[2] = getEnd().getTransform().getScaleX();

        return scaleWorm;
    }

    public void chainMove(GVRContext gvrContext) {

        if (!ScaleWorm.animPlaying) {

            if (MathUtils.distance(wormParent, middle) > CHAIN_DISTANCE_HEAD_MIDDLE
                    * middle.getTransform().getScaleX()) {

                float chainSpeed = CHAIN_SPEED_HEAD_MIDDLE
                        * (float) Util.distance(wormParent.getTransform(), getGVRContext()
                                .getMainScene().getMainCameraRig().getTransform());

                middle.getTransform().setRotationByAxis(
                        MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
                end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

                float newX = middle.getTransform().getPositionX()
                        + (wormParent.getTransform().getPositionX() -
                        middle.getTransform().getPositionX()) * chainSpeed;

                float newY = middle.getTransform().getPositionY()
                        + (wormParent.getTransform().getPositionY() -
                        middle.getTransform().getPositionY()) * chainSpeed;

                float newZ = middle.getTransform().getPositionZ()
                        + (wormParent.getTransform().getPositionZ() -
                        middle.getTransform().getPositionZ()) * chainSpeed;

                middle.getTransform().setPosition(newX, newY, newZ);
            }

            if (MathUtils.distance(middle, end) > CHAIN_DISTANCE_MIDDLE_END
                    * end.getTransform().getScaleX()) {

                float chainSpeed = CHAIN_SPEED_MIDDLE_END
                        * (float) Util.distance(wormParent.getTransform(), getGVRContext()
                                .getMainScene().getMainCameraRig().getTransform());

                middle.getTransform().setRotationByAxis(
                        MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
                end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

                float newX = end.getTransform().getPositionX() + (middle.getTransform().getPositionX() -
                        end.getTransform().getPositionX()) * chainSpeed;

                float newY = end.getTransform().getPositionY() + (middle.getTransform().getPositionY() -
                        end.getTransform().getPositionY()) * chainSpeed;

                float newZ = end.getTransform().getPositionZ() + (middle.getTransform().getPositionZ() -
                        end.getTransform().getPositionZ()) * chainSpeed;

                end.getTransform().setPosition(newX, newY, newZ);
            }
        }
    }

    public void rotateWorm(MovementDirection movementDirection) {
        if (!isRotatingWorm) {
            isRotatingWorm = true;
            float angle = getRotatingAngle(movementDirection);
            new GVRRotationByAxisAnimation(head, .1f, angle, 0, 1, 0).start(
                    getGVRContext().getAnimationEngine()).setOnFinish(
                    new GVROnFinish() {

                        @Override
                        public void finished(GVRAnimation arg0) {
                            isRotatingWorm = false;
                        }
                    });
        }
    }

    private int getRotatingAngle(MovementDirection movementDirection) {
        int movementDiference = movementDirection.ordinal() - wormDirection.ordinal();
        wormDirection = movementDirection;
        if (movementDiference == 1 || movementDiference == -3) {
            return -90;
        } else if (movementDiference == 2 || movementDiference == -2) {
            return 180;
        } else if (movementDiference == 3 || movementDiference == -1) {
            return 90;
        } else {
            return 0;
        }
    }

    public void moveAlongCameraVector(float duration, float movement) {
        if (wormParentAnimation != null) {
            getGVRContext().getAnimationEngine().stop(wormParentAnimation);
        }

        GVRCameraRig cameraObject = getGVRContext().getMainScene().getMainCameraRig();

        float distance = (float) Util.distance(wormParent.getTransform(),
                cameraObject.getTransform())
                + movement;
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                wormParent.getTransform(), distance);

        if (movement < 0
                && MathUtils.distance(cameraObject.getTransform(), newPosition) < Constants.MIN_WORM_MOVE_DISTANCE)
            return;
        if (movement > 0
                && MathUtils.distance(cameraObject.getTransform(),
                        wormParent.getTransform()) > Constants.MAX_WORM_MOVE_DISTANCE)
            return;

        wormParentAnimation = new GVRRelativeMotionAnimation(wormParent.getTransform(),
                duration, newPosition[0] - wormParent.getTransform().getPositionX(),
                0,
                newPosition[2] - wormParent.getTransform().getPositionZ())
                .start(getGVRContext().getAnimationEngine());
    }

    public void rotateAroundCamera(float duration, float degree) {
        if (wormParentAnimation != null) {
            getGVRContext().getAnimationEngine().stop(wormParentAnimation);
        }

        wormParentAnimation = new GVRRotationByAxisWithPivotAnimation(
                wormParent.getTransform(), duration, degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                .start(getGVRContext().getAnimationEngine());

    }

    public void interactWithDPad() {

        if (!ScaleWorm.animPlaying) {

            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_X) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX) >= 1) {

                rotateAroundCamera(.1f, -5f);
                rotateWorm(MovementDirection.Right);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_X) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX) <= -1) {

                rotateAroundCamera(.1f, 5f);
                rotateWorm(MovementDirection.Left);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY) >= 1) {

                moveAlongCameraVector(.1f, -.225f);
                rotateWorm(MovementDirection.Down);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY) <= -1) {

                moveAlongCameraVector(.1f, .225f);
                rotateWorm(MovementDirection.Up);
            }
        }
    }

    public void checkWormEatingApple(GVRContext gvrContext) {

        Vector3D wormPosition = new Vector3D(wormParent.getTransform().getPositionX(), head
                .getParent()
                .getTransform().getPositionY(), wormParent.getTransform().getPositionZ());

        for (Apple a : Apple.appleList) {
            Vector3D applePosition = new Vector3D(a.getTransform().getPositionX(), a.getTransform()
                    .getPositionY(), a.getTransform().getPositionZ());

            if (Vector3D.distance(applePosition, wormPosition) < DISTANCE_TO_EAT_APPLE) {

                a.resetPosition(gvrContext);
            }
        }
    }

    public void animateWormByTouchPad() {

        if (!ScaleWorm.animPlaying) {

            SwipeDirection swipeDirection = TouchPadInput.getCurrent().swipeDirection;

            float duration = 0.6f;
            float movement = 0.75f;
            float degree = 22.5f;

            if (swipeDirection.name() == SwipeDirection.Up.name()) {
                moveAlongCameraVector(duration, movement);
                rotateWorm(MovementDirection.Up);

            } else if (swipeDirection.name() == SwipeDirection.Down.name()) {
                moveAlongCameraVector(duration, -movement);
                rotateWorm(MovementDirection.Down);

            } else if (swipeDirection.name() == SwipeDirection.Forward.name()) {
                rotateAroundCamera(duration, -degree);
                rotateWorm(MovementDirection.Right);

            } else if (swipeDirection.name() == SwipeDirection.Backward.name()) {
                rotateAroundCamera(duration, degree);
                rotateWorm(MovementDirection.Left);
            }
        }
    }

    public GVRSceneObject getHead() {
        return head;
    }

    public GVRSceneObject getMiddle() {
        return middle;
    }

    public GVRSceneObject getEnd() {
        return end;
    }

    public void moveWorm(float scaleFactor) {
        moveWormPart(getHead(), scaleFactor);
        moveWormPart(getEnd(), scaleFactor);
    }

    public void resetScaleWorm(float[] scaleFactor) {

        getHead().getTransform().setScale(scaleFactor[0], scaleFactor[0], scaleFactor[0]);
        getMiddle().getTransform().setScale(scaleFactor[1], scaleFactor[1], scaleFactor[1]);
        getEnd().getTransform().setScale(scaleFactor[2], scaleFactor[2], scaleFactor[2]);
    }

    public void scaleWorm(float scaleFactor) {

        scaleWormPart(getHead(), scaleFactor);
        scaleWormPart(getMiddle(), scaleFactor);
        scaleWormPart(getEnd(), scaleFactor);
    }

    private void moveWormPart(GVRSceneObject part, float scaleFactor) {

        float currentScale = part.getTransform().getScaleX();
        float ratio = (currentScale + scaleFactor) / currentScale;
        float currentPartPositionX = part.getTransform().getPositionX();
        float newPartPositionX = ratio * currentPartPositionX;

        new GVRRelativeMotionAnimation(part, 0.1f,
                newPartPositionX
                        - currentPartPositionX, 0, 0).start(getGVRContext()
                .getAnimationEngine());
    }

    private void scaleWormPart(GVRSceneObject part, float scaleFactor) {

        new GVRScaleAnimation(part, 0.1f, part.getTransform().getScaleX()
                + scaleFactor)
                .setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        ScaleWorm.animPlaying = false;
                    }

                }).start(getGVRContext().getAnimationEngine());
    }
}
