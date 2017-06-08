package org.gearvrf;
import org.joml.Vector3f;

public class GVRBillboard extends GVRBehavior
{
    static private long TYPE_BILLBOARD = newComponentType(GVRBillboard.class);
    private GVRCameraRig mMainCameraRig;

    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f lookat = new Vector3f(0, 0, 0);
    private Vector3f ownerXaxis = new Vector3f(0, 0, 0);
    private Vector3f ownerYaxis = new Vector3f(0, 0, 0);

    /*
    constructor
     */
    public GVRBillboard(GVRContext context )
    {
        super(context);
        mMainCameraRig = context.getMainScene().getMainCameraRig();
    }

    static public long getComponentType() { return TYPE_BILLBOARD; }

    public void onDrawFrame(float frameTime)
    {
        if ( isEnabled() ) {
            faceObjectToCamera();
        }
    }

    /*
    set the model matrix of the owner object to face the main camera rig
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

