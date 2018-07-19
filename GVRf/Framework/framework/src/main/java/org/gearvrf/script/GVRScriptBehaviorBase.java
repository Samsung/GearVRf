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

package org.gearvrf.script;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;

/**
 * Base class for adding javascript related behaviors to a scene object.
 */
public abstract class GVRScriptBehaviorBase extends GVRBehavior {

    protected static long TYPE_SCRIPT_BEHAVIOR = newComponentType(GVRScriptBehaviorBase.class);

    /**
     * Constructor for a script behavior component.
     * @param gvrContext    The current GVRF context
     */
    public GVRScriptBehaviorBase(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * @return the component type (TYPE_SCRIPT_BEHAVIOR)
     */
    public static long getComponentType() { return TYPE_SCRIPT_BEHAVIOR; }
}
