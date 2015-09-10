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
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.animation.GVRColorSwapAnimation;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.model.Apple;
import org.gearvrf.controls.shaders.ColorSwapShader;
import org.gearvrf.controls.util.ColorControls;
import org.gearvrf.controls.util.ColorControls.Color;
import org.gearvrf.controls.util.Constants;
import org.gearvrf.controls.util.MathUtils;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class Worm extends GVRSceneObject {

    private static final float MINIMUM_DISTANCE_FACTOR = 0.5f;
    // Chain Data
    private final float CHAIN_DISTANCE_HEAD_MIDDLE = 0.575f;
    private final float CHAIN_DISTANCE_MIDDLE_END = 0.475f;

    private final float CHAIN_SPEED_HEAD_MIDDLE = 0.055f;
    private final float CHAIN_SPEED_MIDDLE_END = 0.065f;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.9f;

    private final float WORM_SCALE_ANIMATION_DURATION = .1f;

    private float DISTANCE_TO_EAT_APPLE = 0.50f;
    private GVRSceneObject head, middle, end;

    public GVRSceneObject wormParent;
    private boolean isRotatingWorm = false;

    private GVRAnimation wormParentAnimation;

    private MovementDirection wormDirection = MovementDirection.Up;

    public enum MovementDirection {
        Up, Right, Down, Left
    }

    public Worm(GVRContext gvrContext) {

        super(gvrContext);

        wormParent = new GVRSceneObject(gvrContext);

        head = createSegment(gvrContext, R.raw.sphere_head, R.drawable.worm_head_texture);
        middle = createSegment(gvrContext, R.raw.sphere_body, R.drawable.worm_head_texture);
        end = createSegment(gvrContext, R.raw.sphere_tail, R.drawable.worm_head_texture);

        applyShader(gvrContext, head);
        applyShader(gvrContext, middle);
        applyShader(gvrContext, end);
        wormParent.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        head.getTransform().setPosition(0, 0, 0);
        wormParent.addChildObject(head);
        this.addChildObject(wormParent);
        this.addChildObject(middle);
        this.addChildObject(end);

    }

    private void applyShader(GVRContext gvrContext, GVRSceneObject wormPiece) {

        ColorControls gvrColor = new ColorControls(gvrContext.getContext());
        Color color = gvrColor.parseColor(R.color.color10);

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_light));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, texture);

        texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_2));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_DETAILS, texture);
        wormPiece.getRenderData().getMaterial()
                .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(), color.getBlue(),
                        1);
    }

    public GVRSceneObject createSegment(GVRContext gvrContext, int meshID, int textureID) {

        GVRMesh mesh = gvrContext.loadMesh(
                new GVRAndroidResource(gvrContext, meshID));
        GVRTexture texture = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, textureID));

        GVRSceneObject segment = new GVRSceneObject(gvrContext, mesh, texture, new ColorSwapShader(
                getGVRContext()).getShaderId());
        segment.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        segment.getTransform().setScale(0.4f, 0.4f, 0.4f);
        segment.getRenderData().setRenderingOrder(RenderingOrder.WORM);

        return segment;
    }

    public void changeColor(Color color) {

        float[] colorArray = new float[3];
        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();
        new GVRColorSwapAnimation(head, 3, colorArray).start(getGVRContext().getAnimationEngine());
        new GVRColorSwapAnimation(middle, 3, colorArray)
                .start(getGVRContext().getAnimationEngine());
        new GVRColorSwapAnimation(end, 3, colorArray).start(getGVRContext().getAnimationEngine());
        // head.getRenderData()
        // .getMaterial()
        // .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(),
        // color.getBlue(),
        // 1);
        //
        // middle.getRenderData()
        // .getMaterial()
        // .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(),
        // color.getBlue(),
        // 1);
        //
        // end.getRenderData()
        // .getMaterial()
        // .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(),
        // color.getBlue(),
        // 1);
    }

    public void chainMove(GVRContext gvrContext) {

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

    public GVRSceneObject getHead() {
        return head;
    }

    public GVRSceneObject getMiddle() {
        return middle;
    }

    public GVRSceneObject getEnd() {
        return end;
    }

    private void handleMinDistance(GVRSceneObject origin, GVRSceneObject destination,
            float minDistance) {
        float[] newPos = MathUtils.direction(destination.getTransform(), origin.getTransform());

        newPos[0] *= minDistance;
        newPos[1] *= minDistance;
        newPos[2] *= minDistance;
        setNewPartPosition(origin, newPos);

    }

    private void setNewPartPosition(GVRSceneObject origin, float[] newPos) {

        origin.getTransform().setPositionX(origin.getTransform().getPositionX() + newPos[0]);
        origin.getTransform().setPositionY(origin.getTransform().getPositionY() + newPos[1]);
        origin.getTransform().setPositionZ(origin.getTransform().getPositionZ() + newPos[2]);

    }

    public void moveWorm(float scaleFactor) {
        moveWormPart(getHead(), scaleFactor);

        moveWormPart(getEnd(), scaleFactor);

    }

    public void scaleWorm(float scaleFactor) {

        scaleWormPart(getHead(), scaleFactor);

        scaleWormPart(getMiddle(), scaleFactor);

        scaleWormPart(getEnd(), scaleFactor);

        if (scaleFactor > 0) {

            handleMinDistance(middle, wormParent, CHAIN_SPEED_HEAD_MIDDLE * 2
                    * middle.getTransform().getScaleX());

            handleMinDistance(end, wormParent, CHAIN_SPEED_MIDDLE_END * 2
                    * middle.getTransform().getScaleX());

        }
    }

    private void moveWormPart(GVRSceneObject part, float scaleFactor) {

        float currentScale = part.getTransform().getScaleX();
        float ratio = (currentScale + scaleFactor) / currentScale;
        float currentPartPositionX = part.getTransform().getPositionX();
        float newPartPositionX = ratio * currentPartPositionX;
        new GVRRelativeMotionAnimation(part, WORM_SCALE_ANIMATION_DURATION,
                newPartPositionX
                        - currentPartPositionX, 0, 0).start(getGVRContext()
                .getAnimationEngine());
    
    }

    private void scaleWormPart(GVRSceneObject part, float scaleFactor) {
        new GVRScaleAnimation(part, WORM_SCALE_ANIMATION_DURATION, part.getTransform().getScaleX()
                + scaleFactor).start(getGVRContext()
                .getAnimationEngine());
    }
}