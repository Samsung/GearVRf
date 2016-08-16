
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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
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
 * RouteBuilder will construct a RouteObject based on the to/from node/field strings.
 * If the strings refer to actual object (DEFitems, TimeSensors, Iterpolators, etc),
 * RouteBuilder will construct an interactiveObject and add it to an array list,
 * or it will modify an interactiveObject to include a 'pointer' to the TimeSensor,
 * Sensor, Interpolator, etc. to tie all the sensors, timers, interpolators and
 * defined items (Transform, Material, TextureTransform, Color, etc) into one object.
 */

public class RouteBuilder {

  private final static float FRAMES_PER_SECOND = 60.0f;
  private Vector<InteractiveObject> interactiveObjects = new Vector<InteractiveObject>();

  private static final String IS_OVER = "isOver";
  private static final String Is_ACTIVE = "isActive";

  private static final String ROTATION = "rotation";
  private static final String ORIENTATION = "orientation";
  private static final String TRANSLATION = "translation";
  private static final String POSITION = "position";
  private static final String SCALE = "scale";
  public static final String KEY_FRAME_ANIMATION = "KeyFrameAnimation_";

  private X3Dobject x3dObject = null;
  private GVRContext gvrContext = null;
  private GVRModelSceneObject root = null;
  private Vector<DefinedItem> definedItems = null;
  private Vector<Interpolator> interpolators = null;
  private Vector<Sensor> sensors = null;
  private Vector<TimeSensor> timeSensors = null;

  // Append this incremented value to GVRScene names to insure unique
  // GVRSceneObjects when new GVRScene objects are generated to support animation
  private static int animationCount = 1;


  public RouteBuilder(X3Dobject x3dObject, GVRContext gvrContext,
                      GVRModelSceneObject root,
                      Vector<DefinedItem> definedItems,
                      Vector<Interpolator> interpolators,
                      Vector<Sensor> sensors,
                      Vector<TimeSensor> timeSensors) {
    this.x3dObject = x3dObject;
    this.gvrContext = gvrContext;
    this.root = root; // helps search for GVRSCeneObjects by name
    this.definedItems = definedItems;
    this.interpolators = interpolators;
    this.sensors = sensors;
    this.timeSensors = timeSensors;
  }

  public void createRouteObject(String fromNode, String fromField, String toNode, String toField) {
    // scan the arrays of
    Sensor routeFromSensor = null;
    TimeSensor routeToTimeSensor = null;
    TimeSensor routeFromTimeSensor = null;
    Interpolator routeToInterpolator = null;
    Interpolator routeFromInterpolator = null;
    DefinedItem routeToDefinedItem = null;
    // Scripting functionality will eventually be added
    // ScriptingObject scriptingObject = null;
    //Log.e("RouteBldr", "  ");
    //Log.e("RouteBldr", "fromNode: " + fromNode + ", toNode: " + toNode);

    // Get pointers to the Sensor, TimeSensor, Interpolator and/or
    // Defined Items based the nodes of this object
    for (Sensor sensor : sensors) {
      if (sensor.name.equalsIgnoreCase(fromNode)) {
        routeFromSensor = sensor;
        Log.e("RouteBldr", "sensor: " + fromNode + "." + fromField + " to " + toNode + "." + toField);

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

    for (DefinedItem definedItem : definedItems) {
      if (definedItem.getName().equalsIgnoreCase(toNode)) {
        routeToDefinedItem = definedItem;
      }
    }

    // Now assign these pointers to an existing InteractiveObject that matches
    // or create a new InteractiveObject.
    if (routeToTimeSensor != null) {
      //Log.e("RouteBldr", "Add Sensor-TimeSensor InteractiveObject");
      boolean routeToTimeSensorFound = false;
      for (InteractiveObject interactiveObject : interactiveObjects) {
        if (routeToTimeSensor == interactiveObject.getTimeSensor()) {
          if (interactiveObject.getSensor() == null) {
            //This sensor already exists inside an interactive Object
            //Log.e("RouteBldr", "   routeToTimeSensor in Int_Obj.  Set Sensor");
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
        //Log.e("RouteBldr", "   Add new Int_Obj " + fromNode + ", " + toNode);
      }
    }

    if (routeToInterpolator != null) {
      //Log.e("RouteBldr", "Add TimeSensor - Interpolator InteractiveObject");
      boolean routeToInterpolatorFound = false;
      for (InteractiveObject interactiveObject : interactiveObjects) {
        if (routeToInterpolator == interactiveObject.getInterpolator()) {
          if (interactiveObject.getTimeSensor() == null) {
            //This sensor already exists inside an interactive Object
            //Log.e("RouteBldr", "   routeToInterpolaotr in Int_Obj.  Set timeSensor");
            interactiveObject.setTimeSensor(routeFromTimeSensor);
            routeToInterpolatorFound = true;
          }
        }
      }
      if ( !routeToInterpolatorFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setTimeSensor(routeFromTimeSensor);
        interactiveObject.setInterpolator(routeToInterpolator);
        interactiveObjects.add(interactiveObject);
        //Log.e("RouteBldr", "   Add new Int_Obj " + fromNode + ", " + toNode);
      }
    }


    if (routeToDefinedItem != null) {
      //Log.e("RouteBldr", "Add Interpolator - DEFined InteractiveObject");
      boolean routeToDEFinedItemFound = false;
      for (InteractiveObject interactiveObject : interactiveObjects) {
        if (routeFromInterpolator == interactiveObject.getInterpolator()) {
          //if (interactiveObject.getInterpolator() == null) {
            //This sensor already exists inside an interactive Object
            //Log.e("RouteBldr", "   routeToDEFinedIteam in Int_Obj.  Set defined item");
            interactiveObject.setDefinedItem(routeToDefinedItem, toField);
            routeToDEFinedItemFound = true;
          //}
        }
      }
      if ( !routeToDEFinedItemFound ) {
        // construct a new interactiveObject for this sensor and timeSensor
        InteractiveObject interactiveObject = new InteractiveObject();
        interactiveObject.setInterpolator(routeFromInterpolator);
        interactiveObject.setDefinedItem(routeToDefinedItem, toField);
        interactiveObjects.add(interactiveObject);
        //Log.e("RouteBldr", "   Add new Int_Obj " + fromNode + ", " + toNode);
      }
    }
  }  //  end createRouteObject

  public void initAniamtionsAndInteractivity() {
    Log.e("RouteBldr", "initAniamtionsAndInteractivity");
    for (InteractiveObject interactiveObject : interactiveObjects) {
      GVRAnimationChannel gvrAnimationChannel = null;
      GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
      GVRSceneObject gvrAnimatedObject = null;
      if (interactiveObject.getSensor() == null) {
        Log.e("RouteBldr", "no Sensor");
        interactiveObject.printInteractiveObject();
        // likely and animated non-interactive object
        if ( (interactiveObject.getTimeSensor() != null) &&
                (interactiveObject.getInterpolator() != null) &&
                (interactiveObject.getDefinedItem() != null) ) {
          // an animated non-interactive object
          Log.e("RouteBldr", "Animation: " + interactiveObject.getTimeSensor().name + " TO " +
                  interactiveObject.getInterpolator().name + " TO " + interactiveObject.getDefinedItem().getName() + "."
                + interactiveObject.getDefinedItemToField() );
          if ((interactiveObject.getDefinedItemToField().toLowerCase().endsWith(TRANSLATION)) ||
            (interactiveObject.getDefinedItemToField().toLowerCase().endsWith(POSITION))  ){
            gvrAnimatedObject = root
                    .getSceneObjectByName((interactiveObject.getDefinedItem().getName() + x3dObject.TRANSFORM_TRANSLATION_));
            Log.e("RouteBldr", "   Translation " + gvrAnimatedObject.getName());
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
            Log.e("RouteBldr", "   Rotation: " + interactiveObject.getDefinedItem().getName());
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
            Log.e("RouteBldr", "   Scale: " + interactiveObject.getDefinedItem().getName());
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
            Log.e("RouteBldr", interactiveObject.getDefinedItemToField() + " not implemented");
          }
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
            //mAnimations.add((GVRKeyFrameAnimation) gvrKeyFrameAnimation);
            gvrKeyFrameAnimation.start(gvrContext.getAnimationEngine());
            animationCount++;
          }
          else {
            Log.e("RouteBldr", interactiveObject.getDefinedItem().getName() + " possibly not found in the scene.");
          }

        }
      }
    }
  }   //  end initAniamtionsAndInteractivity

}  //  end RouteBuilder class



