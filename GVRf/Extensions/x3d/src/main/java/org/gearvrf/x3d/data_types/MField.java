/* Copyright 2016 Samsung Electronics Co., LTD
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
package org.gearvrf.x3d.data_types;

import java.util.ArrayList;

/**
 * Defines the X3D MField data type
 * Per X3D spec, all Multi-Field (MF) datatypes are to implement these methods
 * http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/MField.html
 */
public interface MField {

    /**
     * Removes all values in the field array, changing the array size to zero.
     */
    public abstract void clear();

    /**
     * Remove one element of the field array at index position, if found. Initial element is at index 0.
     * @param index - position of element in field array that gets removed
     */
    public abstract void remove(int index);

    /**
     * Get the size of the underlying data array.
     * The size is the number of elements for that data type.
     * So for an MFFloat the size would be the number of float values,
     * but for an MFVec3f, it is the number of vectors in the
     * returned array (where a vector is 3 consecutive array indexes in a flat array).
     * @return
     */
    public abstract int size();

}

