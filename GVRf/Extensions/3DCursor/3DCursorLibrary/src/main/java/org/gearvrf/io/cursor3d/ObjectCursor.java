/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRCursorController.ControllerEventListener;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ObjectCursor extends Cursor {
    private static final String TAG = ObjectCursor.class.getSimpleName();
    private static final float POINT_CURSOR_NEAR_DEPTH = -1.0f;
    private static final float[] EMPTY_HIT_POINT = new float[3];
    private Set<GVRSceneObject> intersecting;
    private Set<GVRSceneObject> previousHits;
    private List<GVRSceneObject> newHits;

    ObjectCursor(GVRContext context, CursorManager cursorManager) {
        super(context, CursorType.OBJECT, cursorManager);
        intersecting = new HashSet<GVRSceneObject>();
        previousHits = new HashSet<GVRSceneObject>();
        newHits = new ArrayList<GVRSceneObject>();
    }

    @Override
    void dispatchSensorEvent(SensorEvent event) {
        GVRSceneObject object = event.getObject();
        GVRCursorController controller = event.getCursorController();
        isControllerActive = event.isActive();

        if (intersecting.contains(object)) {
            createAndSendCursorEvent(event.getObject(), true, event.getHitX(), event.getHitY(),
                    event.getHitZ(), true, isControllerActive, controller.getKeyEvent(),
                    controller.getMotionEvents());
        } else {
            createAndSendCursorEvent(event.getObject(), false, event.getHitX(), event.getHitY(),
                    event.getHitZ(), event.isOver(), isControllerActive,
                    controller.getKeyEvent(), controller.getMotionEvents());
        }
    }

    private void createAndSendCursorEvent(GVRSceneObject sceneObject, boolean colliding, float
            hitX, float hitY, float hitZ, boolean isOver, boolean isActive, KeyEvent keyEvent,
                                          List<MotionEvent> motionEvents) {
        CursorEvent cursorEvent = CursorEvent.obtain();
        cursorEvent.setColliding(colliding);
        cursorEvent.setHitPoint(hitX, hitY, hitZ);
        cursorEvent.setCursorPosition(getPositionX(), getPositionY(), getPositionZ());
        cursorEvent.setCursorRotation(getRotationW(), getRotationX(), getRotationY(),
                getRotationZ());
        cursorEvent.setOver(isOver);
        cursorEvent.setObject(sceneObject);
        cursorEvent.setActive(isActive);
        cursorEvent.setCursor(this);
        cursorEvent.setKeyEvent(keyEvent);
        cursorEvent.setMotionEvents(motionEvents);

        if (intersecting.isEmpty() == false) {
            if (isActive) {
                checkAndSetAsset(Action.CLICK);
            } else {
                checkAndSetAsset(Action.INTERSECT);
            }
        } else {
            checkAndSetAsset(Action.DEFAULT);
        }

        dispatchCursorEvent(cursorEvent);
    }

    private boolean pointInBox(float x, float y, float z, Vector3f cubeMin, Vector3f cubeMax) {
        return (x < cubeMin.x || y < cubeMin.y || z < cubeMin.z
                || x > cubeMax.x || y > cubeMax.y || z > cubeMax.z) == false;
    }

    @Override
    ControllerEventListener getControllerEventListener() {
        return listener;
    }

    private ControllerEventListener listener = new ControllerEventListener() {
        @Override
        public void onEvent(GVRCursorController controller) {
            if (scene == null) {
                return;
            }
            boolean sentEvent = false;
            newHits.clear();

            KeyEvent keyEvent = controller.getKeyEvent();
            if(!controller.isEventHandledBySensorManager()) {
                checkControllerActive(controller);
            }

            for (GVRSceneObject object : scene.getSceneObjects()) {
                sentEvent = sentEvent || recurseSceneObject(keyEvent, object, null);
            }

            handleControllerEvent(controller, sentEvent);

            for (GVRSceneObject object : previousHits) {
                if (intersecting.contains(object)) {
                    intersecting.remove(object);
                }
                createAndSendCursorEvent(object, false, EMPTY_HIT_POINT[0],EMPTY_HIT_POINT[1],
                        EMPTY_HIT_POINT[2], false, isControllerActive, keyEvent, null);
            }
            previousHits.clear();
            previousHits.addAll(newHits);
        }
    };

    private boolean recurseSceneObject(KeyEvent keyEvent, GVRSceneObject object,
                                       GVRBaseSensor sensor) {
        boolean sentEvent = false;
        GVRBaseSensor objectSensor = object.getSensor();
        if (objectSensor == null) {
            objectSensor = sensor;
        }

        if (objectSensor != null && objectSensor.isEnabled()) {
            if (object.hasMesh()) {
                GVRMesh mesh = object.getRenderData().getMesh().getBoundingBox();
                Matrix4f matrix4f = object.getTransform().getModelMatrix4f();
                float[] vertices = mesh.getVertices();
                int length = vertices.length;

                Vector3f cubeMin = new Vector3f(vertices[0], vertices[1], vertices[2]);
                Vector3f cubeMax = new Vector3f(vertices[length - 3], vertices[length - 2],
                        vertices[length - 1]);
                cubeMin.mulPoint(matrix4f);
                cubeMax.mulPoint(matrix4f);

                if (!pointInBox(cursorSceneObject.getPositionX(), cursorSceneObject.getPositionY(),
                        cursorSceneObject.getPositionZ(), cubeMin, cubeMax)) {
                    if (isColliding(object)) {
                        addNewHit(object);
                        createAndSendCursorEvent(object, true, EMPTY_HIT_POINT[0],
                                EMPTY_HIT_POINT[1], EMPTY_HIT_POINT[2], true, isControllerActive,
                                keyEvent, null);
                        sentEvent = true;
                    }
                } else {
                    addNewHit(object);
                }
            }
        }

        for (GVRSceneObject child : object.getChildren()) {
            sentEvent = sentEvent || recurseSceneObject(keyEvent, child, objectSensor);
        }
        return sentEvent;
    }

    private void addNewHit(GVRSceneObject object) {
        if (previousHits.contains(object)) {
            previousHits.remove(object);
        }

        if (intersecting.contains(object) == false) {
            intersecting.add(object);
        }

        newHits.add(object);
    }

    @Override
    void setScale(float scale) {
        // place the cursor at half the depth scale
        if(scale > MAX_CURSOR_SCALE) {
            return;
        }
        super.setScale(scale / 2);

        if (ioDevice != null) {
            ioDevice.setNearDepth(POINT_CURSOR_NEAR_DEPTH);
        }
    }

    @Override
    void setIoDevice(IoDevice ioDevice) {
        super.setIoDevice(ioDevice);
        ioDevice.setNearDepth(POINT_CURSOR_NEAR_DEPTH);
    }
}
