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
    private Vector3f ownerZaxis = new Vector3f(0, 0, 0);

    private boolean isCustomUpPresent = false;
    private Vector3f customUp;

    /**
     *  Constructor
     *  @param gvrContext    The current GVRF context
     */
    public GVRBillboard(GVRContext gvrContext )
    {
        super(gvrContext);
        mMainCameraRig = gvrContext.getMainScene().getMainCameraRig();
    }

    /**
     * Constructor
     * @param gvrContext
     * @param up:       the up vector about which the scene object is constrained to rotate
     */
    public GVRBillboard(GVRContext gvrContext, Vector3f up )
    {
        super(gvrContext);
        mMainCameraRig = gvrContext.getMainScene().getMainCameraRig();
        customUp = up;
        isCustomUpPresent = true;
    }

    static public long getComponentType() { return TYPE_BILLBOARD; }

    /**
     * Should not be used from the application.
     */

    public void onDrawFrame(float frameTime)
    {
        if ( isEnabled() ) {

            if (!isCustomUpPresent) {
                faceObjectToCamera();
            }
            else {
                faceObjectToCameraWithCustomUp();
            }
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
        GVRTransform ownerTrans = ownerObject.getTransform();
        float camX = mMainCameraRig.getTransform().getPositionX();
        float camY = mMainCameraRig.getTransform().getPositionY();
        float camZ = mMainCameraRig.getTransform().getPositionZ();
        float ownerX = ownerTrans.getPositionX();
        float ownerY = ownerTrans.getPositionY();
        float ownerZ = ownerTrans.getPositionZ();
        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();

        lookat.set(camX - ownerX, camY - ownerY, camZ - ownerZ);
        lookat = lookat.normalize();

        up.cross(lookat.x, lookat.y, lookat.z, ownerXaxis);
        ownerXaxis = ownerXaxis.normalize();

        lookat.cross(ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, ownerYaxis);
        ownerYaxis = ownerYaxis.normalize();

        ownerTrans.setModelMatrix(new float[]{ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, 0.0f,
                ownerYaxis.x, ownerYaxis.y, ownerYaxis.z, 0.0f,
                lookat.x, lookat.y, lookat.z, 0.0f,
                ownerX, ownerY, ownerZ, 1.0f});
        ownerTrans.setScale(scaleX, scaleY, scaleZ);
    }

    /**
     * Set the model matrix of the owner object to face the camera when the up vector of the
     * owner object is provided. This constraints the owner object to rotate only about that
     * up vector.
     */

    private void faceObjectToCameraWithCustomUp()
    {
        GVRSceneObject ownerObject = getOwnerObject();
        GVRTransform ownerTrans = ownerObject.getTransform();
        float camX = mMainCameraRig.getTransform().getPositionX();
        float camY = mMainCameraRig.getTransform().getPositionY();
        float camZ = mMainCameraRig.getTransform().getPositionZ();
        float ownerX = ownerTrans.getPositionX();
        float ownerY = ownerTrans.getPositionY();
        float ownerZ = ownerTrans.getPositionZ();
        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();

        lookat.set(camX - ownerX, camY - ownerY, camZ - ownerZ);
        lookat = lookat.normalize();
        customUp = customUp.normalize();
        customUp.cross(lookat.x, lookat.y, lookat.z, ownerXaxis);
        ownerXaxis = ownerXaxis.normalize();
        ownerXaxis.cross(customUp.x, customUp.y, customUp.z, ownerZaxis);

        ownerTrans.setModelMatrix(new float[]{ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, 0.0f,
                customUp.x, customUp.y, customUp.z, 0.0f,
                ownerZaxis.x, ownerZaxis.y, ownerZaxis.z, 0.0f,
                ownerX, ownerY, ownerZ, 1.0f});
        ownerTrans.setScale(scaleX, scaleY, scaleZ);
    }
}

