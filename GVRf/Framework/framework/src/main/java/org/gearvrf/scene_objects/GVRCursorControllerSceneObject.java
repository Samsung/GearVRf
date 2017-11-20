package org.gearvrf.scene_objects;


import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.GVRControllerType;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

/**
 * A {@link GVRSceneObject} to provide GUI feedback for the controller.
 * <p>
 * You can attach a scene object which represents the cursor as a child
 * of this scene object. The cursor will be moved based on the controller
 * attached by {@link #setCursorController(GVRCursorController)}.
 * An optional ray can be emitted from the controller object at variable
 * lengths, which is useful for interacting with objects at different distances.
 * One can optionally allow the ray to automatically adjust its length to
 * extend to the first object in the scene that the ray vector would intersect.
 * Lastly, one can use {@link this#enableSurfaceProjection()} to project the cursor onto the
 * object being picked. Read the method documentation for more information.
 */
public class GVRCursorControllerSceneObject extends GVRSceneObject
{
    private final String TAG = GVRCursorControllerSceneObject.class.getSimpleName();
    protected float rayDepth = 1.0f;
    protected boolean projectToSurface = false;
    protected GVRSceneObject cursor = null;
    protected GVRCursorController cursorController = null;
    protected GVRSceneObject ray;


    /**
     * Constructor for GVRGearControllerSceneObject
     *
     * This constructor creates a {@link GVRSceneObject} resembling a
     * physical Gear VR Controller and assigns it as a child to this object
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRCursorControllerSceneObject(GVRContext gvrContext)
    {
        super(gvrContext);
        ray = new GVRLineSceneObject(gvrContext, rayDepth,
                                     new Vector4f(1, 0, 0, 1),
                                     new Vector4f(1, 0, 0, 0.02f));

        final GVRRenderData renderData = ray.getRenderData();

        renderData.getMaterial().setLineWidth(2.0f);
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        renderData.setAlphaBlend(true);
        setName("cursor_controller_group");
        ray.setName("gearvr_controller_ray");
        addChildObject(ray);
    }

    /**
     * Sets a {@link GVRSceneObject} as the cursor to be displayed at
     * the end of the ray of the GearController.
     *
     * @param obj the scene object to represent the cursor
     */
    public void setCursor(GVRSceneObject obj)
    {
        final GVRSceneObject parent = obj.getParent();
        if (null != parent) {
            parent.removeChildObject(obj);
        }
        cursor = obj;
        cursor.getTransform().setPosition(0, 0, -rayDepth);
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
    public void removeCursor()
    {
        if (cursor != null)
        {
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
    public void setRayDepth(float depth)
    {
        rayDepth = Math.abs(depth);
        ray.getTransform().setScaleZ(rayDepth);
        if (cursor != null)
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
     * Sets the {@link GVRCursorController} that controls this object. The cursor controller
     * should have a type of {@link GVRControllerType#CONTROLLER}.
     *
     * @param cursorController the cursor controller that controls this scene object
     */
    public void setCursorController(GVRCursorController cursorController)
    {
        cursorController.setSceneObject(this);
        this.cursorController = cursorController;
    }

    /**
     * Enables surface projection, which will cause the cursor to be projected
     * on the surface of a {@link GVRSceneObject} according to the surface normal at the
     * intersection point of the controller's ray).
     * Note this requires additional calculations for every pick event received.
     * <p>
     * When this is disabled, the ray/cursor depth will still be adjusted,
     * but the cursor will not be transformed according to
     * surface normals. Also note that a {@link org.gearvrf.GVRMeshCollider}
     * must be attached to a {@link GVRSceneObject} in order for surface normals
     * to be calculated during picking.
     */
    public void enableSurfaceProjection()
    {
        cursorController.addPickEventListener(pickHandler);
        projectToSurface = true;
    }

    /**
     * Disables surface projection.
     */
    public void disableSurfaceProjection(){
        projectToSurface = false;
        cursorController.removePickEventListener(pickHandler);
        if(cursor != null){
            GVRSceneObject parent = cursor.getParent();
            if (parent != this) {
                parent.removeChildObject(cursor);
                addChildObject(cursor);
            }
        }
    }

    /**
     * Handler for surface projection of the cursor.
     * Listens for pick events and projects the cursor
     * on the surface of the picked object.
     */
    private IPickEvents pickHandler = new IPickEvents()
    {
        public void onPick(GVRPicker picker) { }

        public void onNoPick(GVRPicker picker) { }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            if (cursor == null)
            {
                return;
            }
            float[] baryCoords = collision.getBarycentricCoords();
            boolean coordinatesCalculated = (baryCoords != null) && !Arrays.equals(baryCoords, new float[] {-1f, -1f, -1f});
            GVRSceneObject parent = cursor.getParent();

            ray.getTransform().setScaleZ(collision.hitDistance);

            if (projectToSurface && coordinatesCalculated)
            {
                if (parent != collision.hitObject)
                {
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
            else
            {
                cursor.getTransform().setPosition(0, 0, -collision.hitDistance);
            }
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            onInside(sceneObj, collision);
        }

        public void onExit(GVRSceneObject sceneObj)
        {
            ray.getTransform().setScaleZ(rayDepth);
            if (cursor == null)
            {
                return;
            }
            GVRSceneObject parent = cursor.getParent();

            if (parent != GVRCursorControllerSceneObject.this)
            {
                parent.removeChildObject(cursor);
                addChildObject(cursor);
            }
            cursor.getTransform().reset();
            cursor.getTransform().setPosition(0, 0, -rayDepth);
        }
    };
}

