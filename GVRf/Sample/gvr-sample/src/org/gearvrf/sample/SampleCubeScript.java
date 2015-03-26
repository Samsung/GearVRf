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


import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

import android.util.Log;


public class SampleCubeScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private GVRContext mGVRContext = null;
    private GVRSceneObject mFrontBoardObject = null;
    private GVRSceneObject mFrontBoardObject2 = null;
    private GVRSceneObject mFrontBoardObject3 = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRTexture front = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.front_png));
        GVRTexture back = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.back_png));
        GVRTexture left = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.left_png));
        GVRTexture right = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.right_png));
        GVRTexture top = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.top_png));
        GVRTexture bottom = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.bottom_png));

        GVRScene scene = mGVRContext.getMainScene();

        mFrontBoardObject = new GVRSceneObject(gvrContext, CUBE_WIDTH,
                CUBE_WIDTH, front);
        mFrontBoardObject.setName("front");
        scene.addSceneObject(mFrontBoardObject);
        mFrontBoardObject.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f);

        mFrontBoardObject2 = new GVRSceneObject(gvrContext, CUBE_WIDTH,
                CUBE_WIDTH, front);
        mFrontBoardObject2.setName("front2");
        scene.addSceneObject(mFrontBoardObject2);
        mFrontBoardObject2.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 2.0f);

        mFrontBoardObject3 = new GVRSceneObject(gvrContext, CUBE_WIDTH,
                CUBE_WIDTH, front);
        mFrontBoardObject3.setName("front3");
        scene.addSceneObject(mFrontBoardObject3);
        mFrontBoardObject3.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 3.0f);

        GVRSceneObject backBoardObject = new GVRSceneObject(gvrContext,
                CUBE_WIDTH, CUBE_WIDTH, back);
        backBoardObject.setName("back");
        scene.addSceneObject(backBoardObject);
        backBoardObject.getTransform().setPosition(0.0f, 0.0f,
                CUBE_WIDTH * 0.5f);
        backBoardObject.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftBoardObject = new GVRSceneObject(gvrContext,
                CUBE_WIDTH, CUBE_WIDTH, left);
        leftBoardObject.setName("left");
        scene.addSceneObject(leftBoardObject);
        leftBoardObject.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f,
                0.0f);
        leftBoardObject.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        leftBoardObject.getRenderData().setRenderMask(GVRRenderMaskBit.Left);

        GVRSceneObject rightBoardObject = new GVRSceneObject(gvrContext,
                CUBE_WIDTH, CUBE_WIDTH, right);
        rightBoardObject.setName("right");
        scene.addSceneObject(rightBoardObject);
        rightBoardObject.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f,
                0.0f);
        rightBoardObject.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        rightBoardObject.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        GVRSceneObject topBoardObject = new GVRSceneObject(gvrContext,
                CUBE_WIDTH, CUBE_WIDTH, top);
        topBoardObject.setName("top");
        scene.addSceneObject(topBoardObject);
        topBoardObject.getTransform()
                .setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topBoardObject.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomBoardObject = new GVRSceneObject(gvrContext,
                CUBE_WIDTH, CUBE_WIDTH, bottom);
        bottomBoardObject.setName("bottom");
        scene.addSceneObject(bottomBoardObject);
        bottomBoardObject.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f,
                0.0f);
        bottomBoardObject.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                gvrContext);
        GVRMeshEyePointee meshEyePointee = new GVRMeshEyePointee(gvrContext,
                mFrontBoardObject.getRenderData().getMesh());
        eyePointeeHolder.addPointee(meshEyePointee);
        mFrontBoardObject.attachEyePointeeHolder(eyePointeeHolder);

        GVREyePointeeHolder eyePointeeHolder2 = new GVREyePointeeHolder(
                gvrContext);
        GVRMeshEyePointee meshEyePointee2 = new GVRMeshEyePointee(gvrContext,
                mFrontBoardObject2.getRenderData().getMesh());
        eyePointeeHolder2.addPointee(meshEyePointee2);
        mFrontBoardObject2.attachEyePointeeHolder(eyePointeeHolder2);

        GVREyePointeeHolder eyePointeeHolder3 = new GVREyePointeeHolder(
                gvrContext);
        GVRMesh boundingBox = mFrontBoardObject3.getRenderData().getMesh()
                .getBoundingBox();
        GVRMeshEyePointee meshEyePointee3 = new GVRMeshEyePointee(gvrContext,
                boundingBox);
        eyePointeeHolder3.addPointee(meshEyePointee3);
        mFrontBoardObject3.attachEyePointeeHolder(eyePointeeHolder3);

        for (GVRSceneObject so : mGVRContext.getMainScene()
                .getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
        mFrontBoardObject.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontBoardObject2.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontBoardObject3.getRenderData().getMaterial().setOpacity(1.0f);
        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker
                .pickScene(mGVRContext.getMainScene());
        for (GVREyePointeeHolder eyePointeeHolder : eyePointeeHolders) {
            if (eyePointeeHolder.getOwnerObject().equals(mFrontBoardObject)) {
                mFrontBoardObject.getRenderData().getMaterial()
                        .setOpacity(0.5f);
            }
            if (eyePointeeHolder.getOwnerObject().equals(mFrontBoardObject2)) {
                mFrontBoardObject2.getRenderData().getMaterial()
                        .setOpacity(0.5f);
            }
            if (eyePointeeHolder.getOwnerObject().equals(mFrontBoardObject3)) {
                mFrontBoardObject3.getRenderData().getMaterial()
                        .setOpacity(0.5f);
            }
        }
    }
}
