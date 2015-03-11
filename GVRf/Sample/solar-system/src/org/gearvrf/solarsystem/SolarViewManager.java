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


package org.gearvrf.solarsystem;

import org.gearvrf.*;
import org.gearvrf.GVRAndroidResource.BitmapTextureCallback;
import org.gearvrf.GVRAndroidResource.MeshCallback;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.utility.Log;

public class SolarViewManager extends GVRScript {

    private GVRAnimationEngine mAnimationEngine;

    private static class AsyncSphericalObject extends GVRSceneObject {

        private static final String TAG = Log.tag(AsyncSphericalObject.class);

        private static Object[] lock = new Object[0];
        private static GVRMesh sphereMesh = null;
        static {
            GVRContext.addResetOnRestartHandler(new Runnable() {

                @Override
                public void run() {
                    sphereMesh = null;
                }
            });
        }

        private static final int MESH_PRIORITY = 1000;
        private static final int TEXTURE_PRIORITY = 100;

        private GVRMesh mesh = null;
        private GVRTexture texture = null;

        AsyncSphericalObject(GVRContext context, String textureName) {
            super(context);

            synchronized (lock) {
                if (sphereMesh != null) {
                    setMesh(sphereMesh);
                } else {
                    try {
                        context.loadMesh(newMeshCallback(), //
                                new GVRAndroidResource(context, "sphere.obj"), //
                                MESH_PRIORITY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                context.loadBitmapTexture(newTextureCallback(), //
                        new GVRAndroidResource(context, textureName),//
                        TEXTURE_PRIORITY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private MeshCallback newMeshCallback() {
            return new MeshCallback() {

                final String TAG = AsyncSphericalObject.TAG + ": MeshCallback";

                @Override
                public void loaded(GVRMesh mesh,
                        GVRAndroidResource androidResource) {
                    Log.d(TAG, "loaded(%s,  %s)", mesh, androidResource);
                    synchronized (lock) {
                        sphereMesh = mesh;
                    }
                    setMesh(mesh);
                }

                @Override
                public void failed(Throwable t,
                        GVRAndroidResource androidResource) {
                    Log.e(TAG, "failed(%s,  %s)", t, androidResource);
                }

                @Override
                public boolean stillWanted(GVRAndroidResource androidResource) {
                    return true;
                }
            };
        }

        private BitmapTextureCallback newTextureCallback() {
            return new BitmapTextureCallback() {

                final String TAG = AsyncSphericalObject.TAG
                        + ": TextureCallback";

                @Override
                public boolean stillWanted(GVRAndroidResource androidResource) {
                    return true;
                }

                @Override
                public void loaded(GVRTexture texture,
                        GVRAndroidResource androidResource) {
                    Log.d(TAG, "loaded(%s,  %s)", texture, androidResource);
                    setTexture(texture);
                }

                @Override
                public void failed(Throwable t,
                        GVRAndroidResource androidResource) {
                    Log.e(TAG, "failed(%s,  %s)", t, androidResource);
                }
            };
        }

        private void setMesh(GVRMesh mesh) {
            this.mesh = mesh;
            setSkin();
        }

        private void setTexture(GVRTexture texture) {
            this.texture = texture;
            setSkin();
        }

        private synchronized void setSkin() {
            if (getRenderData() != null) {
                Log.d(TAG, "setSkin(): getRenderData() != null");
                return; // already setup
            }
            if (mesh == null || texture == null) {
                Log.d(TAG, "setSkin(): mesh == %s || texture == %s", //
                        mesh, texture);
                return; // still missing a piece
            }

            GVRContext context = getGVRContext();
            GVRRenderData renderData = new GVRRenderData(context);

            renderData.setMesh(mesh);

            GVRMaterial material = new GVRMaterial(context,
                    GVRMaterial.GVRShaderType.Unlit.ID);
            material.setMainTexture(texture);
            renderData.setMaterial(material);

            attachRenderData(renderData);
        }
    }

    private static GVRSceneObject asyncSceneObject(GVRContext context,
            String textureName) {
        return new AsyncSphericalObject(context, textureName);
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mAnimationEngine = gvrContext.getAnimationEngine();

        gvrContext.getMainScene().getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        gvrContext.getMainScene().getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        gvrContext.getMainScene().getMainCameraRig().getOwnerObject()
                .getTransform().setPosition(0.0f, 0.0f, 0.0f);

        GVRSceneObject solarSystemObject = new GVRSceneObject(gvrContext);
        gvrContext.getMainScene().addSceneObject(solarSystemObject);

        GVRSceneObject sunRotationObject = new GVRSceneObject(gvrContext);
        solarSystemObject.addChildObject(sunRotationObject);

        GVRSceneObject sunMeshObject = asyncSceneObject(gvrContext,
                "sunmap.jpg");
        sunMeshObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);

        GVRSceneObject mercuryRevolutionObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        GVRSceneObject mercuryRotationObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        GVRSceneObject mercuryMeshObject = asyncSceneObject(gvrContext,
                "mercurymap.jpg");
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);

        GVRSceneObject venusRevolutionObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        GVRSceneObject venusRotationObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        GVRSceneObject venusMeshObject = asyncSceneObject(gvrContext,
                "venusmap.jpg");
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);

        GVRSceneObject earthRevolutionObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        GVRSceneObject earthRotationObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        GVRSceneObject earthMeshObject = asyncSceneObject(gvrContext,
                "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        GVRSceneObject moonRevolutionObject = new GVRSceneObject(gvrContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(gvrContext.getMainScene()
                .getMainCameraRig().getOwnerObject());

        GVRSceneObject marsRevolutionObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        GVRSceneObject marsRotationObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        GVRSceneObject marsMeshObject = asyncSceneObject(gvrContext,
                "mars_1k_color.jpg");
        marsMeshObject.getTransform().setScale(0.6f, 0.6f, 0.6f);
        marsRotationObject.addChildObject(marsMeshObject);

        counterClockwise(sunRotationObject, 50f);

        counterClockwise(mercuryRevolutionObject, 150f);
        counterClockwise(mercuryRotationObject, 100f);

        counterClockwise(venusRevolutionObject, 400f);
        clockwise(venusRotationObject, 400f);

        counterClockwise(earthRevolutionObject, 600f);
        counterClockwise(earthRotationObject, 1.5f);

        counterClockwise(moonRevolutionObject, 60f);

        clockwise(gvrContext.getMainScene().getMainCameraRig().getOwnerObject()
                .getTransform(), 60f);

        counterClockwise(marsRevolutionObject, 1200f);
        counterClockwise(marsRotationObject, 200f);
    }

    @Override
    public void onStep() {
    }

    private void run(GVRAnimation animation) {
        animation
                //
                .setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1)
                .start(mAnimationEngine);
    }

    private void counterClockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRTransform transform, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }
}
