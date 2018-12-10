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
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.debug.cli.CLIException;

import java.util.ArrayList;

public abstract class MRCommon implements IMixedReality
{
    public static String TAG = MRCommon.class.getSimpleName();

    protected final GVRContext mGVRContext;
    protected GVREventReceiver mListeners;

    public MRCommon(GVRContext GVRContext) {
        mGVRContext = GVRContext;
        mListeners = new GVREventReceiver(this);
    }

    @Override
    public GVREventReceiver getEventReceiver() { return mListeners; }

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
    public ArrayList<GVRPlane> getAllPlanes() {
        return onGetAllPlanes();
    }

    @Override
    public GVRSceneObject createAnchorNode(float[] pose)
    {
        GVRAnchor anchor = createAnchor(pose);
        if (anchor != null)
        {
            GVRSceneObject node = new GVRSceneObject(anchor.getGVRContext());
            node.attachComponent(anchor);
            return node;
        }
        return null;
    }

    @Override
    public GVRAnchor createAnchor(float[] pose) {
        return onCreateAnchor(pose);
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
    public void hostAnchor(GVRAnchor anchor, CloudAnchorCallback cb) {
        onHostAnchor(anchor, cb);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, CloudAnchorCallback cb) {
        onResolveCloudAnchor(anchorId, cb);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        onSetEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public GVRHitResult hitTest(GVRPicker.GVRPickedObject collision) {
        return onHitTest(collision);
    }

    @Override
    public GVRHitResult hitTest(float x, float y) {
        return onHitTest(x, y);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        return onGetLightEstimate();
    }

    @Override
    public void setMarker(Bitmap image) {
        onSetMarker(image);
    }

    @Override
    public void setMarkers(ArrayList<Bitmap> imagesList) {
        onSetMarkers(imagesList);
    }

    @Override
    public ArrayList<GVRMarker> getAllMarkers() {
        return onGetAllMarkers();
    }

    @Override
    public float[] makeInterpolated(float[] poseA, float[] poseB, float t) {
        return onMakeInterpolated(poseA, poseB, t);
    }

    protected abstract void onResume();

    protected abstract void onPause();

    protected abstract GVRSceneObject onGetPassThroughObject();

    protected abstract ArrayList<GVRPlane> onGetAllPlanes();

    protected abstract GVRAnchor onCreateAnchor(float[] pose);

    protected abstract void onUpdateAnchorPose(GVRAnchor anchor, float[] pose);

    protected abstract void onRemoveAnchor(GVRAnchor anchor);

    protected abstract void onHostAnchor(GVRAnchor anchor, CloudAnchorCallback cb);

    protected abstract void onResolveCloudAnchor(String anchorId, CloudAnchorCallback cb);

    protected abstract void onSetEnableCloudAnchor(boolean enableCloudAnchor);

    protected abstract GVRHitResult onHitTest(GVRPicker.GVRPickedObject collision);

    protected abstract GVRHitResult onHitTest(float x, float y);

    protected abstract GVRLightEstimate onGetLightEstimate();

    protected abstract void onSetMarker(Bitmap image);

    protected abstract void onSetMarkers(ArrayList<Bitmap> imagesList);

    protected abstract ArrayList<GVRMarker> onGetAllMarkers();

    protected abstract float[] onMakeInterpolated(float[] poseA, float[] poseB, float t);
}
