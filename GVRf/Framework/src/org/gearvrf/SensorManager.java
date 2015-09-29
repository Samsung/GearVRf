package org.gearvrf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class manages {@link GVRBaseSensor}s
 */
class SensorManager {
    private static final String TAG = SensorManager.class.getSimpleName();

    // Create a HashMap to keep reference counts
    private final Map<GVRBaseSensor, Integer> sensors;
    private GVRScene scene;
    private boolean previousActive = false;

    SensorManager(GVRScene scene) {
        this.scene = scene;
        sensors = new HashMap<GVRBaseSensor, Integer>();
    }

    synchronized void setScene(GVRScene scene) {
        this.scene = scene;
    }

    synchronized GVRScene getScene() {
        return scene;
    }

    /**
     * Uses the GVR Picker for now but would need help from the renderer later
     * for efficiency.
     * 
     * We could possibly push this functionality to the native layer. But for
     * now we keep it here.
     */
    void processPick(int pointerId, float rayX, float rayY, float rayZ,
            boolean active) {
        float[] origin = new float[] { 0.0f, 0.0f, 0.0f };
        float[] direction = new float[] { rayX, rayY, rayZ };
        boolean markActiveNodes = false;
        if (previousActive == false && active == true) {
            // active is true, trigger a search for active sensors
            markActiveNodes = true;
        } else if (previousActive == true && active == false) {
            for (GVRBaseSensor sensor : sensors.keySet()) {
                sensor.setActive(false);
            }
        }
        previousActive = active;

        for (GVRSceneObject object : scene.getSceneObjects()) {
            recurseSceneObject(object, null, origin, direction,
                    markActiveNodes);
        }
        for (GVRBaseSensor sensor : sensors.keySet()) {
            sensor.processList(pointerId);
        }

    }

    void recurseSceneObject(GVRSceneObject object, GVRBaseSensor sensor,
            float[] origin, float direction[], boolean markActiveNodes) {
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
                    object, origin[0], origin[1], origin[2], direction[0],
                    direction[1], direction[2]);
            if (hitPoint != null) {
                objectSensor.addSceneObject(object, hitPoint);

                // if we are doing an active search and we found one.
                if (markActiveNodes) {
                    objectSensor.setActive(true);
                }
            }
        }

        for (GVRSceneObject child : object.getChildren()) {
            recurseSceneObject(child, objectSensor, origin, direction,
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
