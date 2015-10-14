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

package org.gearvrf.immersivepedia.scene;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.immersivepedia.MainScript;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.OnClickListener;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.Loader;

public class MenuScene extends GVRScene {

    public static final float DISTANCE_TO_CAMERA = 6.0f;

    private static final float Y_ADJUST = 2f;

    public static final float CAMERA_Y = 1.6f;

    public MenuScene(GVRContext gvrContext) {
        super(gvrContext);
        gvrContext.setMainScene(this);
        GazeController.enableGaze();
        createDinosaursMenuItem();
        createBirdsMenuItem();
        createFishesMenuItem();
        createMammalsMenuItem();
        addSceneObject(createSkybox()); //

        addSceneObject(createBlueSkybox()); //

        getMainCameraRig().getTransform().setPositionY(CAMERA_Y);
    }

    private void createDinosaursMenuItem() {
        MenuItem dinosaurs = new MenuItem(getGVRContext(), R.drawable.dinosaurs_front_idle, R.drawable.dinosaurs_front_hover,
                R.drawable.dinosaurs_back_idle, R.drawable.dinosaurs_back_hover);
        dinosaurs.getTransform().setPositionZ(-DISTANCE_TO_CAMERA);
        dinosaurs.getTransform().setPositionY(Y_ADJUST);
        dinosaurs.setTexts(getGVRContext().getContext().getString(R.string.dinosaurs), getGVRContext().getContext().getString(R.string.empty));

        dinosaurs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIMenuSelectSoundID(), 1.0f, 1.0f);
                for (GVRSceneObject object : getWholeSceneObjects()) {
                    if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                        new GVROpacityAnimation(object, 1f, 0f).start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation arg0) {
                                MainScript.dinosaurScene.show();
                            }
                        });
                    }
                }
            }
        });

        addSceneObject(dinosaurs);
    }

    private void createBirdsMenuItem() {
        MenuItem birds = new MenuItem(getGVRContext(), R.drawable.birds_front_idle, R.drawable.birds_front_hover,
                R.drawable.birds_back_idle, R.drawable.birds_back_hover);
        birds.getTransform().setPositionX(DISTANCE_TO_CAMERA);
        birds.getTransform().setPositionY(Y_ADJUST);
        birds.getTransform().rotateByAxis(-90.0f, 0f, 1f, 0f);
        birds.setTexts(getGVRContext().getContext().getString(R.string.birds), getGVRContext().getContext().getString(R.string.unavailable));
        birds.setOnClickListener(getUnavailableMenuItemClick());
        addSceneObject(birds);
    }

    private void createFishesMenuItem() {
        MenuItem fishes = new MenuItem(getGVRContext(), R.drawable.fishes_front_idle, R.drawable.fishes_front_hover,
                R.drawable.fishes_back_idle, R.drawable.fishes_back_hover);
        fishes.getTransform().setPositionZ(DISTANCE_TO_CAMERA);
        fishes.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        fishes.setTexts(getGVRContext().getContext().getString(R.string.fishes), getGVRContext().getContext().getString(R.string.unavailable));
        fishes.setOnClickListener(getUnavailableMenuItemClick());
        fishes.getTransform().setPositionY(Y_ADJUST);
        addSceneObject(fishes);
    }

    private void createMammalsMenuItem() {
        MenuItem mammals = new MenuItem(getGVRContext(), R.drawable.mammals_front_idle, R.drawable.mammals_front_hover,
                R.drawable.mammals_back_idle, R.drawable.mammals_back_hover);
        mammals.getTransform().setPositionX(-DISTANCE_TO_CAMERA);
        mammals.getTransform().rotateByAxis(90.0f, 0f, 1f, 0f);
        mammals.setTexts(getGVRContext().getContext().getString(R.string.mammals), getGVRContext().getContext().getString(R.string.unavailable));
        mammals.setOnClickListener(getUnavailableMenuItemClick());
        mammals.getTransform().setPositionY(Y_ADJUST);
        addSceneObject(mammals);
    }

    private OnClickListener getUnavailableMenuItemClick() {
        return new OnClickListener() {

            @Override
            public void onClick() {
                AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIMenuSelectWrongSoundID(), 1.0f, 1.0f);
            }
        };
    }

    // private void createSkybox() {
    //
    // GVRMesh mesh = getGVRContext().loadMesh(new
    // GVRAndroidResource(getGVRContext(), R.raw.skybox_esphere));
    // GVRTexture texture = getGVRContext().loadTexture(new
    // GVRAndroidResource(getGVRContext(), R.drawable.menu_skybox));
    // GVRSceneObject skybox = new GVRSceneObject(getGVRContext(), mesh,
    // texture);
    // skybox.getTransform().setScale(1, 1, 1);
    // skybox.getRenderData().setRenderingOrder(0);
    // addSceneObject(skybox);
    // }

    private GVRSceneObject createSkybox() {

        GVRMesh mesh =  getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.environment_walls_mesh));
        GVRTexture texture = getGVRContext().loadTexture(Loader.loadResourceFromFile(getGVRContext(),
                "menu_walls_tex_diffuse.png", 0));
        GVRSceneObject skybox = new GVRSceneObject(getGVRContext(), mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getRenderData().setRenderingOrder(0);

        GVRMesh meshGround = getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.environment_ground_mesh));
        GVRTexture textureGround = getGVRContext().loadTexture(Loader.loadResourceFromFile(getGVRContext(),
                "menu_ground_tex_diffuse.png", 0));
        GVRSceneObject skyboxGround = new GVRSceneObject(getGVRContext(), meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(0);

        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private GVRSceneObject createBlueSkybox() {

        GVRMesh mesh = getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.skybox_mesh));
        GVRTextureParameters textureParameters = new GVRTextureParameters(getGVRContext());
        textureParameters.setWrapSType(TextureWrapType.GL_REPEAT);
        textureParameters.setWrapTType(TextureWrapType.GL_REPEAT);

        GVRTexture texture = getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.starfield_tex_diffuse),
                textureParameters);
        GVRSceneObject skybox = new GVRSceneObject(getGVRContext(), mesh, texture);
        skybox.getTransform().setScale(1, 1, 1);
        skybox.getRenderData().setRenderingOrder(0);
        return skybox;
    }

}
