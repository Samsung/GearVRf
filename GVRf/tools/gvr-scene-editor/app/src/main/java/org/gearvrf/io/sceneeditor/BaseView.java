/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.sceneeditor;

import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;

abstract class BaseView implements IViewEvents {
    protected final GVRViewSceneObject mViewSceneObject;
    protected GVRScene scene;

    interface WindowChangeListener {
        void onClose();
    }

    BaseView(GVRScene scene, int layoutID) {
        this.scene = scene;

        GVRContext ctx = this.scene.getGVRContext();
        mViewSceneObject = new GVRViewSceneObject(ctx, layoutID, this);
    }

    @Override
    // GL Thread
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);
        gvrViewSceneObject.getRenderData().setDepthTest(false);
    }

    void show() {
        scene.addSceneObject(mViewSceneObject);
    }

    void hide() {
        scene.removeSceneObject(mViewSceneObject);
    }

    View findViewById(int id) {
        return mViewSceneObject.findViewById(id);
    }
}
