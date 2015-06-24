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

package org.gearvrf.opacityanigallery;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;

import android.media.MediaPlayer;

public class GalleryViewManager extends GVRScript {

    private static final float ANIMATION_DURATION = 0.3f;
    private static final float SELECTED_SCALE = 2.0f;

    private GVRContext mGVRContext = null;
    private List<GVRSceneObject> mBoards = new ArrayList<GVRSceneObject>();
    private GVRSceneObject mBoardParent;
    private int mSelected = 0;
    private GVRAnimationEngine mAnimationEngine;

    private static final int LOOK_UP = 1;
    private static final int LOOK_FRONT = 0;
    private static final int LOOK_DOWN = -1;
    private static final float LOOK_AT_THRESHOLD = 0.2f;
    private int mLookAtMode = LOOK_FRONT;
    private GVRAnimation mRotationAnimation;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        mAnimationEngine = gvrContext.getAnimationEngine();

        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        mainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

        GVRMesh sphereMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.raw.sphere_mesh));

        GVRSceneObject leftScreen = new GVRSceneObject(mGVRContext, sphereMesh,
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.left_screen_png)));
        leftScreen.getTransform().setScale(10.0f, 10.0f, 10.0f);
        leftScreen.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
        GVRSceneObject rightScreen = new GVRSceneObject(mGVRContext,
                sphereMesh, mGVRContext.loadTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.right_screen_png)));
        rightScreen.getTransform().setScale(10.0f, 10.0f, 10.0f);
        rightScreen.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        mainScene.addSceneObject(leftScreen);
        mainScene.addSceneObject(rightScreen);

        List<GVRTexture> numberTextures = new ArrayList<GVRTexture>();
        int[] resourceIds = new int[] { R.drawable.photo_1_jpg,
                R.drawable.photo_2_jpg, R.drawable.photo_3_jpg,
                R.drawable.photo_4_jpg, R.drawable.photo_5_jpg,
                R.drawable.photo_6_jpg, R.drawable.photo_7_jpg,
                R.drawable.photo_8_jpg, R.drawable.photo_9_jpg };
        for (int id : resourceIds) {
            numberTextures.add(mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, id)));
        }

        for (int i = 0, size = numberTextures.size(); i < size; ++i) {
            GVRSceneObject number = new GVRSceneObject(mGVRContext, 2.0f, 1.0f,
                    numberTextures.get(i));
            number.getRenderData().getMaterial().setOpacity(0.0f);
            number.getTransform().setPosition(0.0f, 0.0f, -5.0f);
            float degree = 360.0f * i / (size + 1);
            number.getTransform().rotateByAxisWithPivot(degree, 0.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 0.0f);
            mBoards.add(number);
        }

        MediaPlayer mediaPlayer = MediaPlayer.create(mGVRContext.getContext(),
                R.drawable.tron);
        // mediaPlayer.start();
        GVRVideoSceneObject video = new GVRVideoSceneObject(mGVRContext, 2.0f,
                1.0f, mediaPlayer, GVRVideoType.MONO);
        video.setName("video");
        video.getRenderData().getMaterial().setOpacity(0.0f);
        video.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        float degree = 360.0f * (numberTextures.size())
                / (numberTextures.size() + 1);
        video.getTransform().rotateByAxisWithPivot(degree, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        mBoards.add(video);

        mBoardParent = new GVRSceneObject(mGVRContext);

        for (GVRSceneObject board : mBoards) {
            mBoardParent.addChildObject(board);
        }

        mainScene.addSceneObject(mBoardParent);

        mBoardParent.getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 0.0f, 0.0f);

        mBoards.get(mSelected).getTransform()
                .setScale(SELECTED_SCALE, SELECTED_SCALE, 0.0f);

        CustomPostEffectShaderManager shaderManager = new CustomPostEffectShaderManager(
                mGVRContext);
        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext,
                shaderManager.getShaderId());
        postEffect.setVec3("ratio_r", 0.393f, 0.769f, 0.189f);
        postEffect.setVec3("ratio_g", 0.349f, 0.686f, 0.168f);
        postEffect.setVec3("ratio_b", 0.272f, 0.534f, 0.131f);
        mainScene.getMainCameraRig().getLeftCamera().addPostEffect(postEffect);
        mainScene.getMainCameraRig().getRightCamera().addPostEffect(postEffect);

    }

    @Override
    public void onStep() {
        float lookAtY = mGVRContext.getMainScene().getMainCameraRig()
                .getLookAt()[1];

        if (mRotationAnimation != null && mRotationAnimation.isFinished()) {
            mRotationAnimation = null;
        }

        if (mRotationAnimation == null) {
            if (mLookAtMode == LOOK_FRONT) {
                if (lookAtY > LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_UP;
                    rotateCounterClockwise();
                } else if (lookAtY < -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_DOWN;
                    rotateClockwise();
                }
            }
            if (mLookAtMode == LOOK_UP) {
                if (lookAtY < -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_DOWN;
                    rotateClockwise();
                } else if (lookAtY < LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_FRONT;
                }
            }
            if (mLookAtMode == LOOK_DOWN) {
                if (lookAtY > LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_UP;
                    rotateCounterClockwise();
                } else if (lookAtY > -LOOK_AT_THRESHOLD) {
                    mLookAtMode = LOOK_FRONT;
                }
            }
        }
    }

    private void rotateCounterClockwise() {
        mRotationAnimation = new GVRRotationByAxisWithPivotAnimation(
                mBoardParent, ANIMATION_DURATION, 360.0f / mBoards.size(),
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f).start(mAnimationEngine);

        new GVRScaleAnimation(mBoards.get(mSelected), ANIMATION_DURATION,
                1.0f / SELECTED_SCALE, 1.0f / SELECTED_SCALE, 1.0f)
                .start(mAnimationEngine);

        new GVROpacityAnimation(mBoards.get(mSelected), ANIMATION_DURATION, 0f)
                .start(mAnimationEngine);

        if (mBoards.get(mSelected) instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards
                    .get(mSelected);
            video.getMediaPlayer().stop();
        }

        if (--mSelected < 0) {
            mSelected += mBoards.size();
        }

        new GVRScaleAnimation(mBoards.get(mSelected), ANIMATION_DURATION,
                SELECTED_SCALE, SELECTED_SCALE, 1.0f).start(mAnimationEngine);

        new GVROpacityAnimation(mBoards.get(mSelected), ANIMATION_DURATION, 1f)
                .start(mAnimationEngine);

        if (mBoards.get(mSelected) instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards
                    .get(mSelected);
            video.getMediaPlayer().start();
        }
    }

    private void rotateClockwise() {
        mRotationAnimation = new GVRRotationByAxisWithPivotAnimation(
                mBoardParent, ANIMATION_DURATION, -360.0f / mBoards.size(),
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f).start(mAnimationEngine);

        new GVRScaleAnimation(mBoards.get(mSelected), ANIMATION_DURATION,
                1.0f / SELECTED_SCALE, 1.0f / SELECTED_SCALE, 1.0f)
                .start(mAnimationEngine);

        new GVROpacityAnimation(mBoards.get(mSelected), ANIMATION_DURATION, 0f)
                .start(mAnimationEngine);

        if (mBoards.get(mSelected) instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards
                    .get(mSelected);
            video.getMediaPlayer().stop();
        }

        if (++mSelected >= mBoards.size()) {
            mSelected -= mBoards.size();
        }

        new GVRScaleAnimation(mBoards.get(mSelected), ANIMATION_DURATION,
                SELECTED_SCALE, SELECTED_SCALE, 1.0f).start(mAnimationEngine);

        new GVROpacityAnimation(mBoards.get(mSelected), ANIMATION_DURATION, 1f)
                .start(mAnimationEngine);

        if (mBoards.get(mSelected) instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) mBoards
                    .get(mSelected);
            video.getMediaPlayer().start();
        }
    }

}
