/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sample.hand.template;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a hand.
 */
public class IOHand {
    private static int PALM_KEY = 0;
    private Map<Integer, IOFinger> fingerMap;
    private Map<Integer, GVRSceneObject> auxSceneObjects;
    private GVRSceneObject handSceneObject;

    /**
     * Create an {@link IOHand} object.
     *
     * @param gvrContext
     */
    public IOHand(GVRContext gvrContext) {
        handSceneObject = new GVRSceneObject(gvrContext);

        // Create the fingers
        IOFinger index = new IOFinger(IOFinger.INDEX, handSceneObject);
        IOFinger middle = new IOFinger(IOFinger.MIDDLE, handSceneObject);
        IOFinger ring = new IOFinger(IOFinger.RING, handSceneObject);
        IOFinger pinky = new IOFinger(IOFinger.PINKY, handSceneObject);
        IOFinger thumb = new IOFinger(IOFinger.THUMB, handSceneObject);

        fingerMap = new HashMap<Integer, IOFinger>(5);

        // Create a map to add auxiliary objects that may be added to the IOHand
        auxSceneObjects = new HashMap<Integer, GVRSceneObject>();

        // add the fingers to the map.
        fingerMap.put(IOFinger.INDEX, index);
        fingerMap.put(IOFinger.MIDDLE, middle);
        fingerMap.put(IOFinger.RING, ring);
        fingerMap.put(IOFinger.PINKY, pinky);
        fingerMap.put(IOFinger.THUMB, thumb);
    }

    /**
     * Return the {@link GVRSceneObject} that is controlled by this {@link IOHand}
     *
     * @return
     */
    public GVRSceneObject getSceneObject() {
        return handSceneObject;
    }

    /**
     * Add a {@link GVRSceneObject} to this {@link IOHand}
     *
     * @param key         an int value that uniquely helps identify this {@link GVRSceneObject}.
     *                    So that
     *                    it can easily be looked up later on.
     * @param sceneObject {@link GVRSceneObject} that is to be added.
     */
    public void addSceneObject(int key, GVRSceneObject sceneObject) {
        // only add if not present
        if (!auxSceneObjects.containsKey(key)) {
            auxSceneObjects.put(key, sceneObject);
            handSceneObject.addChildObject(sceneObject);
        }
    }

    /**
     * A convenience method to add a scene object that represents the palm
     *
     * @param sceneObject
     */
    public void addPalmSceneObject(GVRSceneObject sceneObject) {
        addSceneObject(PALM_KEY, sceneObject);
    }

    /**
     * Use the provided key to look up the {@link GVRSceneObject}
     *
     * @param key
     * @return
     */
    public GVRSceneObject getSceneObject(int key) {
        return auxSceneObjects.get(key);
    }

    /**
     * Get the {@link GVRSceneObject} corresponding to the palm (if there is one)
     *
     * @return
     */
    public GVRSceneObject getPalmSceneObject() {
        return auxSceneObjects.get(PALM_KEY);
    }

    /**
     * Get the {@link IOFinger} corresponding to the given {@link IOFinger} type.
     *
     * @param type
     * @return
     */
    public IOFinger getIOFinger(int type) {
        return fingerMap.get(type);
    }
}
