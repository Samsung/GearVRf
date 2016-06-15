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
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

/**
 * This class manages {@link GVRBaseSensor}s
 */
class SensorManager {
    private static final String TAG = SensorManager.class.getSimpleName();
    private static final float[] ORIGIN = new float[] { 0.0f, 0.0f, 0.0f };

    // Create a HashMap to keep reference counts
    private final Map<GVRBaseSensor, Integer> sensors;
    private static SensorManager instance;

    private SensorManager() {
        sensors = new HashMap<GVRBaseSensor, Integer>();
    }

    static SensorManager getInstance(){
        if(instance == null){
            instance = new SensorManager();
        }
        return instance;
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

    private void recurseSceneObject(GVRCursorController controller,
            GVRSceneObject object, GVRBaseSensor sensor,
            boolean markActiveNodes) {
        GVRBaseSensor objectSensor = object.getSensor();

        if (objectSensor == null) {
            objectSensor = sensor;
        }

        // Well at least we are not comparing against all scene objects.
        if (objectSensor != null && objectSensor.isEnabled() && object.isEnabled()) {

            /**
             * Compare ray against the hierarchical bounding volume and then add
             * the children accordingly.
             */
            Vector3f ray = controller.getRay();
            if (false == object.intersectsBoundingVolume(ORIGIN[0], ORIGIN[1],
                    ORIGIN[2], ray.x, ray.y, ray.z)) {                
                return;
            }

            if (object.hasMesh()) {
                float[] hitPoint = GVRPicker.pickSceneObjectAgainstBoundingBox(
                        object, ORIGIN[0], ORIGIN[1], ORIGIN[2], ray.x, ray.y,
                        ray.z);

                if (hitPoint != null) {
                    objectSensor.addSceneObject(controller, object, hitPoint);

                    // if we are doing an active search and we find one.
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
