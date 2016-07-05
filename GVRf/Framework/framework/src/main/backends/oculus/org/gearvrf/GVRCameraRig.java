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

import static org.gearvrf.utility.Assert.*;

import org.gearvrf.utility.Log;

/** Holds the GVRCameras. */
public class GVRCameraRig extends GVRCameraRigBase {

    /** Constructs a camera rig without cameras attached. */
    public static GVRCameraRig makeInstance(GVRContext gvrContext) {
        final GVRCameraRig result = new GVRCameraRig(gvrContext);
        result.init(gvrContext);
        return result;
    }

    /** Constructs a camera rig without cameras attached. */
    private GVRCameraRig(GVRContext gvrContext) {
        super(gvrContext, NativeCameraRigBase.ctor());
    }

    @Override
    protected void addHeadTransformObject() {
        getOwnerObject().addChildObject(getHeadTransformObject());
    }

    @Override
    public GVRTransform getHeadTransform() {
        return getHeadTransformObject().getTransform();
    }

    /**
     * Predict what the orientation of the camera rig will be at {@code time}
     * based on the current rotation and angular velocity.
     *
     * @param time
     *            Time to predict orientation for, in seconds.
     * @see #setRotationSensorData(long, float, float, float, float, float,
     *      float, float)
     */
    void predict(float time) {
        NativeCameraRig.predict(getNative(), time);
    }
}

class NativeCameraRig {
    static native void predict(long cameraRig, float time);
}
