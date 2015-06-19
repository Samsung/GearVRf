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
import org.gearvrf.GVRTransform;
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

        GVRSceneObject model = gvrContext.getAssimpModel("astro_boy.dae");

        model.getTransform().setPosition(0.0f, -5.0f, 0.0f);
        model.getTransform().setRotationByAxis(-180.0f, 0.0f, 0.0f, 1.0f);

        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getOwnerObject().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

        GVRSceneObject cameraRevolutionObject = new GVRSceneObject(gvrContext);
        cameraRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        cameraRevolutionObject.addChildObject(mMainScene.getMainCameraRig()
                .getOwnerObject());

        mMainScene.addSceneObject(model);

        counterClockwise(cameraRevolutionObject, 60f);

        clockwise(
                mMainScene.getMainCameraRig().getOwnerObject().getTransform(),
                120f);
    }

    @Override
    public void onStep() {
    }

    private List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    private void setup(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
    }

    private void counterClockwise(GVRSceneObject object, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRTransform transform, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

}
