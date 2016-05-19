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
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.View;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.cursor3d.CustomKeyEvent;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;

import java.util.List;

abstract class BaseView {
    private static final String TAG = BaseView.class.getSimpleName();
    private static final float QUAD_X = 10.0f;
    private static final float QUAD_Y = 8f;
    public static final float QUAD_DEPTH = -13f;
    private GVRFrameLayout frameLayout;

    private int frameWidth;
    private int frameHeight;
    private float quadHeight;
    private float halfQuadHeight;
    private float quadWidth;
    private float halfQuadWidth;
    GVRScene scene;
    GVRActivity activity;
    GVRContext context;
    private GVRViewSceneObject layoutSceneObject;
    private boolean sensorEnabled = true;

    private Handler glThreadHandler;
    private final static PointerProperties[] pointerProperties;
    private final static PointerCoords[] pointerCoordsArray;
    private final static PointerCoords pointerCoords;
    int settingsCursorId;

    static {
        PointerProperties properties = new PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        pointerProperties = new PointerProperties[]{properties};
        pointerCoords = new PointerCoords();
        pointerCoordsArray = new PointerCoords[]{pointerCoords};
    }

    BaseView(GVRContext context, GVRScene scene, int settingsCursorId, int layoutID) {
        this(context, scene, settingsCursorId, layoutID, QUAD_Y, QUAD_X);
    }

    BaseView(GVRContext context, GVRScene scene, int settingsCursorId, int layoutID, float
            quadHeight, float quadWidth) {
        this.context = context;
        this.scene = scene;
        this.settingsCursorId = settingsCursorId;
        this.quadHeight = quadHeight;
        this.halfQuadHeight = quadHeight / 2;
        this.quadWidth = quadWidth;
        this.halfQuadWidth = quadWidth / 2;

        activity = context.getActivity();
        frameLayout = new GVRFrameLayout(activity);
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
                layoutSceneObject.setSensor(new GVRBaseSensor(context));
                layoutSceneObject.getEventReceiver().addListener(sensorEvents);
                frameWidth = frameLayout.getWidth();
                frameHeight = frameLayout.getHeight();
                show();
            }
        });
    }

    private ISensorEvents sensorEvents = new ISensorEvents() {
        @Override
        public void onSensorEvent(final SensorEvent event) {
            int id = event.getCursorController().getId();

            if (id != settingsCursorId || !sensorEnabled) {
                return;
            }
            List<KeyEvent> keyEvents = event.getCursorController()
                    .getKeyEvents();

            if (keyEvents.isEmpty() == false) {
                for (KeyEvent keyEvent : keyEvents) {
                    if (keyEvent.getAction() == CustomKeyEvent.ACTION_SWIPE) {
                        sendSwipeEvent(keyEvent);
                        continue;
                    }
                    sendMotionEvent(event.getHitPoint(), keyEvent.getAction());
                }
            } else {
                sendMotionEvent(event.getHitPoint(), MotionEvent.ACTION_MOVE);
            }
        }
    };

    private void sendSwipeEvent(final KeyEvent keyEvent) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                KeyEvent clone = new KeyEvent(keyEvent);
                onSwipeEvent(clone);
            }
        });
    }

    private void sendMotionEvent(float[] hitPoint, final int action) {
        float x = (hitPoint[0] + halfQuadWidth) / quadWidth;
        float y = -(hitPoint[1] - halfQuadHeight) / quadHeight;
        pointerCoords.x = x * getFrameWidth();
        pointerCoords.y = y * getFrameHeight();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = SystemClock.uptimeMillis();
                final MotionEvent clone = MotionEvent.obtain(now, now + 1, action, 1,
                        pointerProperties, pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);

                dispatchMotionEvent(clone);
            }
        });
    }

    int getFrameWidth() {
        return frameWidth;
    }

    int getFrameHeight() {
        return frameHeight;
    }

    void show() {
        scene.addSceneObject(layoutSceneObject);
    }

    void hide() {
        scene.removeSceneObject(layoutSceneObject);
        layoutSceneObject.getEventReceiver().removeListener(sensorEvents);
    }

    void disable() {
        scene.removeSceneObject(layoutSceneObject);
        layoutSceneObject.getEventReceiver().removeListener(sensorEvents);
    }

    void enable() {
        scene.addSceneObject(layoutSceneObject);
        layoutSceneObject.getEventReceiver().addListener(sensorEvents);
    }

    View findViewById(int id) {
        return frameLayout.findViewById(id);
    }

    void dispatchMotionEvent(MotionEvent motionEvent) {
        frameLayout.dispatchTouchEvent(motionEvent);
        frameLayout.invalidate();
        motionEvent.recycle();
    }

    void onSwipeEvent(KeyEvent keyEvent) {
    }

    void setSettingsCursorId(int settingsCursorId) {
        this.settingsCursorId = settingsCursorId;
    }

    void setSensorEnabled(boolean enabled) {
        sensorEnabled = enabled;
    }

    boolean isSensorEnabled() {
        return sensorEnabled;
    }
}
