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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureFilterType;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.controls.anim.ActionWormAnimation;
import org.gearvrf.controls.anim.ColorWorm;
import org.gearvrf.controls.anim.StarBoxSceneObject;
import org.gearvrf.controls.cursor.ControlGazeController;
import org.gearvrf.controls.focus.ControlSceneObjectBehavior;
import org.gearvrf.controls.gamepad.GamepadObject;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.menu.MenuBox;
import org.gearvrf.controls.model.Apple;
import org.gearvrf.controls.model.touchpad.TouchPad;
import org.gearvrf.controls.shaders.TileShader;
import org.gearvrf.controls.util.Constants;
import org.gearvrf.controls.util.RenderingOrder;

public class MainScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRScene scene;

    public static Worm worm;
    private GVRSceneObject skybox, surroundings, sun, ground, fence;
    private Clouds clouds;
    private float GROUND_Y_POSITION = -1;
    private float SKYBOX_SIZE = 1;
    private float SUN_ANGLE_POSITION = 30;
    private float SUN_Y_POSITION = 10;
    private float CLOUDS_DISTANCE = 15;
    private float SCENE_SIZE = 0.75f;
    private float SCENE_Y = -1.0f;
    private float GROUND_SIZE = 55;
    private float SUN_SIZE = 25;
    private int NUMBER_OF_CLOUDS = 8;
    private float GROUND_TILES = 20;

    private GamepadObject gamepadObject;
    private MenuBox menu;

    TouchPad touchpad;

    private Apple apple;
    public static ActionWormAnimation animationColor;
    private static StarBoxSceneObject starBox;

    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        scene = gvrContext.getMainScene();

        gvrContext.getMainScene().getMainCameraRig().getRightCamera()
                .setBackgroundColor(Color.GREEN);
        gvrContext.getMainScene().getMainCameraRig().getLeftCamera()
                .setBackgroundColor(Color.GREEN);

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getRightCamera().setBackgroundColor(Color.GREEN);
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.RED);
        mainCameraRig.getTransform().setPositionY(0);
        createSkybox();
        createClouds();
        createGround();
        createGazeCursor();

        createSun();
        createSurroundings();
        createWorm();
        createFence();
        createMenu();
        createGamepad3D();

        for (int i = 0; i < Constants.NUMBER_OF_APPLES; i++) {
            createApple();
        }

        createTouchPad3D();
        
        createStar();
        enableAnimationWorm();
    }
    
    public static void animWormReset(){
        animationColor.resetAnimationState();
    }
    
    public static void animationColor(org.gearvrf.controls.util.ColorControls.Color color){
        
        ColorWorm.lastColor = worm.getColor();
        ColorWorm.currentColor = color;
        animationColor.showPlayButton();
    }
    
    public static void enableAnimationStar(){
        starBox.showPlayButton();
    }
    
    public void createStar(){
        
        GVRSceneObject object = new GVRSceneObject(mGVRContext);
        
        starBox = new StarBoxSceneObject(mGVRContext);
        
        starBox.getTransform().setPosition(0, .4f, 8.5f);
        starBox.getTransform().rotateByAxisWithPivot(125, 0, 1, 0, 0, 0, 0);
        
        object.addChildObject(starBox);

        scene.addSceneObject(object);
    }
    
    public void enableAnimationWorm(){
        
        GVRSceneObject wormParent = worm.getWormParentation();
            
        animationColor = new ActionWormAnimation(mGVRContext);
       
        GVRSceneObject object = new GVRSceneObject(mGVRContext);
        object.addChildObject(animationColor);
    
        wormParent.addChildObject(object);
    }
    
    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    private void createApple() {

        apple = new Apple(mGVRContext);
        mGVRContext.getMainScene().addSceneObject(apple);
        apple.setAppleRandomPosition(mGVRContext);
        apple.getTransform().setPositionY(Constants.APPLE_INICIAL_YPOS);
        apple.playAnimation(mGVRContext);
    }

    private void createTouchPad3D() {
        touchpad = new TouchPad(mGVRContext);
        touchpad.getTransform().setPositionZ(-8.5f);
        touchpad.getTransform().setPositionY(0.6f);
        touchpad.getTransform().setScale(0.6f, 0.6f, 0.6f);
        touchpad.getTransform().rotateByAxisWithPivot(90 + 45, 0, 1, 0, 0, 0, 0);
        mGVRContext.getMainScene().addSceneObject(touchpad);
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
        worm.enableShadow();
        scene.addSceneObject(worm);
    }

    private void createGround() {

        GVRTextureParameters parameters = new GVRTextureParameters(mGVRContext);
        parameters.setWrapSType(TextureWrapType.GL_REPEAT);
        parameters.setWrapTType(TextureWrapType.GL_REPEAT);
        parameters.setAnisotropicValue(16);
        parameters.setMinFilterType(TextureFilterType.GL_LINEAR);
        parameters.setMagFilterType(TextureFilterType.GL_LINEAR);

        GVRMesh mesh = mGVRContext.createQuad(GROUND_SIZE, GROUND_SIZE);
        GVRTexture texture = mGVRContext.loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.ground_512), parameters);

        ground = new GVRSceneObject(mGVRContext, mesh, texture,
                new TileShader(mGVRContext).getShaderId());
        ground.getTransform().setPositionY(GROUND_Y_POSITION);
        ground.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        ground.getTransform().setRotationByAxis(-45, 0, 0, 1);
        ground.getTransform().setRotationByAxis(-90, 1, 0, 0);
        ground.getRenderData().setRenderingOrder(RenderingOrder.GROUND);

        ground.getRenderData().getMaterial().setFloat(TileShader.TILE_COUNT, GROUND_TILES);
        ground.getRenderData().getMaterial().setTexture(TileShader.TEXTURE_KEY, texture);
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

        clouds = new Clouds(mGVRContext, CLOUDS_DISTANCE, NUMBER_OF_CLOUDS);
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

        GVRMesh mesh = mGVRContext.createQuad(SUN_SIZE, SUN_SIZE);
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
        TouchPadInput.process();

        touchpad.updateIndicator();

        worm.interactWithDPad();
        worm.animateWormByTouchPad();
        ControlSceneObjectBehavior.process(mGVRContext);

        if (gamepadObject != null) {
            gamepadObject.inputControl();
        }
        worm.checkWormEatingApple(mGVRContext);

    }

    private void createMenu() {

        menu = new MenuBox(mGVRContext);
        scene.addSceneObject(menu);
    }

    private void createGazeCursor() {
        ControlGazeController.setupGazeCursor(mGVRContext);
    }

    private void createGamepad3D() {
        gamepadObject = new GamepadObject(mGVRContext);

        gamepadObject.getTransform().setPosition(0, 1.f, -8.5f);
        gamepadObject.getTransform().rotateByAxisWithPivot(225, 0, 1, 0, 0, 0, 0);

        scene.addSceneObject(gamepadObject);
    }
}