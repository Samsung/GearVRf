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

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRViewSceneObject;

abstract class BaseView  {
    private static final String TAG = BaseView.class.getSimpleName();
    private static final float QUAD_X = 10.0f;
    private static final float QUAD_Y = 8f;

    private float quadHeight;
    private float quadWidth;
    protected GVRScene scene;
    protected FrameLayout frameLayout;
    protected GVRViewSceneObject viewSceneObject;
    private boolean scrollable;

    interface WindowChangeListener {
        void onClose();
    }

    BaseView(GVRScene scene, int layoutID, boolean scrollable) {
        this(scene, layoutID, QUAD_Y, QUAD_X, scrollable);
    }

    BaseView(GVRScene scene, int layoutID, float
            quadHeight, float quadWidth, boolean scrollable) {
        this.scene = scene;
        this.quadHeight = quadHeight;
        this.quadWidth = quadWidth;
        this.scrollable = scrollable;

        GVRContext context = scene.getGVRContext();
        GVRActivity activity = context.getActivity();
        frameLayout = new FrameLayout(activity);
        context.getActivity().registerView(frameLayout);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        View.inflate(activity, layoutID, frameLayout);
    }

    void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    boolean getScrollable() {
        return scrollable;
    }

    void initializeViewSceneObject() {
        GVRContext ctx = this.scene.getGVRContext();
        viewSceneObject = new GVRViewSceneObject(ctx, frameLayout,quadWidth, quadHeight);
        viewSceneObject.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);
        viewSceneObject.getRenderData().setDepthTest(false);
    }

    int getFrameWidth() {
        return frameLayout.getWidth();
    }

    int getFrameHeight() {
        return frameLayout.getHeight();
    }

    void show() {
        scene.addSceneObject(viewSceneObject);
    }

    void hide() {
        scene.removeSceneObject(viewSceneObject);
    }

    View findViewById(int id) {
        return frameLayout.findViewById(id);
    }
}
