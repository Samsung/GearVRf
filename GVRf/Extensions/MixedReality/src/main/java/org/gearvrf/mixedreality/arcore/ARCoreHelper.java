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

import android.opengl.Matrix;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Camera;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMarker;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEvents;
import org.gearvrf.mixedreality.IMarkerEvents;
import org.gearvrf.mixedreality.IMixedReality;
import org.gearvrf.mixedreality.IPlaneEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARCoreHelper
{
    private GVRContext mGvrContext;
    private IMixedReality mMixedReality;
    private Map<Plane, ARCorePlane> mArPlanes;
    private Map<AugmentedImage, ARCoreMarker> mArAugmentedImages;
    private List<ARCoreAnchor> mArAnchors;

    private Camera mCamera;// ARCore camera

    public ARCoreHelper(GVRContext gvrContext, IMixedReality mr) {
        mGvrContext = gvrContext;
        mMixedReality = mr;
        mArPlanes = new HashMap<>();
        mArAugmentedImages = new HashMap<>();
        mArAnchors = new ArrayList<>();
    }


    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void updatePlanes(Collection<Plane> allPlanes, float scale) {

        // Don't update planes (or notify) when the plane listener is empty, i.e., there is
        // no listener registered.
        ARCorePlane arCorePlane;

        for (Plane plane: allPlanes) {
            if (plane.getTrackingState() != TrackingState.TRACKING
                    || mArPlanes.containsKey(plane)) {
                continue;
            }

            arCorePlane = createPlane(plane);
            // FIXME: New planes are updated two times
            arCorePlane.update(scale);
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

            arCorePlane.update(scale);
        }
    }

    public void updateAugmentedImages(Collection<AugmentedImage> allAugmentedImages){
        ARCoreMarker arCoreMarker;

        for (AugmentedImage augmentedImage: allAugmentedImages) {
            if (augmentedImage.getTrackingState() != TrackingState.TRACKING
                || mArAugmentedImages.containsKey(augmentedImage)) {
                continue;
            }

            arCoreMarker = createMarker(augmentedImage);
            notifyMarkerDetectionListeners(arCoreMarker);

            mArAugmentedImages.put(augmentedImage, arCoreMarker);
        }

        for (AugmentedImage augmentedImage: mArAugmentedImages.keySet()) {
            arCoreMarker = mArAugmentedImages.get(augmentedImage);

            if (augmentedImage.getTrackingState() == TrackingState.TRACKING &&
                    arCoreMarker.getTrackingState() != GVRTrackingState.TRACKING) {
                arCoreMarker.setTrackingState(GVRTrackingState.TRACKING);
                notifyMarkerStateChangeListeners(arCoreMarker, GVRTrackingState.TRACKING);
            }
            else if (augmentedImage.getTrackingState() == TrackingState.PAUSED &&
                    arCoreMarker.getTrackingState() != GVRTrackingState.PAUSED) {
                arCoreMarker.setTrackingState(GVRTrackingState.PAUSED);
                notifyMarkerStateChangeListeners(arCoreMarker, GVRTrackingState.PAUSED);
            }
            else if (augmentedImage.getTrackingState() == TrackingState.STOPPED &&
                    arCoreMarker.getTrackingState() != GVRTrackingState.STOPPED) {
                arCoreMarker.setTrackingState(GVRTrackingState.STOPPED);
                notifyMarkerStateChangeListeners(arCoreMarker, GVRTrackingState.STOPPED);
            }
        }
    }

    public void updateAnchors(float scale) {
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

            anchor.update(scale);
        }
    }

    public ArrayList<GVRPlane> getAllPlanes() {
        ArrayList<GVRPlane> allPlanes = new ArrayList<>();

        for (Plane plane: mArPlanes.keySet()) {
            allPlanes.add(mArPlanes.get(plane));
        }

        return allPlanes;
    }

    public ArrayList<GVRMarker> getAllMarkers() {
        ArrayList<GVRMarker> allAugmentedImages = new ArrayList<>();

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

    public ARCoreMarker createMarker(AugmentedImage augmentedImage) {
        ARCoreMarker arCoreMarker = new ARCoreMarker(augmentedImage);
        return arCoreMarker;
    }

    public GVRAnchor createAnchor(Anchor arAnchor, float scale) {
        ARCoreAnchor arCoreAnchor = new ARCoreAnchor(mGvrContext);
        arCoreAnchor.setAnchorAR(arAnchor);
        mArAnchors.add(arCoreAnchor);
        arCoreAnchor.update(scale);
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
        GVRSceneObject anchorNode = anchor.getOwnerObject();
        GVRSceneObject anchorParent = anchorNode.getParent();
        anchorParent.removeChildObject(anchorNode);
    }

    public GVRHitResult hitTest(List<HitResult> hitResult, float scale) {
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
                // Convert the value from ARCore to GVRf and set the pose
                ar2gvr(hitPose, scale);
                gvrHitResult.setPose(hitPose);
                // TODO: this distance is using ARCore values, change it to use GVRf instead
                gvrHitResult.setDistance(hit.getDistance());
                gvrHitResult.setPlane(mArPlanes.get(trackable));

                return gvrHitResult;
            }
        }

        return null;
    }

    /**
     * Converts from AR world space to GVRf world space.
     */
    private void ar2gvr(float[] poseMatrix, float scale) {
        // Real world scale
        Matrix.scaleM(poseMatrix, 0, scale, scale, scale);
        poseMatrix[12] = poseMatrix[12] * scale;
        poseMatrix[13] = poseMatrix[13] * scale;
        poseMatrix[14] = poseMatrix[14] * scale;
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

    private void notifyPlaneDetectionListeners(GVRPlane plane) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IPlaneEvents.class,
                "onPlaneDetected",
                plane);
    }

    private void notifyPlaneStateChangeListeners(GVRPlane plane, GVRTrackingState trackingState) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IPlaneEvents.class,
                "onPlaneStateChange",
                plane,
                trackingState);
    }

    private void notifyMergedPlane(GVRPlane childPlane, GVRPlane parentPlane) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IPlaneEvents.class,
                "onPlaneMerging",
                childPlane,
                parentPlane);
    }

    private void notifyAnchorStateChangeListeners(GVRAnchor anchor, GVRTrackingState trackingState) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IAnchorEvents.class,
                "onAnchorStateChange",
                anchor,
                trackingState);
    }

    private void notifyMarkerDetectionListeners(GVRMarker image) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IMarkerEvents.class,
                "onMarkerDetected",
                image);
    }

    private void notifyMarkerStateChangeListeners(GVRMarker image, GVRTrackingState trackingState) {
        mGvrContext.getEventManager().sendEvent(mMixedReality,
                IMarkerEvents.class,
                "onMarkerStateChange",
                image,
                trackingState);
    }

}
