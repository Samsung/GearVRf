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

package org.gearvrf;

/**
 * Maintains the context of each individual input pointer provided to the
 * {@link GVRInputManager}
 * 
 */
class GVRPointer {
    private int id;
    private boolean previousActive;
    private ActiveState activeState = ActiveState.NONE;
    private boolean invalidate = false;
    private boolean active;
    private float x, y, z;
    private float rayX, rayY, rayZ;

    enum ActiveState {
        ACTIVE_PRESSED, ACTIVE_RELEASED, NONE
    };

    GVRPointer(int id) {
        this.id = id;
        rayX = 0.0f;
        rayY = 0.0f;
        rayZ = -1.0f;
    }

    GVRPointer(int id, float x, float y, float z) {
        this(id);
        this.x = x;
        this.y = y;
        this.z = z;
        invalidate = true;
    }

    /**
     * Process the input event and mark the data for update
     * 
     * @param event
     */
    synchronized void processInput(InputEvent event) {
        this.x = event.getX();
        this.y = event.getY();
        this.z = event.getZ();
        this.active = event.getIsActive();
        invalidate = true;
    }

    ActiveState getActiveState() {
        return activeState;
    }

    private float square(float x) {
        return x * x;
    }

    int getId() {
        return id;
    }

    /**
     * Process the input data and return true if changed.
     * 
     * @return
     */
    synchronized boolean update() {
        if (invalidate) {
            if (previousActive == false && active) {
                activeState = ActiveState.ACTIVE_PRESSED;
            } else if (previousActive == true && active == false) {
                activeState = ActiveState.ACTIVE_RELEASED;

            } else {
                activeState = ActiveState.NONE;
            }
            previousActive = active;
            float inverseLength = (float) (1
                    / (Math.sqrt(square(x) + square(y) + square(z))));
            rayX = x * inverseLength;
            rayY = y * inverseLength;
            rayZ = z * inverseLength;

            invalidate = false;
            return true;
        }
        return false;
    }

    float getRayX() {
        return rayX;
    }

    float getRayY() {
        return rayY;
    }

    float getRayZ() {
        return rayZ;
    }

}
