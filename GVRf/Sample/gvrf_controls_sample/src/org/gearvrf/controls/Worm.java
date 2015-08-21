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
import org.gearvrf.controls.util.MathUtils;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;

public class Worm extends GVRSceneObject {

    // Chain Data
    private final float CHAIN_DISTANCE_HEAD_MIDDLE = 0.23f;
    private final float CHAIN_DISTANCE_MIDDLE_END = 0.19f;

    private final float CHAIN_SPEED_HEAD_MIDDLE = 0.055f;
    private final float CHAIN_SPEED_MIDDLE_END = 0.065f;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.9f;
    private float MAX_MOVE_DISTANCE = 14.0f;
    private float MIN_MOVE_DISTANCE = 2f;

    private GVRSceneObject head, middle, end, wormParent;

    private boolean isRotatingWorm = false;

    private GVRAnimation wormParentAnimation;

    private MovementDirection wormDirection = MovementDirection.Up;

    public enum MovementDirection {
        Up, Right, Down, Left
    }

    public Worm(GVRContext gvrContext) {

        super(gvrContext);

        GVRMesh mesh = gvrContext.loadMesh(
                new GVRAndroidResource(gvrContext, R.raw.worm_mesh_head));
        GVRTexture texture = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.worm_head_texture));

        wormParent = new GVRSceneObject(gvrContext);

        head = new GVRSceneObject(gvrContext, mesh, texture);
        head.getTransform().setPosition(0, 0, 0);
        head.getTransform().setScale(0.4f, 0.4f, 0.4f);
        head.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        wormParent.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);

        mesh = gvrContext.loadMesh(
                new GVRAndroidResource(gvrContext, R.raw.worm_mesh_middle));
        middle = new GVRSceneObject(gvrContext, mesh, texture);
        middle.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        middle.getTransform().setScale(0.4f, 0.4f, 0.4f);
        middle.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        mesh = gvrContext.loadMesh(
                new GVRAndroidResource(gvrContext, R.raw.worm_mesh_end));
        end = new GVRSceneObject(gvrContext, mesh, texture);
        end.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        end.getTransform().setScale(0.4f, 0.4f, 0.4f);
        end.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        wormParent.addChildObject(head);
        this.addChildObject(wormParent);
        this.addChildObject(middle);
        this.addChildObject(end);
    }

    public void chainMove(GVRContext gvrContext) {

        if (MathUtils.distance(head.getParent(), middle) > CHAIN_DISTANCE_HEAD_MIDDLE) {

            float chainSpeed = CHAIN_SPEED_HEAD_MIDDLE
                    * (float) Util.distance(head.getParent().getTransform(), getGVRContext()
                            .getMainScene().getMainCameraRig().getTransform());

            middle.getTransform().setRotationByAxis(
                    MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
            end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

            float newX = middle.getTransform().getPositionX()
                    + (head.getParent().getTransform().getPositionX() -
                    middle.getTransform().getPositionX()) * chainSpeed;

            float newY = middle.getTransform().getPositionY()
                    + (head.getParent().getTransform().getPositionY() -
                    middle.getTransform().getPositionY()) * chainSpeed;

            float newZ = middle.getTransform().getPositionZ()
                    + (head.getParent().getTransform().getPositionZ() -
                    middle.getTransform().getPositionZ()) * chainSpeed;

            middle.getTransform().setPosition(newX, newY, newZ);
        }
        if (MathUtils.distance(middle, end) > CHAIN_DISTANCE_MIDDLE_END) {

            float chainSpeed = CHAIN_SPEED_MIDDLE_END
                    * (float) Util.distance(head.getParent().getTransform(), getGVRContext()
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

        float distance = (float) Util.distance(head.getParent().getTransform(),
                cameraObject.getTransform())
                + movement;
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                head.getParent().getTransform(), distance);

        if (movement < 0
                && MathUtils.distance(cameraObject.getTransform(), newPosition) < MIN_MOVE_DISTANCE)
            return;
        if (movement > 0
                && MathUtils.distance(cameraObject.getTransform(),
                        wormParent.getTransform()) > MAX_MOVE_DISTANCE)
            return;

        wormParentAnimation = new GVRRelativeMotionAnimation(head.getParent(),
                duration, newPosition[0] - head.getParent().getTransform().getPositionX(),
                0,
                newPosition[2] - head.getParent().getTransform().getPositionZ())
                .start(getGVRContext().getAnimationEngine());
    }

    public void rotateAroundCamera(float duration, float degree) {
        if (wormParentAnimation != null) {
            getGVRContext().getAnimationEngine().stop(wormParentAnimation);
        }
        wormParentAnimation = new GVRRotationByAxisWithPivotAnimation(
                head.getParent(), duration, degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                .start(getGVRContext().getAnimationEngine());

    }

}
