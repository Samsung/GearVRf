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

import java.util.HashMap;
import java.util.Map;

import org.gearvrf.GVRCursorController.ActiveState;

/**
 * This class manages {@link GVRBaseSensor}s
 */
class SensorManager {
    private static final String TAG = SensorManager.class.getSimpleName();
    private static final float[] ORIGIN = new float[] { 0.0f, 0.0f, 0.0f };

    // Create a HashMap to keep reference counts
    private final Map<GVRBaseSensor, Integer> sensors;

    SensorManager() {
        sensors = new HashMap<GVRBaseSensor, Integer>();
    }

    /**
     * Uses the GVR Picker for now .. but would need help from the renderer
     * later for efficiency.
     * 
     * We could possibly push this functionality to the native layer. But for
     * now we keep it here.
     */
    void processPick(GVRScene scene, GVRCursorController controller) {
        if (scene != null) {
            boolean markActiveNodes = false;
            if (controller.getActiveState() == ActiveState.ACTIVE_PRESSED) {
                // active is true, trigger a search for active sensors
                markActiveNodes = true;
            } else if (controller
                    .getActiveState() == ActiveState.ACTIVE_RELEASED) {
                for (GVRBaseSensor sensor : sensors.keySet()) {
                    sensor.setActive(controller, false);
                }
            }

            for (GVRSceneObject object : scene.getSceneObjects()) {
                recurseSceneObject(controller, object, null, markActiveNodes);
            }
            for (GVRBaseSensor sensor : sensors.keySet()) {
                sensor.processList(controller);
            }
        }
    }

    void recurseSceneObject(GVRCursorController controller,
            GVRSceneObject object, GVRBaseSensor sensor,
            boolean markActiveNodes) {
        GVRBaseSensor objectSensor = object.getSensor();

        if (objectSensor == null) {
            objectSensor = sensor;
        }

        // Well at least we are not comparing against all scene objects.
        if (objectSensor != null && objectSensor.isEnabled()
                && object.getRenderData() != null
                && object.getRenderData().getMesh() != null) {

            /**
             * Ideally we could like to compare against the hierarchical
             * bounding volume and then add the children accordingly. But since
             * the <ray - hierarchical bounding volume> does not work at the
             * moment we will make do with the <ray - bounding box> test.
             */
            float[] hitPoint = GVRPicker.pickSceneObjectAgainstBoundingBox(
                    object, ORIGIN[0], ORIGIN[1], ORIGIN[2],
                    controller.getRayX(), controller.getRayY(),
                    controller.getRayZ());
            if (hitPoint != null) {
                objectSensor.addSceneObject(controller, object, hitPoint);

                // if we are doing an active search and we found one.
                if (markActiveNodes) {
                    objectSensor.setActive(controller, true);
                }
            }
        }

        for (GVRSceneObject child : object.getChildren()) {
            recurseSceneObject(controller, child, objectSensor,
                    markActiveNodes);
        }
    }

    void addSensor(GVRBaseSensor sensor) {
        Integer count = sensors.get(sensor);
        if (count == null) {
            // sensor not in HashMap
            sensors.put(sensor, 1);

        } else {
            // increment count
            sensors.put(sensor, ++count);
        }
    }

    void clear() {
        sensors.clear();
    }

    void removeSensor(GVRBaseSensor sensor) {
        Integer count = sensors.get(sensor);
        if (count == null) {
            // invalid sensor
            return;
        }

        if (count == 1) {
            // last remaining reference, remove
            sensors.remove(sensor);
        } else {
            // decrement count
            sensors.put(sensor, --count);
        }
    }
}
