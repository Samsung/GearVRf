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

/** A C++-like pointer. */
interface GVRReference {
    /**
     * @return The address of the {@code std::shared_ptr} pointing to the C++
     *         instance.
     */
    long getPtr();

    /**
     * @return The contents of the {@code std::shared_ptr}. That is, the actual
     *         address of the C++ instance
     */
    long getNative();

    /**
     * 
     * @return Whether the {@code std::shared_ptr} pointing to the C++ instance
     *         has been deallocated.
     */
    boolean isDeleted();

    /**
     * Deallocates a {@code std::shared_ptr} pointing to the C++ instance.
     */
    void delete();
}
