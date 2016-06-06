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

package org.gearvrf.io.cursor3d;

/**
 * Represents different types of cursors.
 */
public enum CursorType {
    /**
     * A laser cursor represents a pointing cursor type, it returns all interactions based on a
     * vector from the user's camera and the position of the cursor object.
     *
     * For cases where there are multiple objects placed one behind the other, this cursor
     * type will return all the objects that lie on the vector. The cursor itself will be shown
     * on the first object that it intersects with.
     * <p/>
     * Imagine holding a laser pointer in 3D space and pointing it at objects.
     */
    LASER,
    /**
     * A object cursor represents an object in 3D space. It returns interactions based on the the
     * absolute position of the Cursor. This cursor type introduces the concept of depth
     * selections.
     *
     * For cases where there are multiple objects placed one behind the other, this cursor will
     * return events only when the cursor intersects/collides with the object.
     */
    OBJECT,
    /**
     * This cursor type is not recognized by the Cursor Manager.
     */
    UNKNOWN
}
