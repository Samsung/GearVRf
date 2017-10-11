package org.gearvrf.scene_objects;


import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.ISensorEvents;
import org.gearvrf.R;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.GVRControllerType;

import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Arrays;

/**
 * A {@link GVRSceneObject} that respresents a GearVR controller in the scene.
 * One feature of the {@link GVRGearControllerSceneObject} is a default model for
 * representing the Gear Controller in VR as well as a means to replace it with your
 * own model. Additionally, a ray can be emitted from the controller object at variable
 * lengths, which is useful for interacting with objects at different distances.
 * One can optionally allow the ray to automatically adjust its length to
 * extend to the first object in the scene that the ray vector would intersect. This makes
 * it obvious to the user which object they are currently pointing at with the Gear Controller.
 * Lastly, one can use {@link #enableSurfaceProjection()} to project the cursor onto the
 * object being picked. Read the method documentation for more information.
 */
public class GVRGearControllerSceneObject extends GVRCursorControllerSceneObject
{
    private GVRSceneObject controller;

    /**
     * Constructor for GVRGearControllerSceneObject
     *
     * This constructor creates a {@link GVRSceneObject} resembling a
     * physical Gear VR Controller and assigns it as a child to this object
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRGearControllerSceneObject(GVRContext gvrContext)
    {
        super(gvrContext);
        setName("gear_vr_controller_group");
        ray.getTransform().reset();
        try
        {
            controller = gvrContext.getAssetLoader().loadModel("gear_vr_controller.obj");
            addChildObject(controller);
        }
        catch (IOException ex)
        {
            Log.e("GVRGearControllerSceneObject", "ERROR: cannot load GearVR controller model gear_vr_controller.obj");
        }
    }

    /**
     * Sets a {@link GVRSceneObject} to represent the Gear VR Controller in
     * a scene.
     *
     * @param obj the {@link GVRSceneObject} that will represent the controller
     */
    public void setControllerObject(GVRSceneObject obj)
    {
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
    
}
