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
    public GVRSceneObject getPassThroughObject() {
        return onGetPassThroughObject();
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
    public void registerAugmentedImageListener(IAugmentedImageEventsListener listener) {
        onRegisterAugmentedImageListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        return onGetAllPlanes();
    }

    @Override
    public GVRAnchor createAnchor(float[] pose) {
        return onCreateAnchor(pose, null);
    }

    @Override
    public GVRAnchor createAnchor(float[] pose, GVRSceneObject sceneObject) {
        return onCreateAnchor(pose, sceneObject);
    }

    @Override
    public void updateAnchorPose(GVRAnchor anchor, float[] pose) {
        onUpdateAnchorPose(anchor, pose);
    }

    @Override
    public void removeAnchor(GVRAnchor anchor) {
        onRemoveAnchor(anchor);
    }

    @Override
    public void hostAnchor(GVRAnchor anchor, ICloudAnchorListener listener) {
        onHostAnchor(anchor, listener);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, ICloudAnchorListener listener) {
        onResolveCloudAnchor(anchorId, listener);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        onSetEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        return onHitTest(sceneObj, collision);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        return onGetLightEstimate();
    }

    @Override
    public void setAugmentedImage(Bitmap image) {
        onSetAugmentedImage(image);
    }

    @Override
    public void setAugmentedImages(ArrayList<Bitmap> imagesList) {
        onSetAugmentedImages(imagesList);
    }

    @Override
    public ArrayList<GVRAugmentedImage> getAllAugmentedImages() {
        return onGetAllAugmentedImages();
    }

    protected abstract void onResume();

    protected abstract void onPause();

    protected abstract GVRSceneObject onGetPassThroughObject();

    protected abstract void onRegisterPlaneListener(IPlaneEventsListener listener);

    protected abstract void onRegisterAnchorListener(IAnchorEventsListener listener);

    protected abstract void onRegisterAugmentedImageListener(IAugmentedImageEventsListener listener);

    protected abstract ArrayList<GVRPlane> onGetAllPlanes();

    protected abstract GVRAnchor onCreateAnchor(float[] pose, GVRSceneObject sceneObject);

    protected abstract void onUpdateAnchorPose(GVRAnchor anchor, float[] pose);

    protected abstract void onRemoveAnchor(GVRAnchor anchor);

    protected  abstract void onHostAnchor(GVRAnchor anchor, ICloudAnchorListener listener);

    protected abstract void onResolveCloudAnchor(String anchorId, ICloudAnchorListener listener);

    protected abstract void onSetEnableCloudAnchor(boolean enableCloudAnchor);

    protected abstract GVRHitResult onHitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision);

    protected abstract GVRLightEstimate onGetLightEstimate();

    protected abstract void onSetAugmentedImage(Bitmap image);

    protected abstract void onSetAugmentedImages(ArrayList<Bitmap> imagesList);

    protected abstract ArrayList<GVRAugmentedImage> onGetAllAugmentedImages();
}
