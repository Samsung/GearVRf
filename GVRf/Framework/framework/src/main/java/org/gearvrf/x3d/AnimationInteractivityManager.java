
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

import android.graphics.Color;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.script.GVRJavascriptScriptFile;

import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.script.javascript.GVRJavascriptV8File;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFColor;
import org.gearvrf.x3d.data_types.SFFloat;
import org.gearvrf.x3d.data_types.SFTime;
import org.gearvrf.x3d.data_types.SFVec3f;
import org.gearvrf.x3d.data_types.SFRotation;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;

import java.util.ArrayList;
import java.util.Vector;

import javax.script.Bindings;


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
                                         ArrayList<ScriptObject> scriptObjects
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
                                //routeFromScriptObject.setToDefinedItem(field, routeToDefinedItem, toField);
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
                                        * interactiveObject.getTimeSensor().cycleInterval
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
                                        * interactiveObject.getTimeSensor().cycleInterval
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
                                        * interactiveObject.getTimeSensor().cycleInterval
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
                            interactiveObject.getTimeSensor().cycleInterval * FRAMES_PER_SECOND,
                            FRAMES_PER_SECOND);
                    gvrKeyFrameAnimation.addChannel(gvrAnimationChannel);
                    if (interactiveObject.getTimeSensor().loop) {
                        gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
                        gvrKeyFrameAnimation.setRepeatCount(-1);
                    }
                    gvrKeyFrameAnimation.prepare();
                    animationCount++;

                    // Third, determine if this will be animation only, or
                    // interactive triggered in picking
                    if (interactiveObject.getSensor() == null) {
                        // this is an animation without interactivity
                        //TODO: Switch to GVRJassimpAdapter processScene implementation
                        gvrKeyFrameAnimation.start(gvrContext.getAnimationEngine());
                    } else {
                        // this is an interactive object
                        final InteractiveObject interactiveObjectFinal = interactiveObject;
                        final GVRKeyFrameAnimation gvrKeyFrameAnimationFinal = gvrKeyFrameAnimation;

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
                                        interactiveObjectFinal.getSensor().setHitPoint(event.getHitPoint());
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

                if (interactiveObject.getSensor().getType() == Sensor.Type.TOUCH) {

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
                                            GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLightBase.getComponentType());
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
                    if (interactiveObject.getSensor().getType() == Sensor.Type.TOUCH) {

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
                }
            }  // end if a Script (that likely includes a sensor)

            else if ((interactiveObject.getSensor() != null) &&
                    (interactiveObject.getDefinedItem() != null)) {
                // a sensor and defined object, such as a TouchSensor
                //    to a Boolean such as light on/off
                final InteractiveObject interactiveObjectFinal = interactiveObject;
                if (interactiveObject.getSensor().getType() == Sensor.Type.TOUCH) {
                    interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
                        //boolean isRunning;
                        @Override
                        public void onSensorEvent(SensorEvent event) {
                            //Setup SensorEvent callback here
                            GVRSceneObject gvrSceneObject = root
                                    .getSceneObjectByName(interactiveObjectFinal.getDefinedItem().getName());
                            GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLightBase.getComponentType());

                            if (event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) {
                                if (gvrComponent != null) gvrComponent.setEnable(true);
                            } else {
                                if (gvrComponent != null) gvrComponent.setEnable(false);
                            }
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

                if (interactiveObject.getSensor().getType() == Sensor.Type.TOUCH) {

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

        }  // end for loop traversing through all interactive objects
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

            cycleInterval = this.interactiveObjectFinal.getTimeSensor().cycleInterval;
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
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
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
                        scriptParameters.add( timeSensor.cycleInterval );
                    }
                    else scriptParameters.add(argument0); // the time passed in from an SFTime node
                } else if (scriptObject.getFromDefinedItem(field) != null) {
                    if (fieldType.equalsIgnoreCase("SFColor")) {
                        float[] color = {0, 0, 0};
                        if (definedItem.getGVRMaterial() != null) {
                            if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("diffuseColor")) {
                                float[] diffuseColor = definedItem.getGVRMaterial().getVec4("diffuse_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = diffuseColor[i]; // only get the first 3 values, not the alpha value
                                }
                            } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("emissiveColor")) {
                                float[] emissiveColor = definedItem.getGVRMaterial().getVec4("emissive_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = emissiveColor[i]; // only get the first 3 values, not the alpha value
                                }
                            } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("specularColor")) {
                                float[] specularColor = definedItem.getGVRMaterial().getVec4("specular_color");
                                for (int i = 0; i < 3; i++) {
                                    color[i] = specularColor[i]; // only get the first 3 values, not the alpha value
                                }
                            }
                        } else if (definedItem.getGVRSceneObject() != null) {
                            // likely a light object so get its properties
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
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
                        if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("rotation")) {
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
                        else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("orientation")) {
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
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
                            if (gvrComponent != null) {
                                // it's a light
                                float[] parameter = {0, 0, 0};
                                if (gvrComponent instanceof GVRSpotLight) {
                                    GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                    if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("attenuation")) {
                                        parameter[0] = gvrSpotLightBase.getAttenuationConstant();
                                        parameter[1] = gvrSpotLightBase.getAttenuationLinear();
                                        parameter[2] = gvrSpotLightBase.getAttenuationQuadratic();
                                    } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("location")) {
                                        parameter[0] = definedItem.getGVRSceneObject().getTransform().getPositionX();
                                        parameter[1] = definedItem.getGVRSceneObject().getTransform().getPositionY();
                                        parameter[2] = definedItem.getGVRSceneObject().getTransform().getPositionZ();
                                    } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("direction")) {
                                        parameter[0] = definedItem.getDirection().x;
                                        parameter[1] = definedItem.getDirection().y;
                                        parameter[2] = definedItem.getDirection().z;
                                    }
                                } else if (gvrComponent instanceof GVRPointLight) {
                                    GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                    if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("attenuation")) {
                                        parameter[0] = gvrPointLightBase.getAttenuationConstant();
                                        parameter[1] = gvrPointLightBase.getAttenuationLinear();
                                        parameter[2] = gvrPointLightBase.getAttenuationQuadratic();
                                    } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("location")) {
                                        parameter[0] = definedItem.getGVRSceneObject().getTransform().getPositionX();
                                        parameter[1] = definedItem.getGVRSceneObject().getTransform().getPositionY();
                                        parameter[2] = definedItem.getGVRSceneObject().getTransform().getPositionZ();
                                    }
                                } else if (gvrComponent instanceof GVRDirectLight) {
                                    GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                    if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("direction")) {
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
                                    if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("translation")) {
                                        GVRSceneObject gvrSceneObjectTranslation = root
                                                .getSceneObjectByName((definedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_TRANSLATION_));
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionX());
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionY());
                                        scriptParameters.add(gvrSceneObjectTranslation.getTransform().getPositionZ());
                                    } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("scale")) {
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

                    else if (fieldType.equalsIgnoreCase("SFFloat")) {
                        if (definedItem.getGVRMaterial() != null) {
                            if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("shininess")) {
                                scriptParameters.add(
                                        definedItem.getGVRMaterial().getFloat("specular_exponent")
                                );
                            }
                            else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("transparency")) {
                                scriptParameters.add(definedItem.getGVRMaterial().getOpacity());
                            }
                        } else if (definedItem.getGVRSceneObject() != null) {
                            // checking if it's a light
                            GVRComponent gvrComponent = definedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
                            if (gvrComponent != null) {
                                float parameter = 0;
                                if (gvrComponent instanceof GVRSpotLight) {
                                    GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                    if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("beamWidth")) {
                                        parameter = gvrSpotLightBase.getInnerConeAngle() * (float) Math.PI / 180;
                                    } else if (scriptObject.getFromDefinedItemField(field).equalsIgnoreCase("cutOffAngle")) {
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
                    }  // end if SFFloat
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
                complete = gvrJavascriptV8FileFinal.invokeFunction(INITIALIZE_FUNCTION, parametersFinal, "");

                if (complete) {
                    // The JavaScript initialize() function ran ok.
                    // Now set any values (such as X3D data types such as SFColor)
                    //  stored in 'localBindings'.
                    //  Then call SetResultsFromScript() to set the GearVR values
                    Bindings bindings = gvrJavascriptV8FileFinal.getLocalBindings();
                    SetResultsFromScript(interactiveObjectFinal, bindings);
                } else {
                    Log.e(TAG, "Error in SCRIPT node '" +  interactiveObjectFinal.getScriptObject().getName() +
                            "' running V8 Engine JavaScript function initialize()");
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
                    }  // end if SFColor of SFVec3f
                    else if (fieldType.equalsIgnoreCase("SFRotation")) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "], params[" + (argumentNum + 1) + "], params[" + (argumentNum + 2)
                                + "], params[" + (argumentNum + 3) + "]);\n";
                        argumentNum += 4;
                    }  // end if SFRotation

                    else if ((fieldType.equalsIgnoreCase("SFFloat")) || (fieldType.equalsIgnoreCase("SFBool"))) {
                        gearVRinitJavaScript += scriptObject.getFieldName(field) + " = new " + scriptObject.getFieldType(field) +
                                "( params[" + argumentNum + "]);\n";
                        argumentNum += 1;
                    }  // end if SFFloat or SFBool
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
                        "' running V8 Engine JavaScript function '" + functionNameFinal + "'");
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
                    if (returnedJavaScriptValue != null) {
                        if (fieldType.equalsIgnoreCase("SFBool")) {
                            SFBool sfBool = (SFBool) returnedJavaScriptValue;
                            if ( scriptObjectToDefinedItem != null) {
                                if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                    GVRComponent gvrComponent = scriptObjectToDefinedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
                                    if (gvrComponent != null) {
                                        gvrComponent.setEnable(sfBool.getValue());
                                    }
                                }  //  end if the SceneObject has a light component attached
                            }  //  end scriptObjectToDefinedItem != null
                            else if ( scriptObjectToEventUtility != null) {
                                scriptObjectToEventUtility.setToggle(sfBool.getValue());
                            }
                        }  //  end SFBool
                        else if (fieldType.equalsIgnoreCase("SFFloat")) {
                            SFFloat sfFloat = (SFFloat) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("shininess")) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setSpecularExponent(sfFloat.getValue());
                                } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("transparency")) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setOpacity(sfFloat.getValue());
                                }
                            } else if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                GVRComponent gvrComponent = scriptObjectToDefinedItem.getGVRSceneObject().getComponent(GVRLightBase.getComponentType());
                                if (gvrComponent != null) {
                                    if (gvrComponent instanceof GVRSpotLight) {
                                        GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                        if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("beamWidth")) {
                                            gvrSpotLightBase.setInnerConeAngle(sfFloat.getValue() * 180 / (float) Math.PI);
                                        } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("cutOffAngle")) {
                                            gvrSpotLightBase.setOuterConeAngle(sfFloat.getValue() * 180 / (float) Math.PI);
                                        } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("intensity")) {
                                            //TODO: we aren't changing intensity since this would be multiplied by color
                                        } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("radius")) {
                                            //TODO: radius is not currently supported in GearVR for the spot light
                                        }
                                    } else if (gvrComponent instanceof GVRPointLight) {
                                        GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                        if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("intensity")) {
                                            //TODO: we aren't changing intensity since this would be multiplied by color
                                        } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("radius")) {
                                            //TODO: radius is not currently supported in GearVR for the point light
                                        }
                                    } else if (gvrComponent instanceof GVRDirectLight) {
                                        GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                        if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("intensity")) {
                                            //TODO: we aren't changing intensity since GVR multiplies this by color
                                        }
                                    }
                                }  //  end presumed to be a light
                            }  //  end GVRScriptObject ! null
                        }  //  end SFFloat
                        else if (fieldType.equalsIgnoreCase("SFTime")) {
                            SFTime sfTime = (SFTime) returnedJavaScriptValue;
                            //TODO: eventually will be supported

                        }  //  end SFTime
                        else if (fieldType.equalsIgnoreCase("SFColor")) {
                            SFColor sfColor = (SFColor) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRMaterial() != null) {
                                //  SFColor change to a GVRMaterial
                                if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("diffuseColor")) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("diffuse_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("specularColor")) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("specular_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("emissiveColor")) {
                                    scriptObjectToDefinedItem.getGVRMaterial().setVec4("emissive_color", sfColor.getRed(), sfColor.getGreen(), sfColor.getBlue(), 1.0f);
                                }
                            }  //  end SFColor change to a GVRMaterial
                            else if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                // GVRSceneObject
                                GVRSceneObject gvrSceneObject = scriptObjectToDefinedItem.getGVRSceneObject();
                                GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLightBase.getComponentType());
                                if (gvrComponent != null) {
                                    if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("color")) {
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
                        }  //  end SFColor (to a light or Material)
                        else if (fieldType.equalsIgnoreCase("SFVec3f")) {
                            SFVec3f sfVec3f = (SFVec3f) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                //  SFVec3f change to a GVRSceneObject
                                GVRSceneObject gvrSceneObject = scriptObjectToDefinedItem.getGVRSceneObject();
                                if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("translation") ||
                                        scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("location")) {
                                    // location applies to point light and spot light
                                    GVRSceneObject gvrSceneObjectTranslation = root
                                            .getSceneObjectByName((scriptObjectToDefinedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_TRANSLATION_));
                                    if (gvrSceneObjectTranslation != null)
                                        gvrSceneObjectTranslation.getTransform().setPosition(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                    else
                                        gvrSceneObject.getTransform().setPosition(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("scale")) {
                                    GVRSceneObject gvrSceneObjectScale = root
                                            .getSceneObjectByName((scriptObjectToDefinedItem.getGVRSceneObject().getName() + x3dObject.TRANSFORM_SCALE_));
                                    if (gvrSceneObjectScale != null)
                                        gvrSceneObjectScale.getTransform().setScale(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                    else
                                        gvrSceneObject.getTransform().setScale(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                } else {
                                    // could be parameters for a light
                                    GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLightBase.getComponentType());
                                    if (gvrComponent != null) {
                                        if (gvrComponent instanceof GVRSpotLight) {
                                            GVRSpotLight gvrSpotLightBase = (GVRSpotLight) gvrComponent;
                                            if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("attenuation")) {
                                                gvrSpotLightBase.setAttenuation(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("direction")) {
                                                scriptObjectToDefinedItem.setDirection(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                                Vector3f v3 = new Vector3f(sfVec3f).normalize();
                                                Quaternionf q = ConvertDirectionalVectorToQuaternion(v3);
                                                gvrSpotLightBase.getTransform().setRotation(q.w, q.x, q.y, q.z);
                                            } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("location")) {
                                                gvrSpotLightBase.setPosition(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            }
                                        } else if (gvrComponent instanceof GVRPointLight) {
                                            GVRPointLight gvrPointLightBase = (GVRPointLight) gvrComponent;
                                            if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("attenuation")) {
                                                gvrPointLightBase.setAttenuation(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            } else if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("location")) {
                                                gvrPointLightBase.setPosition(sfVec3f.getX(), sfVec3f.getY(), sfVec3f.getZ());
                                            }
                                        } else if (gvrComponent instanceof GVRDirectLight) {
                                            GVRDirectLight gvrDirectLightBase = (GVRDirectLight) gvrComponent;
                                            if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("direction")) {
                                                scriptObjectToDefinedItem.setDirection(sfVec3f.x, sfVec3f.y, sfVec3f.z);
                                                Vector3f v3 = new Vector3f(sfVec3f).normalize();
                                                Quaternionf q = ConvertDirectionalVectorToQuaternion(v3);
                                                gvrDirectLightBase.getTransform().setRotation(q.w, q.x, q.y, q.z);
                                            }
                                        }
                                    }
                                }  // end it could be a light
                            }  // end GVRSceneObject with SFVec3f
                        }  //  end SFVec3f
                        else if (fieldType.equalsIgnoreCase("SFRotation")) {
                            SFRotation sfRotation = (SFRotation) returnedJavaScriptValue;
                            if (scriptObjectToDefinedItem.getGVRSceneObject() != null) {
                                //  SFRotation change to a GVRSceneObject
                                if (scriptObject.getToDefinedItemField(fieldNode).equalsIgnoreCase("rotation")) {
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
                        }  //  end SFRotation
                    }  //  end value != null
                }  //  end OUTPUT-ONLY or INPUT_OUTPUT
            }  // end for-loop list of fields for a single script
        } catch (Exception e) {
            Log.e(TAG, "Error setting values returned from JavaScript in SCRIPT node." +
                    "  Check JavaScript or ROUTE's.  Exception: " + e);
        }
    }  //  end  SetResultsFromScript


    /**
     * Converts a vector into a quaternion.
     * Used for the direction of spot and directional lights
     * Called upon initialization and updates to those vectors
     *
     * @param d
     * @return
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


}  //  end AnimationInteractivityManager class



