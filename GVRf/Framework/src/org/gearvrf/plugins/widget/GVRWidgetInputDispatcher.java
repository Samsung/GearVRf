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

package org.gearvrf.plugins.widget;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRWidgetSceneObject;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidInput;

/**
 * GVRWidgetInputDispatcher is internal class responsible for dispatching input
 * events to GVRWidgetSceneObjects
 */
public class GVRWidgetInputDispatcher {

    private AndroidInput mInput;
    float mDownx = 0, mDowny = 0, mDownabsx = 0, mDownabsy = 0;

    GVRWidgetSceneObject mPickedObject = null;

    class Coords {
        float x;
        float y;
    }

    Coords coords = new Coords();

    public AndroidInput getInput() {
        return mInput;
    }

    public void setInput(AndroidInput in) {
        mInput = in;
    }

    public void setPickedObject(GVRSceneObject obj) {
        if (obj instanceof GVRWidgetSceneObject) {
            mPickedObject = (GVRWidgetSceneObject) obj;
        } else {
            mPickedObject = null;
        }
    }

    public GVRSceneObject getPickedObject() {
        return mPickedObject;
    }

    public boolean dispatchEvent(MotionEvent event, View view) {

        if (mPickedObject == null)
            return false;

        getCoordinates(event, view, mPickedObject.getmeshInfo());
        event.setLocation(coords.x, coords.y);
        mInput.onTouch(view, event);
        return true;
    }

    /**
     * gets the actual screen coordinates of the parent libGDX GLSurfaceView
     * from which the scene object was created, the same are passed to libGDX
     * input engine
     */
    private void getCoordinates(MotionEvent event, View view,
            GVRWidgetSceneObjectMeshInfo info) {
        gethit(mPickedObject.getmeshInfo());
        float[] tex = mPickedObject.getRenderData().getMesh().getTexCoords();

        coords.x = (((Math.abs(tex[2] - tex[0])) * coords.x) + tex[0])
                * view.getWidth();
        coords.y = (((Math.abs(tex[5] - tex[1])) * coords.y) + tex[5])
                * view.getHeight();

        if (event.getAction() == 0) {
            mDownabsx = event.getX();
            mDownabsy = event.getY();
            mDownx = coords.x;
            mDowny = coords.y;
        }

        if (event.getAction() == 2 || event.getAction() == 1) {
            coords.x = event.getX() - mDownabsx + mDownx;
            coords.y = event.getY() - mDownabsy + mDowny;
        }

    }

    /**
     * gets the normalised hit extent from top left relative to current scene
     * object e.g. if pointer is at centre of the scene object, x and y
     * coordinates would be 0.5f each.
     */
    private void gethit(GVRWidgetSceneObjectMeshInfo info) {

        float[] hit = mPickedObject.getEyePointeeHolder().getHit();

        coords.x = (Math.abs(info.mTopLeftX - hit[0]) / Math.abs(info.mTopLeftX
                - info.mBottomRightX));

        coords.y = (Math.abs(info.mTopLeftY - hit[1]) / Math.abs(info.mTopLeftY
                - info.mBottomRightY));
    }

}
