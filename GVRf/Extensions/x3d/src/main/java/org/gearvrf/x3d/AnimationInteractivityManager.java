
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

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImage;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;

import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.script.GVRJavascriptScriptFile;

import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.script.javascript.GVRJavascriptV8File;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFColor;
import org.gearvrf.x3d.data_types.SFFloat;
import org.gearvrf.x3d.data_types.SFInt32;
import org.gearvrf.x3d.data_types.SFString;
import org.gearvrf.x3d.data_types.SFTime;
import org.gearvrf.x3d.data_types.SFVec2f;
import org.gearvrf.x3d.data_types.SFVec3f;
import org.gearvrf.x3d.data_types.SFRotation;
import org.gearvrf.x3d.data_types.MFString;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.script.Bindings;

import lu.flier.script.V8Object;


/**
 * @author m1.williams
 *         AnimationInteractivityManager will construct an InteractiveObject based on the to/from node/field Strings.
 *         If the Strings match actual objects (DEFitems, TimeSensors, Iterpolators, etc) from their
 *         respective array lists, then
 *         AnimationInteractivityManager will either construct an interactiveObject and add it to an array list,
 *         or modify an existing interactiveObject to include a 'pointer' (to the TimeSensor,
 *         Sensor, Interpolator, etc.) to tie all the sensors, timers, interpolators and
 *         defined items (Transform, Material, TextureTransform, Color, etc) into a single object.
 */

public class AnimationInteractivityManager {

    private static final String TAG = AnimationInteractivityManager.class.getSimpleName();
    private final static float FRAMES_PER_SECOND = 60.0f;
    private Vector<InteractiveObject> interactiveObjects = new Vector<InteractiveObject>();

    private static final String IS_OVER = "isOver";
    private static final String Is_ACTIVE = "isActive";

    private static final String ROTATION = "rotation";
    private static final String ORIENTATION = "orientation";
    private static final String TRANSLATION = "translation";
    private static final String POSITION = "position";
    private static final String SCALE = "scale";
    private static final String KEY_FRAME_ANIMATION = "KeyFrameAnimation_";
    private static final String INITIALIZE_FUNCTION = "initialize";
    private static final String GEARVR_INIT_JAVASCRIPT_FUNCTION_NAME = "GearVRinitJavaScript";

    public static final boolean V8JavaScriptEngine = true;


    private X3Dobject x3dObject = null;
    private GVRContext gvrContext = null;
    private GVRSceneObject root = null;
    private Vector<DefinedItem> definedItems = null;
    private Vector<Interpolator> interpolators = null;
    private Vector<Sensor> sensors = null;
    private Vector<TimeSensor> timeSensors = null;
    private Vector<EventUtility> eventUtilities = null;
    private ArrayList<ScriptObject> scriptObjects = null;

    private AnchorImplementation anchorImplementation = null;
    private GVRAnimator gvrAnimator = null;
    private GVRAssetLoader.AssetRequest assetRequest = null;


    private PerFrameScripting perFrameScripting = new PerFrameScripting();

    // Append this incremented value to GVRSceneObject names to insure unique
    // GVRSceneObjects when new GVRScene objects are generated to support animation
    private static int animationCount = 1;


    public AnimationInteractivityManager(X3Dobject x3dObject, GVRContext gvrContext,
                                         GVRSceneObject root,
                                         Vector<DefinedItem> definedItems,
                                         Vector<Interpolator> interpolators,
                                         Vector<Sensor> sensors,
                                         Vector<TimeSensor> timeSensors,
                                         Vector<EventUtility> eventUtilities,
                                         ArrayList<ScriptObject> scriptObjects,
                                         Vector<Viewpoint> viewpoints,
                                         GVRAssetLoader.AssetRequest assetRequest

    ) {
        this.x3dObject = x3dObject;
        this.gvrContext = gvrContext;
        this.root = root; // helps search for GVRSCeneObjects by name
        this.definedItems = definedItems;
        this.interpolators = interpolators;
        this.sensors = sensors;
        this.timeSensors = timeSensors;
        this.eventUtilities = eventUtilities;
        this.scriptObjects = scriptObjects;
        this.assetRequest = assetRequest;

        gvrAnimator = new GVRAnimator(this.gvrContext, true);
        root.attachComponent(gvrAnimator);

        anchorImplementation = new AnchorImplementation(this.gvrContext, this.root, viewpoints);

    }

    /**
     * buildInteractiveObject represents one X3D <ROUTE /> tag.
     * This method matches the fromNode and toNode with objects in sensors, timeSensors,
     * interpolators and DEFinded Items array lists.  It will either construct a new
     * InteractiveObject if a related <ROUTE /> has not called this method, or modify
     * an InteractiveObject if a related <ROUTE /> has been parsed here.
     * For example if a <ROUTE myTouchSensor TO myTimeSensor /> has been parsed, then another
     * call to this method <ROUTE myTimeSensor TO myInterpolator /> will match the previous
     * "myTimeSensor" and modify that InteractiveObject
     * The 4 parameters are from an X3D <ROUTE /> node
     * For example: <ROUTE fromNode.fromField to toNode.toField />
     *
     * @param fromNode
     * @param fromField
     * @param toNode
     * @param toField
     */
    public void buildInteractiveObject(String fromNode, String fromField, String toNode, String toField) {
        Sensor routeFromSensor = null;
        TimeSensor routeToTimeSensor = null;
        TimeSensor routeFromTimeSensor = null;
        Interpolator routeToInterpolator = null;
        Interpolator routeFromInterpolator = null;
        EventUtility routeToEventUtility = null;
        EventUtility routeFromEventUtility = null;
        DefinedItem routeToDefinedItem = null;
        DefinedItem routeFromDefinedItem = null; // used passing items into a Script
        ScriptObject routeFromScriptObject = null;
        ScriptObject routeToScriptObject = null;

        // Get pointers to the Sensor, TimeSensor, Interpolator,
        // EventUtility (such as BooleanToggle), ScriptObject
        // and/or Defined Items based the nodes of this object
        for (Sensor sensor : sensors) {
            if (sensor.getName().equalsIgnoreCase(fromNode)) {
                routeFromSensor = sensor;
            }
        }

        for (TimeSensor timeSensor : timeSensors) {
            if (timeSensor.name.equalsIgnoreCase(toNode)) {
                routeToTimeSensor = timeSensor;
            } else if (timeSensor.name.equalsIgnoreCase(fromNode)) {
                routeFromTimeSensor = timeSensor;
            }
        }

        for (Interpolator interpolator : interpolators) {
            if (interpolator.name.equalsIgnoreCase(toNode)) {
                routeToInterpolator = interpolator;
            } else if (interpolator.name.equalsIgnoreCase(fromNode)) {
                routeFromInterpolator = interpolator;
            }
        }

        for (EventUtility eventUtility : eventUtilities) {
            if (eventUtility.getName().equalsIgnoreCase(toNode)) {
                routeToEventUtility = eventUtility;
            } else if (eventUtility.getName().equalsIgnoreCase(fromNode)) {
                routeFromEventUtility = eventUtility;
            }
        }

        for (ScriptObject scriptObject : scriptObjects) {
            if (scriptObject.getName().equalsIgnoreCase(toNode)) {
                routeToScriptObject = scriptObject;
            } else if (scriptObject.getName().equalsIgnoreCase(fromNode)) {
                routeFromScriptObject = scriptObject;
            }
        }

        for (DefinedItem definedItem : definedItems) {
            if (definedItem.getName().equalsIgnoreCase(toNode)) {
                routeToDefinedItem = definedItem;
            } else if (definedItem.getName().equalsIgnoreCase(fromNode)) {
                routeFromDefinedItem = definedItem;
            }
        }

        // Now build the InteractiveObject by assigning pointers
        // to an existing InteractiveObject matches non-null links
        // or create a new InteractiveObject.
        // The flow is to test where the ROUTE TO goes (instead of FROM)

        // ROUTE TO a TimeSensor
        if (routeToTimeSensor != null) {
            boolean routeToTimeSensorFound = false;
            for (InteractiveObject interactiveObject : interactiveObjects) {
                if (routeToTimeSensor == interactiveObject.getTimeSensor()) {
                    if (interactiveObject.getSensor() == null) {
                        //This sensor already exists inside an Interactive Object
                        interactiveObject.setSensor(routeFromSensor, fromField);
                        routeToTimeSensorFound = true;
                    }
                }
                else if (routeFromScriptObject != null) {
                    // a rare case where a Script node sends data to a TimeSensor
                    if (routeFromScriptObject == interactiveObject.getScriptObject()) {
                        //TODO: complete adding field to this Script Node when sent to TimeSensor
                        for (ScriptObject.Field field : routeFromScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeFromScriptObject.getFieldName(field))) {
                                //routeFromScriptObject.setFromEventUtility(field, routeFromEventUtility, fromField);
                                interactiveObject.getScriptObject().setToTimeSensor(field, routeToTimeSensor, toField);
                                routeToTimeSensorFound = true;
                            }
                        }
                    }
                }
            }
            if (!routeToTimeSensorFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                InteractiveObject interactiveObject = new InteractiveObject();
                interactiveObject.setSensor(routeFromSensor, fromField);
                interactiveObject.setTimeSensor(routeToTimeSensor);
                interactiveObjects.add(interactiveObject);
            }
        }  //  end route To TimeSensor

        // ROUTE TO an Interpolator (Position, Rotation, etc)
        if (routeToInterpolator != null) {
            boolean routeToInterpolatorFound = false;
            for (InteractiveObject interactiveObject : interactiveObjects) {
                if (routeToInterpolator == interactiveObject.getInterpolator()) {
                    if (interactiveObject.getTimeSensor() == null) {
                        //This sensor already exists as part of an interactive Object
                        interactiveObject.setTimeSensor(routeFromTimeSensor);
                        routeToInterpolatorFound = true;
                    }
                }
            }
            if (!routeToInterpolatorFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                for (InteractiveObject interactiveObject : interactiveObjects) {
                    if (routeFromTimeSensor == interactiveObject.getTimeSensor()) {
                        if ((interactiveObject.getInterpolator() == null)
                                && (interactiveObject.getScriptObject() == null)) {
                            //This timer already exists as part of an interactive Object
                            interactiveObject.setInterpolator(routeToInterpolator);
                            routeToInterpolatorFound = true;
                        }
                    }
                }
            }
            if (!routeToInterpolatorFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                InteractiveObject interactiveObject = new InteractiveObject();
                interactiveObject.setTimeSensor(routeFromTimeSensor);
                interactiveObject.setInterpolator(routeToInterpolator);
                interactiveObjects.add(interactiveObject);
            }
        }  //  end route To Interpolator

        // ROUTE TO an Event Utility (such as a BooleanToggle
        if (routeToEventUtility != null) {
            boolean routeToEventUtilityFound = false;
            for (InteractiveObject interactiveObject : interactiveObjects) {
                if (routeToEventUtility == interactiveObject.getEventUtility()) {
                    if ( (interactiveObject.getSensor() == null) && (routeFromSensor != null) ) {
                        interactiveObject.setSensor(routeFromSensor, fromField);
                        routeToEventUtilityFound = true;
                    }
                    else if ( (interactiveObject.getScriptObject() == null) && (routeFromScriptObject != null) ) {
                        for (ScriptObject.Field field : routeFromScriptObject.getFieldsArrayList()) {
                            if (fromField.equalsIgnoreCase(routeFromScriptObject.getFieldName(field))) {
                                routeFromScriptObject.setToEventUtility(field, routeToEventUtility, toField);
                                routeToEventUtilityFound = true;
                            }
                        }
                    }
                }
            }
            if (!routeToEventUtilityFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                InteractiveObject interactiveObject = new InteractiveObject();
                interactiveObject.setSensor(routeFromSensor, fromField);
                interactiveObject.setEventUtility(routeToEventUtility);
                interactiveObject.setScriptObject(routeFromScriptObject);
                interactiveObjects.add(interactiveObject);
            }
        }  //  end routeToEventUtility

        // ROUTE TO a Script Object
        if (routeToScriptObject != null) {
            boolean routeToScriptObjectFound = false;
            for (InteractiveObject interactiveObject : interactiveObjects) {
                if (routeToScriptObject == interactiveObject.getScriptObject()) {
                    if ((interactiveObject.getSensor() == null) && (routeFromSensor != null)) {
                        //This sensor already exists as part of an interactive Object
                        interactiveObject.setSensor(routeFromSensor, fromField);
                        routeToScriptObjectFound = true;
                    }  //  end Sensor
                    else if ((interactiveObject.getDefinedItem() == null) && (routeFromDefinedItem != null)) {
                        //This defined Item already exists as part of an interactive Object
                        for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                                routeToScriptObject.setFromDefinedItem(field, routeFromDefinedItem, fromField);
                            }
                        }
                        routeToScriptObjectFound = true;
                    }  // end definedItem
                    else if ((interactiveObject.getEventUtility() == null) && (routeFromEventUtility != null)) {
                        //This event utility already exists as part of an interactive Object
                        for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                                routeToScriptObject.setFromEventUtility(field, routeFromEventUtility, fromField);
                            }
                        }
                        routeToScriptObjectFound = true;
                    } // end eventUtility

                    else if ((interactiveObject.getTimeSensor() == null) && (routeFromTimeSensor != null)) {
                        //This sensor already exists as part of an interactive Object
                        for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                                routeToScriptObject.setFromTimeSensor(field, routeFromTimeSensor, fromField);
                            }
                        }
                        routeToScriptObjectFound = true;
                    } // end timeSensor

                }
            }
            if (!routeToScriptObjectFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                InteractiveObject interactiveObject = new InteractiveObject();
                interactiveObject.setScriptObject(routeToScriptObject);
                if (routeFromSensor != null) {
                    interactiveObject.setSensor(routeFromSensor, fromField);
                }
                else if (routeFromDefinedItem != null) {
                    // happens when scripting and sending values from item to the script
                    interactiveObject.setDefinedItemFromField(routeFromDefinedItem, fromField);
                    for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                        if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                            routeToScriptObject.setFromDefinedItem(field, routeFromDefinedItem, fromField);
                        }
                    }
                }
                else if (routeFromEventUtility != null) {
                    String fieldName = routeToScriptObject.getFieldName(routeToScriptObject.getField(0));
                    if ( !fieldName.equalsIgnoreCase(toField) ) {
                        // the first field, which is the name of the function
                        for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                                routeToScriptObject.setFromEventUtility(field, routeFromEventUtility, fromField);
                            }
                        }
                    }
                    else interactiveObject.setEventUtility(routeFromEventUtility);
                }
                else if (routeFromTimeSensor != null) {
                    String fieldName = routeToScriptObject.getFieldName(routeToScriptObject.getField(0));
                    if ( !fieldName.equalsIgnoreCase(toField) ) {
                        // the first field, which is the name of the function
                        for (ScriptObject.Field field : routeToScriptObject.getFieldsArrayList()) {
                            if (toField.equalsIgnoreCase(routeToScriptObject.getFieldName(field))) {
                                routeToScriptObject.setFromTimeSensor(field, routeFromTimeSensor, fromField);
                            }
                        }
                    }
                    else interactiveObject.setTimeSensor(routeFromTimeSensor);
                }
                interactiveObjects.add(interactiveObject);
            }
        }  //  end routeToScriptObject

        // ROUTE TO a DEFind Object
        if (routeToDefinedItem != null) {
            boolean routeToDEFinedItemFound = false;
            for (InteractiveObject interactiveObject : interactiveObjects) {
                if ((routeFromInterpolator == interactiveObject.getInterpolator()) &&
                        (routeFromInterpolator != null)) {
                    if (interactiveObject.getDefinedItemToField() == null) {
                        interactiveObject.setDefinedItemToField(routeToDefinedItem, toField);
                        routeToDEFinedItemFound = true;
                    }
                } else if ((routeFromEventUtility == interactiveObject.getEventUtility()) &&
                        (routeFromEventUtility != null)) {
                    if (interactiveObject.getDefinedItemToField() == null) {
                        interactiveObject.setDefinedItemToField(routeToDefinedItem, toField);
                        routeToDEFinedItemFound = true;
                    }
                } else if ((routeFromScriptObject == interactiveObject.getScriptObject()) &&
                        (routeFromScriptObject != null)) {
                    if (interactiveObject.getDefinedItemToField() == null) {
                        for (ScriptObject.Field field : routeFromScriptObject.getFieldsArrayList()) {
                            if (fromField.equalsIgnoreCase(routeFromScriptObject.getFieldName(field))) {
                                routeFromScriptObject.setToDefinedItem(field, routeToDefinedItem, toField);
                            }
                        }
                        routeToDEFinedItemFound = true;
                    }
                }
            }
            if (!routeToDEFinedItemFound) {
                // construct a new interactiveObject for this sensor and timeSensor
                InteractiveObject interactiveObject = new InteractiveObject();
                interactiveObject.setInterpolator(routeFromInterpolator);
                interactiveObject.setDefinedItemToField(routeToDefinedItem, toField);
                if (routeFromSensor != null)
                    interactiveObject.setSensor(routeFromSensor, fromField);
                if (routeFromScriptObject != null) {
                    for (ScriptObject.Field field : routeFromScriptObject.getFieldsArrayList()) {
                        if (fromField.equalsIgnoreCase(routeFromScriptObject.getFieldName(field))) {
                            routeFromScriptObject.setToDefinedItem(field, routeToDefinedItem, toField);
                        }
                    }

                }
                if (routeFromEventUtility != null) {
                    interactiveObject.setEventUtility(routeFromEventUtility);
                }
                interactiveObjects.add(interactiveObject);
            }
        }  //  end if routeToDefinedItem != null
    }  //  end buildInteractiveObject


    /**
     * BuildInteractiveObjectFromAnchor is a special type of interactive object in that it does not get
     * built using ROUTE's.
     *
     * @param anchorSensor is the Sensor that describes the sensor set to an Anchor
     * @param anchorDestination is either another Viewpoint, url to a web site or another x3d scene
     */
    public void BuildInteractiveObjectFromAnchor(Sensor anchorSensor, String anchorDestination) {
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setSensor(anchorSensor, anchorDestination);
        interactiveObjects.add(interactiveObject);
    }

    /**
     * initAnimationsAndInteractivity() called when we parse </scene> in
     * an X3D file.  This method will parse the array list of InteractiveObjects
     * determining which are animations (when interactiveObject.sensor is null)
     * or which are interactive and thus have a event attached to invoke the
     * animation upon a TouchSensor, Anchor, etc.
     */
    public void initAnimationsAndInteractivity() {
        for (InteractiveObject interactiveObject : interactiveObjects) {
            GVRAnimationChannel gvrAnimationChannel = null;
            GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
            GVRSceneObject gvrAnimatedObject = null;

            // both animated and interactive objects currently must have a time
            // sensor, interpolator and a Transform node with a DEF="..." parameter
            if ((interactiveObject.getTimeSensor() != null) &&
                    (interactiveObject.getInterpolator() != null) &&
                    (interactiveObject.getDefinedItem() != null)) {
                // Set up the animation objects, properties
                //   first construct the animation channel based on translation, rotation, scale, etc.
                if ((interactiveObject.getDefinedItemToField().toLowerCase().endsWith(TRANSLATION)) ||
                        (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(POSITION))) {
                    gvrAnimatedObject = root
                            .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_TRANSLATION_));
                    gvrAnimationChannel = new GVRAnimationChannel(
                            gvrAnimatedObject.getName(),
                            interactiveObject.getInterpolator().key.length, 0, 0,
                            GVRAnimationBehavior.LINEAR, GVRAnimationBehavior.LINEAR);
                    for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++) {
                        gvrAnimationChannel.setPosKeyVector(j,
                                interactiveObject.getInterpolator().key[j]
                                        * interactiveObject.getTimeSensor().getCycleInterval()
                                        * FRAMES_PER_SECOND, interactiveObject.getInterpolator().keyValue[j * 3],
                                interactiveObject.getInterpolator().keyValue[j * 3 + 1],
                                interactiveObject.getInterpolator().keyValue[j * 3 + 2]);
                    }
                }  //  end translation

                else if ((interactiveObject.getDefinedItemToField().toLowerCase().endsWith(ROTATION)) ||
                        (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(ORIENTATION))) {
                    gvrAnimatedObject = root
                            .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_ROTATION_));
                    gvrAnimationChannel = new GVRAnimationChannel(
                            gvrAnimatedObject.getName(), 0,
                            interactiveObject.getInterpolator().key.length, 0,
                            GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);

                    for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++) {
                        AxisAngle4f axisAngle4f = new AxisAngle4f(
                                interactiveObject.getInterpolator().keyValue[j * 4 + 3],
                                interactiveObject.getInterpolator().keyValue[j * 4],
                                interactiveObject.getInterpolator().keyValue[j * 4 + 1],
                                interactiveObject.getInterpolator().keyValue[j * 4 + 2]);
                        Quaternionf quaternionf = new Quaternionf(axisAngle4f);
                        gvrAnimationChannel.setRotKeyQuaternion(j,
                                interactiveObject.getInterpolator().key[j]
                                        * interactiveObject.getTimeSensor().getCycleInterval()
                                        * FRAMES_PER_SECOND, quaternionf);
                    }
                }   //  end rotation

                else if (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(SCALE)) {
                    gvrAnimatedObject = root
                            .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_SCALE_));
                    gvrAnimationChannel = new GVRAnimationChannel(
                            gvrAnimatedObject.getName(), 0, 0,
                            interactiveObject.getInterpolator().key.length, GVRAnimationBehavior.DEFAULT,
                            GVRAnimationBehavior.DEFAULT);
                    for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++) {
                        gvrAnimationChannel.setScaleKeyVector(j,
                                interactiveObject.getInterpolator().key[j]
                                        * interactiveObject.getTimeSensor().getCycleInterval()
                                        * FRAMES_PER_SECOND, interactiveObject.getInterpolator().keyValue[j * 3],
                                interactiveObject.getInterpolator().keyValue[j * 3 + 1],
                                interactiveObject.getInterpolator().keyValue[j * 3 + 2]);
                    }
                }  //  end scale
                else {
                    Log.e(TAG, "'" + interactiveObject.getDefinedItemToField() + "' not implemented");
                }

                // Second, set up the KeyFrameAnimation object
                if (gvrAnimatedObject != null) {
                    gvrKeyFrameAnimation = new GVRKeyFrameAnimation(
                            gvrAnimatedObject.getName() + KEY_FRAME_ANIMATION + animationCount,
                            gvrAnimatedObject,
                            interactiveObject.getTimeSensor().getCycleInterval() * FRAMES_PER_SECOND,
                            FRAMES_PER_SECOND);
                    gvrKeyFrameAnimation.addChannel(gvrAnimationChannel);
                    if (interactiveObject.getTimeSensor().getLoop()) {
                        gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
                        gvrKeyFrameAnimation.setRepeatCount(-1);
                    }
                    gvrKeyFrameAnimation.prepare();
                    animationCount++;
                    interactiveObject.getTimeSensor().addGVRKeyFrameAnimation( gvrKeyFrameAnimation );

                    // Third, determine if this will be animation only, or
                    // interactive triggered in picking
                    if (interactiveObject.getSensor() == null) {
                        // this is an animation without interactivity
                        gvrAnimator.addAnimation(gvrKeyFrameAnimation);

                    } else {
                        // this is an interactive object
                        final InteractiveObject interactiveObjectFinal = interactiveObject;
                        final GVRKeyFrameAnimation gvrKeyFrameAnimationFinal = gvrKeyFrameAnimation;
                        interactiveObject.getSensor().getOwnerObject().forAllDescendants(
                                new GVRSceneObject.SceneVisitor()
                                {
                                    public boolean visit (GVRSceneObject obj)
                                    {
                                        obj.attachCollider(new GVRMeshCollider(gvrContext, true));
                                        return true;
                                    }
                                });

                        interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                            boolean isRunning;

                            @Override
                            public void onSensorEvent(SensorEvent event) {
                                //Setup SensorEvent callback here
                                if ((event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) ||
                                        (event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                                .IS_ACTIVE))) {
                                    if (!isRunning) {
                                        isRunning = true;
                                        interactiveObjectFinal.getSensor().setHitPoint(event.getPickedObject().getHitLocation());
                                        gvrKeyFrameAnimationFinal.start(gvrContext.getAnimationEngine())
                                                .setOnFinish(new GVROnFinish() {
                                                    @Override
                                                    public void finished(GVRAnimation animation) {
                                                        isRunning = false;
                                                    }
                                                });
                                    }
                                }
                            }
                        });

                    }
                } else {
                    Log.e(TAG, "'" + interactiveObject.getDefinedItem().getName() + "' possibly not found in the scene.");
                }
            }  // end if at least timer, interpolator and defined object

            // Sensor (such as TouchSensor) to an EventUnity (such as BoleanToggle)
            //   to a DEFined Object
            else if ((interactiveObject.getSensor() != null) &&
                    (interactiveObject.getEventUtility() != null) &&
                    (interactiveObject.getDefinedItem() != null)) {
                // a sensor, eventUtility (such as BooleanToggle) and defined object found
                final InteractiveObject interactiveObjectFinal = interactiveObject;
                final Vector<InteractiveObject> interactiveObjectsFinal = interactiveObjects;

                if (interactiveObject.getSensor().getSensorType() == Sensor.Type.TOUCH) {
                    interactiveObject.getSensor().getOwnerObject().forAllDescendants(
                        new GVRSceneObject.SceneVisitor()
                        {
                            public boolean visit (GVRSceneObject obj)
                            {
                                obj.attachCollider(new GVRMeshCollider(gvrContext, true));
                                return true;
                            }
                        });
                    interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                        boolean stateChanged = false;

                        @Override
                        public void onSensorEvent(SensorEvent event) {
                            if ((event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) ||
                                    (event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                            .IS_ACTIVE))) {
                                if (!stateChanged) {
                                    stateChanged = true;
                                    EventUtility eventUtility = interactiveObjectFinal.getEventUtility();
                                    eventUtility.setToggle(!eventUtility.getToggle());
                                    for (InteractiveObject interactiveObject : interactiveObjectsFinal) {
                                        if (interactiveObject.getEventUtility() == interactiveObjectFinal.getEventUtility()) {
                                            GVRSceneObject gvrSceneObject = root
                                                    .getSceneObjectByName(interactiveObject.getDefinedItem().getName());
                                            GVRComponent gvrComponent = gvrSceneObject.getComponent(
                                                    GVRLight.getComponentType());
                                            gvrComponent.setEnable(eventUtility.getToggle());
                                        }
                                    }
                                }
                            } else if (!event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                                stateChanged = false;
                            } else if (!event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                    .IS_OVER)) {
                                stateChanged = false;
                            }
                        }
                    });
                }  // end if sensor == TOUCH
            }  // end if at least sensor, eventUtility and defined object

            // Sensor (such as TouchSensor) to a Script
            //   to a DEFined Object
            else if (interactiveObject.getScriptObject() != null) {
                // A Sensor with a Script and defined object found
                final InteractiveObject interactiveObjectFinal = interactiveObject;

                if (interactiveObject.getSensor() != null) {
                    if (interactiveObject.getSensor().getSensorType() == Sensor.Type.TOUCH) {
                        interactiveObject.getSensor().getOwnerObject().forAllDescendants(
                                new GVRSceneObject.SceneVisitor()
                                {
                                    public boolean visit (GVRSceneObject obj)
                                    {
                                        obj.attachCollider(new GVRMeshCollider(gvrContext, true));
                                        return true;
                                    }
                                });
                        interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                            boolean stateChanged = false;
                            boolean isActiveDone = false;

                            @Override
                            public void onSensorEvent(SensorEvent event) {

                                Object[] parameters = SetJavaScriptArguments(interactiveObjectFinal, event.isOver(), stateChanged);
                                ScriptObject scriptObject = interactiveObjectFinal.getScriptObject();
                                ScriptObject.Field firstField = scriptObject.getField(0);
                                String functionName = scriptObject.getFieldName(firstField);

                                if (interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) {
                                    parameters[0] = event.isOver();
                                }
                                else if (interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                                    parameters[0] = stateChanged;
                                }
                                if (scriptObject.getTimeStampParameter()) {
                                    parameters[1] = 0;  // set timeStamp to 0.  This isn't used for isOver/isActive events
                                }

                                if ((event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER))) {
                                    // OVER an object with a sensor
                                    if (!stateChanged) {
                                        stateChanged = true;
                                        // Run this SCRIPT's actual JavaScript function
                                        RunScript(interactiveObjectFinal, functionName, parameters);
                                    }
                                } else if (event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                                    // CLICKED while over a sensored object
                                    stateChanged = !stateChanged;
                                    if (!isActiveDone) {
                                        // Run this SCRIPT's actual JavaScript function
                                        RunScript(interactiveObjectFinal, functionName, parameters);
                                    }
                                    isActiveDone = true;
                                } else if (!event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                        .IS_OVER)) {
                                    // An "isOver event', but just existed being over the object - i.e. TouchSensor = false
                                    stateChanged = false;
                                    // Run this SCRIPT's actual JavaScript function
                                    RunScript(interactiveObjectFinal, functionName, parameters);
                                } else if (!event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                                    isActiveDone = false;
                                }
                            }
                        });
                    }  // end if sensor == TOUCH
                }   // end if sensor != null
                else if (interactiveObject.getTimeSensor() != null) {
                    // TimeSensor means this Script will be called per-frame
                    // set up the call-back
                    interactiveObject.getScriptObject().setScriptCalledPerFrame(true);
                    perFrameScripting.setInteractiveObjectVars(interactiveObjectFinal);
                } // time sensor != null

            }  // end if a Script (that likely includes a sensor)

            else if ((interactiveObject.getSensor() != null) &&
                    (interactiveObject.getDefinedItem() != null)) {
                // a sensor and defined object, such as a TouchSensor
                //    to a Boolean such as light on/off
                final InteractiveObject interactiveObjectFinal = interactiveObject;
                if (interactiveObject.getSensor().getSensorType() == Sensor.Type.TOUCH) {
                    interactiveObject.getSensor().getOwnerObject().forAllDescendants(
                            new GVRSceneObject.SceneVisitor()
                            {
                                public boolean visit (GVRSceneObject obj)
                                {
                                    obj.attachCollider(new GVRMeshCollider(gvrContext, true));
                                    return true;
                                }
                            });
                    interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                        boolean isMovieStateSet = false;
                        @Override
                        public void onSensorEvent(SensorEvent event) {
                            //Setup SensorEvent callback here
                            GVRSceneObject gvrSceneObject = root
                                    .getSceneObjectByName(interactiveObjectFinal.getDefinedItem().getName());
                            GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLight.getComponentType());

                            if ( gvrComponent != null ) {
                                if (event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) {
                                    if (gvrComponent != null) gvrComponent.setEnable(true);
                                } else {
                                    if (gvrComponent != null) gvrComponent.setEnable(false);
                               }
                            }
                            else if ( gvrSceneObject instanceof GVRVideoSceneObject) {
                                // isOver, but only go thru once per isOver.
                                if ( event.isOver() && !isMovieStateSet) {
                                    GVRVideoSceneObject gvrVideoSceneObject = (GVRVideoSceneObject) gvrSceneObject;
                                    GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = gvrVideoSceneObject.getMediaPlayer();
                                    try {
                                        if (interactiveObjectFinal.getSensorFromField().contains("touchTime")) {
                                            if (interactiveObjectFinal.getDefinedItemToField().endsWith("stopTime")) {
                                                gvrVideoSceneObjectPlayer.pause();
                                                ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                                exoPlayer.seekTo(0);
                                            } else if (interactiveObjectFinal.getDefinedItemToField().endsWith("pauseTime")) {
                                                gvrVideoSceneObjectPlayer.pause();
                                            } else if (interactiveObjectFinal.getDefinedItemToField().endsWith("startTime")) {
                                                gvrVideoSceneObjectPlayer.start();
                                            } else {
                                                Log.e(TAG, "Error: ROUTE to MovieTexture, " + interactiveObjectFinal.getDefinedItemToField() + " not implemented");
                                            }
                                        } else {
                                            Log.e(TAG, "Error: ROUTE to MovieTexture, " + interactiveObjectFinal.getSensorFromField() + " not implemented");
                                        }
                                    } catch (IllegalStateException e) {
                                        Log.e(TAG, "X3D Movie Texture: IllegalStateException: " + e);
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        Log.e(TAG, "X3D Movie Texture Exception: " + e);
                                        e.printStackTrace();
                                    }
                                    isMovieStateSet = true;
                                } // end if event.isOver()
                                else if ( !event.isOver() && isMovieStateSet){
                                    // No longer over the TouchSensor
                                    isMovieStateSet = false;
                                }
                            } // end if gvrSceneObject
                        }
                    });
                }  // end if sensor == TOUCH
            }  //  end sensor and definedItem != null
            // Sensor (such as TouchSensor) to an EventUnity (such as BoleanToggle)
            else if ((interactiveObject.getSensor() != null) &&
                    (interactiveObject.getEventUtility() != null)) {
                // a sensor, eventUtility (such as BooleanToggle) and defined object found
                final InteractiveObject interactiveObjectFinal = interactiveObject;
                final Vector<InteractiveObject> interactiveObjectsFinal = interactiveObjects;

                if (interactiveObject.getSensor().getSensorType() == Sensor.Type.TOUCH) {
                    interactiveObject.getSensor().getOwnerObject().forAllDescendants(
                            new GVRSceneObject.SceneVisitor()
                            {
                                public boolean visit (GVRSceneObject obj)
                                {
                                    obj.attachCollider(new GVRMeshCollider(gvrContext, true));
                                    return true;
                                }
                            });
                    interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                        boolean stateChanged = false;

                        @Override
                        public void onSensorEvent(SensorEvent event) {
                            if ((event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) ||
                                    (event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                            .IS_ACTIVE))) {
                                if (!stateChanged) {
                                    // only change state upon first rollover, not the 'roll off'
                                    stateChanged = true;
                                    EventUtility eventUtility = interactiveObjectFinal.getEventUtility();
                                    eventUtility.setToggle(!eventUtility.getToggle());
                                }
                            } else if (!event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                                stateChanged = false;
                            } else if (!event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                                    .IS_OVER)) {
                                stateChanged = false;
                            }
                        }
                    });
                }  // end if sensor == TOUCH
            }  // end if at least sensor, and eventUtility
            else if (interactiveObject.getSensor() != null) {
                // Likely this is an Anchor tag since there are no routes with it
                if ( interactiveObject.getSensor().getSensorType() == Sensor.Type.ANCHOR) {
                    anchorImplementation.AnchorInteractivity( interactiveObject );
                }  //  end if Sensor Type is Anchor
            } // end sensor != null

        }  // end for loop traversing through all interactive objects
        // Initiate all the animations, both keyframe and procedural
        if (perFrameScripting.getRunState()) {
            final GVRDrawFrameListener mOnDrawFrame = new DrawFrame();
            gvrContext.registerDrawFrameListener(mOnDrawFrame);
        }
    }   //  end initAnimationsAndInteractivity.


    private class PerFrameScripting {

        InteractiveObject interactiveObjectFinal = null;
        ScriptObject scriptObject = null;
        ScriptObject.Field firstField = null;
        String functionName;
        Object[] parameters = null;
        boolean run = false;
        boolean firstFrameRun_MustInitalize = true;
        float accumulatedTime = 0;
        float cycleInterval = 1;

        final void setInteractiveObjectVars(InteractiveObject interactiveObjectFinal) {

            this.interactiveObjectFinal = interactiveObjectFinal;
            scriptObject = interactiveObjectFinal.getScriptObject();
            firstField = scriptObject.getField(0);
            functionName = scriptObject.getFieldName(firstField);

            String javaScriptCode = scriptObject.getJavaScriptCode();
            int index = 0;
            while (index != -1) {
                index = javaScriptCode.indexOf("function", index);
                if (index != -1) {
                    String funcName = javaScriptCode.substring(index, javaScriptCode.indexOf('(', index));
                    funcName = funcName.substring(funcName.indexOf(' ') + 1, funcName.length());
                    javaScriptCode = javaScriptCode.substring(javaScriptCode.indexOf('(', index), javaScriptCode.length());
                    String paramterString = javaScriptCode.substring(1, javaScriptCode.indexOf(')'));
                    if (paramterString.indexOf(',') != -1) {
                        // we have two parameters to this function and thus the second parameter is the timeStamp
                        scriptObject.setTimeStampParameter(true);
                    }
                    javaScriptCode = javaScriptCode.substring(javaScriptCode.indexOf(')') + 1, javaScriptCode.length());
                }
            }

            cycleInterval = this.interactiveObjectFinal.getTimeSensor().getCycleInterval();
            if (cycleInterval <= 0) cycleInterval = 1;

            BuildInitJavaScript(interactiveObjectFinal);

            parameters = SetJavaScriptArguments(this.interactiveObjectFinal, 0, false); // false is just a place holder
            parameters[0] = 0;
            if (scriptObject.getTimeStampParameter()) parameters[1] = 0;

            run = true;
        }  //  end setInteractiveObjectVars

        public boolean getRunState() {
            return run;
        }

        final void onDrawFrame(float frameTime) {
            if ( interactiveObjectFinal.getScriptObject().getInitializationDone() ) {
                if ( firstFrameRun_MustInitalize ) {
                    String paramString = "var params =[";
                    for (int i = 0; i < parameters.length; i++ ) {
                        paramString += (parameters[i] + ", ");
                    }
                    paramString = paramString.substring(0, (paramString.length()-2)) + "];";

                    GVRJavascriptV8File gvrJavascriptV8File = interactiveObjectFinal.getScriptObject().getGVRJavascriptV8File();

                    final GVRJavascriptV8File gvrJavascriptV8FileFinal = gvrJavascriptV8File;
                    final Object[] parametersFinal = parameters;
                    final String paramStringFinal = paramString;
                    gvrContext.runOnGlThread(new Runnable() {
                        @Override
                        public void run() {
                            RunInitializeScriptThread( gvrJavascriptV8FileFinal, interactiveObjectFinal, parametersFinal, paramStringFinal);
                            firstFrameRun_MustInitalize = false;
                        }
                    });
                }
                // once we run through the initialization of this script, then we can Run the script
                parameters = SetJavaScriptArguments(this.interactiveObjectFinal, 0, false); // false is just a place holder
                accumulatedTime += frameTime;
                parameters[0] = accumulatedTime % cycleInterval;
                if (scriptObject.getTimeStampParameter()) parameters[1] = accumulatedTime;
                // Run this SCRIPT's actal JavaScript function
                RunScript(interactiveObjectFinal, functionName, parameters);
            }
        }  //  end onDrawFrame
    }  //  end private class PerFrameScripting



    private final class DrawFrame implements GVRDrawFrameListener {
        @Override
        public void onDrawFrame(float frameTime) {
            perFrameScripting.onDrawFrame(frameTime);
        }
    }

    /* Allows string matching fields, handling mis-matched case, if value has 'set' so
    'set_translation' and 'translation' or 'translation_changed' and 'translation' match.
    Also gets rid of leading and trailing spaces.  All possible in JavaScript and X3D Routes.
     */
    private boolean StringFieldMatch (String original, String matching) {
        boolean equal = false;
        original = original.toLowerCase().trim();
        matching = matching.toLowerCase();
        if (original.endsWith(matching)) {
            equal = true;
        }
        else if (original.startsWith(matching)) {
            equal = true;
        }
        return equal;
    }

    // funtion called each event and sets the arguments (parameters)
    // from INPUT_ONLY and INPUT_OUTPUT to the function that 'compiles' and run JavaScript
    private Object[] SetJavaScriptArguments(InteractiveObject interactiveObj, Object argument0, boolean stateChanged) {
        ArrayList<Object> scriptParameters = new ArrayList<Object>();

        ScriptObject scriptObject = interactiveObj.getScriptObject();

        // Get the parameters/values passed to the Script/JavaScript
        for (ScriptObject.Field field : scriptObject.getFieldsArrayList()) {
            if ((scriptObject.getFieldAccessType(field) == ScriptObject.AccessType.INPUT_OUTPUT) ||
                    (scriptObject.getFieldAccessType(field) == ScriptObject.AccessType.INPUT_ONLY)) {
                String fieldType = scriptObject.getFieldType(field);
                DefinedItem definedItem = scriptObject.getFromDefinedItem(field);
                EventUtility eventUtility = scriptObject.getFromEventUtility(field);
                TimeSensor timeSensor = scriptObject.getFromTimeSensor(field);

                if (fieldType.equalsIgnoreCase("SFBool")) {
                    if (definedItem != null) {
                        if (definedItem.getGVRSceneObject() != null) {
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(
                                    GVRLight.getComponentType());
                            if (gvrComponent != null) {
                                scriptParameters.add(gvrComponent.isEnabled());
                            }
                        }
                    }
                    else if (eventUtility != null) {
                        scriptParameters.add( eventUtility.getToggle() );
                    }
                    else if (interactiveObj.getSensorFromField() != null) {
                        if (interactiveObj.getSensorFromField().equals(Sensor.IS_OVER)) {
                            scriptParameters.add(argument0);
                        }
                        else if (interactiveObj.getSensorFromField().equals(Sensor.IS_ACTIVE)) {
                            scriptParameters.add(!stateChanged);
                        }
                    }
                    else if ( interactiveObj.getEventUtility() != null) {
                        scriptParameters.add( interactiveObj.getEventUtility().getToggle() );
                    }
                }  // end if SFBool
                else if ((fieldType.equalsIgnoreCase("SFFloat")) && (definedItem == null)) {
                    if (timeSensor != null) {
                        scriptParameters.add( timeSensor.getCycleInterval() );
                    }
                    else scriptParameters.add(argument0); // the time passed in from an SFTime node
                } else if (scriptObject.getFromDefinedItem(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFColor")) {
                        float[] color = {0, 0, 0};
                        if (definedItem.getGVRMaterial() != null) {
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "diffuseColor") ) {
                                float[] diffuseColor = definedItem.getGVRMaterial().getVec4("diffuse_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = diffuseColor[i]; // only get the first 3 values, not the alpha value
                                }
                            } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "emissiveColor") ) {
                                float[] emissiveColor = definedItem.getGVRMaterial().getVec4("emissive_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = emissiveColor[i]; // only get the first 3 values, not the alpha value
                                }
                            } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "specularColor") ) {
                                float[] specularColor = definedItem.getGVRMaterial().getVec4("specular_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = specularColor[i]; // only get the first 3 values, not the alpha value
                                }
                            }
                        } else if (definedItem.getGVRSceneObject() != null) {
                            // likely a light object so get its properties
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(
                                    GVRLight.getComponentType());
                            if (gvrComponent != null) {
                                float[] lightColor = {0, 0, 0, 0};
                                if (gvrComponent instanceof GVRSpotLight) {
                                    GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                    lightColor = gvrSpotLightBase.getDiffuseIntensity();
                                } else if (gvrComponent instanceof GVRPointLight) {
                                    GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                    lightColor = gvrPointLightBase.getDiffuseIntensity();
                                } else if (gvrComponent instanceof GVRDirectLight) {
                                    GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                    lightColor = gvrDirectLightBase.getDiffuseIntensity();
                                }
                                for (int i = 0; i < 3; i++) {
                                    color[i] = lightColor[i]; // only get the first 3 values, not the alpha value
                                }
                            }
                        }
                        // append the parameters of the SFColor passed to the SCRIPT's javascript code
                        for (int i = 0; i < color.length; i++) {
                            scriptParameters.add(color[i]);
                        }
                    }  // end if SFColor

                    else if (fieldType.equalsIgnoreCase("SFRotation")) {
                        if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "rotation") ) {
                            if (definedItem.getGVRSceneObject() != null) {
                                // Likely the rotation in a Transform / GVRTransform
                                // GearVRf saves rotations as quaternions, but X3D scripting uses AxisAngle
                                // So, these values were saved as AxisAngle in the DefinedItem object
                                scriptParameters.add(definedItem.getAxisAngle().x);
                                scriptParameters.add(definedItem.getAxisAngle().y);
                                scriptParameters.add(definedItem.getAxisAngle().z);
                                scriptParameters.add(definedItem.getAxisAngle().angle);
                            }
                        }  // rotation parameter
                        else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "orientation") ) {
                            if ( definedItem.getViewpoint() != null ) {
                                // have a Viewpoint which for the time-being means get the current direction of the camera
                                float[] lookAt = gvrContext.getMainScene().getMainCameraRig().getLookAt();
                                Vector3f cameraDir = new Vector3f(lookAt[0], lookAt[1], lookAt[2]);
                                Quaternionf q = ConvertDirectionalVectorToQuaternion(cameraDir);
                                AxisAngle4f cameraAxisAngle = new AxisAngle4f();
                                q.get(cameraAxisAngle);
                                scriptParameters.add( cameraAxisAngle.x );
                                scriptParameters.add( cameraAxisAngle.y );
                                scriptParameters.add( cameraAxisAngle.z );
                                scriptParameters.add( cameraAxisAngle.angle );
                            }
                        }  // orientation parameter
                    }  // end if SFRotation
                    else if (fieldType.equalsIgnoreCase("SFVec3f")) {
                        if (definedItem.getGVRSceneObject() != null) {
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(
                                    GVRLight.getComponentType());
                            if (gvrComponent != null) {
                                // it's a light
                                float[] parameter = {0, 0, 0};
                                if (gvrComponent instanceof GVRSpotLight) {
                                    GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                    if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "attenuation") ) {
                                        parameter[0] = gvrSpotLightBase.getAttenuationConstant();
                                        parameter[1] = gvrSpotLightBase.getAttenuationLinear();
                                        parameter[2] = gvrSpotLightBase.getAttenuationQuadratic();
                                    } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "location") ) {
                                        parameter[0] = definedItem.getGVRSceneObject().getTransform().getPositionX();
                                        parameter[1] = definedItem.getGVRSceneObject().getTransform().getPositionY();
                                        parameter[2] = definedItem.getGVRSceneObject().getTransform().getPositionZ();
                                    } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "direction") ) {
                                        parameter[0] = definedItem.getDirection().x;
                                        parameter[1] = definedItem.getDirection().y;
                                        parameter[2] = definedItem.getDirection().z;
                                    }
                                } else if (gvrComponent instanceof GVRPointLight) {
                                    GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                    if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "attenuation") ) {
                                        parameter[0] = gvrPointLightBase.getAttenuationConstant();
                                        parameter[1] = gvrPointLightBase.getAttenuationLinear();
                                        parameter[2] = gvrPointLightBase.getAttenuationQuadratic();
                                    } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "location") ) {
                                        parameter[0] = definedItem.getGVRSceneObject().getTransform().getPositionX();
                                        parameter[1] = definedItem.getGVRSceneObject().getTransform().getPositionY();
                                        parameter[2] = definedItem.getGVRSceneObject().getTransform().getPositionZ();
                                    }
                                } else if (gvrComponent instanceof GVRDirectLight) {
                                    GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                    if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "direction") ) {
                                        parameter[0] = definedItem.getDirection().x;
                                        parameter[1] = definedItem.getDirection().y;
                                        parameter[2] = definedItem.getDirection().z;
                                    }
                                }  // end GVRDirectLight
                                // append the parameters of the lights passed to the SCRIPT's javascript code
                                for (int i = 0; i < parameter.length; i++) {
                                    scriptParameters.add(parameter[i]);
                                }
                            }  //  end gvrComponent != null. it's a light
                            else {
                                if (definedItem.getGVRSceneObject() != null) {
                                    if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "translation") ) {
                                        GVRSceneObject gvrSceneObjectTranslation = root
                                                .getSceneObjectByName((definedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_TRANSLATION_));
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionX());
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionY());
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionZ());
                                    } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "scale") ) {
                                        GVRSceneObject gvrSceneObjectScale = root
                                                .getSceneObjectByName((definedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_SCALE_));
                                        scriptParameters.add(gvrSceneObjectScale.getTransform().getScaleX());
                                        scriptParameters.add(gvrSceneObjectScale.getTransform().getScaleY());
                                        scriptParameters.add(gvrSceneObjectScale.getTransform().getScaleZ());
                                    }
                                }
                            }
                        }  // end SFVec3f GVRSceneObject
                    }  // end if SFVec3f

                    else if (fieldType.equalsIgnoreCase("SFVec2f")) {
                        float[] parameter = {0, 0};
                        if (definedItem.getGVRMaterial() != null) {
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "translation") ) {
                                parameter[0] = definedItem.getTextureTranslation().getX();
                                parameter[1] = -definedItem.getTextureTranslation().getY();
                            }
                            else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "scale") ) {
                                parameter[0] = definedItem.getTextureScale().getX();
                                parameter[1] = definedItem.getTextureScale().getY();
                            }
                            else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "center") ) {
                                parameter[0] = -definedItem.getTextureCenter().getX();
                                parameter[1] = definedItem.getTextureCenter().getY();
                            }
                            // append the parameters of the lights passed to the SCRIPT's javascript code
                            for (int i = 0; i < parameter.length; i++) {
                                scriptParameters.add(parameter[i]);
                            }
                        }  // end SFVec3f GVRMaterial
                    }  // end if SFVec2f

                    else if (fieldType.equalsIgnoreCase("SFFloat")) {
                        if (definedItem.getGVRMaterial() != null) {
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "shininess") ) {
                                scriptParameters.add(
                                        definedItem.getGVRMaterial().getFloat("specular_exponent")
                                );
                            }
                            else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "transparency") ) {
                                scriptParameters.add(definedItem.getGVRMaterial().getOpacity());
                            }

                            else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "rotation") ) {
                                scriptParameters.add(definedItem.getTextureRotation().getValue());
                            }

                        } else if (definedItem.getGVRSceneObject() != null) {
                            // checking if it's a light
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(
                                    GVRLight.getComponentType());
                            if (gvrComponent != null) {
                                float parameter = 0;
                                if (gvrComponent instanceof GVRSpotLight) {
                                    GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                    if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "beamWidth") ) {
                                        parameter = gvrSpotLightBase.getInnerConeAngle() * (float) Math.PI / 180;
                                    } else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "cutOffAngle") ) {
                                        parameter = gvrSpotLightBase.getOuterConeAngle() * (float) Math.PI / 180;
                                    }
                                } else if (gvrComponent instanceof GVRPointLight) {
                                    GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;

                                } else if (gvrComponent instanceof GVRDirectLight) {
                                    GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;

                                }
                                scriptParameters.add(parameter);
                            }
                        }
                        else if (definedItem.getGVRVideoSceneObject() != null) {
                            GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = definedItem.getGVRVideoSceneObject().getMediaPlayer();
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field),"speed") ) {
                                if ( gvrVideoSceneObjectPlayer == null) {
                                    // special case upon initialization of the movie texture, so the speed is init to 1
                                    scriptParameters.add( 1 );
                                }
                                else if ( gvrVideoSceneObjectPlayer.getPlayer() == null) {
                                    ; // could occur prior to movie engine is setup
                                }
                                else {
                                    ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                    PlaybackParameters currPlaybackParamters = exoPlayer.getPlaybackParameters();
                                    scriptParameters.add( currPlaybackParamters.speed );
                                }
                            } // end check for speed
                        }  // end if SFFloat GVRVideoSceneObject
                    }  // end if SFFloat
                    else if (fieldType.equalsIgnoreCase("SFTime")) {
                        if (definedItem.getGVRVideoSceneObject() != null) {
                            GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = definedItem.getGVRVideoSceneObject().getMediaPlayer();
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field),"duration") ) {
                                if ( gvrVideoSceneObjectPlayer == null) {
                                    // special case upon initialization of the movie texture, so the speed is init to 1
                                    scriptParameters.add( 1 );
                                }
                                else if ( gvrVideoSceneObjectPlayer.getPlayer() == null) {
                                    ; // could occur prior to movie engine is setup
                                }
                                else {
                                    ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                    scriptParameters.add( exoPlayer.getDuration() );
                                }
                            } // end check for duration
                            else if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field),"elapsedTime") ) {
                                if ( gvrVideoSceneObjectPlayer == null) {
                                    // special case upon initialization of the movie texture, so the elapsedTime is init to 0
                                    scriptParameters.add( 0 );
                                }
                                else if ( gvrVideoSceneObjectPlayer.getPlayer() == null) {
                                    ; // could occur prior to movie engine is setup
                                }
                                else {
                                    ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                    // getContentPosition is for possible advertisements, and returns the same
                                    // value as getCurrentPosition if no ads.
                                    scriptParameters.add( exoPlayer.getContentPosition() );
                                }
                            } // end check for elapsedTime
                        }  // end if SFTime GVRVideoSceneObject
                    } // end if SFTime
                    else if (fieldType.equalsIgnoreCase("SFInt32")) {
                        int parameter = 0;
                        if (definedItem.getGVRSceneObject() != null) {
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(GVRSwitch.getComponentType());
                            if (gvrComponent != null) {
                                if (gvrComponent instanceof GVRSwitch) {
                                    // We have a Switch node
                                    GVRSwitch gvrSwitch = (GVRSwitch) gvrComponent;
                                    parameter = gvrSwitch.getSwitchIndex();
                                }
                            }
                        }
                        scriptParameters.add(parameter);
                    }
                    else if (fieldType.equalsIgnoreCase("SFString")) {
                        if ( definedItem.getGVRTextViewSceneObject() != null) {
                            if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("style")) {
                                GVRTextViewSceneObject.fontStyleTypes styleType =
                                        definedItem.getGVRTextViewSceneObject().getStyleType();
                                String fontStyle = "";
                                if (styleType == GVRTextViewSceneObject.fontStyleTypes.PLAIN)
                                    fontStyle = "PLAIN";
                                else if (styleType == GVRTextViewSceneObject.fontStyleTypes.BOLD)
                                    fontStyle = "BOLD";
                                else if (styleType == GVRTextViewSceneObject.fontStyleTypes.BOLDITALIC)
                                    fontStyle = "BOLDITALIC";
                                else if (styleType == GVRTextViewSceneObject.fontStyleTypes.ITALIC)
                                    fontStyle = "ITALIC";
                                if (fontStyle != "") scriptParameters.add("\'" + fontStyle + "\'");
                                else Log.e(TAG, "style in ROUTE not recognized.");
                            }
                        }
                    }  //  end SFString
                    else if (fieldType.equalsIgnoreCase("MFString")) {
                        //TODO: will need to handle multiple strings particularly for Text node
                        GVRTexture gvrTexture = definedItem.getGVRTexture();
                        if (gvrTexture != null) {
                            // have a url containting a texture map
                            if ( StringFieldMatch( scriptObject.getFromDefinedItemField(field), "url") ) {
                                GVRImage gvrImage = gvrTexture.getImage();
                                if ( gvrImage != null ) {
                                    if ( gvrImage.getFileName() != null) {
                                        scriptParameters.add("\'" + gvrImage.getFileName() + "\'");
                                    }
                                }
                                else Log.e(TAG, "ImageTexture name not DEFined");
                            }
                            else Log.e(TAG, "ImageTexture SCRIPT node url field not found");
                        }
                        else Log.e(TAG, "Unable to set MFString in SCRIPT node");
                    } // end MFString
                }  //  end if definedItem != null
            }  //  end INPUT_ONLY, INPUT_OUTPUT (only ways to pass parameters to JS parser
        }  // for loop checking for parameters passed to the JavaScript parser

        // create the parameters array
        if (scriptObject.getTimeStampParameter())
            scriptParameters.add(1, 0); // insert the timeStamp parameter if it's used
        Object[] parameters = new Object[scriptParameters.size()];
        for (int i = 0; i < scriptParameters.size(); i++) {
            parameters[i] = scriptParameters.get(i);
        }
        return parameters;
    }  //  end  SetJavaScriptArguments

    private void RunInitializeScriptThread (GVRJavascriptV8File gvrJavascriptV8FileFinal, InteractiveObject interactiveObjectFinal, Object[] parametersFinal, String paramStringFinal) {
        boolean complete = gvrJavascriptV8FileFinal.invokeFunction(GEARVR_INIT_JAVASCRIPT_FUNCTION_NAME, parametersFinal, paramStringFinal);
        if (complete) {
            // No errors in the GearVR_Init function, so continue to cal the init function if there are any.
            // if the objects required for this function were constructed, then
            //   check if this <SCRIPT> has an initialize() method that is run just once.
            if (gvrJavascriptV8FileFinal.getScriptText().contains(INITIALIZE_FUNCTION)) {
                // <SCRIPT> node initialize() functions set inputOnly values
                // so we don't continue to run the main script method.
                // http://www.web3d.org/documents/specifications/19775-1/V3.2/Part01/components/scripting.html#Script
                complete = gvrJavascriptV8FileFinal.invokeFunction(INITIALIZE_FUNCTION, parametersFinal, "");
                if ( !complete ) {
                    Log.e(TAG, "Error with initialize() function in SCRIPT '" +
                            interactiveObjectFinal.getScriptObject().getName() + "'");
                }
            }
        } else {
            Log.e(TAG, "Error parsing / running initializing V8 JavaScript function in SCRIPT '" +
                    interactiveObjectFinal.getScriptObject().getName() + "'");
        }
    }  //  end RunInitializeScriptThread

    /**
     * method runs the Script's initialize() method
     */
    public void InitializeScript() {

        for (InteractiveObject interactiveObject : interactiveObjects) {
            if (interactiveObject.getScriptObject() != null) {

                BuildInitJavaScript(interactiveObject);
                Object[] parameters = SetJavaScriptArguments(interactiveObject, 0, false);
                parameters[0] = 0;
                if (interactiveObject.getScriptObject().getTimeStampParameter()) parameters[1] = 0;

                if ( V8JavaScriptEngine ) {
                    // Using V8 JavaScript compiler and run-time engine
                    GVRJavascriptV8File gvrJavascriptV8File = interactiveObject.getScriptObject().getGVRJavascriptV8File();
                    // Append the X3D data type constructors to the end of the JavaScript file
                    if ( !interactiveObject.getScriptObject().getInitializationDone()) {
                        gvrJavascriptV8File.setScriptText(gvrJavascriptV8File.getScriptText() +
                                interactiveObject.getScriptObject().getGearVRinitJavaScript());

                        if ( !interactiveObject.getScriptObject().getScriptCalledPerFrame() ) {
                            // only initialize if this is not called per frame
                            // initialization for scripts called per frame must be called
                            // when we begin the first frame due to V8 engine start-up
                            String paramString = "var params =[";
                            for (int i = 0; i < parameters.length; i++ ) {
                                paramString += (parameters[i] + ", ");
                            }
                            paramString = paramString.substring(0, (paramString.length()-2)) + "];";

                            final GVRJavascriptV8File gvrJavascriptV8FileFinal = gvrJavascriptV8File;
                            final InteractiveObject interactiveObjectFinal = interactiveObject;
                            final Object[] parametersFinal = parameters;
                            final String paramStringFinal = paramString;
                            gvrContext.runOnGlThread(new Runnable() {
                                @Override
                                public void run() {
                                    RunInitializeScriptThread( gvrJavascriptV8FileFinal, interactiveObjectFinal, parametersFinal, paramStringFinal);
                                }
                            });
                        }  // ! per frame script
                    }
                    interactiveObject.getScriptObject().setInitializationDone(true);

                } //  end of running initialize functions in V8 JavaScript engine
                else {
                    // Using older Mozilla Rhino engine
                    GVRJavascriptScriptFile gvrJavascriptFile = interactiveObject.getScriptObject().getGVRJavascriptScriptFile();

                    // Append the X3D data type constructors to the end of the JavaScript file
                    gvrJavascriptFile.setScriptText(gvrJavascriptFile.getScriptText() +
                            interactiveObject.getScriptObject().getGearVRinitJavaScript());

                    // Run the newly created method 'GEARVR_INIT_JAVASCRIPT_FUNCTION' that
                    //    constructs the objects required for this JavaScript program.
                    boolean complete = gvrJavascriptFile.invokeFunction(GEARVR_INIT_JAVASCRIPT_FUNCTION_NAME, parameters);
                    if (complete) {
                        // if the objects required for this function were constructed, then
                        //   check if this <SCRIPT> has an initialize() method that is run just once.
                        if (gvrJavascriptFile.getScriptText().contains(INITIALIZE_FUNCTION)) {
                            RunScript(interactiveObject, INITIALIZE_FUNCTION, parameters);
                        }
                    } else {
                        Log.e(TAG, "Error parsing / running initializing Rhino JavaScript function in SCRIPT '" +
                                interactiveObject.getScriptObject().getName() + "'");
                    }
                }  //  end using older Mozilla Rhino engine
            }  // end check for interactiveObject having a Script Object
        }  // end loop thru all interactiveObjects for a ScriptObject
    }  //  end InitializeScript method


    // Builds string that becomes the GearVRinitJavaScript() function
    // which will initialize / Construct the X3D data types used in
    // this SCRIPT node.
    private void BuildInitJavaScript(InteractiveObject interactiveObject) {
        String gearVRinitJavaScript = "function " + GEARVR_INIT_JAVASCRIPT_FUNCTION_NAME + "()\n{\n";

        //The first two arguments are for the event - could be time or an isOver/isActive boolean -
        // and the second argument is for the timeStamp which is the accumulated time for starting
        // the per Frame calls to JavaScript, or 0 for isOver/isActive touch events
        ScriptObject scriptObject = interactiveObject.getScriptObject();
        int argumentNum = 1;
        if (scriptObject.getTimeStampParameter()) argumentNum = 2;

        // Get the parameters on X3D data types that are included with this JavaScript
        if ( V8JavaScriptEngine ) {
            for (ScriptObject.Field field : scriptObject.getFieldsArrayList()) {
                String fieldType = scriptObject.getFieldType(field);
                if (scriptObject.getFromDefinedItem(field) != null) {
                    if ((fieldType.equalsIgnoreCase("SFColor")) || (fieldType.equalsIgnoreCase("SFVec3f"))) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "], params[" + (argumentNum + 1) + "], params[" + (argumentNum + 2) + "]);\n";
                        argumentNum += 3;
                    }  // end if SFColor of SFVec3f, a 3-value parameter
                    else if (fieldType.equalsIgnoreCase("SFRotation")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "], params[" + (argumentNum + 1) + "], params[" + (argumentNum + 2)
                                + "], params[" + (argumentNum + 3) + "]);\n";
                        argumentNum += 4;
                    }  // end if SFRotation, a 4-value parameter
                    else if (fieldType.equalsIgnoreCase("SFVec2f")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "], params[" + (argumentNum + 1) + "]);\n";
                        argumentNum += 2;
                    }  // end if SFVec2f, a 2-value parameter

                    else if ((fieldType.equalsIgnoreCase("SFFloat")) || (fieldType.equalsIgnoreCase("SFBool"))
                            || (fieldType.equalsIgnoreCase("SFInt32")) || (fieldType.equalsIgnoreCase("SFTime")) ) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if SFFloat, SFBool, SFInt32 or SFTime - a single parameter
                    else if (fieldType.equalsIgnoreCase("SFString") ) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if SFString
                    else if (fieldType.equalsIgnoreCase("MFString") ) {
                        // TODO: need MFString to support more than one argument due to being used for Text Strings
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                            "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if MFString
                    else {
                        Log.e(TAG, "Error unsupported field type '" + fieldType + "' in SCRIPT '" +
                            interactiveObject.getScriptObject().getName() + "'");
                    }
                }
                else if (scriptObject.getFromEventUtility(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFBool")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if SFBool
                }  //  end scriptObject.getFromEventUtility(field) != null
                else if (scriptObject.getFromTimeSensor(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFFloat")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if SFFloat
                }  //  end scriptObject.getFromTimeSensor(field) != null
            }  // for loop checking for parameters passed to the JavaScript parser
        }  //  end if V8 engine
        else {
            // Mozilla Rhino engine
            for (ScriptObject.Field field : scriptObject.getFieldsArrayList()) {
                String fieldType = scriptObject.getFieldType(field);

                if (scriptObject.getFromDefinedItem(field) != null) {
                    if ((fieldType.equalsIgnoreCase("SFColor")) || (fieldType.equalsIgnoreCase("SFVec3f"))) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( arg" + argumentNum + ", arg" + (argumentNum + 1) + ", arg" + (argumentNum + 2) + ");\n";
                        argumentNum += 3;
                    }  // end if SFColor of SFVec3f
                    else if (fieldType.equalsIgnoreCase("SFRotation")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( arg" + argumentNum + ", arg" + (argumentNum + 1) + ", arg" + (argumentNum + 2)
                                + ", arg" + (argumentNum + 3) + ");\n";
                        argumentNum += 4;
                    }  // end if SFRotation

                    else if ((fieldType.equalsIgnoreCase("SFFloat")) || (fieldType.equalsIgnoreCase("SFBool"))) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( arg" + argumentNum + ");\n";
                        argumentNum += 1;
                    }  // end if SFFloat or SFBool
                }  //  end scriptObject.getFromDefinedItem(field) != null
                else if (scriptObject.getFromEventUtility(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFBool")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( arg" + argumentNum + ");\n";
                        argumentNum += 1;
                    }  // end if SFBool
                }  //  end scriptObject.getFromEventUtility(field) != null
                else if (scriptObject.getFromTimeSensor(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFFloat")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( arg" + argumentNum + ");\n";
                        argumentNum += 1;
                    }  // end if SFFloat
                }  //  end scriptObject.getFromTimeSensor(field) != null

            }  // for loop checking for parameters passed to the JavaScript parser
        }  // end if Mozilla Rhino engine
        gearVRinitJavaScript += "}";
        scriptObject.setGearVRinitJavaScript(gearVRinitJavaScript);
    }  //  end  BuildInitJavaScript

    private void RunScriptThread (GVRJavascriptV8File gvrJavascriptV8FileFinal, InteractiveObject interactiveObjectFinal, String functionNameFinal, Object[] parametersFinal, String paramStringFinal) {
        boolean complete = gvrJavascriptV8FileFinal.invokeFunction(GEARVR_INIT_JAVASCRIPT_FUNCTION_NAME, parametersFinal, paramStringFinal);
        if ( complete ) {
            Bindings gvrFunctionBindingValues = gvrJavascriptV8FileFinal.getLocalBindings();
            //set the bindings from X3D Script field with inputOnly / inputOutput
            gvrJavascriptV8FileFinal.setInputValues(gvrFunctionBindingValues);
            // Now run this Script's actual function
            complete = gvrJavascriptV8FileFinal.invokeFunction(functionNameFinal, parametersFinal, paramStringFinal);

            if (complete) {
                // The JavaScript (JS) ran ok.  Now get the return
                // values (saved as X3D data types such as SFColor)
                //  stored in 'localBindings'.
                //  Then call SetResultsFromScript() to set the GearVR values
                Bindings returnedBindingValues = gvrJavascriptV8FileFinal.getLocalBindings();
                SetResultsFromScript(interactiveObjectFinal, returnedBindingValues);
            } // second complete check
            else {
                Log.e(TAG, "Error in SCRIPT node '" + interactiveObjectFinal.getScriptObject().getName() +
                        "' JavaScript function '" + functionNameFinal + "'");
            }
        } // first complete check
    }  //  end RunScriptThread


    // Run the JavaScript program, Output saved in localBindings
    private void RunScript(InteractiveObject interactiveObject, String functionName, Object[] parameters) {
        boolean complete = false;
        if ( V8JavaScriptEngine) {
            GVRJavascriptV8File gvrJavascriptV8File = interactiveObject.getScriptObject().getGVRJavascriptV8File();
            String paramString = "var params =[";
            for (int i = 0; i < parameters.length; i++ ) {
                paramString += (parameters[i] + ", ");
            }
            paramString = paramString.substring(0, (paramString.length()-2)) + "];";

            final GVRJavascriptV8File gvrJavascriptV8FileFinal = gvrJavascriptV8File;
            final InteractiveObject interactiveObjectFinal = interactiveObject;
            final String functionNameFinal = functionName;
            final Object[] parametersFinal = parameters;
            final String paramStringFinal = paramString;
            gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    RunScriptThread (gvrJavascriptV8FileFinal, interactiveObjectFinal, functionNameFinal, parametersFinal, paramStringFinal);
                }
            });
        }  // end V8JavaScriptEngine
        else {
            // Mozilla Rhino engine
            GVRJavascriptScriptFile gvrJavascriptFile = interactiveObject.getScriptObject().getGVRJavascriptScriptFile();

            complete = gvrJavascriptFile.invokeFunction(functionName, parameters);
            if (complete) {
                // The JavaScript (JS) ran.  Now get the return
                // values (saved as X3D data types such as SFColor)
                //  stored in 'localBindings'.
                //  Then call SetResultsFromScript() to set the GearVR values
                Bindings localBindings = gvrJavascriptFile.getLocalBindings();
                SetResultsFromScript(interactiveObject, localBindings);
            } else {
                Log.e(TAG, "Error in SCRIPT node '" +  interactiveObject.getScriptObject().getName() +
                        "' running Rhino Engine JavaScript function '" + functionName + "'");
            }
        }
    }  // end function RunScript

    // Based on the inputs and javascript code, set the properties of the clsses in the GVR scene graph
    // these include the properties of lights, transforms and materials.
    // Possibly GVRMesh values too.
    private void SetResultsFromScript(InteractiveObject interactiveObjectFinal, Bindings localBindings) {
        // A SCRIPT can have mutliple defined objects, so we don't use getDefinedItem()
        // instead we go through the field values
        try {
            ScriptObject scriptObject = interactiveObjectFinal.getScriptObject();
            for (ScriptObject.Field fieldNode : scriptObject.getFieldsArrayList()) {
                if ((scriptObject.getFieldAccessType(fieldNode) == ScriptObject.AccessType.OUTPUT_ONLY) ||
                        (scriptObject.getFieldAccessType(fieldNode) == ScriptObject.AccessType.INPUT_OUTPUT)) {
                    String fieldType = scriptObject.getFieldType(fieldNode);
                    DefinedItem scriptObjectToDefinedItem = scriptObject.getToDefinedItem(fieldNode);
                    EventUtility scriptObjectToEventUtility = scriptObject.getToEventUtility(fieldNode);
                    Object returnedJavaScriptValue = localBindings.get(scriptObject.getFieldName(fieldNode));

                    // Script fields contain all the values that can be returned from a JavaScript function.
                    // However, not all JavaScript functions set returned-values, and thus left null.  For
                    // example the initialize() method may not set some Script field values, so don't
                    // process those and thus check if returnedJavaScriptValue != null
                    if ((returnedJavaScriptValue != null) || ( !(returnedJavaScriptValue instanceof V8Object) )) {
                        if (fieldType.equalsIgnoreCase("SFBool")) {
                            SFBool sfBool = (SFBool) returnedJavaScriptValue;
                            if ( scriptObjectToDefinedItem != null) {
                                if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                    GVRComponent gvrComponent = scriptObjectToDefinedItem.getGVRSceneObject().getComponent(
                                            GVRLight.getComponentType());
                                    if (gvrComponent != null) {
                                        gvrComponent.setEnable(sfBool.getValue());
                                    }
                                }  //  end if the SceneObject has a light component attached
                            }  //  end scriptObjectToDefinedItem != null
                            else if ( scriptObjectToEventUtility != null) {
                                scriptObjectToEventUtility.setToggle(sfBool.getValue());
                            }
                            else if ( scriptObject.getToTimeSensor(fieldNode) != null) {
                                TimeSensor timeSensor = scriptObject.getToTimeSensor(fieldNode);
                                if ( StringFieldMatch( scriptObject.getFieldName(fieldNode), "loop") ) {
                                    timeSensor.setLoop( sfBool.getValue(), gvrContext );
                                }
                                if ( StringFieldMatch( scriptObject.getFieldName(fieldNode), "enabled") ) {
                                    timeSensor.setEnabled( sfBool.getValue(), gvrContext );
                                }
                            }
                            else {
                                Log.e(TAG, "Error: Not setting SFBool '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFBool
                        else if (fieldType.equalsIgnoreCase("SFFloat")) {
                            SFFloat sfFloat = (SFFloat) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                GVRMaterial gvrMaterial= scriptObjectToDefinedItem.getGVRMaterial();
                                // shininess and transparency are part of X3D Material node
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "shininess") ) {
                                    gvrMaterial.setSpecularExponent(sfFloat.getValue());
                                } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "transparency") ) {
                                    gvrMaterial.setOpacity(sfFloat.getValue());
                                }
                                else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "rotation")  ) {
                                    // rotationn is part of TextureTransform node
                                    scriptObjectToDefinedItem.setTextureRotation( sfFloat.getValue() );

                                    Matrix3f textureTransform = SetTextureTransformMatrix(
                                            scriptObjectToDefinedItem.getTextureTranslation(),
                                            scriptObjectToDefinedItem.getTextureCenter(),
                                            scriptObjectToDefinedItem.getTextureScale(),
                                            scriptObjectToDefinedItem.getTextureRotation());

                                    float[] texMtx = new float[9];
                                    textureTransform.get(texMtx);
                                    gvrMaterial.setFloatArray("texture_matrix", texMtx);
                                }
                                else {
                                    Log.e(TAG, "Error: Not setting SFFloat to Material '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                                }
                            } else if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                GVRComponent gvrComponent = scriptObjectToDefinedItem.getGVRSceneObject().getComponent(
                                        GVRLight.getComponentType());
                                if (gvrComponent != null) {
                                    if (gvrComponent instanceof GVRSpotLight) {
                                        GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                        if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "beamWidth") ) {
                                            gvrSpotLightBase.setInnerConeAngle(sfFloat.getValue() * 180 / (float) Math.PI);
                                        } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "cutOffAngle") ) {
                                            gvrSpotLightBase.setOuterConeAngle(sfFloat.getValue() * 180 / (float) Math.PI);
                                        } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "intensity") ) {
                                            //TODO: we aren't changing intensity since this would be multiplied by color
                                        } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "radius") ) {
                                           //TODO: radius is not currently supported in GearVR for the spot light
                                        }
                                    } else if (gvrComponent instanceof GVRPointLight) {
                                        GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                        if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "intensity") ) {
                                            //TODO: we aren't changing intensity since this would be multiplied by color
                                        } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "radius") ) {
                                            //TODO: radius is not currently supported in GearVR for the point light
                                        }
                                    } else if (gvrComponent instanceof GVRDirectLight) {
                                        GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                        if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "intensity") ) {
                                            //TODO: we aren't changing intensity since GVR multiplies this by color
                                        }
                                    }
                                }  //  end presumed to be a light
                            }  //  end GVRScriptObject ! null
                            else if ( scriptObjectToDefinedItem.getGVRVideoSceneObject() != null) {
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "speed") ||
                                        StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "pitch") ) {
                                    GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = scriptObjectToDefinedItem.getGVRVideoSceneObject().getMediaPlayer();
                                    ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();

                                    PlaybackParameters currPlaybackParamters = exoPlayer.getPlaybackParameters();
                                    PlaybackParameters playbackParamters = new PlaybackParameters(sfFloat.getValue(), sfFloat.getValue());
                                    exoPlayer.setPlaybackParameters(playbackParamters);
                                }
                            }
                            else {
                                Log.e(TAG, "Error: Not setting SFFloat '" + scriptObject.getToDefinedItemField(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFFloat
                        else if (fieldType.equalsIgnoreCase("SFTime")) {
                            SFTime sfTime = (SFTime) returnedJavaScriptValue;
                            if ( scriptObject.getToTimeSensor(fieldNode) != null) {
                                TimeSensor timeSensor = scriptObject.getToTimeSensor(fieldNode);
                                if ( StringFieldMatch( scriptObject.getFieldName(fieldNode), "startTime") ) {
                                    timeSensor.startTime = (float)sfTime.getValue();
                                }
                                else if ( StringFieldMatch( scriptObject.getFieldName(fieldNode), "stopTime") ) {
                                    timeSensor.stopTime = (float)sfTime.getValue();
                                }
                                else if ( StringFieldMatch( scriptObject.getFieldName(fieldNode), "cycleInterval") ) {
                                    timeSensor.setCycleInterval( (float)sfTime.getValue() );
                                }
                                else Log.e(TAG, "Error: Not setting " + (float)sfTime.getValue() + " to SFTime '" +
                                            scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                            else if ( scriptObject.getToDefinedItemField( fieldNode ) != null) {
                                DefinedItem definedItem = scriptObject.getToDefinedItem(fieldNode);
                                if ( definedItem.getGVRVideoSceneObject() != null ) {
                                    GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = scriptObjectToDefinedItem.getGVRVideoSceneObject().getMediaPlayer();
                                    ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                    if (StringFieldMatch(scriptObject.getToDefinedItemField(fieldNode), "startTime")) {
                                        exoPlayer.seekTo( (long)sfTime.getValue() );
                                    }
                                    else Log.e(TAG, "Error: MovieTexture " + scriptObject.getToDefinedItemField(fieldNode) + " in " +
                                            scriptObject.getName() + " script not supported." );
                                }
                                else Log.e(TAG, "Error: Not setting " + (float)sfTime.getValue() + " to SFTime. " +
                                        "MovieTexture node may not be defined to connect from SCRIPT '" + scriptObject.getName() + "'." );

                            }
                            else Log.e(TAG, "Error: Not setting SFTime '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                        }  //  end SFTime
                        else if (fieldType.equalsIgnoreCase("SFColor")) {
                            SFColor sfColor = (SFColor) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                //  SFColor change to a GVRMaterial
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "diffuseColor") ) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("diffuse_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "specularColor") ) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("specular_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "emissiveColor") ) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("emissive_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                }
                            }  //  end SFColor change to a GVRMaterial
                            else if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                // GVRSceneObject
                                GVRSceneObject gvrSceneObject = scriptObjectToDefinedItem.getGVRSceneObject();
                                GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLight.getComponentType());
                                if (gvrComponent != null) {
                                    if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "color") ) {
                                        // SFColor change to a GVRSceneObject (likely a Light Component)
                                        if (gvrComponent instanceof GVRSpotLight) {
                                            GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                            gvrSpotLightBase.setDiffuseIntensity(sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1);
                                        } else if (gvrComponent instanceof GVRPointLight) {
                                            GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                            gvrPointLightBase.setDiffuseIntensity(sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1);
                                        } else if (gvrComponent instanceof GVRDirectLight) {
                                            GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                            gvrDirectLightBase.setDiffuseIntensity(sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1);
                                        }
                                    }
                                }
                            }  //  SFColor change to a GVRSceneObject (likely a Light)
                            else {
                                Log.e(TAG, "Error: Not setting SFColor '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFColor (to a light or Material)
                        else if (fieldType.equalsIgnoreCase("SFVec3f")) {
                            SFVec3f sfVec3f = (SFVec3f) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                //  SFVec3f change to a GVRSceneObject
                                GVRSceneObject gvrSceneObject = scriptObjectToDefinedItem.getGVRSceneObject();
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "translation")  ||
                                    StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "location") ) {
                                    // location applies to point light and spot light
                                    GVRSceneObject gvrSceneObjectTranslation = root
                                            .getSceneObjectByName((scriptObjectToDefinedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_TRANSLATION_));
                                    if (gvrSceneObjectTranslation != null)
                                        gvrSceneObjectTranslation.getTransform().setPosition(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                    else
                                        gvrSceneObject.getTransform().setPosition(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "scale") ) {
                                    GVRSceneObject gvrSceneObjectScale = root
                                            .getSceneObjectByName((scriptObjectToDefinedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_SCALE_));
                                    if (gvrSceneObjectScale != null)
                                        gvrSceneObjectScale.getTransform().setScale(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                    else
                                        gvrSceneObject.getTransform().setScale(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                } else {
                                    // could be parameters for a light
                                    GVRComponent gvrComponent = gvrSceneObject.getComponent(
                                            GVRLight.getComponentType());
                                    if (gvrComponent != null) {
                                        if (gvrComponent instanceof GVRSpotLight) {
                                            GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                            if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "attenuation") ) {
                                                gvrSpotLightBase.setAttenuation(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "direction") ) {
                                                scriptObjectToDefinedItem.setDirection(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                                Vector3f v3 = new Vector3f(sfVec3f).normalize();
                                                Quaternionf q = ConvertDirectionalVectorToQuaternion(v3);
                                                gvrSpotLightBase.getTransform().setRotation(q.w, q.x, q.y, q.z);
                                            } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "location") ) {
                                                gvrSpotLightBase.getTransform().setPosition(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            }
                                        } else if (gvrComponent instanceof GVRPointLight) {
                                            GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                            if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "attenuation") ) {
                                                gvrPointLightBase.setAttenuation(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            } else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "location") ) {
                                                gvrPointLightBase.getTransform().setPosition(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            }
                                        } else if (gvrComponent instanceof GVRDirectLight) {
                                            GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                            if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "direction") ) {
                                                scriptObjectToDefinedItem.setDirection(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                                Vector3f v3 = new Vector3f(sfVec3f).normalize();
                                                Quaternionf q = ConvertDirectionalVectorToQuaternion(v3);
                                                gvrDirectLightBase.getTransform().setRotation(q.w, q.x, q.y, q.z);
                                            }
                                        }
                                    }
                                }  // end it could be a light
                            }  // end GVRSceneObject with SFVec3f
                            else {
                                Log.e(TAG, "Error: Not setting SFVec3f '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFVec3f
                        else if (fieldType.equalsIgnoreCase("SFVec2f")) {
                            SFVec2f sfVec2f = (SFVec2f) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                //  SFVec2f change to a Texture Transform
                                GVRMaterial gvrMaterial= scriptObjectToDefinedItem.getGVRMaterial();
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "translation")  ||
                                        StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "scale")  ||
                                        StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "center")  ) {

                                    if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "translation")  )
                                        scriptObjectToDefinedItem.setTextureTranslation(sfVec2f.getX(), -sfVec2f.getY());
                                    else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "scale")  )
                                        scriptObjectToDefinedItem.setTextureScale( sfVec2f.getX(), sfVec2f.getY() );
                                    else if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "center")  )
                                        scriptObjectToDefinedItem.setTextureCenter( -sfVec2f.getX(), sfVec2f.getY());


                                    Matrix3f textureTransform = SetTextureTransformMatrix(
                                            scriptObjectToDefinedItem.getTextureTranslation(),
                                            scriptObjectToDefinedItem.getTextureCenter(),
                                            scriptObjectToDefinedItem.getTextureScale(),
                                            scriptObjectToDefinedItem.getTextureRotation());
                                    float[] texMtx = new float[9];
                                    textureTransform.get(texMtx);
                                    gvrMaterial.setFloatArray("texture_matrix", texMtx);
                                }

                                else {
                                    Log.e(TAG, "Error: Not setting SFVec2f '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                                }
                            }
                            else {
                                Log.e(TAG, "Error: Not setting SFVec2f '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        } // end SFVec2f
                        else if (fieldType.equalsIgnoreCase("SFRotation")) {
                            SFRotation sfRotation = (SFRotation) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                //  SFRotation change to a GVRSceneObject
                                if ( StringFieldMatch( scriptObject.getToDefinedItemField(fieldNode), "rotation") ) {
                                    scriptObjectToDefinedItem.setAxisAngle(sfRotation.angle, sfRotation.x, sfRotation.y, sfRotation.z);

                                    GVRSceneObject gvrSceneObjectRotation = root
                                            .getSceneObjectByName((scriptObjectToDefinedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_ROTATION_));
                                    float angleDegrees = (float) Math.toDegrees(sfRotation.angle);  // convert radians to degrees
                                    if (gvrSceneObjectRotation != null) {
                                        gvrSceneObjectRotation.getTransform().setRotationByAxis(angleDegrees, sfRotation.x, sfRotation.y, sfRotation.z);
                                    } else {
                                        scriptObjectToDefinedItem.getGVRSceneObject().getTransform().setRotationByAxis(sfRotation.angle, sfRotation.x, sfRotation.y, sfRotation.z);
                                    }
                                }  //  definedItem != null
                            }  // end GVRSceneObject with SFRotation
                            else {
                                Log.e(TAG, "Error: Not setting SFRotation '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFRotation
                        else if (fieldType.equalsIgnoreCase("SFInt32")) {
                            try {
                                SFInt32 sfInt32 = new SFInt32(new Integer(returnedJavaScriptValue.toString()).intValue() );
                                if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                    // Check if the field is 'whichChoice', meaning it's a Switch node
                                     if ( StringFieldMatch(scriptObject.getToDefinedItemField(fieldNode), "whichChoice") ) {
                                        GVRSceneObject gvrSwitchSceneObject = scriptObject.getToDefinedItem(fieldNode).getGVRSceneObject();
                                        GVRComponent gvrComponent = gvrSwitchSceneObject.getComponent(GVRSwitch.getComponentType());
                                        if (gvrComponent instanceof GVRSwitch) {
                                            // Set the value inside the Switch node
                                            GVRSwitch gvrSwitch = (GVRSwitch) gvrComponent;
                                            // Check if we are to switch to a value out of range (i.e. no mesh exists)
                                            // and thus set to not show any object.
                                            if ( (gvrSwitchSceneObject.getChildrenCount() <= sfInt32.getValue()) ||
                                                    (sfInt32.getValue() < 0) ) {
                                                sfInt32.setValue( gvrSwitchSceneObject.getChildrenCount() );
                                            }
                                            gvrSwitch.setSwitchIndex( sfInt32.getValue() );
                                        }
                                    }
                                }  // end GVRSceneObject with SFInt32
                                else {
                                    Log.e(TAG, "Error: Not setting SFInt32 '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'.");
                                }
                            }
                            catch (Exception e) {
                                Log.e(TAG, "Error: Not setting SFInt32 '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT " + scriptObject.getName() + "'.");
                                Log.e(TAG, "Exception: " + e);
                            }
                        }  //  end SFInt32
                        else if (fieldType.equalsIgnoreCase("SFString")) {
                            SFString sfString = (SFString) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRTextViewSceneObject() != null) {
                                GVRTextViewSceneObject gvrTextViewSceneObject = scriptObjectToDefinedItem.getGVRTextViewSceneObject();
                                if (scriptObject.getToDefinedItemField(fieldNode).endsWith("style")) {
                                    String value = sfString.getValue().toUpperCase();
                                    GVRTextViewSceneObject.fontStyleTypes fontStyle = null;
                                    if ( value.equals("BOLD")) fontStyle = GVRTextViewSceneObject.fontStyleTypes.BOLD;
                                    else if ( value.equals("ITALIC")) fontStyle = GVRTextViewSceneObject.fontStyleTypes.ITALIC;
                                    else if ( value.equals("BOLDITALIC")) fontStyle = GVRTextViewSceneObject.fontStyleTypes.BOLDITALIC;
                                    else if ( value.equals("PLAIN")) fontStyle = GVRTextViewSceneObject.fontStyleTypes.PLAIN;
                                    if ( fontStyle != null ) {
                                        gvrTextViewSceneObject.setTypeface(gvrContext,
                                                gvrTextViewSceneObject.getFontFamily(), fontStyle);
                                    }
                                    else {
                                        Log.e(TAG, "Error: " + value + " + not recognized X3D FontStyle style in SCRIPT '" + scriptObject.getName() + "'." );
                                    }
                                }
                                else Log.e(TAG, "Error: Setting not SFString  value'" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                            else {
                                Log.e(TAG, "Error: Not setting SFString '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end SFString
                        else if (fieldType.equalsIgnoreCase("MFString")) {
                            MFString mfString = (MFString) returnedJavaScriptValue;

                            if (scriptObjectToDefinedItem.getGVRTexture() != null) {
                                GVRTexture gvrTexture = scriptObjectToDefinedItem.getGVRTexture();
                                //  MFString change to a GVRTexture object
                                if (StringFieldMatch(scriptObject.getToDefinedItemField(fieldNode), "url")) {
                                    if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                        // We have the GVRMaterial that contains a GVRTexture
                                        if (!gvrTexture.getImage().getFileName().equals(mfString.get1Value(0))) {
                                            // Only loadTexture if it is different than the current
                                            GVRAssetLoader.TextureRequest request = new GVRAssetLoader.TextureRequest(assetRequest,
                                                        gvrTexture, mfString.get1Value(0));
                                            assetRequest.loadTexture(request);
                                        }
                                    } // end having GVRMaterial containing GVRTexture
                                    else {
                                        Log.e(TAG, "Error: No GVRMaterial associated with MFString Texture url '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'.");
                                    }
                                }  //  definedItem != null
                                else {
                                    Log.e(TAG, "Error: No url associated with MFString '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'.");
                                }
                            }  // end GVRTexture != null

                            else if (scriptObjectToDefinedItem.getGVRVideoSceneObject() != null) {
                                //  MFString change to a GVRVideoSceneObject object
                                if (StringFieldMatch(scriptObject.getToDefinedItemField(fieldNode), "url")) {
                                    try {
                                        GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = scriptObjectToDefinedItem.getGVRVideoSceneObject().getMediaPlayer();
                                        ExoPlayer exoPlayer = (ExoPlayer) gvrVideoSceneObjectPlayer.getPlayer();
                                        exoPlayer.stop();

                                        final DataSource.Factory dataSourceFactory = new DataSource.Factory() {
                                            @Override
                                            public DataSource createDataSource() {
                                                return new AssetDataSource(gvrContext.getContext());
                                            }
                                        };
                                        final MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("asset:///" + mfString.get1Value(0)),
                                                dataSourceFactory,
                                                new DefaultExtractorsFactory(), null, null);
                                        exoPlayer.prepare(mediaSource);
                                        Log.e(TAG, "Load movie " + mfString.get1Value(0) + ".");
                                        gvrVideoSceneObjectPlayer.start();
                                    } catch (Exception e) {
                                        Log.e(TAG, "X3D MovieTexture Asset " + mfString.get1Value(0) + " not loaded." + e);
                                        e.printStackTrace();
                                    }

                                }  //  end definedItem != null, contains url
                                else {
                                    Log.e(TAG, "Error: No MovieTexure url associated with MFString '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                                }
                            }  // end GVRVideoSceneObject != null

                            if (scriptObjectToDefinedItem.getGVRTextViewSceneObject() != null) {
                                GVRTextViewSceneObject gvrTextViewSceneObject = scriptObjectToDefinedItem.getGVRTextViewSceneObject();
                                if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("string")) {
                                    gvrTextViewSceneObject.setText(mfString.get1Value(0));
                                }
                                else Log.e(TAG, "Error: Setting not MFString string '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "'." );
                            }
                        }  //  end MFString
                        else {
                            Log.e(TAG, "Error: " + fieldType + " in '" + scriptObject.getFieldName(fieldNode) + "' value from SCRIPT '" + scriptObject.getName() + "' not supported." );
                        }
                    }  //  end value != null
                    else {
                        Log.e(TAG, "Warning: " + fieldType + " '" + scriptObject.getFieldName(fieldNode) + "' from SCRIPT '" + scriptObject.getName() + "' may not be set.");
                    }
                }  //  end OUTPUT-ONLY or INPUT_OUTPUT
            }  // end for-loop list of fields for a single script
        } catch (Exception e) {
            Log.e(TAG, "Error setting values returned from JavaScript in SCRIPT '" +
                            interactiveObjectFinal.getScriptObject().getName() +
                    "'. Check JavaScript or ROUTE's.  Exception: " + e);

        }
    }  //  end  SetResultsFromScript


    /**
     * Converts a vector into a quaternion.
     * Used for the direction of spot and directional lights
     * Called upon initialization and updates to those vectors
     *
     * @param d
     */
    public Quaternionf ConvertDirectionalVectorToQuaternion(Vector3f d) {
        d.negate();
        Quaternionf q = new Quaternionf();
        // check for exception condition
        if ((d.x == 0) && (d.z == 0)) {
            // exception condition if direction is (0,y,0):
            // straight up, straight down or all zero's.
            if (d.y > 0) { // direction straight up
                AxisAngle4f angleAxis = new AxisAngle4f(-(float) Math.PI / 2, 1, 0,
                        0);
                q.set(angleAxis);
            } else if (d.y < 0) { // direction straight down
                AxisAngle4f angleAxis = new AxisAngle4f((float) Math.PI / 2, 1, 0, 0);
                q.set(angleAxis);
            } else { // All zero's. Just set to identity quaternion
                q.identity();
            }
        } else {
            d.normalize();
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f s = new Vector3f();
            d.cross(up, s);
            s.normalize();
            Vector3f u = new Vector3f();
            d.cross(s, u);
            u.normalize();
            Matrix4f matrix = new Matrix4f(s.x, s.y, s.z, 0, u.x, u.y, u.z, 0, d.x,
                    d.y, d.z, 0, 0, 0, 0, 1);
            q.setFromNormalized(matrix);
        }
        return q;
    } // end ConvertDirectionalVectorToQuaternion

    public Matrix3f SetTextureTransformMatrix(SFVec2f translation, SFVec2f center, SFVec2f scale, SFFloat rotation) {
        // Texture Transform Matrix equation:
        // TC' = -C * S * R * C * T * TC
        // matrix is:
        //    moo m10 m20
        //    m01 m11 m21
        //    m02 m12 m22

        Matrix3f translationMatrix = new Matrix3f().identity();
        translationMatrix.m02 = translation.x;
        translationMatrix.m12 = translation.y;

        Matrix3f centerMatrix = new Matrix3f().identity();
        centerMatrix.m02 = center.x;
        centerMatrix.m12 = center.y;
        Matrix3f negCenterMatrix = new Matrix3f().identity();
        negCenterMatrix.m02 = -center.x;
        negCenterMatrix.m12 = -center.y;

        Matrix3f scaleMatrix = new Matrix3f().scale(scale.x, scale.y, 1);

        Matrix3f rotationMatrix = new Matrix3f().identity().rotateZ(rotation.getValue());

        Matrix3f textureTransformMatrix = new Matrix3f().identity();
        return textureTransformMatrix.mul(negCenterMatrix).mul(scaleMatrix).mul(rotationMatrix).mul(centerMatrix).mul(translationMatrix);
    }



    }  //  end AnimationInteractivityManager class



