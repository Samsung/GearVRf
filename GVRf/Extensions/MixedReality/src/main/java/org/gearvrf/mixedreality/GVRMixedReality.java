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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.view.MotionEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IActivityEvents;
import org.gearvrf.mixedreality.arcore.ARCoreSession;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GVRMixedReality extends GVRBehavior implements IMRCommon {
    private final IActivityEvents mActivityEventsHandler;

    private MRCommon mSession;

    public GVRMixedReality(final GVRContext gvrContext) {
        super(gvrContext, 0);

        mActivityEventsHandler = new ActivityEventsHandler();

        GVRSceneObject owner = new GVRSceneObject(gvrContext);
        owner.attachComponent(this);

        mSession = new ARCoreSession(gvrContext, owner);

    }

    @Override
    public void resume() {
        mSession.resume();
    }

    @Override
    public void pause() {
        mSession.pause();
    }

    private class ActivityEventsHandler implements IActivityEvents {
        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onResume() {
            resume();
        }

        @Override
        public void onDestroy() {}

        @Override
        public void onSetMain(GVRMain script) {}

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {}

        @Override
        public void onConfigurationChanged(Configuration config) {}

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {}

        @Override
        public void onTouchEvent(MotionEvent event) {}

        @Override
        public void dispatchTouchEvent(MotionEvent event) {}

        @Override
        public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchpadPoint, boolean touched) {}
    }
}
