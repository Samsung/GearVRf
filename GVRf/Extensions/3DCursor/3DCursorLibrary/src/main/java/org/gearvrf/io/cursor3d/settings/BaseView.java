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

package org.gearvrf.io.cursor3d.settings;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRViewSceneObject;

abstract class BaseView {
    private static final String TAG = BaseView.class.getSimpleName();
    private static final float QUAD_X = 10.0f;
    private static final float QUAD_Y = 8f;
    public static final float QUAD_DEPTH = -13f;
    private FrameLayout frameLayout;

    private float quadHeight;
    private float quadWidth;
    GVRScene scene;
    GVRActivity activity;
    GVRContext context;
    private GVRViewSceneObject layoutSceneObject;
    private Handler glThreadHandler;
    int settingsCursorId;

    BaseView(GVRContext context, GVRScene scene, int settingsCursorId, int layoutID) {
        this(context, scene, settingsCursorId, layoutID, QUAD_Y, QUAD_X);
    }

    BaseView(GVRContext context, GVRScene scene, int settingsCursorId, int layoutID, float
            quadHeight, float quadWidth) {
        this.context = context;
        this.scene = scene;
        this.settingsCursorId = settingsCursorId;
        this.quadHeight = quadHeight;
        this.quadWidth = quadWidth;

        activity = context.getActivity();
        frameLayout = new FrameLayout(activity);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        View.inflate(activity, layoutID, frameLayout);
        glThreadHandler = new Handler(Looper.getMainLooper());
    }

    void render(final float x, final float y, final float z) {
        glThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                layoutSceneObject = new GVRViewSceneObject(context, frameLayout,
                        context.createQuad(quadWidth, quadHeight));
                layoutSceneObject.getTransform().setPosition(x, y, z);
                show();
            }
        });
    }

    void show() {
        scene.addSceneObject(layoutSceneObject);
    }

    void hide() {
        scene.removeSceneObject(layoutSceneObject);
    }

    void disable() {
        scene.removeSceneObject(layoutSceneObject);
    }

    void enable() {
        scene.addSceneObject(layoutSceneObject);
    }

    View findViewById(int id) {
        return frameLayout.findViewById(id);
    }

    void setSettingsCursorId(int settingsCursorId) {
        this.settingsCursorId = settingsCursorId;
    }

    protected void setGestureDetector(GestureDetector detector) {
        layoutSceneObject.setGestureDetector(detector);
    }
}
