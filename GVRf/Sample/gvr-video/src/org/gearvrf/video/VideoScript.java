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

package org.gearvrf.video;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.gearvrf.*;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.util.FPSCounter;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

public class VideoScript extends GVRScript {

    private static final String TAG = "VideoScript";

    private GVRContext mGVRContext = null;
    private RadiosityShader mRadiosityShader = null;
    private AdditiveShader mAdditiveShader = null;
    private ScreenShader mScreenShader = null;
    private MediaPlayer mMediaPlayer = null;
    private SurfaceTexture mVideoSurfaceTexture = null;

    private int mCinemaNum = 2;
    private GVRSceneObject[] mCinema = new GVRSceneObject[mCinemaNum];

    private GVRSceneObject mLeftSceneObject = null;
    private GVRSceneObject mRightSceneObject = null;
    private GVRSceneObject mScreenL = null;
    private GVRSceneObject mScreenR = null;

    private GVRSceneObject mOculusSceneObject1 = null;
    private GVRSceneObject mOculusSceneObject2 = null;
    private GVRSceneObject mOculusScreenL = null;
    private GVRSceneObject mOculusScreenR = null;

    private GVRSceneObject mHeadTracker = null;
    private GVRSceneObject mPlayPauseButton = null;
    private GVRSceneObject mFrontButton = null;
    private GVRSceneObject mBackButton = null;
    private GVRSceneObject mImaxButton = null;
    private GVRSceneObject mSelectButton = null;
    private GVRSceneObject mButtonBoard = null;
    private Seekbar mSeekbar = null;
    private GVRTexture mInactivePause = null;
    private GVRTexture mActivePause = null;
    private GVRTexture mInactivePlay = null;
    private GVRTexture mActivePlay = null;
    private GVRTexture mInactiveBack = null;
    private GVRTexture mActiveBack = null;
    private GVRTexture mInactiveFront = null;
    private GVRTexture mActiveFront = null;
    private GVRTexture mInactiveImax = null;
    private GVRTexture mActiveImax = null;
    private GVRTexture mInactiveSelect = null;
    private GVRTexture mActiveSelect = null;

    private GVRSceneObject mGlobalMenuRoot = null;
    private GVRSceneObject mGlobalReorient = null;
    private GVRSceneObject mGlobalPassthrough = null;
    private GVRSceneObject mGlobalHome = null;
    private GVRSceneObject mGlobalTime = null;
    private GVRSceneObject mGlobalBattery = null;
    private GVRSceneObject mGlobalBatteryInside = null;
    private GVRTexture mInactiveReorient = null;
    private GVRTexture mActiveReorient = null;
    private GVRTexture mInactivePassthrough = null;
    private GVRTexture mActivePassThrough = null;
    private GVRTexture mInactiveHome = null;
    private GVRTexture mActiveHome = null;

    /*
     * Camera related
     */
    private SurfaceTexture mCameraSurfaceTexture = null;
    private GVRSceneObject mPassThroughObject = null;
    private Camera mCamera = null;

    private boolean mIsUIHidden = true;
    private boolean mIsGlobalMenuOn = false;
    private boolean mIsButtonDown = false;
    private boolean mIsLongButtonPressed = false;
    private boolean mIsTouched = false;
    private boolean mIsSingleTapped = false;
    private int mBatteryLevel = 100;
    private String mDateText = "";

    private boolean mIsIMAX = false;
    private boolean mIsPassThrough = false;

    private float mTransitionWeight = 0.0f;
    private float mTransitionTarget = 0.0f;

    private int mCurrentCinema = 0;
    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    private GVRActivity mActivity;

    VideoScript(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                mMediaPlayer.start();
            }
        });

        mRadiosityShader = new RadiosityShader(gvrContext);
        mAdditiveShader = new AdditiveShader(gvrContext);
        mScreenShader = new ScreenShader(gvrContext);

        /*
         * Media player with a linked texture.
         */
        GVRTexture screenTexture = new GVRExternalTexture(gvrContext);
        mVideoSurfaceTexture = new SurfaceTexture(screenTexture.getId());

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);

        AssetFileDescriptor afd;
        try {
            afd = gvrContext.getContext().getAssets().openFd("tron.mp4");
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            mMediaPlayer.setSurface(new Surface(mVideoSurfaceTexture));

            /*
             * Head tracker
             */
            GVRTexture headTrackerTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "head-tracker.png"));
            mHeadTracker = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.5f, 0.5f), headTrackerTexture);
            mHeadTracker.getTransform().setPositionZ(-9.0f);
            mHeadTracker.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.OVERLAY);
            mHeadTracker.getRenderData().setDepthTest(false);
            mHeadTracker.getRenderData().setRenderingOrder(100000);
            mainScene.getMainCameraRig().addChildObject(mHeadTracker);

            /*
             * FXGear Background
             */
            mCinema[0] = new GVRSceneObject(mGVRContext);

            GVRMesh backgroundMesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background.obj"));
            GVRTexture leftBackgroundLightOffTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_left_light_off.jpg"));
            GVRTexture leftBackgroundLightOnTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_left_light_on.jpg"));
            GVRTexture rightBackgroundLightOffTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_right_light_off.jpg"));
            GVRTexture rightBackgroundLightOnTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_right_light_on.jpg"));
            mLeftSceneObject = new GVRSceneObject(gvrContext, backgroundMesh,
                    leftBackgroundLightOffTexture);
            mLeftSceneObject.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left);
            mRightSceneObject = new GVRSceneObject(gvrContext, backgroundMesh,
                    rightBackgroundLightOffTexture);
            mRightSceneObject.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Right);
            mLeftSceneObject.getTransform().setPosition(-0.031f, 0.0f, 0.0f);
            mRightSceneObject.getTransform().setPosition(0.031f, 0.0f, 0.0f);
            mLeftSceneObject.getRenderData().setCullTest(false);
            mRightSceneObject.getRenderData().setCullTest(false);

            mCinema[0].addChildObject(mLeftSceneObject);
            mCinema[0].addChildObject(mRightSceneObject);

            /*
             * Radiosity settings
             */
            mLeftSceneObject.getRenderData().getMaterial()
                    .setShaderType(mRadiosityShader.getShaderId());
            mLeftSceneObject
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_OFF_KEY,
                            leftBackgroundLightOffTexture);
            mLeftSceneObject
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_ON_KEY,
                            leftBackgroundLightOnTexture);
            mLeftSceneObject.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            mRightSceneObject.getRenderData().getMaterial()
                    .setShaderType(mRadiosityShader.getShaderId());
            mRightSceneObject
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_OFF_KEY,
                            rightBackgroundLightOffTexture);
            mRightSceneObject
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_ON_KEY,
                            rightBackgroundLightOnTexture);
            mRightSceneObject.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            /*
             * Uv setting for radiosity
             */

            GVRMesh radiosity_mesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater1/radiosity.obj"));
            backgroundMesh.setNormals(radiosity_mesh.getVertices());

            /*
             * Screen
             */

            GVRMesh screenMesh = gvrContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "theater1/screen.obj"));
            GVRRenderData renderDataL = new GVRRenderData(gvrContext);
            GVRRenderData renderDataR = new GVRRenderData(gvrContext);
            GVRMaterial material = new GVRMaterial(gvrContext,
                    mScreenShader.getShaderId());
            material.setTexture(ScreenShader.SCREEN_KEY, screenTexture);
            renderDataL.setMesh(screenMesh);
            renderDataL.setMaterial(material);
            renderDataR.setMesh(screenMesh);
            renderDataR.setMaterial(material);

            mScreenL = new GVRSceneObject(gvrContext);
            mScreenR = new GVRSceneObject(gvrContext);
            mScreenL.attachRenderData(renderDataL);
            mScreenR.attachRenderData(renderDataR);

            mScreenL.getTransform().setPosition(-0.031f, 0.0f, 0.0f);
            mScreenR.getTransform().setPosition(0.031f, 0.0f, 0.0f);
            mScreenL.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
            mScreenR.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

            mCinema[0].addChildObject(mScreenL);
            mCinema[0].addChildObject(mScreenR);

            mainScene.addSceneObject(mCinema[0]);

            /*
             * Oculus Background
             */
            mCinema[1] = new GVRSceneObject(mGVRContext);

            GVRMesh backgroundMesh1 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema.obj"));
            GVRMesh backgroundMesh2 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/additive.obj"));
            GVRTexture AdditiveTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/additive.png"));
            GVRTexture BackgroundLightOffTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema1.png"));
            GVRTexture BackgroundLightOnTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema2.png"));
            mOculusSceneObject1 = new GVRSceneObject(gvrContext,
                    backgroundMesh1, BackgroundLightOnTexture);
            mOculusSceneObject1.getRenderData().setCullTest(false);
            mOculusSceneObject2 = new GVRSceneObject(gvrContext,
                    backgroundMesh2, AdditiveTexture);
            mOculusSceneObject2.getRenderData().setCullTest(false);
            mOculusSceneObject2.getRenderData().setRenderingOrder(2500);

            mCinema[1].addChildObject(mOculusSceneObject1);
            mCinema[1].addChildObject(mOculusSceneObject2);

            /*
             * Radiosity settings
             */
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setShaderType(mRadiosityShader.getShaderId());
            mOculusSceneObject1
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_OFF_KEY,
                            BackgroundLightOnTexture);
            mOculusSceneObject1
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_ON_KEY,
                            BackgroundLightOffTexture);
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            mOculusSceneObject2.getRenderData().getMaterial()
                    .setShaderType(mAdditiveShader.getShaderId());
            mOculusSceneObject2.getRenderData().getMaterial()
                    .setTexture(AdditiveShader.TEXTURE_KEY, AdditiveTexture);

            /*
             * Uv setting for radiosity
             */

            GVRMesh oculus_radiosity_mesh1 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/radiosity1.obj"));
            GVRMesh oculus_radiosity_mesh2 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/radiosity2.obj"));
            backgroundMesh1.setNormals(oculus_radiosity_mesh1.getVertices());
            backgroundMesh2.setNormals(oculus_radiosity_mesh2.getVertices());

            /*
             * Screen
             */

            GVRMesh oculus_screenMesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/screen.obj"));
            GVRRenderData oculus_renderDataL = new GVRRenderData(gvrContext);
            GVRRenderData oculus_renderDataR = new GVRRenderData(gvrContext);
            GVRMaterial oculus_material = new GVRMaterial(gvrContext,
                    mScreenShader.getShaderId());
            oculus_material.setTexture(ScreenShader.SCREEN_KEY, screenTexture);
            oculus_renderDataL.setMesh(oculus_screenMesh);
            oculus_renderDataL.setMaterial(oculus_material);
            oculus_renderDataR.setMesh(oculus_screenMesh);
            oculus_renderDataR.setMaterial(oculus_material);

            mOculusScreenL = new GVRSceneObject(gvrContext);
            mOculusScreenR = new GVRSceneObject(gvrContext);
            mOculusScreenL.attachRenderData(oculus_renderDataL);
            mOculusScreenR.attachRenderData(oculus_renderDataR);
            mOculusScreenL.getRenderData().setCullTest(false);
            mOculusScreenR.getRenderData().setCullTest(false);

            mOculusScreenL.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
            mOculusScreenR.getRenderData()
                    .setRenderMask(GVRRenderMaskBit.Right);

            mCinema[1].addChildObject(mOculusScreenL);
            mCinema[1].addChildObject(mOculusScreenR);

            float pivot_x = -3.353f;
            float pivot_y = 0.401f;
            float pivot_z = -0.000003f;

            mCinema[1].getTransform().setPosition(-pivot_x, -pivot_y, -pivot_z);
            mCinema[1].getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 0.0f);

            mainScene.addSceneObject(mCinema[1]);
            for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                mCinema[1].getChildByIndex(i).getRenderData().setRenderMask(0);

            /*
             * Buttons
             */
            mInactivePause = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/pause-inactive.png"));
            mActivePause = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/pause-active.png"));
            mInactivePlay = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/play-inactive.png"));
            mActivePlay = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/play-active.png"));
            mPlayPauseButton = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.7f, 0.7f), mInactivePause);
            mPlayPauseButton.getRenderData().getMaterial()
                    .setTexture("active_play", mActivePlay);
            mPlayPauseButton.getRenderData().getMaterial()
                    .setTexture("inactive_play", mInactivePlay);
            mPlayPauseButton.getRenderData().getMaterial()
                    .setTexture("active_pause", mActivePause);
            mPlayPauseButton.getRenderData().getMaterial()
                    .setTexture("inactive_pause", mInactivePause);
            mPlayPauseButton.getTransform().setPosition(0.0f, -0.8f, -8.0f);
            mPlayPauseButton.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 1);
            mPlayPauseButton.getRenderData().setOffset(true);
            mPlayPauseButton.getRenderData().setOffsetFactor(-1.0f);
            mPlayPauseButton.getRenderData().setOffsetUnits(-1.0f);
            GVREyePointeeHolder playPauseHolder = new GVREyePointeeHolder(
                    gvrContext);
            playPauseHolder.addPointee(new GVRMeshEyePointee(gvrContext,
                    mPlayPauseButton.getRenderData().getMesh()));
            mPlayPauseButton.attachEyePointeeHolder(playPauseHolder);
            mainScene.addSceneObject(mPlayPauseButton);

            mInactiveFront = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/front-inactive.png"));
            mActiveFront = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/front-active.png"));
            mFrontButton = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.7f, 0.7f), mInactiveFront);
            mFrontButton.getRenderData().getMaterial()
                    .setTexture("active_front", mActiveFront);
            mFrontButton.getRenderData().getMaterial()
                    .setTexture("inactive_front", mInactiveFront);
            mFrontButton.getTransform().setPosition(1.2f, -0.8f, -8.0f);
            mFrontButton.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 1);
            mFrontButton.getRenderData().setOffset(true);
            mFrontButton.getRenderData().setOffsetFactor(-1.0f);
            mFrontButton.getRenderData().setOffsetUnits(-1.0f);
            GVREyePointeeHolder frontHolder = new GVREyePointeeHolder(
                    gvrContext);
            frontHolder.addPointee(new GVRMeshEyePointee(gvrContext,
                    mFrontButton.getRenderData().getMesh()));
            mFrontButton.attachEyePointeeHolder(frontHolder);
            mainScene.addSceneObject(mFrontButton);

            mInactiveBack = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/back-inactive.png"));
            mActiveBack = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/back-active.png"));
            mBackButton = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                    0.7f, 0.7f), mInactiveBack);
            mBackButton.getRenderData().getMaterial()
                    .setTexture("active_back", mActiveBack);
            mBackButton.getRenderData().getMaterial()
                    .setTexture("inactive_back", mInactiveBack);
            mBackButton.getTransform().setPosition(-1.2f, -0.8f, -8.0f);
            mBackButton.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 1);
            mBackButton.getRenderData().setOffset(true);
            mBackButton.getRenderData().setOffsetFactor(-1.0f);
            mBackButton.getRenderData().setOffsetUnits(-1.0f);
            GVREyePointeeHolder backHolder = new GVREyePointeeHolder(gvrContext);
            backHolder.addPointee(new GVRMeshEyePointee(gvrContext, mBackButton
                    .getRenderData().getMesh()));
            mBackButton.attachEyePointeeHolder(backHolder);
            mainScene.addSceneObject(mBackButton);

            mInactiveImax = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/imaxoutline.png"));
            mActiveImax = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/imaxselect.png"));
            mImaxButton = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                    0.9f, 0.35f), mInactiveImax);
            mImaxButton.getRenderData().getMaterial()
                    .setTexture("active_imax", mActiveImax);
            mImaxButton.getRenderData().getMaterial()
                    .setTexture("inactive_imax", mInactiveImax);
            mImaxButton.getTransform().setPosition(2.5f, -0.9f, -7.5f);
            mImaxButton.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 1);
            mImaxButton.getRenderData().setOffset(true);
            mImaxButton.getRenderData().setOffsetFactor(-1.0f);
            mImaxButton.getRenderData().setOffsetUnits(-1.0f);
            GVREyePointeeHolder imaxHolder = new GVREyePointeeHolder(gvrContext);
            imaxHolder.addPointee(new GVRMeshEyePointee(gvrContext, mImaxButton
                    .getRenderData().getMesh()));
            mImaxButton.attachEyePointeeHolder(imaxHolder);
            mainScene.addSceneObject(mImaxButton);

            mInactiveSelect = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/selectionoutline.png"));
            mActiveSelect = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/selectionselect.png"));
            mSelectButton = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.9f, 0.35f), mInactiveSelect);
            mSelectButton.getRenderData().getMaterial()
                    .setTexture("active_select", mActiveSelect);
            mSelectButton.getRenderData().getMaterial()
                    .setTexture("inactive_select", mInactiveSelect);
            mSelectButton.getTransform().setPosition(-2.5f, -0.9f, -7.5f);
            mSelectButton.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 1);
            mSelectButton.getRenderData().setOffset(true);
            mSelectButton.getRenderData().setOffsetFactor(-1.0f);
            mSelectButton.getRenderData().setOffsetUnits(-1.0f);
            GVREyePointeeHolder selectHolder = new GVREyePointeeHolder(
                    gvrContext);
            selectHolder.addPointee(new GVRMeshEyePointee(gvrContext,
                    mSelectButton.getRenderData().getMesh()));
            mSelectButton.attachEyePointeeHolder(selectHolder);
            mainScene.addSceneObject(mSelectButton);

            mButtonBoard = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(8.2f, 1.35f),
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "button/button-board.png")));
            mButtonBoard.getTransform().setPosition(-0.1f, -0.6f, -8.0f);
            mButtonBoard.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT);
            mainScene.addSceneObject(mButtonBoard);

            /*
             * Seek bar
             */
            mSeekbar = new Seekbar(gvrContext);
            mainScene.addSceneObject(mSeekbar);

            /*
             * Global menus
             */
            mGlobalMenuRoot = new GVRSceneObject(gvrContext);
            mainScene.addSceneObject(mGlobalMenuRoot);

            mInactiveReorient = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "global/reorient-inactive.png"));
            mActiveReorient = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "global/reorient-active.png"));
            mGlobalReorient = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(3.775f, 1.875f), mInactiveReorient);
            mGlobalReorient.getRenderData().getMaterial()
                    .setTexture("active_reorient", mActiveReorient);
            mGlobalReorient.getRenderData().getMaterial()
                    .setTexture("inactive_reorient", mInactiveReorient);
            mGlobalReorient.getTransform().setPosition(0.0f, 2.0f, -15.0f);
            mGlobalReorient.getRenderData().setDepthTest(false);
            mGlobalReorient.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5000);
            mGlobalReorient.getRenderData().setRenderMask(0);
            GVREyePointeeHolder reorientHolder = new GVREyePointeeHolder(
                    gvrContext);
            reorientHolder.addPointee(new GVRMeshEyePointee(gvrContext,
                    mGlobalReorient.getRenderData().getMesh()));
            mGlobalReorient.attachEyePointeeHolder(reorientHolder);
            reorientHolder.setEnable(false);
            mGlobalMenuRoot.addChildObject(mGlobalReorient);

            mInactivePassthrough = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "global/passthrough-inactive.png"));
            mActivePassThrough = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "global/passthrough-active.png"));
            mGlobalPassthrough = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(3.775f, 1.875f), mInactivePassthrough);
            mGlobalPassthrough.getRenderData().getMaterial()
                    .setTexture("active_passthrough", mActivePassThrough);
            mGlobalPassthrough.getRenderData().getMaterial()
                    .setTexture("inactive_passthrough", mInactivePassthrough);
            mGlobalPassthrough.getTransform().setPosition(0.0f, 0.0f, -15.0f);
            mGlobalPassthrough.getRenderData().setDepthTest(false);
            mGlobalPassthrough.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5000);
            mGlobalPassthrough.getRenderData().setRenderMask(0);
            GVREyePointeeHolder passthroughHolder = new GVREyePointeeHolder(
                    gvrContext);
            passthroughHolder.addPointee(new GVRMeshEyePointee(gvrContext,
                    mGlobalPassthrough.getRenderData().getMesh()));
            mGlobalPassthrough.attachEyePointeeHolder(passthroughHolder);
            passthroughHolder.setEnable(false);
            mGlobalMenuRoot.addChildObject(mGlobalPassthrough);

            mInactiveHome = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "global/home-inactive.png"));
            mActiveHome = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "global/home-active.png"));
            mGlobalHome = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                    3.775f, 1.875f), mInactiveHome);
            mGlobalHome.getRenderData().getMaterial()
                    .setTexture("active_home", mActiveHome);
            mGlobalHome.getRenderData().getMaterial()
                    .setTexture("inactive_home", mInactiveHome);
            mGlobalHome.getTransform().setPosition(0.0f, -2.0f, -15.0f);
            mGlobalHome.getRenderData().setDepthTest(false);
            mGlobalHome.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5000);
            mGlobalHome.getRenderData().setRenderMask(0);
            GVREyePointeeHolder homeHolder = new GVREyePointeeHolder(gvrContext);
            homeHolder.addPointee(new GVRMeshEyePointee(gvrContext, mGlobalHome
                    .getRenderData().getMesh()));
            mGlobalHome.attachEyePointeeHolder(homeHolder);
            homeHolder.setEnable(false);
            mGlobalMenuRoot.addChildObject(mGlobalHome);

            mGlobalTime = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                    4.0f, 0.8f), TextFactory.create(gvrContext, mDateText));
            mGlobalTime.getTransform().setPosition(-3.0f, -4.5f, -15.0f);
            mGlobalTime.getRenderData().setDepthTest(false);
            mGlobalTime.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5000);
            mGlobalTime.getRenderData().setRenderMask(0);
            mGlobalMenuRoot.addChildObject(mGlobalTime);

            mGlobalBattery = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(1.41f, 0.84f),
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "global/battery.png")));
            mGlobalBattery.getTransform().setPosition(3.0f, -4.5f, -15.0f);
            mGlobalBattery.getRenderData().setDepthTest(false);
            mGlobalBattery.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5000);
            mGlobalBattery.getRenderData().setRenderMask(0);
            mGlobalMenuRoot.addChildObject(mGlobalBattery);

            GVRSceneObject globalBatteryInsideRoot = new GVRSceneObject(
                    gvrContext);
            globalBatteryInsideRoot.getTransform().setPosition(3.0f - 0.075f,
                    -4.5f, -15.0f);
            globalBatteryInsideRoot.getTransform().setScale(1.02f, 0.60f, 1.0f);
            mGlobalMenuRoot.addChildObject(globalBatteryInsideRoot);

            Bitmap bitmap = Bitmap.createBitmap(new int[] { 0xff37e420 }, 1, 1,
                    Config.ARGB_8888);
            mGlobalBatteryInside = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(1.0f, 1.0f), new GVRBitmapTexture(
                            gvrContext, bitmap));
            mGlobalBatteryInside.getRenderData().setDepthTest(false);
            mGlobalBatteryInside.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 5001);
            mGlobalBatteryInside.getRenderData().setRenderMask(0);
            globalBatteryInsideRoot.addChildObject(mGlobalBatteryInside);

            /*
             * Passthrough
             */
            GVRExternalTexture passThroughTexture = new GVRExternalTexture(
                    gvrContext);
            mPassThroughObject = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(3.0f, 1.5f), passThroughTexture);
            mPassThroughObject.getRenderData().getMaterial()
                    .setShaderType(GVRShaderType.OES.ID);
            mPassThroughObject.getTransform().setPositionZ(-3.0f);
            mPassThroughObject.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT + 3000);
            mPassThroughObject.getRenderData().setRenderMask(0);

            mCameraSurfaceTexture = new SurfaceTexture(
                    passThroughTexture.getId());
            mainScene.getMainCameraRig().addChildObject(mPassThroughObject);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
        mVideoSurfaceTexture.updateTexImage();

        float step = 0.2f;

        mTransitionWeight += step * (mTransitionTarget - mTransitionWeight);
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);

        if (mIsPassThrough) {
            mCameraSurfaceTexture.updateTexImage();
            mMediaPlayer.pause();
            mFadeWeight = 0.0f;
            for (int i = 0; i < mCinema[0].getChildrenCount(); i++)
                mCinema[0].getChildByIndex(i).getRenderData().setRenderMask(0);
            for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                mCinema[1].getChildByIndex(i).getRenderData().setRenderMask(0);
            mIsUIHidden = true;
        } else {
            if (mCurrentCinema == 0) {
                for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                    mCinema[1].getChildByIndex(i).getRenderData()
                            .setRenderMask(0);
                for (int i = 0; i < mCinema[0].getChildrenCount(); i++)
                    mCinema[0]
                            .getChildByIndex(i)
                            .getRenderData()
                            .setRenderMask(
                                    GVRRenderMaskBit.Left
                                            | GVRRenderMaskBit.Right);

                mLeftSceneObject
                        .getRenderData()
                        .getMaterial()
                        .setFloat(RadiosityShader.WEIGHT_KEY, mTransitionWeight);
                mRightSceneObject
                        .getRenderData()
                        .getMaterial()
                        .setFloat(RadiosityShader.WEIGHT_KEY, mTransitionWeight);
                mLeftSceneObject.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
                mRightSceneObject.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
                mLeftSceneObject.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.LIGHT_KEY, 2.0f);
                mRightSceneObject.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.LIGHT_KEY, 2.0f);
            } else {
                for (int i = 0; i < mCinema[0].getChildrenCount(); i++)
                    mCinema[0].getChildByIndex(i).getRenderData()
                            .setRenderMask(0);
                for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                    mCinema[1]
                            .getChildByIndex(i)
                            .getRenderData()
                            .setRenderMask(
                                    GVRRenderMaskBit.Left
                                            | GVRRenderMaskBit.Right);

                mOculusSceneObject1
                        .getRenderData()
                        .getMaterial()
                        .setFloat(RadiosityShader.WEIGHT_KEY, mTransitionWeight);
                mOculusSceneObject1.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
                mOculusSceneObject1.getRenderData().getMaterial()
                        .setFloat(RadiosityShader.LIGHT_KEY, 1.0f);
                mOculusSceneObject2.getRenderData().getMaterial()
                        .setFloat(AdditiveShader.WEIGHT_KEY, mTransitionWeight);
                mOculusSceneObject2.getRenderData().getMaterial()
                        .setFloat(AdditiveShader.FADE_KEY, mFadeWeight);
            }
        }

        float scale = 1.0f + 1.0f * (mTransitionWeight - 1.0f);
        if (scale >= 1.0f) {
            mButtonBoard.getTransform().setScale(scale, scale, 1.0f);
            mButtonBoard.getTransform().setPosition(-0.1f,
                    -0.6f - 0.26f * scale, -8.0f);
            mScreenL.getTransform().setScale(scale, scale, 1.0f);
            mScreenR.getTransform().setScale(scale, scale, 1.0f);
            mLeftSceneObject.getTransform().setScale(scale, scale, 1.0f);
            mRightSceneObject.getTransform().setScale(scale, scale, 1.0f);
        }

        boolean isButtonDown = mIsButtonDown;
        boolean isLongButtonPressed = mIsLongButtonPressed;
        boolean isTouched = mIsTouched;
        boolean isSingleTapped = mIsSingleTapped;
        mIsButtonDown = false;
        mIsLongButtonPressed = false;
        mIsTouched = false;
        mIsSingleTapped = false;

        GVREyePointeeHolder[] pickedHolders = null;

        boolean isUIHiden = mIsUIHidden;
        boolean isAnythingPointed = false;

        if (!mIsUIHidden) {
            mPlayPauseButton.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mFrontButton.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mBackButton.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mImaxButton.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mSelectButton.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mButtonBoard.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mSeekbar.setRenderMask(GVRRenderMaskBit.Left
                    | GVRRenderMaskBit.Right);
            mPlayPauseButton.getEyePointeeHolder().setEnable(true);
            mFrontButton.getEyePointeeHolder().setEnable(true);
            mBackButton.getEyePointeeHolder().setEnable(true);
            mImaxButton.getEyePointeeHolder().setEnable(true);
            mSelectButton.getEyePointeeHolder().setEnable(true);

            if (pickedHolders == null) {
                pickedHolders = GVRPicker.pickScene(mGVRContext.getMainScene());
            }

            boolean playPausePointed = false;
            boolean frontPointed = false;
            boolean backPointed = false;
            boolean imaxPointed = false;
            boolean selectPointed = false;
            Float seekbarRatio = mSeekbar.getRatio(mGVRContext.getMainScene()
                    .getMainCameraRig().getLookAt());

            for (GVREyePointeeHolder holder : pickedHolders) {
                if (holder.equals(mPlayPauseButton.getEyePointeeHolder())) {
                    playPausePointed = true;
                } else if (holder.equals(mFrontButton.getEyePointeeHolder())) {
                    frontPointed = true;
                } else if (holder.equals(mBackButton.getEyePointeeHolder())) {
                    backPointed = true;
                } else if (holder.equals(mImaxButton.getEyePointeeHolder())) {
                    imaxPointed = true;
                } else if (holder.equals(mSelectButton.getEyePointeeHolder())) {
                    selectPointed = true;
                }
            }

            if (playPausePointed || frontPointed || backPointed || imaxPointed
                    || selectPointed || seekbarRatio != null) {
                isAnythingPointed = true;
            }

            if (playPausePointed) {
                if (isSingleTapped) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    } else {
                        mMediaPlayer.start();
                    }
                }
            }

            if (mMediaPlayer.isPlaying()) {
                if (playPausePointed) {
                    mPlayPauseButton.getRenderData().getMaterial()
                            .setMainTexture(mActivePlay);
                } else {
                    mPlayPauseButton.getRenderData().getMaterial()
                            .setMainTexture(mInactivePlay);
                }
            } else {
                if (playPausePointed) {
                    mPlayPauseButton.getRenderData().getMaterial()
                            .setMainTexture(mActivePause);
                } else {
                    mPlayPauseButton.getRenderData().getMaterial()
                            .setMainTexture(mInactivePause);
                }
            }

            if (frontPointed) {
                if (isSingleTapped) {
                    mMediaPlayer
                            .seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                }
                mFrontButton.getRenderData().getMaterial()
                        .setMainTexture(mActiveFront);
            } else {
                mFrontButton.getRenderData().getMaterial()
                        .setMainTexture(mInactiveFront);
            }

            if (backPointed) {
                if (isSingleTapped) {
                    mMediaPlayer
                            .seekTo(mMediaPlayer.getCurrentPosition() - 10000);

                }
                mBackButton.getRenderData().getMaterial()
                        .setMainTexture(mActiveBack);
            } else {
                mBackButton.getRenderData().getMaterial()
                        .setMainTexture(mInactiveBack);
            }

            if (imaxPointed) {
                if (isSingleTapped) {
                    if (!mIsIMAX) {
                        mIsIMAX = true;
                        mTransitionTarget = 2.0f;
                    } else {
                        mIsIMAX = false;
                        mTransitionTarget = 1.0f;
                    }
                }
                mImaxButton.getRenderData().getMaterial()
                        .setMainTexture(mActiveImax);
            } else {
                mImaxButton.getRenderData().getMaterial()
                        .setMainTexture(mInactiveImax);
            }

            if (selectPointed) {
                if (isSingleTapped) {
                    mFadeWeight = 0.0f;
                    mCurrentCinema++;
                    if (mCurrentCinema >= mCinemaNum)
                        mCurrentCinema = 0;
                }
                mSelectButton.getRenderData().getMaterial()
                        .setMainTexture(mActiveSelect);
            } else {
                mSelectButton.getRenderData().getMaterial()
                        .setMainTexture(mInactiveSelect);
            }

            if (seekbarRatio != null) {
                mSeekbar.glow();
            } else {
                mSeekbar.unglow();
            }

            if (isTouched && seekbarRatio != null) {
                int current = (int) (mMediaPlayer.getDuration() * seekbarRatio);
                mMediaPlayer.seekTo(current);
                mSeekbar.setTime(mGVRContext, current,
                        mMediaPlayer.getDuration());
            } else {
                mSeekbar.setTime(mGVRContext,
                        mMediaPlayer.getCurrentPosition(),
                        mMediaPlayer.getDuration());
            }
        } else {

            turnOffGUIMenu();

            if (isSingleTapped) {
                mIsUIHidden = false;
            }
        }

        if (mIsGlobalMenuOn) {
            if (pickedHolders == null) {
                pickedHolders = GVRPicker.pickScene(mGVRContext.getMainScene());
            }

            boolean reorientPointed = false;
            boolean passthroughPointed = false;
            boolean homePointed = false;

            for (GVREyePointeeHolder holder : pickedHolders) {
                if (holder.equals(mGlobalReorient.getEyePointeeHolder())) {
                    reorientPointed = true;
                } else if (holder.equals(mGlobalPassthrough
                        .getEyePointeeHolder())) {
                    passthroughPointed = true;
                } else if (holder.equals(mGlobalHome.getEyePointeeHolder())) {
                    homePointed = true;
                }
            }

            if (reorientPointed || passthroughPointed || homePointed) {
                isAnythingPointed = true;
            }

            if (reorientPointed) {
                mGlobalReorient.getRenderData().getMaterial()
                        .setMainTexture(mActiveReorient);
                if (isSingleTapped) {
                    mGVRContext.getMainScene().getMainCameraRig().resetYaw();
                    turnOffGlobalMenu();
                }
            } else {
                mGlobalReorient.getRenderData().getMaterial()
                        .setMainTexture(mInactiveReorient);
            }

            if (passthroughPointed) {
                mGlobalPassthrough.getRenderData().getMaterial()
                        .setMainTexture(mActivePassThrough);
                if (isSingleTapped) {
                    if (mIsPassThrough) {
                        mPassThroughObject.getRenderData().setRenderMask(0);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        mIsPassThrough = false;
                    } else {
                        mPassThroughObject.getRenderData().setRenderMask(
                                GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                        mCamera = Camera.open();
                        try {
                            mCamera.setPreviewTexture(mCameraSurfaceTexture);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Parameters params = mCamera.getParameters();
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                        mIsPassThrough = true;
                    }
                    turnOffGlobalMenu();
                }
            } else {
                mGlobalPassthrough.getRenderData().getMaterial()
                        .setMainTexture(mInactivePassthrough);
            }

            if (homePointed) {
                mGlobalHome.getRenderData().getMaterial()
                        .setMainTexture(mActiveHome);
                if (isSingleTapped) {
                    turnOffGlobalMenu();
                }
            } else {
                mGlobalHome.getRenderData().getMaterial()
                        .setMainTexture(mInactiveHome);
            }

            String date = new SimpleDateFormat("dd/MM/yy HH:mm:ss",
                    Locale.KOREA).format(new Date());
            if (!date.equals(mDateText)) {
                mDateText = date;
                mGlobalTime
                        .getRenderData()
                        .getMaterial()
                        .setMainTexture(
                                TextFactory.create(mGVRContext, mDateText));
            }

            float batteryRatio = mBatteryLevel / 100.0f;
            mGlobalBatteryInside.getTransform().setScaleX(batteryRatio);
            mGlobalBatteryInside.getTransform().setPositionX(
                    -0.5f + batteryRatio * 0.5f);

            if (isButtonDown) {
                turnOffGlobalMenu();
            }
        } else {
            if (isLongButtonPressed) {
                mIsGlobalMenuOn = true;
                float yaw = mGVRContext.getMainScene().getMainCameraRig()
                        .getTransform().getRotationYaw();
                float pitch = mGVRContext.getMainScene().getMainCameraRig()
                        .getTransform().getRotationPitch();
                if (Math.abs(pitch) >= 90.0f) {
                    if (yaw > 0.0)
                        yaw = 180.0f - yaw;
                    else
                        yaw = -180.0f - yaw;
                }
                mGlobalMenuRoot.getTransform().rotateByAxis(yaw, 0.0f, 1.0f,
                        0.0f);

                mGlobalReorient.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalPassthrough.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalHome.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalTime.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalBattery.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalBatteryInside.getRenderData().setRenderMask(
                        GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                mGlobalReorient.getEyePointeeHolder().setEnable(true);
                mGlobalPassthrough.getEyePointeeHolder().setEnable(true);
                mGlobalHome.getEyePointeeHolder().setEnable(true);
            } else if (isButtonDown)
                mTransitionTarget = 1.0f - mTransitionTarget;
        }

        if (!mIsUIHidden || mIsGlobalMenuOn) {
            mHeadTracker.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
        } else {
            mHeadTracker.getRenderData().setRenderMask(0);
        }

        if (!isUIHiden && isSingleTapped && !isAnythingPointed) {
            mIsUIHidden = true;
        }
    }

    void onPause() {
        mMediaPlayer.pause();
    }

    void onButtonDown() {
        mIsButtonDown = true;
    }

    void onLongButtonPress() {
        mIsLongButtonPressed = true;
    }

    void onTouchEvent(MotionEvent event) {
        mIsTouched = true;
    }

    void onSingleTap(MotionEvent e) {
        mIsSingleTapped = true;
    }

    void setBatteryLevel(int level) {
        mBatteryLevel = level;
    }

    private void turnOffGUIMenu() {
        mPlayPauseButton.getRenderData().setRenderMask(0);
        mFrontButton.getRenderData().setRenderMask(0);
        mBackButton.getRenderData().setRenderMask(0);
        mImaxButton.getRenderData().setRenderMask(0);
        mSelectButton.getRenderData().setRenderMask(0);
        mButtonBoard.getRenderData().setRenderMask(0);
        mSeekbar.setRenderMask(0);
        mSeekbar.unglow();
        mPlayPauseButton.getEyePointeeHolder().setEnable(false);
        mFrontButton.getEyePointeeHolder().setEnable(false);
        mBackButton.getEyePointeeHolder().setEnable(false);
        mImaxButton.getEyePointeeHolder().setEnable(false);
        mSelectButton.getEyePointeeHolder().setEnable(false);
    }

    private void turnOffGlobalMenu() {
        mIsGlobalMenuOn = false;
        mGlobalReorient.getRenderData().setRenderMask(0);
        mGlobalPassthrough.getRenderData().setRenderMask(0);
        mGlobalHome.getRenderData().setRenderMask(0);
        mGlobalTime.getRenderData().setRenderMask(0);
        mGlobalBattery.getRenderData().setRenderMask(0);
        mGlobalBatteryInside.getRenderData().setRenderMask(0);
        mGlobalReorient.getEyePointeeHolder().setEnable(false);
        mGlobalPassthrough.getEyePointeeHolder().setEnable(false);
        mGlobalHome.getEyePointeeHolder().setEnable(false);
    }
}
