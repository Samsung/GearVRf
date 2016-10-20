
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

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Vector;

/**
 * 
 * @author m1.williams
 * AnimationInteractivityManager will construct an InteractiveObject based on the to/from node/field Strings.
 * If the Strings match actual objects (DEFitems, TimeSensors, Iterpolators, etc) from their
 * respective array lists, then
 * AnimationInteractivityManager will either construct an interactiveObject and add it to an array list,
 * or modify an existing interactiveObject to include a 'pointer' (to the TimeSensor,
 * Sensor, Interpolator, etc.) to tie all the sensors, timers, interpolators and
 * defined items (Transform, Material, TextureTransform, Color, etc) into a single object.
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

  private X3Dobject x3dObject = null;
  private GVRContext gvrContext = null;
  private GVRSceneObject root = null;
  private Vector<DefinedItem> definedItems = null;
  private Vector<Interpolator> interpolators = null;
  private Vector<Sensor> sensors = null;
  private Vector<TimeSensor> timeSensors = null;
  private Vector<EventUtility> eventUtilities = null;

  // Append this incremented value to GVRSceneObject names to insure unique
  // GVRSceneObjects when new GVRScene objects are generated to support animation
  private static int animationCount = 1;


  public AnimationInteractivityManager(X3Dobject x3dObject, GVRContext gvrContext,
                                       GVRSceneObject root,
                                       Vector<DefinedItem> definedItems,
                                       Vector<Interpolator> interpolators,
                                       Vector<Sensor> sensors,
                                       Vector<TimeSensor> timeSensors,
                                       Vector<EventUtility> eventUtilities) {
    this.x3dObject = x3dObject;
    this.gvrContext = gvrContext;
    this.root = root; // helps search for GVRSCeneObjects by name
    this.definedItems = definedItems;
    this.interpolators = interpolators;
    this.sensors = sensors;
    this.timeSensors = timeSensors;
    this.eventUtilities = eventUtilities;
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
    // TODO: Scripting functionality will eventually be added
    // TODO: ScriptingObject scriptingObject = null;

    // Get pointers to the Sensor, TimeSensor, Interpolator and/or
    // Defined Items based the nodes of this object
    for (Sensor sensor : sensors) {
      if (sensor.getName().equalsIgnoreCase(fromNode)) {
        routeFromSensor = sensor;
      }
    }

    for (TimeSensor timeSensor : timeSensors) {
      if (timeSensor.name.equalsIgnoreCase(toNode)) {
        routeToTimeSensor = timeSensor;
      }
      else if (timeSensor.name.equalsIgnoreCase(fromNode)) {
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

    for (DefinedItem definedItem : definedItems) {
      if (definedItem.getName().equalsIgnoreCase(toNode)) {
        routeToDefinedItem = definedItem;
      }
    }

    // Now assign these pointers to an existing InteractiveObject that matches
    // or create a new InteractiveObject.
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
      if ( !routeToTimeSensorFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setSensor(routeFromSensor, fromField);
        interactiveObject.setTimeSensor(routeToTimeSensor);
        interactiveObjects.add(interactiveObject);
      }
    }

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
      if ( !routeToInterpolatorFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        for (InteractiveObject interactiveObject : interactiveObjects) {
          if (routeFromTimeSensor == interactiveObject.getTimeSensor()) {
            if (interactiveObject.getInterpolator() == null) {
              //This timer already exists as part of an interactive Object
              interactiveObject.setInterpolator(routeToInterpolator);
              routeToInterpolatorFound = true;
            }
          }
        }
      }
      if ( !routeToInterpolatorFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setTimeSensor(routeFromTimeSensor);
        interactiveObject.setInterpolator(routeToInterpolator);
        interactiveObjects.add(interactiveObject);
      }
    }

    if (routeToEventUtility != null) {
      boolean routeToEventUtilityFound = false;
      for (InteractiveObject interactiveObject : interactiveObjects) {
        if (routeToEventUtility == interactiveObject.getEventUtility()) {
          if (interactiveObject.getSensor() == null) {
            //This sensor already exists as part of an interactive Object
            interactiveObject.setSensor(routeFromSensor, fromField);
            routeToEventUtilityFound = true;
          }
        }
      }
      if ( !routeToEventUtilityFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setSensor(routeFromSensor, fromField);
        interactiveObject.setEventUtility(routeToEventUtility);
        interactiveObjects.add(interactiveObject);
      }
    }

    if (routeToDefinedItem != null) {
      boolean routeToDEFinedItemFound = false;
      for (InteractiveObject interactiveObject : interactiveObjects) {
        if ( (routeFromInterpolator == interactiveObject.getInterpolator()) &&
                (routeFromInterpolator != null) ) {
          if (interactiveObject.getDefinedItemToField() == null ) {
            interactiveObject.setDefinedItem(routeToDefinedItem, toField);
            routeToDEFinedItemFound = true;
          }
        }
        else if ( (routeFromEventUtility == interactiveObject.getEventUtility()) &&
                (routeFromEventUtility != null)) {
          if (interactiveObject.getDefinedItemToField() == null ) {
            interactiveObject.setDefinedItem(routeToDefinedItem, toField);
            routeToDEFinedItemFound = true;
          }
        }
      }
      if ( !routeToDEFinedItemFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setInterpolator(routeFromInterpolator);
        interactiveObject.setDefinedItem(routeToDefinedItem, toField);
        if (routeFromSensor != null) interactiveObject.setSensor(routeFromSensor, fromField);
        if (routeFromEventUtility != null) {
          interactiveObject.setEventUtility(routeFromEventUtility);
        }
        interactiveObjects.add(interactiveObject);
      }
    }  //  end if routeToDefinedItem != null
  }  //  end createRouteObject

  /**
   * initAniamtionsAndInteractivity() called when we parse </scene> in
   * an X3D file.  This method will parse the array list of InteractiveObjects
   * determining which are animations (when interactiveObject.sensor is null)
   * or which are interactive and thus have a event attached to invoke the
   * animation upon a TouchSensor, Anchor, etc.
   */
  public void initAniamtionsAndInteractivity() {
    for (InteractiveObject interactiveObject : interactiveObjects) {
      GVRAnimationChannel gvrAnimationChannel = null;
      GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
      GVRSceneObject gvrAnimatedObject = null;

      //interactiveObject.printInteractiveObject();
      // both animated and interactive objects currently must have a time
      // sensor, interpolator and a Transform node with a DEF="..." parameter
      if ( (interactiveObject.getTimeSensor() != null) &&
                (interactiveObject.getInterpolator() != null) &&
                (interactiveObject.getDefinedItem() != null) ) {
        // Set up the animation objects, properties
        //   first construct the animation channel based on translation, rotation, scale, etc.
        if ((interactiveObject.getDefinedItemToField().toLowerCase().endsWith(TRANSLATION)) ||
            (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(POSITION))  ){
          gvrAnimatedObject = root
                    .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_TRANSLATION_));
          gvrAnimationChannel = new GVRAnimationChannel(
                    gvrAnimatedObject.getName(),
                    interactiveObject.getInterpolator().key.length, 0, 0,
                    GVRAnimationBehavior.LINEAR, GVRAnimationBehavior.LINEAR);
          for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++) {
            Vector3f vector3f = new Vector3f(
                      interactiveObject.getInterpolator().keyValue[j * 3],
                      interactiveObject.getInterpolator().keyValue[j * 3 + 1],
                      interactiveObject.getInterpolator().keyValue[j * 3 + 2]);
            gvrAnimationChannel.setPosKeyVector(j,
                      interactiveObject.getInterpolator().key[j]
                              * interactiveObject.getTimeSensor().cycleInterval
                              * FRAMES_PER_SECOND, vector3f);
          }
        }  //  end translation

        else if ((interactiveObject.getDefinedItemToField().toLowerCase().endsWith(ROTATION)) ||
                  (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(ORIENTATION))  ) {
          gvrAnimatedObject = root
                    .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_ROTATION_));
          gvrAnimationChannel = new GVRAnimationChannel(
                    gvrAnimatedObject.getName(), 0,
                    interactiveObject.getInterpolator().key.length, 0,
                    GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);

          for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++)
          {
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

        else if ( interactiveObject.getDefinedItemToField().toLowerCase().endsWith(SCALE)  ) {
          gvrAnimatedObject = root
                    .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_SCALE_));
          gvrAnimationChannel = new GVRAnimationChannel(
                    gvrAnimatedObject.getName(), 0, 0,
                    interactiveObject.getInterpolator().key.length, GVRAnimationBehavior.DEFAULT,
                    GVRAnimationBehavior.DEFAULT);
          for (int j = 0; j < interactiveObject.getInterpolator().key.length; j++)
          {
            Vector3f vector3f = new Vector3f(
                      interactiveObject.getInterpolator().keyValue[j * 3],
                      interactiveObject.getInterpolator().keyValue[j * 3 + 1],
                      interactiveObject.getInterpolator().keyValue[j * 3 + 2]);
            gvrAnimationChannel.setScaleKeyVector(j,
                      interactiveObject.getInterpolator().key[j]
                              * interactiveObject.getTimeSensor().cycleInterval
                              * FRAMES_PER_SECOND, vector3f);
          }
        }  //  end scale
        else {
          Log.e(TAG, interactiveObject.getDefinedItemToField() + " not implemented");
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
            gvrKeyFrameAnimation.start(gvrContext.getAnimationEngine());
          }
          else {
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
                    interactiveObjectFinal.getSensor().setHitPoint( event.getHitPoint() );
                    gvrKeyFrameAnimationFinal.start(gvrContext.getAnimationEngine())
                            .setOnFinish(new GVROnFinish() {
                                @Override
                                public void finished(GVRAnimation animation) {isRunning = false;
                                }
                              });
                  }
                }
              }
            });

          }
        }
        else {
          Log.e(TAG, interactiveObject.getDefinedItem().getName() + " possibly not found in the scene.");
        }
      }  // end if at least timer, interpolator and defined object

      else if ( (interactiveObject.getSensor() != null) &&
              (interactiveObject.getEventUtility() != null) &&
              (interactiveObject.getDefinedItem() != null) ) {
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
                if ( !stateChanged ) {
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
              }
              else if ( !event.isActive() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_ACTIVE) ){
                stateChanged = false;
              }
              else if ( !event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor
                      .IS_OVER) ){
                stateChanged = false;
              }
            }
          });
        }  // end if sensor == TOUCH
      }  // end if at least sensor, eventUtility and defined object

      else if ( (interactiveObject.getSensor() != null) &&
              (interactiveObject.getDefinedItem() != null) ) {
        // a sensor and defined object
        final InteractiveObject interactiveObjectFinal = interactiveObject;

        if (interactiveObject.getSensor().getType() == Sensor.Type.TOUCH) {

          interactiveObject.getSensor().addISensorEvents(new ISensorEvents() {
            @Override
            public void onSensorEvent(SensorEvent event) {
              //Setup SensorEvent callback here
              GVRSceneObject gvrSceneObject = root
                      .getSceneObjectByName( interactiveObjectFinal.getDefinedItem().getName() );
              GVRComponent gvrComponent = gvrSceneObject.getComponent(GVRLightBase.getComponentType());

              if (event.isOver() && interactiveObjectFinal.getSensorFromField().equals(Sensor.IS_OVER)) {
                  if (gvrComponent != null) gvrComponent.setEnable(true);
              }
              else {
                if (gvrComponent != null) gvrComponent.setEnable(false);
              }
            }
          });
        }  // end if sensor == TOUCH
      }  //  end sensor and definedItem != null
    }  // end for loop traversing through all interactive objects
  }   //  end initAniamtionsAndInteractivity

}  //  end AnimationInteractivityManager class



