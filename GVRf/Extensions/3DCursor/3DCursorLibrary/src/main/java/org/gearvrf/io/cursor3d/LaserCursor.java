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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRCursorController.ControllerEventListener;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.gearvrf.utility.Log;

/**
 * Class that represents a laser type cursor.
 */
class LaserCursor extends Cursor {
    private static final String TAG = "LaserCursor";
    private static final boolean COLLIDING = true;

    /**
     * Create a laser type 3D cursor for GVRf.
     *
     * @param context The GVRf Context
     */
    LaserCursor(GVRContext context, CursorManager manager) {
        super(context, CursorType.LASER, manager);
        Log.d(TAG, Integer.toHexString(hashCode()) + " constructed");
    }

    @Override
    void dispatchSensorEvent(SensorEvent event) {
        CursorEvent cursorEvent = CursorEvent.obtain();
        cursorEvent.setOver(event.isOver());
        cursorEvent.setColliding(COLLIDING);
        cursorEvent.setActive(event.isActive());
        cursorEvent.setCursor(this);
        GVRPicker.GVRPickedObject pickedObject = event.getPickedObject();
        GVRSceneObject object = pickedObject.getHitObject();
        SelectableGroup selectableGroup = (SelectableGroup) object.getComponent
                (SelectableGroup.getComponentType());

        if (selectableGroup != null) {
            //if part of a selectable group and disabled then ignore
            if (!object.isEnabled()) {
                return;
            }
            object = selectableGroup.getParent();
        }

        cursorEvent.setObject(object);
        cursorEvent.setHitPoint(pickedObject.hitLocation[0], pickedObject.hitLocation[1], pickedObject.hitLocation[2]);
        cursorEvent.setCursorPosition(getPositionX(), getPositionY(), getPositionZ());
        cursorEvent.setCursorRotation(getRotationW(), getRotationX(), getRotationY(),
                getRotationZ());
        GVRCursorController controller = event.getCursorController();
        cursorEvent.setMotionEvents(controller.getMotionEvents());
        cursorEvent.setKeyEvent(controller.getKeyEvent());
        isControllerActive = event.isActive();
        cursorEvent.setEventGroup(event.getEventGroup());

        if (event.isActive()) {
            checkAndSetAsset(Action.CLICK);
        } else {
            if (event.isOver()) {
                checkAndSetAsset(Action.HOVER);
            } else {
                checkAndSetAsset(Action.DEFAULT);
            }
        }
        dispatchCursorEvent(cursorEvent);
    }

    @Override
    ControllerEventListener getControllerEventListener() {
        return listener;
    }

    @Override
    void setScale(float scale) {
        if(scale > MAX_CURSOR_SCALE) {
            return;
        }
        super.setScale(scale);
        /* the laser cursor does not use depth, set a fixed depth.*/
        cursorSceneObject.setScale(scale);
        if (ioDevice != null) {
            ioDevice.setFarDepth(-scale);
            ioDevice.setNearDepth(-scale);
        }
    }

    @Override
    void setIoDevice(IoDevice ioDevice) {
        super.setIoDevice(ioDevice);
        ioDevice.setFarDepth(-scale);
        ioDevice.setNearDepth(-scale);
    }

    @Override
    void setupIoDevice(IoDevice ioDevice) {
        super.setupIoDevice(ioDevice);
        ioDevice.setDisableRotation(true);
    }

    private ControllerEventListener listener = new ControllerEventListener() {

        @Override
        public void onEvent(GVRCursorController controller) {
            if(!controller.isEventHandledBySensorManager()) {
                checkControllerActive(controller);
            }
            handleControllerEvent(controller, false);
        }
    };
}