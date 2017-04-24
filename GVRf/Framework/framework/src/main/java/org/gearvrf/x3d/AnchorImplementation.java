
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
import android.view.View;

import org.gearvrf.GVRScene;

import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import android.content.Context;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


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

    private float[] initialHitPoint = new float[3];


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
    private GVRWebView gvrWebView = null;

    private String webPageContent = "";





    public AnchorImplementation(GVRContext gvrContext ) {
      this.gvrContext = gvrContext;
    }

    /**
     * AnchorInteractivity() called when an object associated with an Anchor tag
     * is clicked.  There are 3 possible outcomes:
     *    A web page is opened, we go to a new .x3d scene, or we go to a new Camera (Viewpoint)
     */
    public void AnchorInteractivity(InteractiveObject interactiveObject) {
        // The interactiveObject contains an Anchor tag.
        // Could be a link to another x3d page, <Viewpoint> (camera) or web page
        final InteractiveObject interactiveObjectFinal = interactiveObject;
        interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
            boolean isActiveDone = false;

            @Override
            public void onSensorEvent(SensorEvent event) {

                if (event.isActive() ) {
                    if (!isActiveDone) {
                        isActiveDone = true;
                        String url = interactiveObjectFinal.getSensor().getAnchorURL();
                        if ( url.toLowerCase().endsWith(".x3d")) {
                            // Go to another X3D page
                            Log.e("X3D-Anchor", "      go to .x3d: " + url);
                        }  // end if .x3d file
                        else if ( url.startsWith("#")) {
                            // go to another Viewpoint
                            Log.e("X3D-Anchor", "      go to Viewpoint: " + url);
                        }  // end if new camera
                        else if ((url.endsWith(".xml")) || (url.endsWith(".rss"))) {
                            Log.e("X3D-Anchor", "   (LWP) XML");
                            webPageContent = "<HTML><BODY><FONT SIZE=7>Issue pressenting RSS / XML page</FONT></BODY></HTML>";
                            final String urlFinal = url;
                            Threads.spawn(new Runnable() {

                                public void run() {
                                    File file = null;
                                    FileInputStream fileInputStream = null;
                                    try {
                                        Context context = gvrContext.getContext();
                                        file = gvrContext.getAssetLoader().downloadFile(context, urlFinal);
                                        fileInputStream = new FileInputStream(file);

                                        // Parse the XML/RSS file
                                        SAXParserFactory factory = SAXParserFactory.newInstance();
                                        SAXParser saxParser = factory.newSAXParser();
                                        UserHandler userhandler = new UserHandler();

                                        webPageContent = "<html><body>";
                                        saxParser.parse(fileInputStream, userhandler);
                                        webPageContent += "</body></html>";

                                      LaunchWebPage(interactiveObjectFinal, urlFinal);
                                    } catch (FileNotFoundException e) {
                                        Log.e(TAG, "File " + urlFinal + " not found: " + e);
                                        Log.e("X3D-Anchor", "File " + urlFinal + " not found: " + e);
                                    } catch (IOException e) {
                                        Log.e(TAG, "File " + urlFinal + " not found: " + e);
                                        Log.e("X3D-Anchor", "File " + urlFinal + " not found: " + e);
                                    }
                                    catch (Exception e) {
                                        Log.e(TAG, "File " + urlFinal + " exception: " + e);
                                        Log.e("X3D-Anchor", "File " + urlFinal + " exception: " + e);

                                    } finally {
                                        if (fileInputStream != null) {
                                            try {
                                                fileInputStream.close();
                                            } catch (IOException e) {
                                            }
                                        }
                                        if (file != null) {
                                            file.delete();
                                        }
                                    }
                                }
                            });
                        } // end if rss or xml data

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
                    gvrWebView = new GVRWebView(gvrContext.getActivity());
                    Log.e("X3D-Anchor", "   (LWP) new GVRWebView(gvrActivity)");
                    gvrWebView.setInitialScale(100);
                    gvrWebView.measure(1600, 1200);
                    gvrWebView.layout(0, 0, 1600, 1200);
                    WebSettings webSettings = gvrWebView.getSettings();
                    webSettings.setJavaScriptEnabled(true);


                    if ((urlFinal.endsWith(".xml")) || (urlFinal.endsWith(".rss"))) {
                        if (gvrWebView != null) gvrWebView.loadData(webPageContent, "text/html", null);
                    } // end if rss or xml data
                    else if (urlFinal.endsWith(".tst")) {
                        //TODO: this is just for tests and should be removed
                        Log.e("X3D-Anchor", "   (LWP) Test File (overwrite data)");

                        String summary = "<html><body>" +
                                "<H1>You scored <b>192</b> points.<P>Test of html</H1><P>" +
                                "<img src=\"https://a.mtstatic.com/@public/production/site_6566/1452738115-apple-touch-icon.png\"><P>" +
                                "SDC<P><img src=\"https://a.mtstatic.com/@public/production/site_6566/1452737966-logo.png\"><P>" +
                                "SDC<P><img src=\"https://resources.samsungdevelopers.com/@api/deki/files/1156/GearVRf_Architecture.png\"><P>" +
                                "</body></html>";
                        if (gvrWebView != null) gvrWebView.loadData(summary, "text/html", null);
                    }  // end if test page.
                    else {
                        // Open a Web page
                        Log.e("X3D-Anchor", "   (LWP) url " + urlFinal);
                        //View view = gvrWebView.getView();
                        gvrWebView.setWebViewClient(new WebViewClient() {
                            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                Log.e("X3D-Anchor", "   setWebViewClient Error ");
                                Log.e("X3D-Anchor", "   errorCode " + errorCode + "; description " + description + "; url " + failingUrl);
                            }
                            //WebViewClient webViewClient = this;
                            //Log.e("X3D-Anchor", "   webViewClient created");
                            //webViewClient.
                        });
                        if (gvrWebView != null) gvrWebView.loadUrl(urlFinal);
                    }  //  end if url data - a web address

                    if (gvrWebView != null) {

                        //}
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

                        GVRCubeSceneObject gvrUICubeSceneObject = new GVRCubeSceneObject(gvrContext, true, cubeUISize);
                        gvrUICubeSceneObject.getTransform().setPosition(cubeUIPosition[0], cubeUIPosition[1], cubeUIPosition[2]);
                        gvrUICubeSceneObject.getRenderData().getMaterial().setDiffuseColor(.3f, .5f, .7f, 1);
                        gvrUICubeSceneObject.getRenderData().setShaderTemplate(GVRPhongShader.class);

                        // Add the icons to close, scale, translate and rotate
                        gvrTextExitObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " x ");
                        gvrTextExitObject.setTextColor(textColorDefault);
                        gvrTextExitObject.setBackgroundColor(textColorBackground);
                        gvrTextExitObject.getTransform().setPosition(textExitPosition[0], textExitPosition[1], textExitPosition[2]);
                        gvrUICubeSceneObject.addChildObject(gvrTextExitObject);

                        gvrTextTranslationObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " t ");
                        gvrTextTranslationObject.setTextColor(textColorDefault);
                        gvrTextTranslationObject.setBackgroundColor(textColorBackground);
                        gvrTextTranslationObject.getTransform().setPosition(textTranslatePosition[0], textTranslatePosition[1], textTranslatePosition[2]);
                        gvrUICubeSceneObject.addChildObject(gvrTextTranslationObject);

                        gvrTextRotateObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " r ");
                        gvrTextRotateObject.setTextColor(textColorDefault);
                        gvrTextRotateObject.setBackgroundColor(textColorBackground);
                        gvrTextRotateObject.getTransform().setPosition(textRotatePosition[0], textRotatePosition[1], textRotatePosition[2]);
                        gvrUICubeSceneObject.addChildObject(gvrTextRotateObject);

                        gvrTextScaleObject = new GVRTextViewSceneObject(gvrContext, 0.5f, 0.45f, " s ");
                        gvrTextScaleObject.setTextColor(textColorDefault);
                        gvrTextScaleObject.setBackgroundColor(textColorBackground);
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
                        webPagePlusUISceneObject.getTransform().setPosition(webPagePlusUIPosition[0], webPagePlusUIPosition[1], webPagePlusUIPosition[2]);

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
                            boolean uiObjectIsActive = false;
                            boolean clickDown = false;

                            @Override
                            public void onSensorEvent(SensorEvent event) {
                                if (event.isActive()) {
                                    clickDown = !clickDown;
                                    Log.e("X3D-Anchor", "(LWP) eventisActive " + uiObjectIsActive + ", clickDown " + clickDown);
                                    if (clickDown) {
                                        Log.e("X3D-Anchor", "   (LWP) clickDown " + clickDown);
                                        // only go through once even if object is clicked a long time
                                        uiObjectIsActive = !uiObjectIsActive;
                                        if (uiObjectIsActive) {
                                            if (event.getHitX() > cubeUISize.x / 4.0f) {
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
                                                gvrWebView = null;
                                            } else if (event.getHitX() > 0) {
                                                Log.e("X3D-Anchor", "   (LWP) else condition; 0> click > 1, ROTATION, not implemented");
                                                webViewTranslation = false;

                                            } else if (event.getHitX() > -cubeUISize.x / 4.0f) {
                                                Log.e("X3D-Anchor", "   (LWP) -1> click > 0; create TRANSLATION control");
                                                TranslationControl(gvrWebViewSceneObjectFinal);
                                            }   // end transltion web window, hit between 0 and .25,
                                            else {
                                                // Add A Scale sphere to the upper right corner
                                                Log.e("X3D-Anchor", "    (LWP)  click < -1; create SCALE control");
                                                webViewTranslation = false;
                                                ScaleControl(gvrWebViewSceneObjectFinal, gvrSceneObjectAnchorFinal, urlFinal);

                                            }  // end scaling web window, hit between -2 and -1,
                                        } else {  // uiObjectIsActive is false
                                            if (mOnDrawFrame != null)
                                                gvrContext.unregisterDrawFrameListener(mOnDrawFrame);
                                            if (event.getHitX() > 0) {
                                                Log.e("X3D-Anchor", "   (LWP) else condition; 0> click > 1, ROTATION, not implemented");
                                            } else if (event.getHitX() > -cubeUISize.x / 4.0f) {
                                                Log.e("X3D-Anchor", "   (LWP) -1> click > 0; create TRANSLATION control");
                                                if (gvrTextTranslationObject != null) {
                                                    gvrTextTranslationObject.setTextColor(textColorIsOver);
                                                }
                                                webViewTranslation = false;
                                            }   // end transltion web window, hit between 0 and .25,
                                            else if (event.getHitX() < -cubeUISize.x / 4.0f) {
                                                Log.e("X3D-Anchor", "   (LWP) else end Scale");
                                                if (gvrTextScaleObject != null) {
                                                    gvrTextScaleObject.setTextColor(textColorIsOver);
                                                }
                                                webViewScale = false;
                                            }  // end scaling web window, hit between -2 and -1,
                                        }
                                    }
                                } else if (event.isOver() && !uiObjectIsActive) {
                                    //Log.e("X3D-Anchor", "   (LWP) event.getHitX() = " + event.getHitX());
                                    // highlight the icons to give visual cue to users
                                    if (event.getHitX() > cubeUISize.x / 4.0f) {
                                        if (gvrTextExitObject != null)
                                            gvrTextExitObject.setTextColor(textColorIsOver);
                                        if (gvrTextRotateObject != null)
                                            gvrTextRotateObject.setTextColor(textColorDefault);
                                        if (gvrTextTranslationObject != null)
                                            gvrTextTranslationObject.setTextColor(textColorDefault);
                                        if (gvrTextScaleObject != null)
                                            gvrTextScaleObject.setTextColor(textColorDefault);
                                        webViewTranslation = false;
                                        webViewScale = false;
                                    } else if (event.getHitX() > 0) {
                                        if (gvrTextExitObject != null)
                                            gvrTextExitObject.setTextColor(textColorDefault);
                                        if (gvrTextRotateObject != null)
                                            gvrTextRotateObject.setTextColor(textColorIsOver);
                                        if (gvrTextTranslationObject != null)
                                            gvrTextTranslationObject.setTextColor(textColorDefault);
                                        if (gvrTextScaleObject != null)
                                            gvrTextScaleObject.setTextColor(textColorDefault);
                                        webViewTranslation = false;
                                        webViewScale = false;
                                    }  // end scaling web window, hit between 0 and .25,
                                    else if (event.getHitX() > -cubeUISize.x / 4.0f) {
                                        if (gvrTextExitObject != null)
                                            gvrTextExitObject.setTextColor(textColorDefault);
                                        if (gvrTextRotateObject != null)
                                            gvrTextRotateObject.setTextColor(textColorDefault);
                                        if (gvrTextTranslationObject != null)
                                            gvrTextTranslationObject.setTextColor(textColorIsOver);
                                        if (gvrTextScaleObject != null)
                                            gvrTextScaleObject.setTextColor(textColorDefault);
                                        webViewTranslation = false;
                                        webViewScale = false;
                                    } else {
                                        if (gvrTextExitObject != null)
                                            gvrTextExitObject.setTextColor(textColorDefault);
                                        if (gvrTextRotateObject != null)
                                            gvrTextRotateObject.setTextColor(textColorDefault);
                                        if (gvrTextTranslationObject != null)
                                            gvrTextTranslationObject.setTextColor(textColorDefault);
                                        if (gvrTextScaleObject != null)
                                            gvrTextScaleObject.setTextColor(textColorIsOver);
                                        webViewTranslation = false;
                                        webViewScale = false;
                                    }
                                } else if (!event.isOver() && !uiObjectIsActive) {
                                    if (gvrTextExitObject != null)
                                        gvrTextExitObject.setTextColor(textColorDefault);
                                    if (gvrTextRotateObject != null)
                                        gvrTextRotateObject.setTextColor(textColorDefault);
                                    if (gvrTextTranslationObject != null)
                                        gvrTextTranslationObject.setTextColor(textColorDefault);
                                    if (gvrTextScaleObject != null)
                                        gvrTextScaleObject.setTextColor(textColorDefault);
                                    webViewTranslation = false;
                                    webViewScale = false;
                                }
                            }
                        });
                    }
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

        for (int i = 0; i < 3; i++ ) {
            initialHitPoint[i] = (initialCameralookAt[i] * translationObjectTranslationGlobal.z) / initialCameralookAt[2];
        }
        beginWebPagePlusUIScale[0] = webPagePlusUIScale[0];
        beginWebPagePlusUIScale[1] = webPagePlusUIScale[1];

        Log.e("X3D-Anchor", "   (SC) beginUIClickPos: (" + beginUIClickPos[0] + ", " + beginUIClickPos[1] + ", " + beginUIClickPos[2] + ")");
        Log.e("X3D-Anchor", "   (SC) beginWebPagePlusUIScale: (" + beginWebPagePlusUIScale[0] + ", " + beginWebPagePlusUIScale[1] + ")");
        Log.e("X3D-Anchor", "   (SC) initial Camera lookAt: (" + initialCameralookAt[0] + ", " + initialCameralookAt[1] + ", " + initialCameralookAt[2] + ")");
        Log.e("X3D-Anchor", "   (SC) initial Hit Pt: (" + initialHitPoint[0] + ", " + initialHitPoint[1] + ", " + initialHitPoint[2] + ")");

        mOnDrawFrame = new DrawFrame();
        gvrContext.registerDrawFrameListener(mOnDrawFrame);
    }  //  end ScaleControl


    private class PerFrameWebViewControl {

        final void onDrawFrame(float frameTime) {
            GVRScene mainScene = gvrContext.getMainScene();
            GVRCameraRig gvrCameraRig = mainScene.getMainCameraRig();
            float[] currentCameraLookAt = gvrCameraRig.getLookAt();
            if ( webViewScale ) {
                //Log.e("X3D-Anchor", "ODF: scale");
                //float[] transitionClickPt = {0, 0, translationObjectTranslationGlobal.z};
                //transitionClickPt[0] = currentCameraLookAt[0] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                //transitionClickPt[1] = currentCameraLookAt[1] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];

                //float[] scaleChange = new float[2];
                //webPagePlusUIScale[0] = (transitionClickPt[0] - beginUIClickPos[0])/beginWebPagePlusUIScale[0];
                //webPagePlusUIScale[1] = (transitionClickPt[1] - beginUIClickPos[1])/beginWebPagePlusUIScale[1];
                float zFactor = initialCameralookAt[2] / currentCameraLookAt[2];
                webPagePlusUIScale[0] = (currentCameraLookAt[0] / initialCameralookAt[0]) * zFactor * beginWebPagePlusUIScale[0];
                webPagePlusUIScale[1] = (currentCameraLookAt[1] / initialCameralookAt[1]) * zFactor * beginWebPagePlusUIScale[1];
                Log.e("X3D-Anchor", "ODF: currentCameraLookAt (" + currentCameraLookAt[0] + ", " + currentCameraLookAt[1] + ", " + currentCameraLookAt[2] + ")");
                Log.e("X3D-Anchor", "ODF:    normalized (" + (currentCameraLookAt[0] * zFactor) + ", " + (currentCameraLookAt[1] * zFactor) + ", " + (currentCameraLookAt[2] * zFactor) + ")");

                float[] currentHitPoint = new float[3];
                for (int i = 0; i < 3; i++ ) {
                    currentHitPoint[i] = (currentCameraLookAt[i] * translationObjectTranslationGlobal.z) / currentCameraLookAt[2];
                }
                Log.e("X3D-Anchor", "ODF: current Hit Pt: (" + currentHitPoint[0] + ", " + currentHitPoint[1] + ", " + currentHitPoint[2] + ")");

                Log.e("X3D-Anchor", "ODF: scaleChange (" + webPagePlusUIScale[0] + ", " + webPagePlusUIScale[1] + ")");
                //beginWebPagePlusUIScale[0] = webPagePlusUIScale[0];
                //beginWebPagePlusUIScale[1] = webPagePlusUIScale[1];
                //webPagePlusUIScale
                Log.e("X3D-Anchor", "ODF: alt scale (" + (currentHitPoint[0]/initialHitPoint[0]) + ", " + (currentHitPoint[1]/initialHitPoint[1]) + ")");

                if (webPagePlusUIScale[0] < 1 ) webPagePlusUIScale[0] = 1;
                else {
                    webPagePlusUIScale[0] = ((webPagePlusUIScale[0] - 1) / 2.0f) + 1;
                }
                Log.e("X3D-Anchor", "ODF: scaleChange modified (" + webPagePlusUIScale[0] + ", " + webPagePlusUIScale[1] + ")");
                //webPagePlusUISceneObject.getTransform().setScale(webPagePlusUIScale[0], webPagePlusUIScale[1], 1);
                webPagePlusUISceneObject.getTransform().setScale(webPagePlusUIScale[0], webPagePlusUIScale[0], 1);

            }  // end if scale
            else if ( webViewTranslation ) {

                Log.e("X3D-Anchor", "ODF: translation");
                float[] transitionClickPt = {0, 0, translationObjectTranslationGlobal.z};
                transitionClickPt[0] = currentCameraLookAt[0] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                transitionClickPt[1] = currentCameraLookAt[1] * translationObjectTranslationGlobal.z/currentCameraLookAt[2];
                for (int i = 0; i < 3; i++) {
                    transitionClickPt[i] -= diffWebPageUIClick[i];
                }
                webPagePlusUISceneObject.getTransform().setPosition( transitionClickPt[0], transitionClickPt[1], transitionClickPt[2]);
            }
        }  //  end onDrawFrame
    }  //  end private class PerFrameScripting

    private final class DrawFrame implements GVRDrawFrameListener {
        @Override
        public void onDrawFrame(float frameTime) {
            perFrameWebViewControl.onDrawFrame(frameTime);
        }
    }

    class UserHandler extends DefaultHandler {

        boolean title = false;
        boolean description = false;
        boolean link = false;
        String attributeValue;
        String titleString = "";
        String xmlElement;

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            Log.e("X3D-Anchor", "   XML parser startElement " + qName);
            if (qName.equalsIgnoreCase("title")) {
                title = true;
            }
            else if (qName.equalsIgnoreCase("description")) {
                description = true;
            }
            else if (qName.equalsIgnoreCase("description")) {
                link = true;
            }
            else if (qName.equalsIgnoreCase("media:content")) {
                String url = "";
                int height = 96;
                int width = 128;
                String type = "";
                attributeValue = attributes.getValue("url");
                if (attributeValue != null) {
                    attributeValue = attributeValue.replace("\"", ""); // remove double and
                    // single quotes
                    attributeValue = attributeValue.replace("\'", "");
                    url = attributeValue;
                }
                attributeValue = attributes.getValue("height");
                if (attributeValue != null) {
                    try {
                        height = Integer.parseInt(attributeValue);
                    }
                    catch (Exception e) {};
                }
                attributeValue = attributes.getValue("width");
                if (attributeValue != null) {
                    try {
                        width = Integer.parseInt(attributeValue);
                    }
                    catch (Exception e) {};
                }
                attributeValue = attributes.getValue("type");
                if (attributeValue != null) {
                    try {
                        type = attributeValue;
                    }
                    catch (Exception e) {};
                }
                Log.e("X3D-Anchor", "      url = " + url);
                Log.e("X3D-Anchor", "         height = " + height + ", width = " + width + ", type = " + type);
                webPageContent += "<img src=\"" + url + "\"><P>";

            }
        }  //  end startElement

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            /*
            if (qName.equalsIgnoreCase("title")) {
                //Log.e("X3D-Anchor", "      END title");
            }
            else if (qName.equalsIgnoreCase("media:content")) {
                //Log.e("X3D-Anchor", "      END media:content");
            }
            else if (qName.equalsIgnoreCase("rss")) {
                Log.e("X3D-Anchor", "      END res");
            }
            */
        }  //  end endElement

        public void characters(char ch[], int start, int length) throws SAXException {
            if (title) {
                titleString = new String(ch, start, length);
                Log.e("X3D-Anchor", "      title = " + titleString);
                webPageContent += "<P><FONT SIZE=7><B>" + titleString + "</B></FONT></P>";
                title = false;
            }
            else if (description) {
                xmlElement = new String(ch, start, length);
                Log.e("X3D-Anchor", "      description = " + xmlElement);
                webPageContent += "<P><FONT SIZE=5>" + xmlElement + "</FONT></P>";
                description = false;
            }
            else if (link) {
                xmlElement = new String(ch, start, length);
                Log.e("X3D-Anchor", "      link = " + xmlElement);
                webPageContent += "<P><FONT SIZE=6><A HREF=\"" + xmlElement + "\">" + titleString + "</A></FONT></P>";
                description = false;
            }
        }  //  end characters prsing

    }  //  end UserHandler (XML parsing)

}  //  end AnchorInteractivity



