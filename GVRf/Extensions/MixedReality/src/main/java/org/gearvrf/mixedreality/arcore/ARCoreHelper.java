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

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRAugmentedImage;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IAugmentedImageEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARCoreHelper {
    private GVRContext mGvrContext;
    private GVRScene mGvrScene;

    private Map<Plane, ARCorePlane> mArPlanes;
    private Map<AugmentedImage, ARCoreAugmentedImage> mArAugmentedImages;
    private List<ARCoreAnchor> mArAnchors;

    private ArrayList<IPlaneEventsListener> planeEventsListeners = new ArrayList<>();
    private ArrayList<IAnchorEventsListener> anchorEventsListeners = new ArrayList<>();
    private ArrayList<IAugmentedImageEventsListener> augmentedImageEventsListeners = new ArrayList<>();

    public ARCoreHelper(GVRContext gvrContext, GVRScene gvrScene) {
        mGvrContext = gvrContext;
        mGvrScene = gvrScene;
        mArPlanes = new HashMap<>();
        mArAugmentedImages = new HashMap<>();
        mArAnchors = new ArrayList<>();
    }

    public void updatePlanes(Collection<Plane> allPlanes, float[] arViewMatrix,
                             float[] vrCamMatrix, float scale) {
        ARCorePlane arCorePlane;

        for (Plane plane: allPlanes) {
            if (plane.getTrackingState() != TrackingState.TRACKING
                    || mArPlanes.containsKey(plane)) {
                continue;
            }

            arCorePlane = createPlane(plane);
            // FIXME: New planes are updated two times
            arCorePlane.update(arViewMatrix, vrCamMatrix, scale);
            notifyPlaneDetectionListeners(arCorePlane);
        }

        for (Plane plane: mArPlanes.keySet()) {
            arCorePlane = mArPlanes.get(plane);

            if (plane.getTrackingState() == TrackingState.TRACKING &&
                    arCorePlane.getTrackingState() != GVRTrackingState.TRACKING) {
                arCorePlane.setTrackingState(GVRTrackingState.TRACKING);
                notifyPlaneStateChangeListeners(arCorePlane, GVRTrackingState.TRACKING);
            }
            else if (plane.getTrackingState() == TrackingState.PAUSED &&
                    arCorePlane.getTrackingState() != GVRTrackingState.PAUSED) {
                arCorePlane.setTrackingState(GVRTrackingState.PAUSED);
                notifyPlaneStateChangeListeners(arCorePlane, GVRTrackingState.PAUSED);
            }
            else if (plane.getTrackingState() == TrackingState.STOPPED &&
                    arCorePlane.getTrackingState() != GVRTrackingState.STOPPED) {
                arCorePlane.setTrackingState(GVRTrackingState.STOPPED);
                notifyPlaneStateChangeListeners(arCorePlane, GVRTrackingState.STOPPED);
            }

            if (plane.getSubsumedBy() != null && arCorePlane.getParentPlane() == null) {
                arCorePlane.setParentPlane(mArPlanes.get(plane.getSubsumedBy()));
                notifyMergedPlane(arCorePlane, arCorePlane.getParentPlane());
            }

            arCorePlane.update(arViewMatrix, vrCamMatrix, scale);
        }
    }

    public void updateAugmentedImages(Collection<AugmentedImage> allAugmentedImages){
        ARCoreAugmentedImage arCoreAugmentedImage;

        for (AugmentedImage augmentedImage: allAugmentedImages) {
            if (augmentedImage.getTrackingState() != TrackingState.TRACKING
                || mArAugmentedImages.containsKey(augmentedImage)) {
                continue;
            }

            arCoreAugmentedImage = createAugmentedImage(augmentedImage);
            notifyAugmentedImageDetectionListeners(arCoreAugmentedImage);

            mArAugmentedImages.put(augmentedImage, arCoreAugmentedImage);
        }

        for (AugmentedImage augmentedImage: mArAugmentedImages.keySet()) {
            arCoreAugmentedImage = mArAugmentedImages.get(augmentedImage);

            if (augmentedImage.getTrackingState() == TrackingState.TRACKING &&
                    arCoreAugmentedImage.getTrackingState() != GVRTrackingState.TRACKING) {
                arCoreAugmentedImage.setTrackingState(GVRTrackingState.TRACKING);
                notifyAugmentedImageStateChangeListeners(arCoreAugmentedImage, GVRTrackingState.TRACKING);
            }
            else if (augmentedImage.getTrackingState() == TrackingState.PAUSED &&
                    arCoreAugmentedImage.getTrackingState() != GVRTrackingState.PAUSED) {
                arCoreAugmentedImage.setTrackingState(GVRTrackingState.PAUSED);
                notifyAugmentedImageStateChangeListeners(arCoreAugmentedImage, GVRTrackingState.PAUSED);
            }
            else if (augmentedImage.getTrackingState() == TrackingState.STOPPED &&
                    arCoreAugmentedImage.getTrackingState() != GVRTrackingState.STOPPED) {
                arCoreAugmentedImage.setTrackingState(GVRTrackingState.STOPPED);
                notifyAugmentedImageStateChangeListeners(arCoreAugmentedImage, GVRTrackingState.STOPPED);
            }
        }
    }

    public void updateAnchors(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        for (ARCoreAnchor anchor: mArAnchors) {
            Anchor arAnchor = anchor.getAnchorAR();

            if (arAnchor.getTrackingState() == TrackingState.TRACKING &&
                    anchor.getTrackingState() != GVRTrackingState.TRACKING) {
                anchor.setTrackingState(GVRTrackingState.TRACKING);
                notifyAnchorStateChangeListeners(anchor, GVRTrackingState.TRACKING);
            }
            else if (arAnchor.getTrackingState() == TrackingState.PAUSED &&
                    anchor.getTrackingState() != GVRTrackingState.PAUSED) {
                anchor.setTrackingState(GVRTrackingState.PAUSED);
                notifyAnchorStateChangeListeners(anchor, GVRTrackingState.PAUSED);
            }
            else if (arAnchor.getTrackingState() == TrackingState.STOPPED &&
                    anchor.getTrackingState() != GVRTrackingState.STOPPED) {
                anchor.setTrackingState(GVRTrackingState.STOPPED);
                notifyAnchorStateChangeListeners(anchor, GVRTrackingState.STOPPED);
            }

            anchor.update(arViewMatrix, vrCamMatrix, scale);
        }
    }

    public ArrayList<GVRPlane> getAllPlanes() {
        ArrayList<GVRPlane> allPlanes = new ArrayList<>();

        for (Plane plane: mArPlanes.keySet()) {
            allPlanes.add(mArPlanes.get(plane));
        }

        return allPlanes;
    }

    public ArrayList<GVRAugmentedImage> getAllAugmentedImages() {
        ArrayList<GVRAugmentedImage> allAugmentedImages = new ArrayList<>();

        for (AugmentedImage augmentedImage: mArAugmentedImages.keySet()) {
            allAugmentedImages.add(mArAugmentedImages.get(augmentedImage));
        }

        return allAugmentedImages;
    }

    public ARCorePlane createPlane(Plane plane) {
        ARCorePlane arCorePlane = new ARCorePlane(mGvrContext, plane);
        mArPlanes.put(plane, arCorePlane);
        return arCorePlane;
    }

    public ARCoreAugmentedImage createAugmentedImage(AugmentedImage augmentedImage) {
        ARCoreAugmentedImage arCoreAugmentedImage = new ARCoreAugmentedImage(augmentedImage);
        return arCoreAugmentedImage;
    }

    public GVRAnchor createAnchor(Anchor arAnchor, GVRSceneObject sceneObject) {
        ARCoreAnchor arCoreAnchor = new ARCoreAnchor(mGvrContext);
        arCoreAnchor.setAnchorAR(arAnchor);
        mArAnchors.add(arCoreAnchor);

        if (sceneObject != null) {
            arCoreAnchor.attachSceneObject(sceneObject);
        }

        return arCoreAnchor;
    }

    public void updateAnchorPose(ARCoreAnchor anchor, Anchor arAnchor) {
        if (anchor.getAnchorAR() != null) {
            anchor.getAnchorAR().detach();
        }
        anchor.setAnchorAR(arAnchor);
    }

    public void removeAnchor(ARCoreAnchor anchor) {
        anchor.getAnchorAR().detach();
        mArAnchors.remove(anchor);
        mGvrScene.removeSceneObject(anchor);
    }

    public GVRHitResult hitTest(List<HitResult> hitResult) {
        for (HitResult hit : hitResult) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();
            // Creates an anchor if a plane or an oriented point was hit.
            if ((trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                    && ((Plane) trackable).getSubsumedBy() == null) {
                GVRHitResult gvrHitResult = new GVRHitResult();
                float[] hitPose = new float[16];

                hit.getHitPose().toMatrix(hitPose, 0);
                gvrHitResult.setPose(hitPose);
                gvrHitResult.setDistance(hit.getDistance());
                gvrHitResult.setPlane(mArPlanes.get(trackable));

                return gvrHitResult;
            }
        }

        return null;
    }

    public GVRLightEstimate getLightEstimate(LightEstimate lightEstimate) {
        ARCoreLightEstimate arCoreLightEstimate = new ARCoreLightEstimate();
        GVRLightEstimate.GVRLightEstimateState state;

        arCoreLightEstimate.setPixelIntensity(lightEstimate.getPixelIntensity());
        state = (lightEstimate.getState() == LightEstimate.State.VALID) ?
                GVRLightEstimate.GVRLightEstimateState.VALID :
                GVRLightEstimate.GVRLightEstimateState.NOT_VALID;
        arCoreLightEstimate.setState(state);

        return arCoreLightEstimate;
    }

    public void registerPlaneListener(IPlaneEventsListener listener) {
        planeEventsListeners.add(listener);
    }

    public void registerAnchorListener(IAnchorEventsListener listener) {
        anchorEventsListeners.add(listener);
    }

    public void registerAugmentedImageListener(IAugmentedImageEventsListener listener) {
        augmentedImageEventsListeners.add(listener);
    }

    private void notifyPlaneDetectionListeners(GVRPlane plane) {
        for (IPlaneEventsListener listener: planeEventsListeners) {
            listener.onPlaneDetection(plane);
        }
    }

    private void notifyPlaneStateChangeListeners(GVRPlane plane, GVRTrackingState trackingState) {
        for (IPlaneEventsListener listener: planeEventsListeners) {
            listener.onPlaneStateChange(plane, trackingState);
        }
    }

    private void notifyMergedPlane(GVRPlane childPlane, GVRPlane parentPlane) {
        for (IPlaneEventsListener listener: planeEventsListeners) {
            listener.onPlaneMerging(childPlane, parentPlane);
        }
    }

    private void notifyAnchorStateChangeListeners(GVRAnchor anchor, GVRTrackingState trackingState) {
        for (IAnchorEventsListener listener: anchorEventsListeners) {
            listener.onAnchorStateChange(anchor, trackingState);
        }
    }

    private void notifyAugmentedImageDetectionListeners(GVRAugmentedImage image) {
        for (IAugmentedImageEventsListener listener: augmentedImageEventsListeners) {
            listener.onAugmentedImageDetection(image);
        }
    }

    private void notifyAugmentedImageStateChangeListeners(GVRAugmentedImage image, GVRTrackingState trackingState) {
        for (IAugmentedImageEventsListener listener: augmentedImageEventsListeners) {
            listener.onAugmentedImageStateChange(image, trackingState);
        }
    }
}
