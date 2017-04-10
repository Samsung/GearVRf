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

import org.gearvrf.GVRSceneObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * The class represents a finger
 */
public class IOFinger extends IOBaseComponent {
    public static final int THUMB = 0;
    public static final int INDEX = 1;
    public static final int MIDDLE = 2;
    public static final int RING = 3;
    public static final int PINKY = 4;

    private Map<Integer, IOBone> boneMap;
    private Map<Integer, IOJoint> jointMap;

    public static String getString(int i) {
        switch (i) {
            case THUMB:
                return "THUMB";
            case INDEX:
                return "INDEX";
            case MIDDLE:
                return "MIDDLE";
            case RING:
                return "RING";
            case PINKY:
                return "PINKY";
            default:
                return "THUMB";
        }
    }

    /**
     * Create an {@link IOFinger} of the given type
     *
     * @param type            the type of the
     *                        {@link IOFinger}. Use the {@link IOFinger#getString(int)} call to
     *                        know the readable type of the finger.
     * @param handSceneObject This is the root {@link GVRSceneObject} that represents the hand.
     */
    public IOFinger(int type, GVRSceneObject handSceneObject) {
        super(type, handSceneObject);

        IOBone distal = new IOBone(IOBone.DISTAL, handSceneObject);
        IOBone intermediate = new IOBone(IOBone.INTERMEDIATE, handSceneObject);
        IOBone proximal = new IOBone(IOBone.PROXIMAL, handSceneObject);

        // Not required
        //IOBone metacarpal = new IOBone(handSceneObject, IOBone.METACARPAL);

        IOJoint jointTip = new IOJoint(IOJoint.TIP, handSceneObject);
        IOJoint jointDip = new IOJoint(IOJoint.DIP, handSceneObject);
        IOJoint jointPip = new IOJoint(IOJoint.PIP, handSceneObject);
        IOJoint jointMcp = new IOJoint(IOJoint.MCP, handSceneObject);

        boneMap = new HashMap<Integer, IOBone>(4);
        boneMap.put(IOBone.DISTAL, distal);
        boneMap.put(IOBone.INTERMEDIATE, intermediate);
        boneMap.put(IOBone.PROXIMAL, proximal);

        //boneMap.put(IOBone.METACARPAL, metacarpal);

        jointMap = new HashMap<Integer, IOJoint>(4);
        jointMap.put(IOJoint.TIP, jointTip);
        jointMap.put(IOJoint.DIP, jointDip);
        jointMap.put(IOJoint.PIP, jointPip);
        jointMap.put(IOJoint.MCP, jointMcp);
    }

    /**
     * Get the {@link IOJoint} corresponding to the given {@link IOJoint} type.
     *
     * @param type the type of the {@link IOJoint}
     * @return the corresponding {@link IOJoint}
     */
    public IOJoint getIOJoint(int type) {
        return jointMap.get(type);
    }

    /**
     * Get the {@link IOBone} corresponding to the given {@link IOBone} type.
     *
     * @param type the type of the {@link IOBone}
     * @return the corresponding {@link IOBone}
     */
    public IOBone getIOBone(int type) {
        return boneMap.get(type);
    }
}
