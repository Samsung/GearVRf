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

/**
 * This class represents a joint.
 */
public class IOJoint extends IOBaseComponent {
    public static final int CMC = 0;
    public static final int MCP = 1;
    public static final int PIP = 2;
    public static final int DIP = 3;
    //Create an imaginary tip joint
    public static final int TIP = 4;

    /**
     * Get the String value for the corresponding {@link IOJoint} type.
     *
     * @param type
     * @return
     */
    public static String getString(int type) {
        switch (type) {
            case CMC:
                return "CMC";
            case MCP:
                return "MCP";
            case PIP:
                return "PIP";
            case DIP:
                return "DIP";
            case TIP:
                return "TIP";

            default:
                return "TIP";
        }
    }

    /**
     * Create an {@link IOJoint} of the provided type.
     *
     * @param type            the type of the
     *                        {@link IOJoint}. Use the {@link IOJoint#getString(int)} call to
     *                        know the readable type of the joint.
     * @param handSceneObject This is the root {@link GVRSceneObject} that represents the hand.
     */
    public IOJoint(int type, GVRSceneObject handSceneObject) {
        super(type, handSceneObject);
    }
}
