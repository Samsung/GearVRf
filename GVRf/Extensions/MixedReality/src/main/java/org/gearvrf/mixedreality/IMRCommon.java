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


import android.graphics.Bitmap;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;

/**
 * This interface defines the AR functionalities of the MixedReality API.
 */
public interface IMRCommon {
    /**
     * Resume the usage of AR functions.
     */
    void resume();

    /**
     * Pause the usage of AR functions.
     */
    void pause();

    /**
     *
     * @return The passthrough object
     */
    GVRSceneObject getPassThroughObject();

    /**
     * Register a listener to GVRPlane events.
     *
     * @param listener
     */
    void registerPlaneListener(IPlaneEventsListener listener);

    /**
     * Register a listener to GVRAnchor events.
     *
     * @param listener
     */
    void registerAnchorListener(IAnchorEventsListener listener);

    /**
     * Register a listener to GVRAugmentedImage events.
     *
     * @param listener
     */
    void registerAugmentedImageListener(IAugmentedImageEventsListener listener);

    /**
     * Gets all detected planes.
     *
     * @return A ArrayList of GVRPlanes
     */
    ArrayList<GVRPlane> getAllPlanes();

    /**
     * Create an anchor on pose specified.
     *
     * @param pose
     * @return The anchor created
     */
    GVRAnchor createAnchor(float[] pose);

    /**
     * Create an anchor on pose specified and associate to the sceneObject
     *
     * @param pose
     * @param sceneObject
     * @return The anchor created
     */
    GVRAnchor createAnchor(float[] pose, GVRSceneObject sceneObject);

    /**
     * Update the pose of an anchor
     *
     * @param anchor
     * @param pose
     */
    void updateAnchorPose(GVRAnchor anchor, float[] pose);

    /**
     * Remove the anchor specified
     *
     * @param anchor
     */
    void removeAnchor(GVRAnchor anchor);

    /**
     * Host an anchor to be shared
     *
     * @param anchor
     */
    void hostAnchor(GVRAnchor anchor, ICloudAnchorListener listener);

    /**
     * Get an anchor previously hosted
     *
     * @param anchorId
     * @param listener
     */
    void resolveCloudAnchor(String anchorId, ICloudAnchorListener listener);

    /**
     * Set if cloud anchors will be available or not
     *
     * @param enableCloudAnchor
     */
    void setEnableCloudAnchor(boolean enableCloudAnchor);

    /**
     *
     * @param sceneObj
     * @param collision
     * @return
     */
    GVRHitResult hitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision);

    /**
     *
     * @return The light estimate
     */
    GVRLightEstimate getLightEstimate();

    /**
     * Set an image to be detected
     *
     * @param image
     */
    void setAugmentedImage(Bitmap image);

    /**
     * Set a list of reference images to be detected
     *
     * @param imagesList
     */
    void setAugmentedImages(ArrayList<Bitmap> imagesList);

    /**
     * Get all detected augmented images
     *
     * @return An ArrayList of GVRAugmentedImage
     */
    ArrayList<GVRAugmentedImage> getAllAugmentedImages();
}
