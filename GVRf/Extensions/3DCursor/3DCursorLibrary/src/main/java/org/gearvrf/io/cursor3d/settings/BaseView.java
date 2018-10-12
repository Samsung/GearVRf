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

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;

abstract class BaseView {
    private static final String TAG = BaseView.class.getSimpleName();
    private static final float DEFAULT_SCALE = 10.0f;
    public static final float QUAD_DEPTH = -13f;

    GVRScene scene;
    GVRContext context;
    Activity activity;
    private GVRViewSceneObject layoutSceneObject;
    int settingsCursorId;

    BaseView(GVRContext context, GVRScene scene, int settingsCursorId, int layoutID) {
        this(context, scene, settingsCursorId, layoutID, DEFAULT_SCALE);
    }

    BaseView(final GVRContext context, final GVRScene scene,
             final int settingsCursorId, final int layoutID, final float scale) {
        this.context = context;
        this.scene = scene;
        this.activity = context.getActivity();
        this.settingsCursorId = settingsCursorId;

        layoutSceneObject = new GVRViewSceneObject(context, layoutID, new IViewEvents() {
            @Override
            public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
                BaseView.this.onInitView(view);
            }

            @Override
            public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
                gvrViewSceneObject.getTransform().setScale(scale, scale, 1.0f);
                BaseView.this.onStartRendering();
            }
        });

        layoutSceneObject.setTextureBufferSize(1024);
    }

    abstract protected void onInitView(View view);
    abstract protected void onStartRendering();

    void render(final float x, final float y, final float z) {
        layoutSceneObject.getTransform().setPosition(x, y, z);
        show();
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

    void setSettingsCursorId(int settingsCursorId) {
        this.settingsCursorId = settingsCursorId;
    }

    protected void setGestureDetector(GestureDetector detector) {
        layoutSceneObject.setGestureDetector(detector);
    }
}
