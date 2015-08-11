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

package org.gearvrf.jassimpmodelloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.utility.Log;

import android.graphics.Color;

public class JassimpModelLoaderViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log
            .tag(JassimpModelLoaderViewManager.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        mAnimationEngine = gvrContext.getAnimationEngine();

        mMainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                for (GVRAnimation animation : mAnimations) {
                    animation.start(mAnimationEngine);
                }
                mAnimations = null;
            }
        });

        // Apply frustum culling
        mMainScene.setFrustumCulling(true);

        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        // Model with texture
        GVRSceneObject astroBoyModel = gvrContext
                .getAssimpModel("astro_boy.dae");

        // Model with color
        GVRSceneObject benchModel = gvrContext.getAssimpModel("bench.dae");

        ModelPosition astroBoyModelPosition = new ModelPosition();

        astroBoyModelPosition.setPosition(0.0f, -4.0f, -5.0f);

        astroBoyModel.getTransform().setPosition(astroBoyModelPosition.x,
                astroBoyModelPosition.y, astroBoyModelPosition.z);
        astroBoyModel.getTransform()
                .setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        ModelPosition benchModelPosition = new ModelPosition();

        benchModelPosition.setPosition(0.0f, -4.0f, -30.0f);

        benchModel.getTransform().setPosition(benchModelPosition.x,
                benchModelPosition.y, benchModelPosition.z);
        benchModel.getTransform()
        .setRotationByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        mMainScene.addSceneObject(astroBoyModel);
        mMainScene.addSceneObject(benchModel);

        rotateModel(astroBoyModel, 10f, astroBoyModelPosition);
    }

    @Override
    public void onStep() {
    }

    void onTap() {
        // toggle whether stats are displayed.
        boolean statsEnabled = mMainScene.getStatsEnabled();
        mMainScene.setStatsEnabled(!statsEnabled);
    }

    private List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    private void setup(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
    }

    private void rotateModel(GVRSceneObject model, float duration,
            ModelPosition modelPosition) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                model, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                modelPosition.x, modelPosition.y, modelPosition.z));
    }
}

class ModelPosition {
    float x;
    float y;
    float z;

    void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
