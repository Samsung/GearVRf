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
package org.gearvrf.io;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Future;

import org.gearvrf.*;

import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;

import com.gearvrf.stl.STLController.ControllerListener;

import android.opengl.Matrix;
import android.view.Gravity;

public class ControllerScript extends GVRScript implements ControllerListener {

    private static final String TAG = ControllerScript.class.getSimpleName();

    private static final String SELECT_TEXT = "Move the cursor till the cube turns red (try both sticks)";
    private static final String BUTTON_SELECT_TEXT = "Press and hold down a joystick button to select";
    private static final String MOVE_CUBE_TEXT = "Now, use the joystick to move the cube";

    private static final float UNPICKED_COLOR_R = 0.8f;
    private static final float UNPICKED_COLOR_G = 0.8f;
    private static final float UNPICKED_COLOR_B = 0.8f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float OVER_COLOR_R = 0.5f;
    private static final float OVER_COLOR_G = 0.5f;
    private static final float OVER_COLOR_B = 0.5f;
    private static final float OVER_COLOR_A = 1.0f;

    private static final float MOVE_COLOR_R = 0.0f;
    private static final float MOVE_COLOR_G = 1.0f;
    private static final float MOVE_COLOR_B = 0.0f;
    private static final float MOVE_COLOR_A = 1.0f;

    private static final float SENSITIVITY = 0.6f;
    private static final int POINTER_ID = 1;

    private GVRContext mGVRContext = null;
    private ColorShader mColorShader = null;
    private List<GVRSceneObject> selected = new ArrayList<GVRSceneObject>();
    private GVRSceneObject cursor;
    private GVRInputManager inputManager;
    private GVRScene mainScene;
    private boolean intialized = false;

    ControllerScript() {
        // empty constructor
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        mainScene = mGVRContext.getNextMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);
        mColorShader = new ColorShader(mGVRContext);

        // set up the input manager for the main scene
        inputManager = gvrContext.setupInputManagerForScene(mainScene);

        final GVRTextViewSceneObject text = new GVRTextViewSceneObject(
                gvrContext, gvrContext.getActivity(), 200.0f, 200.f,
                SELECT_TEXT);

        text.setGravity(Gravity.CENTER);
        text.setTextSize(20.0f);
        text.getTransform().setPosition(0.0f, -70.0f, -200.0f);
        mainScene.addSceneObject(text);

        // Add the cursor
        cursor = new GVRSceneObject(gvrContext);
        GVRMaterial cursorMaterial = new GVRMaterial(mGVRContext,
                mColorShader.getShaderId());
        cursorMaterial.setVec4(ColorShader.COLOR_KEY, 0.1f, 0.1f, 0.1f, 1.0f);
        Future<GVRMesh> futureCursorMesh = gvrContext.loadFutureMesh(
                new GVRAndroidResource(mGVRContext, R.raw.sphere));
        GVRRenderData cursorRenderData = new GVRRenderData(gvrContext);
        cursorRenderData.setMesh(futureCursorMesh);
        cursorRenderData.setMaterial(cursorMaterial);
        cursor.attachRenderData(cursorRenderData);
        cursor.getTransform().setPosition(0.0f, 0.0f, -7.0f);
        mainScene.addSceneObject(cursor);

        // Create a new sensor for the cube
        GVRBaseSensor sensor = new GVRBaseSensor(gvrContext);
        sensor.registerSensorEventListener(new SensorEventListener() {
            @Override
            public void onSensorEvent(SensorEvent event) {

                if (event.isActive() == false && selected.isEmpty() == false) {
                    selected.clear();
                }

                if (event.isActive()) {
                    if (event.getObject().isColliding(cursor)
                            && selected.contains(event.getObject()) == false) {
                        selected.add(event.getObject());
                        Log.d(TAG, "Adding to selected " + selected.isEmpty());
                        event.getObject().getRenderData().getMaterial().setVec4(
                                ColorShader.COLOR_KEY, MOVE_COLOR_R,
                                MOVE_COLOR_G, MOVE_COLOR_B, MOVE_COLOR_A);
                        text.setText(MOVE_CUBE_TEXT);
                    }
                } else if (event.isOver()) {
                    if (event.getObject().isColliding(cursor)) {
                        event.getObject().getRenderData().getMaterial().setVec4(
                                ColorShader.COLOR_KEY, PICKED_COLOR_R,
                                PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
                        text.setText(BUTTON_SELECT_TEXT);
                    } else {
                        event.getObject().getRenderData().getMaterial().setVec4(
                                ColorShader.COLOR_KEY, OVER_COLOR_R,
                                OVER_COLOR_G, OVER_COLOR_B, OVER_COLOR_A);
                        text.setText(SELECT_TEXT);
                    }
                } else {
                    event.getObject().getRenderData().getMaterial().setVec4(
                            ColorShader.COLOR_KEY, UNPICKED_COLOR_R,
                            UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                            UNPICKED_COLOR_A);
                    text.setText(SELECT_TEXT);
                }
            }
        });

        GVRSceneObject cube = new GVRSceneObject(gvrContext);
        GVRMaterial cubeMaterial = new GVRMaterial(mGVRContext,
                mColorShader.getShaderId());
        cubeMaterial.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        Future<GVRMesh> futureCubeMesh = gvrContext.loadFutureMesh(
                new GVRAndroidResource(mGVRContext, R.raw.cube));
        GVRRenderData cubeRenderData = new GVRRenderData(gvrContext);
        cubeRenderData.setMesh(futureCubeMesh);
        cubeRenderData.setMaterial(cubeMaterial);
        cube.attachRenderData(cubeRenderData);
        cube.getTransform().setPosition(0.0f, 1.0f, -10.0f);
        mainScene.addSceneObject(cube);

        // add the sensor to the cube
        cube.addSensor(sensor);

        // initialize the pointer
        inputManager.setupPointer(POINTER_ID, 0.0f, 0.0f, -7.0f);
        intialized = true;
    }

    @Override
    public void onStep() {
        // tick(); uncomment for FPS
    }

    public static float square(float x) {
        return x * x;
    }

    private static int frames = 0;
    private static long startTimeMillis = 0;
    private static final long interval = 100;

    public static void tick() {
        ++frames;
        if (System.currentTimeMillis() - startTimeMillis >= interval) {
            Log.d(TAG, "FPS : " + frames / (interval / 1000.0f));
            frames = 0;
            startTimeMillis = System.currentTimeMillis();
        }
    }

    @Override
    public void onEvent(float x, float y, float z, boolean active) {

        // It is important to note that this callback happens on the
        // EventHandlerThread created by the STL library.
        // In this case we are not holding up either the Main UI
        // or GL thread.
        if (intialized) {

            float[] viewMatrix = mainScene.getMainCameraRig().getHeadTransform()
                    .getModelMatrix();

            float[] displacement = new float[] { x * SENSITIVITY,
                    y * SENSITIVITY, z * SENSITIVITY, 1.0f };
            float[] translation = new float[4];

            // Get the heading direction using the view matrix
            Matrix.multiplyMV(translation, 0, viewMatrix, 0, displacement, 0);
            if (selected.isEmpty() == false) {
                for (GVRSceneObject select : selected) {
                    select.getTransform().translate(translation[0],
                            translation[1], translation[2]);
                }
            }
            cursor.getTransform().translate(translation[0], translation[1],
                    translation[2]);

            // Create the InputEvent for the input manager.
            InputEvent event = InputEvent.obtain();
            event.setX(cursor.getTransform().getPositionX());
            event.setY(cursor.getTransform().getPositionY());
            event.setZ(cursor.getTransform().getPositionZ());
            event.setActive(active);
            event.setPointerId(POINTER_ID);
            inputManager.dispatchInputEvent(event);

            // recycle the event
            event.recycle();
        }
    }
}
