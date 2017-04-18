
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
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

import org.gearvrf.GVRScene;

import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import android.graphics.Color;
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
    private final float[] scaleControlInitPosition = {-2, 1.5f, 0};
    //private float[] webViewScale = {1, 1}; //web view scale
    // when the scale icon is clicked on, saves the initial value for the camera's look at
    private float[] initialCameralookAt_scaleObj = new float[3];

    //private GVRSphereSceneObject gvrTranslationObject = null;
    private Vector3f translationObjectTranslationLocal = new Vector3f();
    private Vector3f translationObjectTranslationGlobal = new Vector3f();
    // when the tranlsation icon is clicked on, saves the initial value for the camera's look at
    private float[] initialCameralookAt = new float[3];

    private final float[] webViewDimensions = {4, 3}; //web view is width 4, height 3
    private final Vector3f cubeUISize = new Vector3f(webViewDimensions[0], .5f, .2f);
    //private final float[] cubeUIPosition = {0, 1.75f, -.1f}; // center, above the web page
    private final float[] cubeUIPosition = {0, (webViewDimensions[1] + cubeUISize.y)/2.0f, -cubeUISize.z/2.0f}; // center, above the web page

    private GVRTextViewSceneObject gvrTextExitObject = null;
    private GVRTextViewSceneObject gvrTextTranslationObject = null;
    private GVRTextViewSceneObject gvrTextScaleObject = null;
    private GVRTextViewSceneObject gvrTextRotateObject = null;
    private final float[] textExitPosition = {1.5f, 0, .125f};
    private final float[] textScalePosition = {-1.5f, 0, .125f};
    private final float[] textTranslatePosition = {-.5f, 0, .125f};
    private final float[] textRotatePosition = {.5f, 0, .125f};
    private float[] webPagePlusUIPosition = {1.0f, -1.0f, -5.0f};
    private float[] webPagePlusUIScale = {1, 1}; // retains current Web page Scale.  z is always 1
    private float[] beginWebPagePlusUIScale = {1, 1}; // retains the beginning Web page Scale when first clicked
    //private final float[] translationControlInitPosition = {-.5f, 1.5f, webPagePlusUIPosition[2]};


    private float[] beginUIClickPos = {0, 0, 0}; // where we clicked on UI to control the web page U.I.
    private float[] diffWebPageUIClick = {0, 0, 0}; // difference between beginUIClickPos and webPagePlusUIPosition



    private GVRSceneObject webPagePlusUISceneObject = null;

    //private final int textColorDefault = Color.BLACK;
    private final int textColorDefault = Color.YELLOW;
    private final int textColorIsOver   = Color.LTGRAY;
    //private final int textColorIsActive = Color.WHITE;
    private final int textColorIsActive = Color.RED;
    private final int textColorBackground = Color.CYAN;

    private GVRDrawFrameListener mOnDrawFrame = null;
    private boolean webViewTranslation = false;
    private boolean webViewScale = false;




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
                        }  // end if .x3d file
                        else if ( url.startsWith("#")) {
                            // go to another Viewpoing
                            Log.e("X3D-Anchor", "      go to Viewpoint: " + url);
                        }  // end if new camera
                        else  {
                            // Launch a web page window
                            Log.e("X3D-Anchor", "      go to web page: " + url);
                            LaunchWebPage(interactiveObjectFinal, url);
                        }  // end launching a Web Page
                    }  // end if isActiveDone = false
                } else if (!event.isActive() ) {
                    isActiveDone = false;
                }
            }  // end of onSensorEvent
        });  // end of AddISensorEvent

    }  //  end AnchorImplementation class



    private void LaunchWebPage(InteractiveObject interactiveObjectFinal, String url) {
        if (webPagePlusUISceneObject == null) {
            final String urlFinal = url;
            final GVRSceneObject gvrSceneObjectAnchor = interactiveObjectFinal.getSensor().getGVRSceneObject();
            gvrContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Launch a new WebView window and place the web page there.
                    GVRWebView gvrWebView = new GVRWebView(gvrContext.getActivity());
                    Log.e("X3D-Anchor", "   (LWP) new GVRWebView(gvrActivity)");
                    gvrWebView.setInitialScale(100);
                    gvrWebView.measure(1600, 900);
                    gvrWebView.layout(0, 0, 1600, 900);
                    WebSettings webSettings = gvrWebView.getSettings();
                    webSettings.setJavaScriptEnabled(true);

                    gvrWebView.loadUrl(urlFinal);

                    GVRViewSceneObject gvrWebViewSceneObject = new GVRViewSceneObject(gvrContext,
                            gvrWebView, webViewDimensions[0], webViewDimensions[1]);
                    gvrWebViewSceneObject.setName("Web View");

                    gvrWebViewSceneObject.getRenderData().getMaterial().setOpacity(1.0f);
                    gvrWebViewSceneObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
                    gvrWebViewSceneObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);

                    gvrWebViewSceneObject.getRenderData().setRenderMask(0);

                    gvrWebViewSceneObject.getRenderData().setRenderMask(
                            GVRRenderData.GVRRenderMaskBit.Left
                                    | GVRRenderData.GVRRenderMaskBit.Right);

                    //GVRMaterial background = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.BeingGenerated.ID);
                    //background.setDiffuseColor(.3f, .5f, .7f, 1);
                    GVRCubeSceneObject gvrUICubeSceneObject = new GVRCubeSceneObject(gvrContext, true, cubeUISize);
                    gvrUICubeSceneObject.getTransform().setPosition(cubeUIPosition[0], cubeUIPosition[1], cubeUIPosition[2]);
                    gvrUICubeSceneObject.getRenderData().getMaterial().setDiffuseColor( .3f, .5f, .7f, 1);
                    gvrUICubeSceneObject.getRenderData().setShaderTemplate(GVRPhongShader.class);

                    // Add the icons to close, scale, translate and rotate
                    gvrTextExitObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " x ");
                    gvrTextExitObject.setTextColor( textColorDefault );
                    gvrTextExitObject.setBackgroundColor( textColorBackground );
                    gvrTextExitObject.getTransform().setPosition(textExitPosition[0], textExitPosition[1], textExitPosition[2]);
                    gvrUICubeSceneObject.addChildObject(gvrTextExitObject);

                    gvrTextTranslationObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " t ");
                    gvrTextTranslationObject.setTextColor( textColorDefault );
                    gvrTextTranslationObject.setBackgroundColor( textColorBackground );
                    gvrTextTranslationObject.getTransform().setPosition(textTranslatePosition[0], textTranslatePosition[1], textTranslatePosition[2]);
                    gvrUICubeSceneObject.addChildObject(gvrTextTranslationObject);

                    gvrTextRotateObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " r ");
                    gvrTextRotateObject.setTextColor( textColorDefault );
                    gvrTextRotateObject.setBackgroundColor( textColorBackground );
                    gvrTextRotateObject.getTransform().setPosition(textRotatePosition[0], textRotatePosition[1], textRotatePosition[2]);
                    gvrUICubeSceneObject.addChildObject(gvrTextRotateObject);

                    gvrTextScaleObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " s ");
                    gvrTextScaleObject.setTextColor( textColorDefault );
                    gvrTextScaleObject.setBackgroundColor( textColorBackground );
                    gvrTextScaleObject.getTransform().setPosition(textScalePosition[0], textScalePosition[1], textScalePosition[2]);
                    gvrUICubeSceneObject.addChildObject(gvrTextScaleObject);


                    // Currently req to show an object dynamically added
                    GVRScene mainScene = gvrContext.getMainScene();
                    mainScene.bindShaders(gvrUICubeSceneObject);
                    mainScene.bindShaders(gvrTextExitObject);
                    mainScene.bindShaders(gvrTextTranslationObject);
                    mainScene.bindShaders(gvrTextRotateObject);
                    mainScene.bindShaders(gvrTextScaleObject);

                    webPagePlusUISceneObject = new GVRSceneObject(gvrContext);
                    webPagePlusUISceneObject.getTransform().setPosition( webPagePlusUIPosition[0], webPagePlusUIPosition[1], webPagePlusUIPosition[2] );

                    // Set up sensor for the U.I.
                    Sensor uibObjectSensor = new Sensor(urlFinal, Sensor.Type.TOUCH, gvrUICubeSceneObject);
                    //final Sensor uiObjectSensorFinal = uibObjectSensor;
                    final GVRSceneObject gvrWebViewSceneObjectFinal = gvrWebViewSceneObject;
                    final GVRSceneObject gvrUICubeSceneObjectFinal = gvrUICubeSceneObject;
                    final GVRSceneObject webPagePlusUISceneObjectFinal = webPagePlusUISceneObject;

                    final GVRSceneObject gvrSceneObjectAnchorFinal = gvrSceneObjectAnchor;
                    //final GVRTextViewSceneObject gvrTextExitObjectFinal = gvrTextExitObject;
                    //final GVRTextViewSceneObject gvrTextTranslationObjectFinal = gvrTextTranslationObject;
                    //final GVRTextViewSceneObject gvrTextScaleObjectFinal = gvrTextScaleObject;
                    //final GVRTextViewSceneObject gvrTextRotateObjectFinal = gvrTextRotateObject;

                    webPagePlusUISceneObjectFinal.addChildObject(gvrWebViewSceneObjectFinal);
                    webPagePlusUISceneObjectFinal.addChildObject(gvrUICubeSceneObjectFinal);
                    gvrSceneObjectAnchorFinal.addChildObject(webPagePlusUISceneObjectFinal);

                    uibObjectSensor.addISensorEvents(new ISensorEvents() {
                        //boolean stateChanged = false;
                        boolean uiObjectIsActive = false;
                        boolean clickDown = false;

                        @Override
                        public void onSensorEvent(SensorEvent event) {
                            if (event.isActive()) {
                                clickDown = !clickDown;
                                Log.e("X3D-Anchor", "(LWP) eventisActive " + uiObjectIsActive + ", clickDown " + clickDown );
                                if (clickDown) {
                                    Log.e("X3D-Anchor", "   (LWP) clickDown " + clickDown);
                                    // only go through once even if object is clicked a long time
                                    uiObjectIsActive = !uiObjectIsActive;
                                    if (uiObjectIsActive) {
                                        if (event.getHitX() > cubeUISize.x/4.0f) {
                                            // Delete the WebView page
                                            Log.e("X3D-Anchor", "   (LWP) delete webView and UI, click > 1");
                                            if (gvrTextScaleObject != null) {
                                                gvrWebViewSceneObjectFinal.removeChildObject(gvrTextScaleObject);
                                            }
                                            if (gvrTextRotateObject != null) {
                                                gvrWebViewSceneObjectFinal.removeChildObject(gvrTextRotateObject);
                                            }
                                            if (gvrTextTranslationObject != null) {
                                                gvrWebViewSceneObjectFinal.removeChildObject(gvrTextTranslationObject);
                                            }
                                            gvrTextExitObject = null;
                                            gvrTextScaleObject = null;
                                            gvrTextRotateObject = null;
                                            gvrTextTranslationObject = null;
                                            gvrSceneObjectAnchorFinal.removeChildObject(webPagePlusUISceneObjectFinal);
                                            webPagePlusUISceneObject = null;
                                            webViewTranslation = false;
                                            webViewScale = false;
                                        } else if (event.getHitX() > 0) {
                                            Log.e("X3D-Anchor", "   (LWP) else condition; 0> click > 1, ROTATION, not implemented");
                                            webViewTranslation = false;

                                        } else if (event.getHitX() > -cubeUISize.x/4.0f) {
                                            Log.e("X3D-Anchor", "   (LWP) -1> click > 0; create TRANSLATION control");
                                            TranslationControl(gvrWebViewSceneObjectFinal);
                                            /*
                                            GVRScene mainScene = gvrContext.getMainScene();
                                            GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
                                            initialCameralookAt = gvrCameraRig.getLookAt();

                                            gvrWebViewSceneObjectFinal.getTransform().getModelMatrix4f().getTranslation(translationObjectTranslationGlobal);
                                            gvrWebViewSceneObjectFinal.getTransform().getLocalModelMatrix4f().getTranslation(translationObjectTranslationLocal);

                                            if (gvrTextTranslationObject != null) {
                                                gvrTextTranslationObject.setTextColor(textColorIsActive);
                                            }
                                            webViewTranslation = true;
                                            Log.e("X3D-Anchor", "   (TC) translationObjectTranslationLocal translation: (" + translationObjectTranslationLocal.x + ", " + translationObjectTranslationLocal.y + ", " + translationObjectTranslationLocal.z + " )");
                                            Log.e("X3D-Anchor", "   (TC) translationObjectTranslationGlobal translation: (" + translationObjectTranslationGlobal.x + ", " + translationObjectTranslationGlobal.y + ", " + translationObjectTranslationGlobal.z + " )");
                                            Log.e("X3D-Anchor", "   (TC) initial Camera lookAt: (" + initialCameralookAt[0] + ", " + initialCameralookAt[1] + ", " + initialCameralookAt[2]);

                                            for ( int i = 0; i < 3; i++ ) {
                                                if (i == 2) beginUIClickPos[i] = webPagePlusUIPosition[i] - cubeUIPosition[i];
                                                else beginUIClickPos[i] = webPagePlusUIPosition[i] + cubeUIPosition[i] + textTranslatePosition[i];
                                                diffWebPageUIClick[i] = beginUIClickPos[i] - webPagePlusUIPosition[i];
                                            }
                                            Log.e("X3D-Anchor", "   (TC) beginUIClickPos: (" + beginUIClickPos[0] + ", " + beginUIClickPos[1] + ", " + beginUIClickPos[2] + ")");
                                            Log.e("X3D-Anchor", "   (TC) diffWebPageUIClick: (" + diffWebPageUIClick[0] + ", " + diffWebPageUIClick[1] + ", " + diffWebPageUIClick[2] + ")");

                                            mOnDrawFrame = new DrawFrame();
                                            gvrContext.registerDrawFrameListener(mOnDrawFrame);
                                            */

                                            //TranslationControl(gvrWebViewSceneObjectFinal, initialCameralookAt, translationObjectTranslationGlobal);
                                        }   // end transltion web window, hit between 0 and .25,
                                        else {
                                            // Add A Scale sphere to the upper right corner
                                            Log.e("X3D-Anchor", "    (LWP)  click < -1; create SCALE control");
                                            webViewTranslation = false;
                                            ScaleControl(gvrWebViewSceneObjectFinal, gvrSceneObjectAnchorFinal, urlFinal);

                                        }  // end scaling web window, hit between -2 and -1,
                                    }
                                    else {  // uiObjectIsActive is false
                                        if ( mOnDrawFrame != null ) gvrContext.unregisterDrawFrameListener( mOnDrawFrame );
                                        if (event.getHitX() > 0) {
                                            Log.e("X3D-Anchor", "   (LWP) else condition; 0> click > 1, ROTATION, not implemented");
                                        } else if (event.getHitX() > -cubeUISize.x/4.0f) {
                                            Log.e("X3D-Anchor", "   (LWP) -1> click > 0; create TRANSLATION control");
                                            if (gvrTextTranslationObject != null) {
                                                gvrTextTranslationObject.setTextColor( textColorIsOver );
                                            }
                                            webViewTranslation = false;
                                        }   // end transltion web window, hit between 0 and .25,
                                        else if (event.getHitX() < -cubeUISize.x/4.0f){
                                            Log.e("X3D-Anchor", "   (LWP) else end Scale");
                                            if (gvrTextScaleObject != null) {
                                                gvrTextScaleObject.setTextColor( textColorIsOver );
                                            }
                                            webViewScale = false;
                                         }  // end scaling web window, hit between -2 and -1,
                                    }
                                }
                            }
                            else if (event.isOver() && !uiObjectIsActive) {
                                //Log.e("X3D-Anchor", "   (LWP) event.getHitX() = " + event.getHitX());
                                // highlight the icons to give visual cue to users
                                if (event.getHitX() > cubeUISize.x/4.0f) {
                                    if (gvrTextExitObject != null) gvrTextExitObject.setTextColor( textColorIsOver );
                                    if (gvrTextRotateObject != null) gvrTextRotateObject.setTextColor( textColorDefault );
                                    if (gvrTextTranslationObject != null) gvrTextTranslationObject.setTextColor( textColorDefault );
                                    if (gvrTextScaleObject != null) gvrTextScaleObject.setTextColor( textColorDefault );
                                    webViewTranslation = false;
                                    webViewScale = false;
                                } else if (event.getHitX() > 0) {
                                    if (gvrTextExitObject != null) gvrTextExitObject.setTextColor( textColorDefault );
                                    if (gvrTextRotateObject != null) gvrTextRotateObject.setTextColor( textColorIsOver );
                                    if (gvrTextTranslationObject != null) gvrTextTranslationObject.setTextColor( textColorDefault );
                                    if (gvrTextScaleObject != null) gvrTextScaleObject.setTextColor( textColorDefault );
                                    webViewTranslation = false;
                                    webViewScale = false;
                                    //Log.e("X3D-Anchor", "      (LWP) rotate " );
                                }  // end scaling web window, hit between 0 and .25,
                                else if (event.getHitX() > -cubeUISize.x/4.0f) {
                                    if (gvrTextExitObject != null) gvrTextExitObject.setTextColor( textColorDefault );
                                    if (gvrTextRotateObject != null) gvrTextRotateObject.setTextColor( textColorDefault );
                                    if (gvrTextTranslationObject != null) gvrTextTranslationObject.setTextColor( textColorIsOver );
                                    if (gvrTextScaleObject != null) gvrTextScaleObject.setTextColor( textColorDefault );
                                    webViewTranslation = false;
                                    webViewScale = false;
                                    //Log.e("X3D-Anchor", "      (LWP) translation " );
                                } else {
                                    //Log.e("X3D-Anchor", "   (LWP) else condition; -.5> click > -.25");
                                    if (gvrTextExitObject != null) gvrTextExitObject.setTextColor( textColorDefault );
                                    if (gvrTextRotateObject != null) gvrTextRotateObject.setTextColor( textColorDefault );
                                    if (gvrTextTranslationObject != null) gvrTextTranslationObject.setTextColor( textColorDefault );
                                    if (gvrTextScaleObject != null) gvrTextScaleObject.setTextColor( textColorIsOver );
                                    webViewTranslation = false;
                                    webViewScale = false;
                                    //Log.e("X3D-Anchor", "      (LWP) scale " );

                                }
                            }
                            else if (!event.isOver()  && !uiObjectIsActive) {
                                if (gvrTextExitObject != null) gvrTextExitObject.setTextColor( textColorDefault );
                                if (gvrTextRotateObject != null) gvrTextRotateObject.setTextColor( textColorDefault );
                                if (gvrTextTranslationObject != null) gvrTextTranslationObject.setTextColor( textColorDefault );
                                if (gvrTextScaleObject != null) gvrTextScaleObject.setTextColor( textColorDefault );
                                webViewTranslation = false;
                                webViewScale = false;
                            }
                        }
                    });
                }  // end run
            });
        }  // end if webPagePlusUISceneObject == null
        else {
            Log.e("X3D-Anchor", "   (LWP) REFRESH webPagePlusUISceneObject != null");
            /*
            GVRScene mainScene = gvrContext.getMainScene();
            mainScene.bindShaders(webPagePlusUISceneObject);
            GVRSceneObject gvrSceneObject = webPagePlusUISceneObject.getChildByIndex(0);
            mainScene.bindShaders(gvrSceneObject);
            gvrSceneObject = webPagePlusUISceneObject.getChildByIndex(1);
            mainScene.bindShaders(gvrSceneObject);
            */
        }
    }  //  end LaunchWebPage


    private void TranslationControl(GVRSceneObject gvrWebViewSceneObjectFinal) {
        GVRScene mainScene = gvrContext.getMainScene();
        GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
        initialCameralookAt = gvrCameraRig.getLookAt();

        gvrWebViewSceneObjectFinal.getTransform().getModelMatrix4f().getTranslation(translationObjectTranslationGlobal);
        gvrWebViewSceneObjectFinal.getTransform().getLocalModelMatrix4f().getTranslation(translationObjectTranslationLocal);

        if (gvrTextTranslationObject != null) {
            gvrTextTranslationObject.setTextColor(textColorIsActive);
        }
        webViewTranslation = true;
        Log.e("X3D-Anchor", "   (TC) translationObjectTranslationLocal translation: (" + translationObjectTranslationLocal.x + ", " + translationObjectTranslationLocal.y + ", " + translationObjectTranslationLocal.z + " )");
        Log.e("X3D-Anchor", "   (TC) translationObjectTranslationGlobal translation: (" + translationObjectTranslationGlobal.x + ", " + translationObjectTranslationGlobal.y + ", " + translationObjectTranslationGlobal.z + " )");
        Log.e("X3D-Anchor", "   (TC) initial Camera lookAt: (" + initialCameralookAt[0] + ", " + initialCameralookAt[1] + ", " + initialCameralookAt[2]);

        for ( int i = 0; i < 3; i++ ) {
            if (i == 2) beginUIClickPos[i] = webPagePlusUIPosition[i] - cubeUIPosition[i];
            else beginUIClickPos[i] = webPagePlusUIPosition[i] + cubeUIPosition[i] + textTranslatePosition[i];
            diffWebPageUIClick[i] = beginUIClickPos[i] - webPagePlusUIPosition[i];
        }
        Log.e("X3D-Anchor", "   (TC) beginUIClickPos: (" + beginUIClickPos[0] + ", " + beginUIClickPos[1] + ", " + beginUIClickPos[2] + ")");
        Log.e("X3D-Anchor", "   (TC) diffWebPageUIClick: (" + diffWebPageUIClick[0] + ", " + diffWebPageUIClick[1] + ", " + diffWebPageUIClick[2] + ")");

        mOnDrawFrame = new DrawFrame();
        gvrContext.registerDrawFrameListener(mOnDrawFrame);
    }  //  end TranslationControl



    private void ScaleControl(GVRSceneObject gvrWebViewSceneObjectFinal, GVRSceneObject  gvrSceneObjectAnchorFinal, String urlFinal) {
        GVRScene mainScene = gvrContext.getMainScene();
        GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
        initialCameralookAt = gvrCameraRig.getLookAt();

        gvrWebViewSceneObjectFinal.getTransform().getModelMatrix4f().getTranslation(translationObjectTranslationGlobal);
        gvrWebViewSceneObjectFinal.getTransform().getLocalModelMatrix4f().getTranslation(translationObjectTranslationLocal);

        if (gvrTextScaleObject != null) {
            gvrTextScaleObject.setTextColor(textColorIsActive);
        }
        webViewScale = true;
        Log.e("X3D-Anchor", "   (SC) translationObjectTranslationLocal translation: (" + translationObjectTranslationLocal.x + ", " + translationObjectTranslationLocal.y + ", " + translationObjectTranslationLocal.z + " )");
        Log.e("X3D-Anchor", "   (SC) translationObjectTranslationGlobal translation: (" + translationObjectTranslationGlobal.x + ", " + translationObjectTranslationGlobal.y + ", " + translationObjectTranslationGlobal.z + " )");
        Log.e("X3D-Anchor", "   (SC) initial Camera lookAt: (" + initialCameralookAt[0] + ", " + initialCameralookAt[1] + ", " + initialCameralookAt[2]);

        for ( int i = 0; i < 3; i++ ) {
            if (i == 2) beginUIClickPos[i] = webPagePlusUIPosition[i] - cubeUIPosition[i];
            else beginUIClickPos[i] = webPagePlusUIPosition[i] + cubeUIPosition[i] + textTranslatePosition[i];
            //diffWebPageUIClick[i] = beginUIClickPos[i] - webPagePlusUIPosition[i];
        }
        beginWebPagePlusUIScale[0] = webPagePlusUIScale[0];
        beginWebPagePlusUIScale[1] = webPagePlusUIScale[1];

        Log.e("X3D-Anchor", "   (SC) beginUIClickPos: (" + beginUIClickPos[0] + ", " + beginUIClickPos[1] + ", " + beginUIClickPos[2] + ")");
        Log.e("X3D-Anchor", "   (SC) beginWebPagePlusUIScale: (" + beginWebPagePlusUIScale[0] + ", " + beginWebPagePlusUIScale[1] + ")");

    //    mOnDrawFrame = new DrawFrame();
    //    gvrContext.registerDrawFrameListener(mOnDrawFrame);
        /*
        if (gvrScaleObject == null) {
            //GVRMaterial sphereScaleColor = new GVRMaterial(gvrContext);
            //sphereScaleColor.setDiffuseColor(1, 0, 0, 1);
            gvrScaleObject = new GVRSphereSceneObject(gvrContext, true, .5f);
            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 1);
            //gvrScaleObject.getTransform().setScale(.5f, .5f, .5f);
            gvrScaleObject.getTransform().setPosition(scaleControlInitPosition[0], scaleControlInitPosition[1], scaleControlInitPosition[2]);

            gvrScaleObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
            // Currently req to show an object dynamically added
            GVRScene mainScene = gvrContext.getMainScene();
            mainScene.bindShaders(gvrScaleObject);
            gvrWebViewSceneObjectFinal.addChildObject(gvrScaleObject);

            Sensor sphereScaleSensor = new Sensor(urlFinal, Sensor.Type.TOUCH, gvrScaleObject);
            final Sensor sphereScaleSensorFinal = sphereScaleSensor;
            final GVRDrawFrameListener mOnDrawFrame = new DrawFrame();

            sphereScaleSensor.addISensorEvents(new ISensorEvents() {
                boolean isOverScaleObject = false;
                boolean isActiveScaleObject = false;
                boolean activeState = false;
                public void onSensorEvent(SensorEvent event) {

                    if (event.isActive() && !activeState) {
                        if ( gvrScaleObject != null ) {
                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 1, .5f, 1);
                            gvrScaleObject.getTransform().getLocalModelMatrix4f().getTranslation(scaleObjectTranslationLocal);
                            gvrScaleObject.getTransform().getModelMatrix4f().getTranslation(scaleObjectTranslationGlobal);
                        }
                        activeState = true;
                        GVRScene mainScene = gvrContext.getMainScene();
                        GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
                        initialCameralookAt_scaleObj = gvrCameraRig.getLookAt();
                        gvrContext.registerDrawFrameListener(mOnDrawFrame);
                        Log.e("X3D-Anchor", "   (SC) scaleObjMatrixLocal translation: (" + scaleObjectTranslationLocal.x + ", " + scaleObjectTranslationLocal.y + ", " + scaleObjectTranslationLocal.z + " )");
                        Log.e("X3D-Anchor", "   (SC) scaleObjMatrixGlobal translation: (" + scaleObjectTranslationGlobal.x + ", " + scaleObjectTranslationGlobal.y + ", " + scaleObjectTranslationGlobal.z + " )");
                        Log.e("X3D-Anchor", "   (SC) Camera Rig lookAt: (" + initialCameralookAt_scaleObj[0] + ", " + initialCameralookAt_scaleObj[1] + ", " + initialCameralookAt_scaleObj[2]);
                    }
                    else if (event.isActive() && activeState) {
                        gvrContext.unregisterDrawFrameListener(mOnDrawFrame);
                        if ( gvrScaleObject != null ) {
                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, .5f, .25f, 1);
                        }
                        activeState = false;
                    }

                    else if (event.isOver() && !activeState) {
                        if ( gvrScaleObject != null ) {
                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, .5f, .25f, 1);
                        }
                    }
                    if ( !isActiveScaleObject && !event.isOver() && !activeState ) {
                        if ( gvrScaleObject != null ) {
                            gvrScaleObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 1);
                        }
                    }
                }
            });
        }  //  end if gvrScaleObject == null
        */
    }  //  end ScaleControl


    private class PerFrameWebViewControl {

        final void onDrawFrame(float frameTime) {
            GVRScene mainScene = gvrContext.getMainScene();
            GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
            float[] currentCameraLookAt = gvrCameraRig.getLookAt();
            if ( webViewScale ) {
                Log.e("X3D-Anchor", "ODF: scale");
                float[] transitionClickPt = {0, 0, translationObjectTranslationGlobal.z};
                transitionClickPt[0] = currentCameraLookAt[0] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                transitionClickPt[1] = currentCameraLookAt[1] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];

                //float[] scaleChange = new float[2];
                webPagePlusUIScale[0] = (transitionClickPt[0] - beginUIClickPos[0])/beginWebPagePlusUIScale[0];
                webPagePlusUIScale[1] = (transitionClickPt[1] - beginUIClickPos[1])/beginWebPagePlusUIScale[1];
                Log.e("X3D-Anchor", "ODF: scaleChange " + webPagePlusUIScale[0] + ", " + webPagePlusUIScale[1]);

                //beginWebPagePlusUIScale[0] = webPagePlusUIScale[0];
                //beginWebPagePlusUIScale[1] = webPagePlusUIScale[1];
                //webPagePlusUIScale

                webPagePlusUISceneObject.getTransform().setScale(1, 1, 1);

            }  // end if scale
            /*
            if ( (gvrScaleObject != null) && (webPagePlusUISceneObject != null) ) {

                float scaleObjectZdepth = scaleObjectTranslationGlobal.z;
                float x = currentCameraLookAt[0] * scaleObjectZdepth/currentCameraLookAt[2];
                float y = currentCameraLookAt[1] * scaleObjectZdepth/currentCameraLookAt[2];
                Log.e("X3D-Anchor", "ODF: Camera Rig lookAt: (" + x + ", " +
                        y + ", " + (currentCameraLookAt[2] * scaleObjectZdepth/currentCameraLookAt[2]));

                Vector3f diff = new Vector3f();
                scaleObjectTranslationGlobal.sub(scaleObjectTranslationLocal, diff);
                Log.e("X3D-Anchor", "   ODF: Vector diff: (" + diff.x + ", " + diff.y + ", " + diff.z + ")");
                gvrScaleObject.getTransform().setPosition( (x - diff.x), (y - diff.y), scaleObjectZdepth - diff.z);

                webPagePlusUISceneObject.getTransform().setScale( (x - diff.x)/scaleControlInitPosition[0], (y - diff.y)/scaleControlInitPosition[1], 1);
                //GVRScene gvrMainScene = gvrContext.getMainScene();
                mainScene.bindShaders(webPagePlusUISceneObject);

                float[] diffLookAt = new float[3];
                for (int i = 0; i < 3; i++) {
                    diffLookAt[i] = currentCameraLookAt[i] - initialCameralookAt_scaleObj[i];
                }
                Log.e("X3D-Anchor", "   ODF: Camera Rig lookAt Diff: (" + diffLookAt[0] + ", " + diffLookAt[1] + ", " + diffLookAt[2]);
            }
            */
            else if ( webViewTranslation ) {

                Log.e("X3D-Anchor", "ODF: translation");
                float[] transitionClickPt = {0, 0, translationObjectTranslationGlobal.z};
                transitionClickPt[0] = currentCameraLookAt[0] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                transitionClickPt[1] = currentCameraLookAt[1] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                for (int i = 0; i < 3; i++) {
                    transitionClickPt[i] -= diffWebPageUIClick[i];
                }
                webPagePlusUISceneObject.getTransform().setPosition( transitionClickPt[0], transitionClickPt[1], transitionClickPt[2]);

                /*
                float translationObjectZdepth = translationObjectTranslationGlobal.z;
                float x = currentCameraLookAt[0] * translationObjectZdepth/currentCameraLookAt[2];
                float y = currentCameraLookAt[1] * translationObjectZdepth/currentCameraLookAt[2];
                Log.e("X3D-Anchor", "   ODF: Camera Rig lookAt: (" + x + ", " +
                        y + ", " + (currentCameraLookAt[2] * translationObjectZdepth/currentCameraLookAt[2]));
                //diffWebPageUIClick
                Vector3f diff = new Vector3f();
                translationObjectTranslationGlobal.sub(translationObjectTranslationLocal, diff);
                Log.e("X3D-Anchor", "   ODF: Vector diff: (" + diff.x + ", " + diff.y + ", " + diff.z + ")");
                //gvrTranslationObject.getTransform().setPosition( (x - diff.x), (y - diff.y), translationObjectZdepth - diff.z);
                */

                //webPagePlusUISceneObject.getTransform().setPosition( (x - diff.x)/translationControlInitPosition[0], (y - diff.y)/translationControlInitPosition[1], translationControlInitPosition[2]);
                //GVRScene newMainScene = gvrContext.getMainScene();
                //newMainScene.bindShaders(webPagePlusUISceneObject);
                //mainScene.bindShaders(webPagePlusUISceneObject);
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



