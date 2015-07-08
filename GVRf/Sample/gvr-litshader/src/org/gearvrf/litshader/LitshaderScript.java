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

package org.gearvrf.litshader;

import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

import android.util.Log;
import android.view.MotionEvent;

public class LitshaderScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;
    private GVRContext mGVRContext;
    private GVRLight mLight;
    private static final float LIGHT_Z = 100.0f;
    private static final float LIGHT_ROTATE_RADIUS = 100.0f;

    GVRSceneObject rotateObject;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));

        GVRSceneObject mFrontFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace.setName("front");
        scene.addSceneObject(mFrontFace);
        mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

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

        // lit object
        Future<GVRMesh> futureSphereMesh = gvrContext
                .loadFutureMesh(new GVRAndroidResource(mGVRContext,
                        R.raw.sphere));
        GVRMaterial litMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.Texture.ID);
        litMaterial.setMainTexture(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.earthmap1k)));
        litMaterial.setColor(0.5f, 0.5f, 0.5f);
        litMaterial.setOpacity(1.0f);
        litMaterial.setAmbientColor(1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setDiffuseColor(0.8f, 0.8f, 0.8f, 1.0f);
        litMaterial.setSpecularColor(1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setSpecularExponent(128.0f);
        mLight = new GVRLight(gvrContext);
        mLight.setPosition(LIGHT_ROTATE_RADIUS, 0.0f, LIGHT_Z);
        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1.0f);
        mLight.setSpecularIntensity(1.0f, 0.5f, 0.5f, 1.0f);

        rotateObject = new GVRSceneObject(gvrContext, futureSphereMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.earthmap1k)));
        rotateObject.getRenderData().setMaterial(litMaterial);
        rotateObject.getRenderData().setLight(mLight);
        rotateObject.getRenderData().enableLight();
        rotateObject.setName("sphere");
        scene.addSceneObject(rotateObject);
        rotateObject.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR,
                SCALE_FACTOR);
        rotateObject.getTransform()
                .setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.25f);

        for (GVRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }
    }

    private double theta = 0.0;

    @Override
    public void onStep() {
        FPSCounter.tick();

        theta += 0.01;
        double sine = Math.cos(theta);
        double cosine = Math.sin(theta);
        mLight.setPosition((float) sine * LIGHT_ROTATE_RADIUS, (float) cosine
                * LIGHT_ROTATE_RADIUS, LIGHT_Z);

        if (rotateObject != null) {
            rotateObject.getTransform().rotateByAxis(0.2f, 0.0f, 1.0f, 1.0f);
        }
    }

    private boolean lightEnabled = true;

    public void onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if (rotateObject != null) {
                if (lightEnabled) {
                    rotateObject.getRenderData().disableLight();
                    lightEnabled = false;
                } else {
                    rotateObject.getRenderData().enableLight();
                    lightEnabled = true;
                }
            }
        }
    }
}
