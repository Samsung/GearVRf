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

/**
 * This class represents a bone. Use the class methods to set its properties.
 */
public class IOBone extends IOBaseComponent {
    public static final int METACARPAL = 0;
    public static final int PROXIMAL = 1;
    public static final int INTERMEDIATE = 2;
    public static final int DISTAL = 3;

    /**
     * Get the String value for the corresponding {@link IOBone} type.
     *
     * @param type
     * @return
     */
    public static String getString(int type) {
        switch (type) {
            case METACARPAL:
                return "METACARPAL";
            case PROXIMAL:
                return "PROXIMAL";
            case INTERMEDIATE:
                return "INTERMEDIATE";
            case DISTAL:
                return "DISTAL";
            default:
                return "DISTAL";
        }
    }

    /**
     * Create an {@link IOBone} of the provided type.
     *
     * @param type            the type of the
     *                        {@link IOBone}. Use the {@link IOBone#getString(int)} call to
     *                        know the readable type of the bone.
     * @param handSceneObject This is the root {@link GVRSceneObject} that represents the hand.
     */
    public IOBone(int type, GVRSceneObject handSceneObject) {
        super(type, handSceneObject);
    }
}
