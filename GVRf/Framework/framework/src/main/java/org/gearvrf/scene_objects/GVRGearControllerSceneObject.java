/* Copyright 2017 Samsung Electronics Co., LTD
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
package org.gearvrf.scene_objects;


import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISensorEvents;
import org.gearvrf.R;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.GVRControllerType;
import org.joml.Vector3f;

import java.util.Arrays;

/**
 * A {@link GVRSceneObject} meant to better respresent a GearVR controller.
 * One feature of the {@link GVRGearControllerSceneObject} is a default model for
 * representing the Gear Controller in VR as well as a means to replace it with your
 * own model. Additionally, a ray can be emitted from the controller object at variable
 * lengths, which is useful for interacting with objects at different distances.
 * One can optionally allow the ray to automatically adjust its length to
 * extend to the first object in the scene that the ray vector would intersect. This makes
 * it obvious to the user which object they are currently pointing at with the Gear Controller.
 * Lastly, one can use {@link this#enableSurfaceProjection()} to project the cursor onto the
 * object being picked. Read the method documentation for more information.
 */

public class GVRGearControllerSceneObject extends GVRSceneObject {
    private final String TAG = GVRGearControllerSceneObject.class.getSimpleName();
    private float rayDepth = 1.0f;
    private boolean projectToSurface = false;
    private GVRBaseSensor disabledSensor;
    private GVRSceneObject cursor = null;
    private GVRCursorController gearCursorController = null;
    private SensorListener sensorListener = new SensorListener();
    private GVRSceneObject controller;
    private GVRSceneObject ray;


    /**
     * Constructor for GVRGearControllerSceneObject
     *
     * This constructor creates a {@link GVRSceneObject} resembling a
     * physical Gear VR Controller and assigns it as a child to this object
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRGearControllerSceneObject(GVRContext gvrContext){
        super(gvrContext);
        disabledSensor = new GVRBaseSensor(getGVRContext());
        disabledSensor.disable();

        controller = new GVRSceneObject(gvrContext, gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.gear_vr_controller)));
        GVRTexture tex = (gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.gear_vr_controller_color_1024)));
        GVRMaterial mat = new GVRMaterial(gvrContext);
        mat.setMainTexture(tex);
        controller.getRenderData().setMaterial(mat);
        controller.getRenderData().getTransform().setScale(0.05f,0.05f,0.05f);
        controller.getRenderData().getTransform().rotateByAxis(270f, 1, 0, 0);
        controller.getRenderData().getTransform().rotateByAxis(180f, 0, 1,0);
        addChildObject(controller);

        ray = new GVRLineSceneObject(gvrContext, rayDepth);
        final GVRMaterial rayMaterial = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        rayMaterial.setDiffuseColor(0.5f,0.5f,0.5f,1);
        rayMaterial.setLineWidth(2.0f);
        ray.getRenderData().setMaterial(rayMaterial);
        addChildObject(ray);
    }

    /**
     * Sets a {@link GVRSceneObject} as the cursor to be displayed at
     * the end of the ray of the GearController.
     *
     * @param obj the scene object to represent the cursor
     */
    public void setCursor(GVRSceneObject obj){
        cursor = obj;
        cursor.getTransform().setPosition(0, 0, -rayDepth);
        cursor.setSensor(disabledSensor); //necessary for projection
        addChildObject(cursor);
    }

    /**
     * Returns the {@link GVRSceneObject} set to represent the cursor
     * @return the cursor object
     */
    public GVRSceneObject getCursor(){
        return cursor;
    }

    /**
     * Removes the {@link GVRSceneObject} set to represent the cursor
     * if one has been set.
     */
    public void removeCursor(){
        if(cursor != null) {
            removeChildObject(cursor);
            cursor = null;
        }
    }

    /**
     * Returns the {@link GVRSceneObject} that represents a ray emanating from
     * the controller
     *
     * @return  the ray {@link GVRSceneObject}
     */
    public GVRSceneObject getRay(){
        return ray;
    }

    /**
     * Enables the ray {@link GVRSceneObject}, making it visible if it wasn't
     * before.
     */
    public void enableRay(){
        ray.setEnable(true);
    }


    /**
     * Disables the ray {@link GVRSceneObject}, making it invisible if it wasn't
     * before.
     */
    public void disableRay(){
        ray.setEnable(false);
    }

    /**
     * Sets the ray depth, which determines how far the ray extends as well as
     * where the cursor is placed relative to the controller {@link GVRSceneObject}
     *
     * @param depth distance the ray should extend
     */
    public void setRayDepth(float depth){
        rayDepth = Math.abs(depth);
        ray.getTransform().setScaleZ(rayDepth);
        if(cursor != null)
            cursor.getTransform().setPosition(0, 0, -rayDepth);
    }

    /**
     * Returns the current ray depth.
     *
     * @return the ray depth
     */
    public float getRayDepth(){
        return this.rayDepth;
    }

    /**
     * Sets a {@link GVRSceneObject} to represent the Gear VR Controller in
     * a scene.
     *
     * @param obj the {@link GVRSceneObject} that will represent the controller
     */
    public void setControllerObject(GVRSceneObject obj){
        removeChildObject(controller);
        controller = obj;
        addChildObject(controller);
    }

    /**
     * Returns the current {@link GVRSceneObject} representing the Gear VR controller.
     * @return the controller {@link GVRSceneObject}
     */
    public GVRSceneObject getControllerObject(){
        return controller;
    }

    /**
     * Sets the {@link GVRCursorController} that controls this object. The cursor controller
     * should have a type of {@link org.gearvrf.io.GVRControllerType#CONTROLLER}.
     *
     * Note that a CursorController must be set in order to receive a non-null
     * {@link ISensorEvents} from {@link this#getProjectionListener()}
     *
     * @param gearCursorController the cursor controller that controls this scene object
     */
    public void setCursorController(GVRCursorController gearCursorController){
        if(gearCursorController != null && gearCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
            gearCursorController.setSceneObject(this);
            this.gearCursorController = gearCursorController;
        }
    }

    /**
     * Returns an {@link ISensorEvents} instance that can be added to a
     * {@link GVRSceneObject}'s set of {@link SensorEvent} listeners to enable automatic
     * adjustment of ray depth when the GearController's ray intersects the object
     * to create the appearance of the cursor/ray projecting on the object.
     *
     * Note: A {@link GVRBaseSensor} must be set with {@link GVRSceneObject#setSensor(GVRBaseSensor)}
     * and enabled in order for an object to receive SensorEvents.
     *
     * @return a custom {@link ISensorEvents} instance
     */
    public ISensorEvents getProjectionListener(){
        if(gearCursorController != null)
            return sensorListener;
        else
            return null;
    }

    /**
     * Enables surface projection, which will cause the cursor to be projected
     * on the surface of a {@link GVRSceneObject} according to the surface normal at the
     * intersection point of the controller's ray (the object must have attached the
     * {@link ISensorEvents} instance given by {@link this#getProjectionListener()}).
     * Note this requires additional calculations for every {@link SensorEvent} received.
     *
     * When this is disabled, the ray/cursor depth will still be adjusted by the
     * ProjectionListener, but the cursor will not be transformed according to
     * surface normals. Also note that a {@link org.gearvrf.GVRMeshCollider}
     * must be attached to a {@link GVRSceneObject} in order for surface normals
     * to be calculated during picking.
     */
    public void enableSurfaceProjection(){
        projectToSurface = true;
    }

    /**
     * Disables surface projection.
     */
    public void disableSurfaceProjection(){
        projectToSurface = false;
        if(cursor != null){
            GVRSceneObject parent = cursor.getParent();
            if (parent != this) {
                parent.removeChildObject(cursor);
                addChildObject(cursor);
            }
        }
    }


    private class SensorListener implements ISensorEvents {
        private final float[] nullCoords = {-1f, -1f, -1f};
        @Override
        public void onSensorEvent(SensorEvent event){
            if(event.getCursorController() != gearCursorController) { return; }

            GVRPicker.GVRPickedObject collision = event.getPickedObject();
            boolean cursorNull = cursor==null;
            boolean coordinatesCalculated = !Arrays.equals(collision.getBarycentricCoords(), nullCoords);
            GVRSceneObject parent = null;

            if(!cursorNull)
                parent = cursor.getParent();

            ray.getTransform().setScaleZ(collision.hitDistance);

            if(event.isOver()){
                if(!cursorNull){
                    if(projectToSurface && coordinatesCalculated) {
                        if (parent != collision.hitObject) {
                            parent.removeChildObject(cursor);
                            collision.hitObject.addChildObject(cursor);

                        }
                        float[] normal = collision.getNormalCoords();
                        float[] location = collision.getHitLocation();
                        Vector3f lookat = new Vector3f(normal[0], normal[1], normal[2]);
                        Vector3f Xaxis = new Vector3f();
                        Vector3f Yaxis = new Vector3f();
                        Vector3f up = new Vector3f(0, 1, 0);

                        up.cross(lookat.x, lookat.y, lookat.z, Xaxis);
                        Xaxis = Xaxis.normalize();
                        lookat.cross(Xaxis.x, Xaxis.y, Xaxis.z, Yaxis);
                        Yaxis = Yaxis.normalize();

                        cursor.getTransform().setModelMatrix(new float[]{Xaxis.x, Xaxis.y, Xaxis.z, 0.0f,
                                Yaxis.x, Yaxis.y, Yaxis.z, 0.0f,
                                lookat.x, lookat.y, lookat.z, 0.0f,
                                location[0], location[1], location[2], 1.0f});
                    }
                    else {
                        cursor.getTransform().setPosition(0,0,-collision.hitDistance);
                    }
                }
            }
            else {
                ray.getTransform().setScaleZ(rayDepth);
                if(!cursorNull) {
                    if (parent != GVRGearControllerSceneObject.this) {
                        parent.removeChildObject(cursor);
                        addChildObject(cursor);
                    }
                    cursor.getTransform().reset();
                    cursor.getTransform().setPosition(0, 0, -rayDepth);
                }
            }
        }
    }

}
