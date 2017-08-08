package org.gearvrf;
import org.joml.Vector3f;

/**
 *  A billboard is a component that can be attached to a
 *  scene object to make it always face the camera. Note that
 *  the up-vector of the sceneobject does not change with the
 *  camera orientation.
 */

public class GVRBillboard extends GVRBehavior
{
    static private long TYPE_BILLBOARD = newComponentType(GVRBillboard.class);
    private GVRCameraRig mMainCameraRig;

    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f lookat = new Vector3f(0, 0, 0);
    private Vector3f ownerXaxis = new Vector3f(0, 0, 0);
    private Vector3f ownerYaxis = new Vector3f(0, 0, 0);

    /**
     *  Constructor
     *  @param gvrContext    The current GVRF context
     */
    public GVRBillboard(GVRContext gvrContext )
    {
        super(gvrContext);
        mMainCameraRig = gvrContext.getMainScene().getMainCameraRig();
    }

    static public long getComponentType() { return TYPE_BILLBOARD; }

    /**
     * Should not be used from the application.
     */

     public void onDrawFrame(float frameTime)
    {
        if ( isEnabled() ) {
            faceObjectToCamera();
        }
    }

    /**
     * Set the model matrix of the owner object to face the main camera rig.
     * Does two cross products: First, between the world up vector and the camera
     * to object vector. This gives one of the axis of the local rotation of the
     * object. A second cross product between the object to camera
     * vector and this axis gives the up vector of the object. Together,
     * with the object position, this yields the desired model matrix
     */

    private void faceObjectToCamera()
    {
        GVRSceneObject ownerObject = getOwnerObject();

        float camX = mMainCameraRig.getTransform().getPositionX();
        float camY = mMainCameraRig.getTransform().getPositionY();
        float camZ = mMainCameraRig.getTransform().getPositionZ();

        float ownerX = ownerObject.getTransform().getPositionX();
        float ownerY = ownerObject.getTransform().getPositionY();
        float ownerZ = ownerObject.getTransform().getPositionZ();

        lookat.set(camX - ownerX, camY - ownerY, camZ - ownerZ);
        lookat = lookat.normalize();

        up.cross(lookat.x, lookat.y, lookat.z, ownerXaxis);
        ownerXaxis = ownerXaxis.normalize();

        lookat.cross(ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, ownerYaxis);
        ownerYaxis = ownerYaxis.normalize();

        ownerObject.getTransform().setModelMatrix(new float[]{ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, 0.0f,
                ownerYaxis.x, ownerYaxis.y, ownerYaxis.z, 0.0f,
                lookat.x, lookat.y, lookat.z, 0.0f,
                ownerX, ownerY, ownerZ, 1.0f});
    }

}

