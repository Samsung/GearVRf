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

package org.gearvrf.controls;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.Worm.MovementDirection;
import org.gearvrf.controls.focus.ControlSceneObjectBehavior;
import org.gearvrf.controls.gamepad.GamepadObject;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.menu.Menu;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class MainScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRScene scene;

    private Worm worm;
    private GVRSceneObject skybox, surroundings, sun, ground, fence;
    private GVRSceneObject clouds;
    private float GROUND_Y_POSITION = -1;
    private float SKYBOX_SIZE = 1;
    private float SUN_ANGLE_POSITION = 30;
    private float SUN_Y_POSITION = 10;
    private float CLOUDS_DISTANCE = 15;

    private float SCENE_SIZE = 0.75f;
    private float SCENE_Y = -1.0f;

    private Menu mMenu = null;
    
    private GamepadObject gamepadObject;

    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        scene = gvrContext.getMainScene();
        
        gvrContext.getMainScene().getMainCameraRig().getRightCamera().setBackgroundColor(Color.GREEN);
        gvrContext.getMainScene().getMainCameraRig().getLeftCamera().setBackgroundColor(Color.GREEN);

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getRightCamera().setBackgroundColor(Color.GREEN);
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.RED);
        mainCameraRig.getTransform().setPositionY(0);
        createSkybox();
        createClouds();
        createGround();

        createSun();
        createSurroundings();
        createWorm();
        createFence();
        createMenu();
        createGamepad3D();
    }

    private void createFence() {

        GVRMesh mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.fence));
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.atlas01));
        fence = new GVRSceneObject(mGVRContext, mesh, texture);
        fence.getTransform().setPositionY(GROUND_Y_POSITION);
        fence.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        fence.getRenderData().setCullFace(GVRCullFaceEnum.None);
        fence.getRenderData().setRenderingOrder(RenderingOrder.FENCE);
        scene.addSceneObject(fence);

    }

    private void createWorm() {

        worm = new Worm(mGVRContext);
        scene.addSceneObject(worm);
    }

    private void createGround() {

        GVRMesh mesh = mGVRContext.createQuad(55, 55);
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.ground_tile));

        ground = new GVRSceneObject(mGVRContext, mesh, texture);
        ground.getTransform().setPositionY(GROUND_Y_POSITION);
        ground.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        ground.getTransform().setRotationByAxis(-90, 1, 0, 0);
        ground.getRenderData().setRenderingOrder(RenderingOrder.GROUND);
        scene.addSceneObject(ground);
    }

    private void createSkybox() {

        GVRMesh mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.skybox));
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.skybox));

        skybox = new GVRSceneObject(mGVRContext, mesh, texture);
        skybox.getTransform().setScale(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
        skybox.getRenderData().setRenderingOrder(RenderingOrder.SKYBOX);
        scene.addSceneObject(skybox);
    }

    private void createClouds() {

        clouds = new Clouds(mGVRContext, CLOUDS_DISTANCE, 9);
    }

    private void createSurroundings() {

        GVRMesh mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.stones));
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.atlas01));

        surroundings = new GVRSceneObject(mGVRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.FLOWERS);
        scene.addSceneObject(surroundings);
        // ground.addChildObject(surroundings);

        mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.grass));
        texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.atlas01));

        surroundings = new GVRSceneObject(mGVRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        scene.addSceneObject(surroundings);
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.GRASS);

        mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.flowers));
        texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.atlas01));

        surroundings = new GVRSceneObject(mGVRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        scene.addSceneObject(surroundings);
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.FLOWERS);

        mesh = mGVRContext.loadMesh(
                new GVRAndroidResource(mGVRContext, R.raw.wood));
        texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.atlas01));
        surroundings = new GVRSceneObject(mGVRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        surroundings.getRenderData().setCullFace(GVRCullFaceEnum.None);
        scene.addSceneObject(surroundings);
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.WOOD);
    }

    private void createSun() {

        GVRMesh mesh = mGVRContext.createQuad(25, 25);
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.sun));

        sun = new GVRSceneObject(mGVRContext, mesh, texture);
        sun.getTransform().setRotationByAxis(90, 1, 0, 0);
        sun.getTransform().setPositionY(SUN_Y_POSITION);
        sun.getTransform().rotateByAxisWithPivot(SUN_ANGLE_POSITION, 1, 0, 0, 0, 0, 0);
        sun.getRenderData().setRenderingOrder(RenderingOrder.SUN);
        scene.addSceneObject(sun);
    }

    @Override
    public void onStep() {
        worm.chainMove(mGVRContext);

        GamepadInput.process();

        GamepadInput.interactWithDPad(worm);
        ControlSceneObjectBehavior.process(mGVRContext);

        if(gamepadObject != null){

            gamepadObject.getGamepadVirtual().handlerAnalogL(
                    GamepadInput.getCenteredAxis(MotionEvent.AXIS_X), 
                    GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y), 
                    0);
            
            gamepadObject.getGamepadVirtual().handlerAnalogR(
                    GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX), 
                    GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY), 
                    0);
            
            gamepadObject.getGamepadVirtual().dpadTouch(GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X), 
                    GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y));
            
            
            gamepadObject.getGamepadVirtual().handlerLRButtons(GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_L1),
                    GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_R1));
            
            gamepadObject.getGamepadVirtual().buttonsPressed(
                    GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_X),
                    GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_Y),
                    GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_A),  
                    GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_B));
        }
    }

    public void animateWorm(org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection swipeDirection) {

        float duration = 0.6f;
        float movement = 0.75f;
        float degree = 22.5f;

        if (swipeDirection.name() == SwipeDirection.Up.name()) {
            worm.moveAlongCameraVector(duration, movement);
            worm.rotateWorm(MovementDirection.Up);

        } else if (swipeDirection.name() == SwipeDirection.Down.name()) {
            worm.moveAlongCameraVector(duration, -movement);
            worm.rotateWorm(MovementDirection.Down);

        } else if (swipeDirection.name() == SwipeDirection.Forward.name()) {
            worm.rotateAroundCamera(duration, -degree);
            worm.rotateWorm(MovementDirection.Right);

        } else {
            worm.rotateAroundCamera(duration, degree);
            worm.rotateWorm(MovementDirection.Left);
        }
    }

    private void createMenu() {
        mMenu = new Menu(mGVRContext);
        mMenu.getTransform().setScale(0.4f, 0.4f, 0.4f);
        mMenu.getTransform().setPosition(0, -.5f, -3f);
        mMenu.getRenderData().getMaterial().setOpacity(0.5f);
//        scene.addSceneObject(mMenu);
    }
    
    private void createGamepad3D() {
        GVRSceneObject cameraObject = mGVRContext.getMainScene()
                .getMainCameraRig().getOwnerObject();
        gamepadObject = new GamepadObject(mGVRContext);

        gamepadObject.getTransform().setPosition(-3, 1.f, 8f);
        float angle = Util.getYRotationAngle(gamepadObject,cameraObject);
        
        gamepadObject.getTransform().rotateByAxis(angle, 0, 1, 0);
        
        scene.addSceneObject(gamepadObject);
    }
}