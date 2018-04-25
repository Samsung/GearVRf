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
    public void setDisplayGeometry(int displayRotation, int widthPx, int heightPx) {
        onSetDisplayGeometry(displayRotation, widthPx, heightPx);
    }

    @Override
    public void registerPlaneListener(IPlaneEventsListener listener) {
        onRegisterPlaneListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        return onGetAllPlanes();
    }

    public void updateFrame() { onUpdateFrame(); }


    protected abstract void onResume();

    protected abstract void onPause();

    protected abstract void onSetDisplayGeometry(int displayRotation, int widthPx, int heightPx);

    protected abstract void onRegisterPlaneListener(IPlaneEventsListener listener);

    protected abstract ArrayList<GVRPlane> onGetAllPlanes();

    protected abstract void onUpdateFrame();

}
