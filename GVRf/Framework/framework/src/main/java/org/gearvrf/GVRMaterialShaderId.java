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
 * Opaque type that specifies a material shader.
 * 
 * The inheritance tree represents the fact that stock shaders do not use
 * {@link GVRMaterialMap name maps.}
 */
public abstract class GVRMaterialShaderId {
    private final static SparseArray<GVRMaterialShaderId> sIds = new SparseArray<GVRMaterialShaderId>();

    static {
        GVRContext.addResetOnRestartHandler(new Runnable() {

            @Override
            public void run() {
                // Remove any custom shaders, which are GVRContext-dependent;
                // leave all stock shaders, which are not
                SparseArray<GVRMaterialShaderId> clone = sIds.clone();
                for (int index = 0, size = clone.size(); index < size; ++index) {
                    if (clone.valueAt(index) instanceof GVRCustomMaterialShaderId) {
                        sIds.removeAt(index);
                    }
                }
            }
        });
    }

    final int ID;

    /** @deprecated Probably unnecessary ... */
    @SuppressWarnings("unchecked")
    protected final static <T extends GVRMaterialShaderId> T get(int id) {
        return (T) sIds.get(id);
    }

    protected final static void put(int id, GVRMaterialShaderId wrapper) {
        sIds.put(id, wrapper);
    }

    protected GVRMaterialShaderId(int id) {
        ID = id;
        put(id, this);
    }
}