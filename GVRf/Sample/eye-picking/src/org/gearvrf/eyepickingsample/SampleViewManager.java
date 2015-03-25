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


package org.gearvrf.eyepickingsample;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

public class SampleViewManager extends GVRScript {

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;
    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private GVRContext mGVRContext = null;
    private ColorShader mColorShader = null;
    private List<GVRSceneObject> mObjects = new ArrayList<GVRSceneObject>();

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mGVRContext.getMainScene().getMainCameraRig().getLeftCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mGVRContext.getMainScene().getMainCameraRig().getRightCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mColorShader = new ColorShader(mGVRContext);

        /*
         * Adding Boards
         */
        GVRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, -3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 0.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 0.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, -3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, -3.0f, -5.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        /*
         * Adding bunnies.
         */
        // These 2 are testing by the whole mesh.
        object = getColorMesh(1.0f, "bunny.obj");
        object.getTransform().setPosition(0.0f, 0.0f, -2.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, "bunny.obj");
        object.getTransform().setPosition(3.0f, 3.0f, -2.0f);
        attachDefaultEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        object.getRenderData().setCullTest(false);
        mObjects.add(object);

        // These 2 are testing by the bounding box of the mesh.
        object = getColorMesh(2.0f, "bunny.obj");
        object.getTransform().setPosition(-5.0f, 0.0f, -2.0f);
        attachBoundingBoxEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, "bunny.obj");
        object.getTransform().setPosition(0.0f, -5.0f, -2.0f);
        attachBoundingBoxEyePointee(object);
        mGVRContext.getMainScene().addSceneObject(object);
        mObjects.add(object);
    }

    @Override
    public void onStep() {
        for (GVRSceneObject object : mObjects) {
            object.getRenderData()
                    .getMaterial()
                    .setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R,
                            UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                            UNPICKED_COLOR_A);
        }
        for (GVREyePointeeHolder eph : GVRPicker.pickScene(mGVRContext
                .getMainScene())) {
            for (GVRSceneObject object : mObjects) {
                if (eph.getOwnerObject().equals(object)) {
                    object.getRenderData()
                            .getMaterial()
                            .setVec4(ColorShader.COLOR_KEY, PICKED_COLOR_R,
                                    PICKED_COLOR_G, PICKED_COLOR_B,
                                    PICKED_COLOR_A);
                    break;
                }
            }
        }
    }

    private GVRSceneObject getColorBoard(float width, float height) {
        GVRMaterial material = new GVRMaterial(mGVRContext,
                mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(mGVRContext, width, height);
        board.getRenderData().setMaterial(material);

        return board;
    }

    private GVRSceneObject getColorMesh(float scale, String fileName) {
        GVRMaterial material = new GVRMaterial(mGVRContext,
                mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRMesh mesh = mGVRContext.loadMesh(fileName);
        GVRSceneObject meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);

        return meshObject;
    }

    private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mGVRContext,
                sceneObject.getRenderData().getMesh());
        eyePointeeHolder.addPointee(eyePointee);
        sceneObject.attachEyePointeeHolder(eyePointeeHolder);
    }

    private void attachBoundingBoxEyePointee(GVRSceneObject sceneObject) {
        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mGVRContext,
                sceneObject.getRenderData().getMesh().getBoundingBox());
        eyePointeeHolder.addPointee(eyePointee);
        sceneObject.attachEyePointeeHolder(eyePointeeHolder);
    }
}
