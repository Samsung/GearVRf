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

package org.gearvrf.sample_skeletalanimation;

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
import org.gearvrf.skinning.GVRSkeleton;
import org.gearvrf.utility.Log;

import android.graphics.Color;

public class SkeletalAnimationViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log
    .tag(SkeletalAnimationViewManager.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    GVRSkeleton skeleton;
    int step = 0;

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

        // Work in progress
        if (true) {
            // Model with texture
            GVRSceneObject astroBoyModel = gvrContext.loadJassimpModel("astro_boy.dae");

            // Start animation
            animateAllBones(gvrContext, astroBoyModel);

            // Model with color
            GVRSceneObject benchModel = gvrContext.loadJassimpModel("bench.dae");

            ModelPosition astroBoyModelPosition = new ModelPosition();

            astroBoyModelPosition.setPosition(0.0f, -0.4f, -0.5f);

            astroBoyModel.getTransform().setScale(3, 3, 3);

            astroBoyModel.getTransform().setPosition(astroBoyModelPosition.x,
                                                     astroBoyModelPosition.y, astroBoyModelPosition.z);
            //astroBoyModel.getTransform().setRotationByAxis(180.0f, 0.0f, 1.0f, 0.0f);

            ModelPosition benchModelPosition = new ModelPosition();

            benchModelPosition.setPosition(0.0f, -4.0f, -30.0f);

            benchModel.getTransform().setPosition(benchModelPosition.x,
                                                  benchModelPosition.y, benchModelPosition.z);
            benchModel.getTransform()
            .setRotationByAxis(180.0f, 0.0f, 1.0f, 0.0f);

            mMainScene.addSceneObject(astroBoyModel);
            mMainScene.addSceneObject(benchModel);

//             rotateModel(astroBoyModel, 10f, astroBoyModelPosition);
        } else {
            // Model with bones
            try {
                // Need assimp that supports Collada 1.5
                GVRSceneObject juliaModel = gvrContext.loadJassimpModel("Peggy.fbx");

                // Talia
                juliaModel.getTransform().setScale(.4f, .4f, .4f);
                juliaModel.getTransform().setPosition(0, -50f, -50f);
                mMainScene.addSceneObject(juliaModel);
                
                // Start animation
                animateAllBones(gvrContext, juliaModel);
            } catch (IOException e) {
                Log.v(TAG, "exception %s", e);
            }
        }
    }

    private void animateAllBones(GVRContext gvrContext, GVRSceneObject sceneRoot) {
        List<GVRSceneObject> boyNodes = sceneRoot.findAllChildrenByName("boy");
        GVRSceneObject boy = boyNodes.get(0);
        GVRSceneObject boy1 = boyNodes.get(1);
        GVRSceneObject boy2 = boyNodes.get(2);
        GVRSceneObject boy3 = boyNodes.get(3);
        GVRSceneObject boy4 = boyNodes.get(4);

        skeleton = new GVRSkeleton(gvrContext, sceneRoot, boy);
    }

    @Override
    public void onStep() {
        step++;

        // Simple animation: animate all bones along y-axis
        float angleDegrees = (float) (20f * Math.sin(Math.PI * 2.0f * step / 60.f));
        skeleton.animate(null, (float) angleDegrees);

        // TODO: play an imported animation
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
