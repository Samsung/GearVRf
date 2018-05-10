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

package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;

public abstract class MRCommon implements IMRCommon {
    public static String TAG = MRCommon.class.getSimpleName();

    protected final GVRContext mGvrContext;

    public MRCommon(GVRContext gvrContext) {
        mGvrContext = gvrContext;
    }

    @Override
    public void resume() {
        onResume();
    }

    @Override
    public void pause() {
        onPause();
    }

    @Override
    public void registerPlaneListener(IPlaneEventsListener listener) {
        onRegisterPlaneListener(listener);
    }

    @Override
    public void registerAnchorListener(IAnchorEventsListener listener) {
        onRegisterAnchorListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        return onGetAllPlanes();
    }


    @Override
    public GVRAnchor createAnchor(float[] pose) {
        return onCreateAnchor(pose);
    }

    @Override
    public GVRAnchor createAnchor(float[] pose, GVRSceneObject sceneObject) {
        return onCreateAnchor(pose, sceneObject);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        return onHitTest(sceneObj, collision);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        return onGetLightEstimate();
    }

    protected abstract void onResume();

    protected abstract void onPause();

    protected abstract void onRegisterPlaneListener(IPlaneEventsListener listener);

    protected abstract void onRegisterAnchorListener(IAnchorEventsListener listener);

    protected abstract ArrayList<GVRPlane> onGetAllPlanes();

    protected abstract GVRAnchor onCreateAnchor(float[] pose);

    protected abstract GVRAnchor onCreateAnchor(float[] pose, GVRSceneObject sceneObject);

    protected abstract GVRHitResult onHitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision);

    protected abstract GVRLightEstimate onGetLightEstimate();
}
