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

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that stores and contains recyclable objects till they aren't
 * needed anymore. Prevents them from losing the final reference in C++.
 */
class GVRRecyclableObjectProtector {

    private List<GVRRecyclableObject> mRecyclableObjects = new ArrayList<GVRRecyclableObject>();
    /**
     * We can't synchronize {@link #mRecyclableObjects} because we keep
     * replacing it; we don't want to {@code synchronize(this)} for fear of
     * deadlock.
     */
    private final Object[] mLock = new Object[0];

    /**
     * Constructs an empty {@link GVRRecyclableObjectProtector} object
     */
    GVRRecyclableObjectProtector() {
    }

    /**
     * Adds additional {@link GVRRecyclableObject} object in the class to be
     * stored
     * 
     * @param recyclableObject
     *            the {@link GVRRecyclableObject} to be stored
     */
    void addRecyclableObject(GVRRecyclableObject recyclableObject) {
        synchronized (mLock) {
            mRecyclableObjects.add(recyclableObject);
        }
    }

    /**
     * Removes all the stored {@link GVRRecyclableObject} not in use in the
     * class (note: {@link GVRRecyclableObject#isUnique() isUnique()} returns
     * use_count of a specific {@link GVRRecyclableObject} equal to one)
     */
    void clean() {
        List<GVRRecyclableObject> recyclableObjects = new ArrayList<GVRRecyclableObject>();
        synchronized (mLock) {
            for (GVRRecyclableObject recyclableObject : mRecyclableObjects) {
                if (!recyclableObject.isUnique()) {
                    recyclableObjects.add(recyclableObject);
                }
            }
            mRecyclableObjects = recyclableObjects;
        }
    }
}
