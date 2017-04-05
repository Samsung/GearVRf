
/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.x3d;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

import org.gearvrf.GVRScene;

import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;



/**
 * @author m1.williams
 *         AnchorImplementation handles the functions specific to X3D's Anchor node.
 *         An Anchor is a subclass of the TouchSensor (though only 'isActive' is
 *         implemented, there is no 'isOver').
 *         The Anchor can either:
 *          Animate to a new Viewpoint if the url begins with a # followed by the Viewpoint's DEF name
 *          Go to a new X3D scene if the url ends with ".x3d"
 *          Or open up a new web page.
 */

public class AnchorImplementation {

    private static final String TAG = AnchorImplementation.class.getSimpleName();

    private static final String IS_OVER = "isOver";
    private static final String Is_ACTIVE = "isActive";

    private GVRContext gvrContext = null;

    private PerFrameWebViewControl perFrameWebViewControl = new PerFrameWebViewControl();
    private GVRSphereSceneObject gvrScaleObject = null;
    private Vector3f scaleObjectTranslationLocal = new Vector3f();
    private Vector3f scaleObjectTranslationGlobal = new Vector3f();

    // when the scale icon is clicked on, saves the initial value for the camera's look at
    private float[] initialCameralookAt_scaleObj = new float[3];



    public AnchorImplementation(GVRContext gvrContext ) {
      this.gvrContext = gvrContext;
    }



    /**
     * BuildInteractiveObjectFromAnchor is a special type of interactive object in that it does not get
     * built using ROUTE's.
     *
     * @param anchorSensor is the Sensor that describes the sensor set to an Anchor
     * @param anchorDestination is either another Viewpoint, url to a web site or another x3d scene
     */
     /*
    public void BuildInteractiveObjectFromAnchor(Sensor anchorSensor, String anchorDestination) {
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setSensor(anchorSensor, anchorDestination);
        interactiveObjects.add(interactiveObject);
    }
    */

    /**
     * AnchorInteractivity() called when an object associated with an Anchor tag
     * is clicked.  There are 3 possible outcomes:
     *    A web page is opened, we go to a new .x3d scene, or we go to a new Camera (Viewpoint)
     */
    public void AnchorInteractivity(InteractiveObject interactiveObject) {
        // The interactiveObject contains an Anchor tag.  Process below
        final InteractiveObject interactiveObjectFinal = interactiveObject;
        interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
            //boolean stateChanged = false;
            boolean isActiveDone = false;

            @Override
            public void onSensorEvent(SensorEvent event) {

                if (event.isActive() ) {
                    if (!isActiveDone) {
                        // only go through once even if object is clicked a long time
                        isActiveDone = true;
                        String url = interactiveObjectFinal.getSensor().getAnchorURL();
                        if ( url.toLowerCase().endsWith(".x3d")) {
                            // Go to another X3D page
                            Log.e("X3D-Anchor", "      go to .x3d: " + url);
                        }
                        else if ( url.startsWith("#")) {
                            // go to another Viewpoing
                            Log.e("X3D-Anchor", "      go to Viewpoint: " + url);
                        }
                        else  {
                            // Launch a web page window
                            Log.e("X3D-Anchor", "      go to web page: " + url);
                            final String urlFinal = url;
                            final GVRSceneObject gvrSceneObjectAnchor = interactiveObjectFinal.getSensor().getGVRSceneObject();
                            gvrContext.getActivity().runOnUiThread( new Runnable() {
                                @Override
                                public void run(){
                                    // Launch a new WebView window and place the web page there.
                                    Log.e("X3D-Anchor", "         BGN new GVRWebView(gvrActivity)");
                                    GVRWebView gvrWebView = new GVRWebView(gvrContext.getActivity());
                                    Log.e("X3D-Anchor", "         END new GVRWebView(gvrActivity)");
                                    gvrWebView.setInitialScale(100);
                                    gvrWebView.measure(1600, 900);
                                    gvrWebView.layout(0, 0, 1600, 900);
                                    WebSettings webSettings = gvrWebView.getSettings();
                                    webSettings.setJavaScriptEnabled(true);

                                    Log.e("X3D-Anchor", "   BGN THREAD gvrWebViewFinal.loadUrl( " + urlFinal + " )");
                                    gvrWebView.loadUrl(urlFinal);

                                    Log.e("X3D-Anchor", "   Create web object");
                                    GVRViewSceneObject gvrWebViewSceneObject = new GVRViewSceneObject(gvrContext,
                                            gvrWebView, 4.0f, 3.0f);
                                    gvrWebViewSceneObject.setName("web view object");

                                    gvrWebViewSceneObject.getRenderData().getMaterial().setOpacity(1.0f);
                                    gvrWebViewSceneObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
                                    gvrWebViewSceneObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
                                    //gvrWebViewSceneObject.getTransform().setPosition(1.0f, -1.0f, -5.0f);
                                    //gvrWebView.invalidate();

                                    gvrWebViewSceneObject.getRenderData().setRenderMask(0);

                                    gvrWebViewSceneObject.getRenderData().setRenderMask(
                                            GVRRenderData.GVRRenderMaskBit.Left
                                                    | GVRRenderData.GVRRenderMaskBit.Right);

                                    Log.e("X3D-Anchor", "BGN addChildObject(s)");
                                    GVRMaterial background = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.BeingGenerated.ID);
                                    background.setDiffuseColor(0, 0, .5f, 1);
                                    GVRCubeSceneObject gvrUICubeSceneObject = new GVRCubeSceneObject(gvrContext, true, background);
                                    gvrUICubeSceneObject.getTransform().setScale(4, .5f, .2f);
                                    gvrUICubeSceneObject.getTransform().setPosition(0, 1.75f, -.1f);
                                    gvrUICubeSceneObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
                                    // Currently req to show an object dynamically added
                                    GVRScene mainScene = gvrContext.getMainScene();
                                    mainScene.bindShaders(gvrUICubeSceneObject);

                                    GVRSceneObject combinedGVRSceneObject = new GVRSceneObject(gvrContext);
                                    combinedGVRSceneObject.getTransform().setPosition(1.0f, -1.0f, -5.0f);

                                    // Set up sensor for the U.I.
                                    Sensor uibObjectSensor = new Sensor(urlFinal, Sensor.Type.TOUCH, gvrUICubeSceneObject);
                                    final Sensor  uiObjectSensorFinal = uibObjectSensor;
                                    final GVRSceneObject  gvrWebViewSceneObjectFinal = gvrWebViewSceneObject;
                                    final GVRSceneObject  gvrUICubeSceneObjectFinal = gvrUICubeSceneObject;
                                    final GVRSceneObject  combinedGVRSceneObjectFinal = combinedGVRSceneObject;
                                    final GVRSceneObject  gvrSceneObjectAnchorFinal = gvrSceneObjectAnchor;
                                    combinedGVRSceneObjectFinal.addChildObject(gvrWebViewSceneObjectFinal);
                                    combinedGVRSceneObjectFinal.addChildObject(gvrUICubeSceneObjectFinal);
                                    gvrSceneObjectAnchorFinal.addChildObject(combinedGVRSceneObjectFinal);

                                    uibObjectSensor.addISensorEvents(new ISensorEvents() {
                                        //boolean stateChanged = false;
                                        boolean uiObjectIsActiveDone = false;
                                        @Override
                                        public void onSensorEvent(SensorEvent event) {
                                            if (event.isActive() ) {
                                                if (!uiObjectIsActiveDone) {
                                                    // only go through once even if object is clicked a long time
                                                    uiObjectIsActiveDone = true;
                                                    Log.e("X3D-Anchor", "BGN webObjectIsActiveDone");
                                                    Log.e("X3D-Anchor", "   event: " + event.getHitX() + ", "  + event.getHitY() + ", " + event.getHitZ() );
                                                    if (event.getHitX() > .25f) {
                                                        // Delete the WebView page
                                                        Log.e("X3D-Anchor", "   delete webView and UI" );
                                                        if (gvrScaleObject != null) gvrWebViewSceneObjectFinal.removeChildObject(gvrScaleObject);
                                                        gvrScaleObject = null;
                                                        gvrSceneObjectAnchorFinal.removeChildObject(combinedGVRSceneObjectFinal);
                                                    }
                                                    else if (event.getHitX() > 0) {
                                                        // Add A Scale sphere to the upper right corner
                                                        Log.e("X3D-Anchor", "    > 0; create sphere for scaling" );
                                                        if (gvrScaleObject == null) {
                                                            GVRMaterial sphereScaleColor = new GVRMaterial(gvrContext);
                                                            sphereScaleColor.setDiffuseColor(1, 0, 0, 1);
                                                            gvrScaleObject = new GVRSphereSceneObject(gvrContext, true, sphereScaleColor);
                                                            gvrScaleObject.getTransform().setScale(.25f, .25f, .25f);
                                                            gvrScaleObject.getTransform().setPosition(2, 1.5f, 0);
                                                            gvrScaleObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
                                                            // Currently req to show an object dynamically added
                                                            GVRScene mainScene = gvrContext.getMainScene();
                                                            mainScene.bindShaders(gvrScaleObject);
                                                            gvrWebViewSceneObjectFinal.addChildObject(gvrScaleObject);

                                                            Sensor sphereScaleSensor = new Sensor(urlFinal, Sensor.Type.TOUCH, gvrScaleObject);
                                                            final Sensor  sphereScaleSensorFinal = sphereScaleSensor;
                                                            //final GVRSceneObject gvrScaleObjectFinal = gvrScaleObject;
                                                            final GVRDrawFrameListener mOnDrawFrame = new DrawFrame();

                                                            sphereScaleSensor.addISensorEvents(new ISensorEvents() {
                                                                boolean isOverScaleObject = false;
                                                                boolean isActiveScaleObject = false;
                                                                public void onSensorEvent(SensorEvent event) {

                                                                    if (event.isActive() || event.isOver() ) {
                                                                        if ( event.isActive() ) {
                                                                            if ( !isActiveScaleObject ) {
                                                                                isActiveScaleObject = true;
                                                                                Log.e("X3D-Anchor", "       Scale isActive");
                                                                                gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 1, .5f, 1);
                                                                                //float[] scaleObjectTranslation = {gvrScaleObject.getTransform().getPositionX(),
                                                                                //        gvrScaleObject.getTransform().getPositionY(), gvrScaleObject.getTransform().getPositionZ()};
                                                                                //Log.e("X3D-Anchor", "       scale Obj Translation " + gvrScaleObject.getTransform().getPositionX() +
                                                                                //        ", " + gvrScaleObject.getTransform().getPositionY() + ", " + gvrScaleObject.getTransform().getPositionZ());
                                                                                //Log.e("X3D-Anchor", "       scale Obj Translation " + scaleObjectTranslation[0] +
                                                                                //        ", " + scaleObjectTranslation[1] + ", " + scaleObjectTranslation[2]);
                                                                                //Matrix4f scaleObjectMatrixLocal = gvrScaleObject.getTransform().getLocalModelMatrix4f();

                                                                                //scaleObjectMatrixLocal.getTranslation(scaleObjectTranslationLocal);

                                                                                gvrScaleObject.getTransform().getLocalModelMatrix4f().getTranslation(scaleObjectTranslationLocal);
                                                                                Log.e("X3D-Anchor", "          scaleObjectMatrixLocal translation: (" + scaleObjectTranslationLocal.x + ", " + scaleObjectTranslationLocal.y + ", " + scaleObjectTranslationLocal.z + " )");

                                                                                //Matrix4f scaleObjectMatrixGlobal = gvrScaleObject.getTransform().getModelMatrix4f();
                                                                                //scaleObjectMatrixGlobal.getTranslation(scaleObjectTranslationGlobal);
                                                                                gvrScaleObject.getTransform().getModelMatrix4f().getTranslation(scaleObjectTranslationGlobal);
                                                                                Log.e("X3D-Anchor", "          scaleObjectMatrixGlobal translation: (" + scaleObjectTranslationGlobal.x + ", " + scaleObjectTranslationGlobal.y + ", " + scaleObjectTranslationGlobal.z + " )");

                                                                                // Get the current camera direction
                                                                                GVRScene mainScene = gvrContext.getMainScene();
                                                                                GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
                                                                                initialCameralookAt_scaleObj = gvrCameraRig.getLookAt();
                                                                                Log.e("X3D-Anchor", "          Camera Rig lookAt: (" + initialCameralookAt_scaleObj[0] + ", " + initialCameralookAt_scaleObj[1] + ", " + initialCameralookAt_scaleObj[2]);
                                                                                gvrContext.registerDrawFrameListener(mOnDrawFrame);
                                                                            }
                                                                            else {
                                                                                isActiveScaleObject = false;
                                                                                gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, .5f, .5f, 1);
                                                                                gvrContext.unregisterDrawFrameListener(mOnDrawFrame);
                                                                                Log.e("X3D-Anchor", "       Scale NOT Active");

                                                                            }

                                                                        }
                                                                        else if (event.isOver()) {
                                                                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, .5f, .5f, 1);
                                                                        }
                                                                        //else {
                                                                        //    gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 1);
                                                                        //}
                                                                    }  // end if event.isActive or event.isOver
                                                                    if ( !isActiveScaleObject && !event.isOver()  ) {
                                                                        gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 1);
                                                                    }
                                                                    /*
                                                                    else if (event.isOver()) {
                                                                        if ( !isActiveScaleObject ) {
                                                                            if (!isOverScaleObject) {
                                                                                gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, .5f, .5f, 1);
                                                                                isOverScaleObject = true;
                                                                                Log.e("X3D-Anchor", "       Scale isOver" );
                                                                            }
                                                                        }
                                                                    }
                                                                    else if (!event.isOver()) {
                                                                        if ( !isActiveScaleObject ) {
                                                                            isOverScaleObject = false;
                                                                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 1);
                                                                        }
                                                                    }
                                                                    */
                                                                    //else if (!event.isActive()) {
                                                                    //
                                                                    //}
                                                                }
                                                            });
                                                        }
                                                    }  // end scaling web window, hit between 0 and .25,
                                                    else if (event.getHitX() > -.25f) {
                                                        Log.e("X3D-Anchor", "    > -.25" );

                                                    }
                                                    else  {
                                                        Log.e("X3D-Anchor", "    click else condition" );

                                                    }

                                                }
                                            }  else if (!event.isActive() ) {
                                                uiObjectIsActiveDone = false;
                                            }

                                        }
                                    });
                                    Log.e("X3D-Anchor", "END addChildObject(s)");
                                }  // end run
                            });
                        }  // end launching a Web Page
                    }  // end if isActiveDone = false
                } else if (!event.isActive() ) {
                    isActiveDone = false;
                }
            }  // end of onSensorEvent
        });  // end of AddISensorEvent

    }  //  end AnchorImplementation class


    private class PerFrameWebViewControl {

        final void onDrawFrame(float frameTime) {
            GVRScene mainScene = gvrContext.getMainScene();
            GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
            float[] currentCameraLookAt = gvrCameraRig.getLookAt();
            if (gvrScaleObject != null) {
                float scaleObjectZdepth = scaleObjectTranslationGlobal.z;
                //Log.e("X3D-Anchor", "PerFrameWebViewControl: Camera Rig lookAt: (" + currentCameraLookAt[0] + ", " + currentCameraLookAt[1] + ", " + currentCameraLookAt[2]);
                Log.e("X3D-Anchor", "onDrawFrame: Camera Rig lookAt: (" + (currentCameraLookAt[0] * scaleObjectZdepth/currentCameraLookAt[2]) + ", " +
                        (currentCameraLookAt[1] * scaleObjectZdepth/currentCameraLookAt[2]) + ", " + (currentCameraLookAt[2] * scaleObjectZdepth/currentCameraLookAt[2]));

                Vector3f diff = new Vector3f();
                scaleObjectTranslationGlobal.sub(scaleObjectTranslationLocal, diff);
                Log.e("X3D-Anchor", "   Vector diff: (" + diff.x + ", " + diff.y + ", " + diff.z + ")");

                float[] diffLookAt = new float[3];
                for (int i = 0; i < 3; i++) {
                    diffLookAt[i] = currentCameraLookAt[i] - initialCameralookAt_scaleObj[i];
                }
                Log.e("X3D-Anchor", "    Camera Rig lookAt Diff: (" + diffLookAt[0] + ", " + diffLookAt[1] + ", " + diffLookAt[2]);

            }

        }  //  end onDrawFrame
    }  //  end private class PerFrameScripting



    private final class DrawFrame implements GVRDrawFrameListener {
        @Override
        public void onDrawFrame(float frameTime) {
            perFrameWebViewControl.onDrawFrame(frameTime);
        }
    }


}  //  end AnchorInteractivity



