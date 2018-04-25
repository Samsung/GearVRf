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

package org.gearvrf.mixedreality.arcore;

import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ARCoreHelper {
    private GVRContext mGvrContext;
    private GVRScene mGvrScene;

    private Map<Plane, ARCorePlane> mArPlanes;

    private ArrayList<IPlaneEventsListener> planeEventsListeners = new ArrayList<>();

    public ARCoreHelper(GVRContext gvrContext, GVRScene gvrScene) {
        mGvrContext = gvrContext;
        mGvrScene = gvrScene;
        mArPlanes = new HashMap<>();
    }

    public void updatePlanes(Collection<Plane> allPlanes, float[] arViewMatrix,
                              float[] vrCamMatrix, float scale) {
        for (Plane plane: allPlanes) {
            if (plane.getTrackingState() != TrackingState.TRACKING || plane.getSubsumedBy() != null) {
                continue;
            }

            ARCorePlane arCorePlane = mArPlanes.get(plane);
            if (arCorePlane == null) {
                arCorePlane = new ARCorePlane(mGvrContext);
                arCorePlane.setARPlane(plane);
                mGvrScene.addSceneObject(arCorePlane);
                mArPlanes.put(plane, arCorePlane);
                notifyPlaneDetectionListeners(arCorePlane);
            }
            arCorePlane.update(arViewMatrix, vrCamMatrix, scale);
        }
    }

    public ArrayList<GVRPlane> getAllPlanes() {
        ArrayList<GVRPlane> allPlanes = new ArrayList<>();

        Iterator it = mArPlanes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry map = (Map.Entry)it.next();
            allPlanes.add((GVRPlane)map.getValue());
        }
        return allPlanes;
    }

    public void registerPlaneListener(IPlaneEventsListener listener) {
        planeEventsListeners.add(listener);
    }

    public void notifyPlaneDetectionListeners(GVRPlane plane) {
        for (IPlaneEventsListener listener: planeEventsListeners) {
            listener.onDetection(plane);
        }
    }

    public void notifyPlaneStateChangeListeners(GVRPlane plane, GVRTrackingState state) {
        for (IPlaneEventsListener listener: planeEventsListeners) {
            listener.onTrackingStateChange(plane, state);
        }
    }
}
