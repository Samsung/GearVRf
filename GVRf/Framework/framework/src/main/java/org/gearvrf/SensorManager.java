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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private ByteBuffer readbackBufferB;
    private FloatBuffer readbackBuffer;
    // Data size 3 for x, y, z.
    private static int DATA_SIZE = 3;
    private static int BYTE_TO_FLOAT = 4;


    private SensorManager() {
        sensors = new ConcurrentHashMap<GVRBaseSensor, Integer>();
        // Create a readback buffer and forward it to the native layer
        readbackBufferB = ByteBuffer.allocateDirect(DATA_SIZE * BYTE_TO_FLOAT);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();
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

            if(isValidRay(controller.getRay())) {
                for (GVRSceneObject object : scene.getSceneObjects()) {
                    recurseSceneObject(controller, object, null, markActiveNodes);
                }
            }
            for (GVRBaseSensor sensor : sensors.keySet()) {
                sensor.processList(controller);
            }
        }
    }

    private boolean isValidRay(Vector3f ray) {
        if(ray == null || Float.isNaN(ray.x) || Float.isNaN(ray.y) || Float.isNaN(ray.z) ||
                (ray.x == 0 && ray.y == 0 && ray.z == 0)) {
            return false;
        } else {
            return true;
        }
    }

    private void recurseSceneObject(GVRCursorController controller,
                                    GVRSceneObject object, GVRBaseSensor sensor,
                                    boolean markActiveNodes) {
        GVRBaseSensor objectSensor = object.getSensor();

        if (objectSensor == null) {
            objectSensor = sensor;
        }
        /**
         * Compare ray against the hierarchical bounding volume and then add
         * the children accordingly.
         */
        Vector3f ray = controller.getRay();
        if (!object.intersectsBoundingVolume(ORIGIN[0], ORIGIN[1],
                ORIGIN[2], ray.x, ray.y, ray.z)) {
            return;
        }

        // Well at least we are not comparing against all scene objects.
        if (objectSensor != null && objectSensor.isEnabled() && object.isEnabled()
                & object.hasMesh()) {

            boolean result = GVRPicker.pickSceneObjectAgainstBoundingBox(
                    object, ORIGIN[0], ORIGIN[1], ORIGIN[2], ray.x, ray.y,
                    ray.z, readbackBufferB);

            if (result) {
                objectSensor.addSceneObject(controller, object, readbackBuffer.get(0),
                        readbackBuffer.get(1), readbackBuffer.get(2)
                );

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
