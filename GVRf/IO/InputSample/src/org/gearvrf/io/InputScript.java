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

import java.util.concurrent.Future;
import org.gearvrf.*;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.gearvrf.utility.Log;
import org.gearvrf.stl.STLController.ControllerListener;
import android.graphics.Color;
import android.opengl.Matrix;
import android.util.SparseArray;
import android.view.Gravity;

public class InputScript extends GVRScript implements ControllerListener {
    private static final String TAG = InputScript.class.getSimpleName();

    private static final String SELECT_TEXT = "Move the cursor till the cube turns red (try both sticks)";
    private static final String BUTTON_SELECT_TEXT = "Press and hold down a trigger button to select";
    private static final String MOVE_CUBE_TEXT = "Now, use the joystick to move the cube";

    private static final float[] UP_VECTOR = { 0.0f, 1.0f, 0.0f, 1.0f };
    private static final float[] RIGHT_VECTOR = { 1.0f, 0.0f, 0.0f, 1.0f };
    private static final float[] INITIAL_CURSOR_POSITION = { 0.0f, 0.0f,
            -7.0f };

    private static final float MAX_RADIUS_SQUARE = 30.0f * 30.0f;
    private static final float MIN_RADIUS_SQUARE = 2.0f * 2.0f;

    private static final float ONE_RADIAN = 1.0f;
    private static final float DEPTH_STEP = (float) (1
            / Math.toDegrees(ONE_RADIAN));

    private static final float CUBE_WIDTH = 200.0f;

    // Change this value to increase or decrease the cursor speed
    private static final float SPEED = 20f;

    private final SparseArray<Cursor> cursors;
    private GVRContext mGVRContext = null;
    private CustomShaderManager shaderManager;

    private GVRInputManager inputManager;
    private GVRScene mainScene;
    private boolean initialized = false;

    // Variables for FPS
    private int frames = 0;
    private long startTimeMillis = 0;
    private final long interval = 100;

    InputScript() {
        cursors = new SparseArray<Cursor>();
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        mainScene = mGVRContext.getNextMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);

        addSurroundings(gvrContext, mainScene);

        // set up the input manager for the main scene
        inputManager = gvrContext.setupInputManagerForScene(mainScene);

        final GVRTextViewSceneObject text = new GVRTextViewSceneObject(
                gvrContext, gvrContext.getActivity(), 40.0f, 20.0f,
                SELECT_TEXT);

        text.setGravity(Gravity.CENTER);
        text.setTextSize(35.0f);
        text.setTextColor(Color.WHITE);
        text.setRefreshFrequency(IntervalFrequency.HIGH);
        text.getRenderData().setRenderingOrder(10002);
        text.getTransform().setPosition(0.0f, 20.0f, -45.0f);
        mainScene.addSceneObject(text);

        shaderManager = new CustomShaderManager(gvrContext);
        GVRSceneObject cube1 = new Cube(gvrContext, "Cube 1", shaderManager);
        cube1.getTransform().setPosition(0.0f, 1.0f, -10.0f);
        mainScene.addSceneObject(cube1);
        // add the sensor to the cube
        cube1.addSensor(new CubeSensor(gvrContext, text));

        GVRSceneObject cube2 = new Cube(gvrContext, "Cube 2", shaderManager);
        cube2.getTransform().setPosition(0.0f, 1.0f, -12.0f);
        cube2.getTransform().rotateByAxisWithPivot(-15.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube2.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        mainScene.addSceneObject(cube2);
        // add the sensor to the cube
        cube2.addSensor(new CubeSensor(gvrContext, text));

        GVRSceneObject cube3 = new Cube(gvrContext, "Cube 3", shaderManager);
        cube3.getTransform().setPosition(0.0f, 1.0f, -15.0f);
        cube3.getTransform().rotateByAxisWithPivot(15.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube3.getTransform().rotateByAxisWithPivot(15.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube3.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        mainScene.addSceneObject(cube3);
        // add the sensor to the cube
        cube3.addSensor(new CubeSensor(gvrContext, text));

        // initialize the pointer
        for (int i = 0; i < cursors.size(); ++i) {
            int pointerId = cursors.keyAt(i);
            Cursor cursor = cursors.get(pointerId);
            if (cursor == null) {
                cursor = new Cursor(mGVRContext, shaderManager);
                cursor.getTransform().setPosition(INITIAL_CURSOR_POSITION[0],
                        INITIAL_CURSOR_POSITION[1], INITIAL_CURSOR_POSITION[2]);
                cursors.append(pointerId, cursor);
                mainScene.addSceneObject(cursor);
                inputManager.setupPointer(pointerId, INITIAL_CURSOR_POSITION[0],
                        INITIAL_CURSOR_POSITION[1], INITIAL_CURSOR_POSITION[2]);
            }
        }

        initialized = true;
    }

    private class CubeSensor extends GVRBaseSensor
            implements SensorEventListener {
        private final GVRTextViewSceneObject text;
        private GVRSceneObject selected;
        private int selectedPointerId;

        public CubeSensor(GVRContext gvrContext, GVRTextViewSceneObject text) {
            super(gvrContext);
            this.text = text;
            registerSensorEventListener(this);
        }

        @Override
        public void onSensorEvent(SensorEvent event) {
            Cursor cursor = cursors.get(event.getPointerId());

            // Safe to assume that the returned object is a cube
            Cube cube = (Cube) event.getObject();
            if (selected != null && selectedPointerId != event.getPointerId()) {
                Log.d(TAG, "Return null");
                return;
            }

            if (event.isActive() == false && selected != null
                    && event.getPointerId() == selectedPointerId) {
                cursor.removeChildObject(selected);
                selected.getTransform().setPosition(
                        +cursor.getTransform().getPositionX()
                                + selected.getTransform().getPositionX(),
                        +cursor.getTransform().getPositionY()
                                + selected.getTransform().getPositionY(),
                        +cursor.getTransform().getPositionZ()
                                + selected.getTransform().getPositionZ());
                selected = null;
                selectedPointerId = -1;
            }

            if (event.isActive()) {
                if (cube.isColliding(cursor) && selected == null) {
                    selected = cube;
                    selectedPointerId = event.getPointerId();
                    event.getObject().getTransform()
                            .setPosition(-cursor.getTransform().getPositionX()
                                    + selected.getTransform().getPositionX(),
                            -cursor.getTransform().getPositionY()
                                    + selected.getTransform().getPositionY(),
                            -cursor.getTransform().getPositionZ()
                                    + selected.getTransform().getPositionZ());
                    cursor.addChildObject(selected);

                    cube.setGreen();
                    text.setText(MOVE_CUBE_TEXT);
                }
            } else if (event.isOver()) {
                if (cube.isColliding(cursor)) {
                    cube.setRed();
                    text.setText(BUTTON_SELECT_TEXT);
                } else {
                    cube.setGrey();
                    text.setText(SELECT_TEXT);
                }
            } else {
                cube.setGrey();
                text.setText(SELECT_TEXT);
            }
        }
    }

    @Override
    public void onStep() {
        // tick(); uncomment for FPS
    }

    private static float square(float x) {
        return x * x;
    }

    private void tick() {
        ++frames;
        if (System.currentTimeMillis() - startTimeMillis >= interval) {
            Log.d(TAG, "FPS : " + frames / (interval / 1000.0f));
            frames = 0;
            startTimeMillis = System.currentTimeMillis();
        }
    }

    private boolean checkBounds(float[] point) {
        float lhs = square(point[0]) + square(point[1]) + square(point[2]);
        return (lhs <= MAX_RADIUS_SQUARE && lhs >= MIN_RADIUS_SQUARE);
    }

    // The assets for the Cubemap are taken from the Samsung Developers website:
    // http://www.samsung.com/us/samsungdeveloperconnection/developer-resources/
    // gear-vr/apps-and-games/exercise-2-creating-the-splash-scene.html
    private void addSurroundings(GVRContext gvrContext, GVRScene scene) {
        FutureWrapper<GVRMesh> futureQuadMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));
        Future<GVRTexture> futureCubemapTexture = gvrContext
                .loadFutureCubemapTexture(
                        new GVRAndroidResource(mGVRContext, R.raw.earth));

        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.Cubemap.ID);
        cubemapMaterial.setMainTexture(futureCubemapTexture);

        // surrounding cube
        GVRSceneObject frontFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        frontFace.getRenderData().setMaterial(cubemapMaterial);
        frontFace.setName("front");
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        backFace.getRenderData().setMaterial(cubemapMaterial);
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        leftFace.getRenderData().setMaterial(cubemapMaterial);
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        rightFace.getRenderData().setMaterial(cubemapMaterial);
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        topFace.getRenderData().setMaterial(cubemapMaterial);
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        bottomFace.getRenderData().setMaterial(cubemapMaterial);
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
    }

    @Override
    public void onEvent(int id, float x, float y, float z, boolean active) {

        // It is important to note that this callback happens on the
        // EventHandlerThread created by the STL library.
        // In this case we are not holding up either the Main UI
        // or GL thread.
        if (initialized) {
            Cursor cursor = cursors.get(id);
            if (cursor == null) {
                Log.d(TAG, "Cursor is null for id %d", 0);
                return;
            }

            float[] viewMatrix = mainScene.getMainCameraRig().getHeadTransform()
                    .getModelMatrix();

            float[] xAxis = new float[4];
            float[] yAxis = new float[4];

            Matrix.multiplyMV(xAxis, 0, viewMatrix, 0, UP_VECTOR, 0);
            Matrix.multiplyMV(yAxis, 0, viewMatrix, 0, RIGHT_VECTOR, 0);

            transform(cursor, x, y, z, xAxis, yAxis);

            // Create the InputEvent for the input manager.
            InputEvent event = InputEvent.obtain();
            event.setX(cursor.getTransform().getPositionX());
            event.setY(cursor.getTransform().getPositionY());
            event.setZ(cursor.getTransform().getPositionZ());
            event.setActive(active);
            event.setPointerId(id);
            inputManager.dispatchInputEvent(event);

            // recycle the event
            event.recycle();

        }
    }

    public void transform(GVRSceneObject cursor, float x, float y, float z,
            float[] xAxis, float[] yAxis) {
        float sensitivity = (50f + SPEED) / 100f;

        if (x != 0 || y != 0) {
            float angle = (float) Math.atan2(y, x);
            float displacementX = (float) Math.cos(angle);
            float displacementY = (float) Math.sin(angle);
            cursor.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);

            cursor.getTransform().rotateByAxisWithPivot(
                    -displacementX * sensitivity, xAxis[0], xAxis[1], xAxis[2],
                    0.0f, 0.0f, 0.0f);
            cursor.getTransform().rotateByAxisWithPivot(
                    displacementY * sensitivity, yAxis[0], yAxis[1], yAxis[2],
                    0.0f, 0.0f, 0.0f);

        }
        float[] cursorPosition = new float[] {
                cursor.getTransform().getPositionX(),
                cursor.getTransform().getPositionY(),
                cursor.getTransform().getPositionZ() };
        if (z != 0.0f) {
            float step = (z < 0) ? DEPTH_STEP * sensitivity
                    : -DEPTH_STEP * sensitivity;

            float[] point = { cursorPosition[0] + cursorPosition[0] * step,
                    cursorPosition[1] + cursorPosition[1] * step,
                    cursorPosition[2] + cursorPosition[2] * step };

            if (checkBounds(point)) {
                cursor.getTransform().setPosition(point[0], point[1], point[2]);
            }

        }

    }

    @Override
    public void onControllerAdded(final int id) {
        if (initialized) {
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    // add a cursor object
                    Cursor cursor = new Cursor(mGVRContext, shaderManager);
                    cursor.getTransform().setPosition(
                            INITIAL_CURSOR_POSITION[0],
                            INITIAL_CURSOR_POSITION[1],
                            INITIAL_CURSOR_POSITION[2]);
                    cursors.append(id, cursor);
                    mainScene.addSceneObject(cursor);
                    inputManager.setupPointer(id, INITIAL_CURSOR_POSITION[0],
                            INITIAL_CURSOR_POSITION[1],
                            INITIAL_CURSOR_POSITION[2]);
                }
            });
        } else {
            // add a null object till we have a context
            cursors.append(id, null);
        }
    }

    @Override
    public void onControllerRemoved(int id) {
        if (initialized) {
            final Cursor cursor = cursors.get(id);
            if (cursor != null) {
                mGVRContext.runOnGlThread(new Runnable() {
                    @Override
                    public void run() {

                        mainScene.removeSceneObject(cursor);
                    }
                });
                cursors.remove(id);
            }
        }
    }
}