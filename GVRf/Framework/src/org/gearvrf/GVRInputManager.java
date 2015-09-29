package org.gearvrf;

import android.util.SparseArray;

/**
 * The {@link GVRInputManager} class handles all the input coming into GVRf
 * using the call {@link #dispatchInputEvent(InputEvent)}
 * 
 * {@link InputEvent}s are dispatched across the attached scene graph to
 * activate the related sensors in the graph.
 * 
 */
public class GVRInputManager {
    private static final String TAG = GVRInputManager.class.getSimpleName();
    private SparseArray<PointerHolder> pointers = new SparseArray<PointerHolder>();
    private final SensorManager sensorManager;

    GVRInputManager(GVRContext gvrContext, GVRScene scene) {
        sensorManager = new SensorManager(scene);
        gvrContext.registerDrawFrameListener(drawFrameListener);
    }

    /**
     * This method sets a new scene for the {@link GVRInputManager}
     * 
     * @param scene
     */
    public void setScene(GVRScene scene) {
        sensorManager.setScene(scene);
    }

    /**
     * Use the {@link InputEvent} class to feed input data to the
     * {@link GVRInputManager}.
     * 
     * @param event
     */
    public void dispatchInputEvent(InputEvent event) {
        int id = event.getPointerId();
        PointerHolder holder = pointers.get(id);
        if (holder == null) {
            holder = new PointerHolder(id);
            pointers.put(id, holder);
        }
        holder.invalidate(event.getX(), event.getY(), event.getZ(),
                event.getIsActive());
    }

    public void setupPointer(int pointerId, float x, float y, float z) {
        PointerHolder holder = pointers.get(pointerId);
        if (holder == null) {
            holder = new PointerHolder(pointerId);
            pointers.put(pointerId, holder);
        }
        holder.invalidate(x, y, z, false);
    }

    private class PointerHolder {
        private boolean invalidate = false;
        private float x, y, z;

        private boolean active;
        private int id;
        float rayX, rayY, rayZ;

        public PointerHolder(int id) {
            this.id = id;
            rayX = 0.0f;
            rayY = 0.0f;
            rayZ = -1.0f;
        }

        public synchronized void invalidate(float x, float y, float z,
                boolean active) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.active = active;
            invalidate = true;
        }

        private float square(float x) {
            return x * x;
        }

        public synchronized boolean update() {
            if (invalidate) {
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
    }

    /**
     * Returns the direction the pointer is looking at.
     * 
     * @return The normalized direction in which the pointer is looking at.
     */

    public float[] getPointerDirection(int pointerId) {
        PointerHolder holder = pointers.get(pointerId);
        if (holder == null) {
            return null;
        } else {
            return new float[] { holder.rayX, holder.rayY, holder.rayZ };
        }
    }

    void close() {
        pointers.clear();
        sensorManager.clear();
    }

    private GVRDrawFrameListener drawFrameListener = new GVRDrawFrameListener() {
        @Override
        public void onDrawFrame(float frameTime) {
            for (int i = 0; i < pointers.size(); i++) {
                int key = pointers.keyAt(i);
                // get the object by the key.
                PointerHolder holder = pointers.get(key);
                if (holder.update()) {
                    sensorManager.processPick(holder.id, holder.rayX,
                            holder.rayY, holder.rayZ, holder.active);
                }
            }
        }
    };

    void addSensor(GVRBaseSensor sensor) {
        sensorManager.addSensor(sensor);
    }

    void removeSensor(GVRBaseSensor sensor) {
        sensorManager.removeSensor(sensor);
    }

}
