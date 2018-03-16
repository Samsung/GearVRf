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
package org.gearvrf.io.sceneeditor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Environment;
import android.view.Gravity;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IAssetEvents;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.MovableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior.ObjectState;
import org.gearvrf.io.cursor3d.SelectableBehavior.StateChangedListener;
import org.gearvrf.io.sceneeditor.EditableBehavior.DetachListener;
import org.gearvrf.io.sceneeditor.FileBrowserView.FileViewListener;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.utlis.sceneserializer.SceneSerializer;
import org.gearvrf.utlis.sceneserializer.SceneSerializer.SceneLoaderListener;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This sample can be used with a Laser Cursor as well as an Object Cursor. By default the Object
 * Cursor is enabled. To switch to a Laser Cursor simply rename the "laser_cursor_settings.xml"
 * in the assets directory to "settings.xml"
 */
public class SceneEditorMain extends GVRMain {
    private static final String TAG = SceneEditorMain.class.getSimpleName();
    private static final float TARGET_RADIUS = 1.0f;
    private static final String CUBEMAP_EXTENSION = ".zip";
    private static final float DEFAULT_ENVIRONMENT_SCALE = 200.0f;
    private static final float ICON_TITLE_OFFSET = -1.0f;
    private static final float DEFAULT_OBJECT_DEPTH = -5.0f;
    private static final float ICON_QUAD_HEIGHT = 1.0f;
    private static final float ICON_QUAD_WIDTH = 1.0f;
    private static final float ICON_Z_POSITION = -5.0f;
    private static final float ICON_Y_POSITION = -3.0f;
    private static final float ICON_X_ROTATION_DEG = -25;
    private static final float ICON_TITLE_X_ROTATION_DEG = -45;
    private static final float ICON_TITLE_TEXT_SIZE = 6;
    private GVRScene mainScene;
    private CursorManager cursorManager;
    private EditableBehavior editableBehavior;
    private GVRContext gvrContext;
    private Resources resources;

    private List<GVRSceneObject> menuSceneObjects;

    private FileBrowserView fileBrowserView;
    private SceneSerializer sceneSerializer;
    private GVRCubeSceneObject environmentCube;
    private GVRSphereSceneObject environmentSphere;
    private GVRSceneObject environmentSceneObject;
    private String currentModel;


    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        this.resources = gvrContext.getContext().getResources();
        mainScene = gvrContext.getMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.DKGRAY);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.DKGRAY);
        cursorManager = new CursorManager(gvrContext, mainScene);
        editableBehavior = new EditableBehavior(cursorManager, mainScene, detachListener);
        sceneSerializer = new SceneSerializer();
        menuSceneObjects = new LinkedList<GVRSceneObject>();
        //TODO fix cursor manager to not require this at start
        float[] position = new float[]{0.0f, -10.0f, 0f};
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.mipmap
                .ic_launcher)));
        final GVRCubeSceneObject cubeSceneObject = new GVRCubeSceneObject(gvrContext, true,
                material);
        cubeSceneObject.getTransform().setPosition(position[0], position[1], position[2]);
        addToSceneEditor(cubeSceneObject);
        addSceneEditorMenu();
        cursorManager.sendEventsToActivity(false);
        gvrContext.getEventReceiver().addListener(assetEventListener);
        sceneSerializer.importScene(gvrContext, mainScene, sceneLoaderListener);
    }

    private SceneLoaderListener sceneLoaderListener = new SceneLoaderListener() {
        @Override
        public void onEnvironmentLoaded(GVRSceneObject envSceneObject) {
            if (envSceneObject == null) {
                addDefaultSurroundings(gvrContext);
            } else {
                environmentSceneObject = envSceneObject;
                if (environmentSceneObject instanceof GVRCubeSceneObject) {
                    environmentCube = (GVRCubeSceneObject) environmentSceneObject;
                } else if (environmentSceneObject instanceof GVRSphereSceneObject) {
                    environmentSphere = (GVRSphereSceneObject) environmentSceneObject;
                }
            }
        }

        @Override
        public void onSceneObjectLoaded(GVRSceneObject sceneObject) {
            attachMovableBehavior(sceneObject);
        }
    };

    IAssetEvents assetEventListener = new IAssetEvents() {
        @Override
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath,
                                  String errors) {
        }

        @Override
        public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath) {
            if (currentModel != null && currentModel.endsWith(filePath)) {
                model.getTransform().setPosition(0, 0, DEFAULT_OBJECT_DEPTH);
                addToSceneEditor(model);
                fileBrowserView.modelLoaded();
                sceneSerializer.addToSceneData(model, currentModel);
            }
        }

        @Override
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath) {
        }

        @Override
        public void onModelError(GVRContext context, String error, String filePath) {
            Log.d(TAG, "onModelError:%s:%s", error, filePath);
            if (currentModel != null && currentModel.endsWith(filePath)) {
                fileBrowserView.modelLoaded();
            }
        }

        @Override
        public void onTextureError(GVRContext context, String error, String filePath) {
            Log.d(TAG, "onTextureError:%s:%s", error, filePath);
            if (currentModel != null && currentModel.endsWith(filePath)) {
                fileBrowserView.modelLoaded();
            }
        }
    };

    private void loadModelToScene(String modelFileName) {
        Log.d(TAG, "Loading the model to scene:%s", modelFileName);
        try {
            currentModel = modelFileName;
            gvrContext.getAssetLoader().loadModel("sd:" + modelFileName);
        } catch (IOException e) {
            Log.e(TAG, "Could not load model:" + modelFileName + e.getMessage());
        }
    }

    FileViewListener modelFileViewListener = new FileViewListener() {
        @Override
        public void onClose() {
            cursorManager.disableSettingsCursor();
            Log.d(TAG, "Re-enable file browser icon");
            setMenuVisibility(true);
        }

        @Override
        public void onFileSelected(String modelFileName) {
            loadModelToScene(modelFileName);
        }
    };

    FileViewListener environFileViewListener = new FileViewListener() {
        @Override
        public void onClose() {
            cursorManager.disableSettingsCursor();
            Log.d(TAG, "Re-enable file browser icon");
            setMenuVisibility(true);
        }

        @Override
        public void onFileSelected(String fileName) {
            addSurroundings(fileName);
        }
    };

    private void addSceneEditorMenu() {
        addIcon(R.drawable.environment_icon, R.string.load_environment, 1.25f, 1.0f, 2.5f, new
                StateChangedListener() {
                    @Override
                    public void onStateChanged(SelectableBehavior behavior, ObjectState
                            prev, ObjectState current, Cursor cursor, GVRPicker.GVRPickedObject hit) {
                        if (current == ObjectState.CLICKED) {
                            setMenuVisibility(false);
                            cursorManager.enableSettingsCursor(cursor);
                            if(fileBrowserView == null) {
                                fileBrowserView = new FileBrowserView(mainScene, environFileViewListener, FileBrowserView
                                        .ENVIRONMENT_EXTENSIONS, resources.getString(R.string
                                        .environment_picker_title));
                            }
                            fileBrowserView.reset(getGVRContext().getActivity(),
                                    environFileViewListener,
                                    FileBrowserView.ENVIRONMENT_EXTENSIONS,
                                    resources.getString(R.string.environment_picker_title),
                                    "environments");
                            fileBrowserView.render();
                        }
                        if (current == ObjectState.BEHIND) {
                            behavior.getOwnerObject().getRenderData().getMaterial().
                                    setOpacity(0.5f);
                        } else {
                            behavior.getOwnerObject().getRenderData().getMaterial().setOpacity(1f);
                        }
                    }
                });
        addIcon(R.drawable.model_3d_icon, R.string.load_model, -1.25f, 1.0f, 2.0f, new
                StateChangedListener() {
                    @Override
                    public void onStateChanged(SelectableBehavior behavior, ObjectState
                            prev, ObjectState current, Cursor cursor, GVRPicker.GVRPickedObject hit) {
                        if (current == ObjectState.CLICKED) {
                            cursorManager.enableSettingsCursor(cursor);
                            if(fileBrowserView == null) {
                                fileBrowserView = new FileBrowserView(mainScene, modelFileViewListener,
                                        FileBrowserView.MODEL_EXTENSIONS, resources.getString(R
                                        .string.model_picker_title));
                            }
                            fileBrowserView.reset(getGVRContext().getActivity(),
                                    modelFileViewListener,
                                    FileBrowserView.MODEL_EXTENSIONS,
                                    resources.getString(R.string.model_picker_title),
                                    "models");
                            fileBrowserView.render();
                            setMenuVisibility(false);
                        }
                        if (current == ObjectState.BEHIND) {
                            behavior.getOwnerObject().getRenderData().getMaterial()
                                    .setOpacity(0.5f);
                        } else {
                            behavior.getOwnerObject().getRenderData().getMaterial().setOpacity(1f);
                        }
                    }
                });
    }

    private void addIcon(int iconResource, int titleResource, float xPos, float titleHeight, float
            titleWidth, StateChangedListener listener) {
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                iconResource)));
        GVRSceneObject iconQuad = new GVRSceneObject(gvrContext, ICON_QUAD_WIDTH,
                ICON_QUAD_HEIGHT);
        iconQuad.getRenderData().setMaterial(material);

        SelectableBehavior fileBrowserBehavior = new SelectableBehavior(cursorManager);
        iconQuad.attachComponent(fileBrowserBehavior);
        iconQuad.getTransform().setPosition(xPos, ICON_Y_POSITION, ICON_Z_POSITION);
        iconQuad.getTransform().rotateByAxis(ICON_X_ROTATION_DEG, 1, 0, 0);
        iconQuad.getRenderData().setRenderingOrder(GVRRenderingOrder.TRANSPARENT);

        mainScene.addSceneObject(iconQuad);

        GVRTextViewSceneObject iconTitle = new GVRTextViewSceneObject(gvrContext, titleWidth,
                titleHeight, resources.getString(titleResource));
        iconTitle.setTextColor(Color.WHITE);
        iconTitle.setBackgroundColor(R.drawable.rounded_rect_bg);
        iconTitle.setGravity(Gravity.CENTER);
        iconTitle.getRenderData().setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        iconTitle.getTransform().setPosition(0f, ICON_TITLE_OFFSET, 0);
        iconTitle.getTransform().rotateByAxis(ICON_TITLE_X_ROTATION_DEG, 1, 0, 0);
        iconQuad.addChildObject(iconTitle);
        iconTitle.setTextSize(ICON_TITLE_TEXT_SIZE);
        menuSceneObjects.add(iconQuad);
        menuSceneObjects.add(iconTitle);
        fileBrowserBehavior.setStateChangedListener(listener);
    }

    void setMenuVisibility(boolean visibility) {
        for (GVRSceneObject sceneObject : menuSceneObjects) {
            sceneObject.setEnable(visibility);
        }
    }

    private void attachMovableBehavior(GVRSceneObject gvrSceneObject) {
        MovableBehavior movableCubeBehavior = new MovableBehavior(cursorManager);
        gvrSceneObject.attachComponent(movableCubeBehavior);
        movableCubeBehavior.setStateChangedListener(stateChangedListener);
    }

    private void addToSceneEditor(GVRSceneObject newSceneObject) {
        attachMovableBehavior(newSceneObject);
        mainScene.addSceneObject(newSceneObject);
        float radius = newSceneObject.getBoundingVolume().radius;
        float scalingFactor = TARGET_RADIUS / radius;
        newSceneObject.getTransform().setScale(scalingFactor, scalingFactor, scalingFactor);
    }

    @Override
    public void onStep() {
    }

    void close() {
        if (cursorManager != null) {
            cursorManager.close();
        }
    }

    private EditableBehavior.DetachListener detachListener = new DetachListener() {
        @Override
        public void onDetach() {
            setMenuVisibility(true);
        }

        @Override
        public void onRemoveFromScene(GVRSceneObject gvrSceneObject) {
            sceneSerializer.removeFromSceneData(gvrSceneObject);
            mainScene.removeSceneObject(gvrSceneObject);
        }
    };

    private StateChangedListener stateChangedListener = new StateChangedListener() {
        public static final long CLICK_THRESHOLD = 500;
        public long prevClickTimeStamp = 0;

        @Override
        public void onStateChanged(final SelectableBehavior behavior, ObjectState prev,
                                   ObjectState current, Cursor cursor, GVRPicker.GVRPickedObject hit) {
            if (prev == ObjectState.CLICKED) {
                long currentTimeStamp = System.currentTimeMillis();
                if (prevClickTimeStamp != 0 && (currentTimeStamp - prevClickTimeStamp) <
                        CLICK_THRESHOLD) {
                    if (behavior.getOwnerObject().getComponent(EditableBehavior.getComponentType
                            ()) == null) {
                        Log.d(TAG, "Attaching Editable Behavior");
                        editableBehavior.setCursor(cursor);
                        behavior.getOwnerObject().attachComponent(editableBehavior);
                        setMenuVisibility(false);
                    }
                }
                prevClickTimeStamp = System.currentTimeMillis();
            }

            if (current == ObjectState.BEHIND) {
                setRenderDataMode(behavior.getOwnerObject(), GLES20.GL_LINES);
            } else {
                setRenderDataMode(behavior.getOwnerObject(), GLES20.GL_TRIANGLES);
            }
        }
    };

    private void setRenderDataMode(GVRSceneObject sceneObject, int mode) {
        if (sceneObject == null) {
            return;
        }

        GVRRenderData renderData = sceneObject.getRenderData();
        if (renderData != null) {
            renderData.setDrawMode(mode);
        }
        for (GVRSceneObject so : sceneObject.getChildren()) {
            setRenderDataMode(so, mode);
        }
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        // return the correct splash screen bitmap
        GVRTexture tex = new GVRTexture(gvrContext);
        tex.setImage(new GVRBitmapImage(gvrContext, bitmap));
        return tex;
    }

    private void addSurroundings(String fileName) {
        GVRAndroidResource resource = null;
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + fileName;
        try {
            resource = new GVRAndroidResource(fullPath);
        } catch (IOException e) {
            Log.e(TAG, "Could not load texture file:%s", e.getMessage());
            fileBrowserView.modelLoaded();
            return;
        }

        if (fileName.endsWith(CUBEMAP_EXTENSION)) {
            initializeSurroundingCube();
            GVRTexture futureCubeTexture = gvrContext.getAssetLoader().loadCubemapTexture
                    (resource);
            environmentCube.getRenderData().getMaterial().setMainTexture(futureCubeTexture);
            if (environmentSceneObject != environmentCube) {
                mainScene.removeSceneObject(environmentSceneObject);
                environmentSceneObject = environmentCube;
                mainScene.addSceneObject(environmentSceneObject);
            }
        } else {
            initializeSurroundingSphere();
            final GVRAndroidResource finalResource = resource;
            GVRTexture futureSphereTexture = gvrContext.getAssetLoader().loadTexture(finalResource);
            environmentSphere.getRenderData().getMaterial().setMainTexture(futureSphereTexture);
            if (environmentSceneObject != environmentSphere) {
                mainScene.removeSceneObject(environmentSceneObject);
                environmentSceneObject = environmentSphere;
                mainScene.addSceneObject(environmentSceneObject);
            }
        }
        //TODO assuming scale is the same along all axis, extend to use 3 scale values
        sceneSerializer.setEnvironmentData(fullPath, environmentSceneObject.getTransform()
                .getScaleX());
        fileBrowserView.modelLoaded();
    }

    private void initializeSurroundingSphere() {
        if(environmentSphere == null) {
            GVRMaterial material = new GVRMaterial(gvrContext);
            environmentSphere = new GVRSphereSceneObject(gvrContext, false,
                    material);
            environmentSphere.getTransform().setScale(DEFAULT_ENVIRONMENT_SCALE,
                    DEFAULT_ENVIRONMENT_SCALE, DEFAULT_ENVIRONMENT_SCALE);
        }
    }

    private void addDefaultSurroundings(GVRContext gvrContext) {
        GVRTexture futureTexture = gvrContext.getAssetLoader().loadTexture(new
                GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        initializeSurroundingSphere();
        environmentSphere.getRenderData().getMaterial().setMainTexture(futureTexture);
        environmentSphere.getRenderData().disableLight();
        environmentSceneObject = environmentSphere;
        mainScene.addSceneObject(environmentSceneObject);
    }

    private void initializeSurroundingCube() {
        //TODO do not initialize every time once FutureTexture loading bug is fixed
        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.Cubemap.ID);
        environmentCube = new GVRCubeSceneObject(gvrContext, false,
                cubemapMaterial);
        environmentCube.getTransform().setScale(DEFAULT_ENVIRONMENT_SCALE,
                DEFAULT_ENVIRONMENT_SCALE, DEFAULT_ENVIRONMENT_SCALE);
    }

    public void saveState() {
        try {
            if (sceneSerializer != null) {
                sceneSerializer.exportScene();
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not export scene:%s", e.getMessage());
        }
    }
}
