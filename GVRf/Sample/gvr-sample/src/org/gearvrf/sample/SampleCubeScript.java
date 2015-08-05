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

package org.gearvrf.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
import org.gearvrf.utility.Threads;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class SampleCubeScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private GVRContext mGVRContext = null;
    private GVRSceneObject mFrontFace = null;
    private GVRSceneObject mFrontFace2 = null;
    private GVRSceneObject mFrontFace3 = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));

        mFrontFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace.setName("front");
        scene.addSceneObject(mFrontFace);
        mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        mFrontFace2 = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace2.setName("front2");
        scene.addSceneObject(mFrontFace2);
        mFrontFace2.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 2.0f);

        mFrontFace3 = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace3.setName("front3");
        scene.addSceneObject(mFrontFace3);
        mFrontFace3.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 3.0f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.back)));
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.left)));
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        leftFace.getRenderData().setRenderMask(GVRRenderMaskBit.Left);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.right)));
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        rightFace.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.top)));
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.bottom)));
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                gvrContext);
        GVRMeshEyePointee meshEyePointee = new GVRMeshEyePointee(gvrContext,
                mFrontFace.getRenderData().getMesh());
        eyePointeeHolder.addPointee(meshEyePointee);
        mFrontFace.attachEyePointeeHolder(eyePointeeHolder);

        GVREyePointeeHolder eyePointeeHolder2 = new GVREyePointeeHolder(
                gvrContext);
        GVRMeshEyePointee meshEyePointee2 = new GVRMeshEyePointee(gvrContext,
                mFrontFace2.getRenderData().getMesh());
        eyePointeeHolder2.addPointee(meshEyePointee2);
        mFrontFace2.attachEyePointeeHolder(eyePointeeHolder2);

        GVREyePointeeHolder eyePointeeHolder3 = new GVREyePointeeHolder(
                gvrContext);
        GVRMesh boundingBox = mFrontFace3.getRenderData().getMesh()
                .getBoundingBox();
        GVRMeshEyePointee meshEyePointee3 = new GVRMeshEyePointee(gvrContext,
                boundingBox);
        eyePointeeHolder3.addPointee(meshEyePointee3);
        mFrontFace3.attachEyePointeeHolder(eyePointeeHolder3);

        for (GVRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
        mFrontFace.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontFace2.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontFace3.getRenderData().getMaterial().setOpacity(1.0f);
        for (GVRPickedObject pickedObject : GVRPicker.findObjects(mGVRContext
                .getMainScene())) {
            if (pickedObject.getHitObject().equals(mFrontFace)) {
                mFrontFace.getRenderData().getMaterial().setOpacity(0.5f);
            }
            if (pickedObject.getHitObject().equals(mFrontFace2)) {
                mFrontFace2.getRenderData().getMaterial().setOpacity(0.5f);
            }
            if (pickedObject.getHitObject().equals(mFrontFace3)) {
                mFrontFace3.getRenderData().getMaterial().setOpacity(0.5f);
            }
        }
    }

    private boolean lastScreenshotLeftFinished = true;
    private boolean lastScreenshotRightFinished = true;
    private boolean lastScreenshotCenterFinished = true;
    private boolean lastScreenshot3DFinished = true;

    // mode 0: center eye; mode 1: left eye; mode 2: right eye
    public void captureScreen(final int mode, final String filename) {
        Threads.spawn(new Runnable() {
            public void run() {
                switch (mode) {
                case 0:
                    if (lastScreenshotCenterFinished) {
                        mGVRContext.captureScreenCenter(newScreenshotCallback(
                                filename, 0));
                        lastScreenshotCenterFinished = false;
                    }
                    break;
                case 1:
                    if (lastScreenshotLeftFinished) {
                        mGVRContext.captureScreenLeft(newScreenshotCallback(
                                filename, 1));
                        lastScreenshotLeftFinished = false;
                    }
                    break;
                case 2:
                    if (lastScreenshotRightFinished) {
                        mGVRContext.captureScreenRight(newScreenshotCallback(
                                filename, 2));
                        lastScreenshotRightFinished = false;
                    }
                    break;
                }
            }
        });
    }

    public void captureScreen3D(String filename) {
        if (lastScreenshot3DFinished) {
            mGVRContext.captureScreen3D(newScreenshot3DCallback(filename));
            lastScreenshot3DFinished = false;
        }
    }

    private GVRScreenshotCallback newScreenshotCallback(final String filename,
            final int mode) {
        return new GVRScreenshotCallback() {

            @Override
            public void onScreenCaptured(Bitmap bitmap) {
                if (bitmap != null) {
                    File file = new File(
                            Environment.getExternalStorageDirectory(), filename
                                    + ".png");
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                outputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e("SampleActivity", "Returned Bitmap is null");
                }

                // enable next screenshot
                switch (mode) {
                case 0:
                    lastScreenshotCenterFinished = true;
                    break;
                case 1:
                    lastScreenshotLeftFinished = true;
                    break;
                case 2:
                    lastScreenshotRightFinished = true;
                    break;
                }
            }
        };
    }

    private GVRScreenshot3DCallback newScreenshot3DCallback(
            final String filename) {
        return new GVRScreenshot3DCallback() {

            @Override
            public void onScreenCaptured(Bitmap[] bitmapArray) {
                Log.d("SampleActivity", "Length of bitmapList: "
                        + bitmapArray.length);
                if (bitmapArray.length > 0) {
                    for (int i = 0; i < bitmapArray.length; i++) {
                        Bitmap bitmap = bitmapArray[i];
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                filename + "_" + i + ".png");
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                    outputStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    Log.e("SampleActivity", "Returned Bitmap List is empty");
                }

                // enable next screenshot
                lastScreenshot3DFinished = true;
            }
        };
    }
}
