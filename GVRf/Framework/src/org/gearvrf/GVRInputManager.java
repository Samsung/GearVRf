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
    private SparseArray<GVRPointer> pointers = new SparseArray<GVRPointer>();
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
        GVRPointer pointer = pointers.get(id);
        if (pointer == null) {
            pointer = new GVRPointer(id);
            pointers.put(id, pointer);
        }
        pointer.processInput(event);
    }

    public void setupPointer(int pointerId, float x, float y, float z) {
        GVRPointer pointer = pointers.get(pointerId);
        if (pointer == null) {
            pointer = new GVRPointer(pointerId, x, y, z);
            pointers.put(pointerId, pointer);
        }
    }

    /**
     * Returns the direction the pointer is looking at.
     * 
     * @return The normalized direction in which the pointer is looking at.
     */
    public float[] getPointerDirection(int pointerId) {
        GVRPointer pointer = pointers.get(pointerId);
        if (pointer == null) {
            return null;
        } else {
            return new float[] { pointer.getRayX(), pointer.getRayY(),
                    pointer.getRayZ() };
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
                GVRPointer pointer = pointers.get(key);
                if (pointer.update()) {
                    sensorManager.processPick(pointer);
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
