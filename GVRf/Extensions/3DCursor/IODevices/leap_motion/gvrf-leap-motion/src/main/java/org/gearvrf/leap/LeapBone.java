package org.gearvrf.leap;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

class LeapBone {
    static final int TYPE_METACARPAL = 0;
    static final int TYPE_PROXIMAL = 1;
    static final int TYPE_INTERMEDIATE = 2;
    static final int TYPE_DISTAL = 3;

    //creating object to reduce load on GC
    private Quaternionf scratchRotation = new Quaternionf();

    public static String getString(int i) {
        switch (i) {
            case TYPE_METACARPAL:
                return "TYPE_METACARPAL";
            case TYPE_PROXIMAL:
                return "TYPE_PROXIMAL";
            case TYPE_INTERMEDIATE:
                return "TYPE_INTERMEDIATE";
            case TYPE_DISTAL:
                return "TYPE_DISTAL";

            default:
                return "TYPE_DISTAL";
        }
    }

    private GVRBone bone;
    private Vector3f position;

    private SkeletalController controller;
    private Quaternionf initialRotation = new Quaternionf();

    private Quaternionf reorientation;
    private GVRScene scene;

    LeapBone(GVRScene scene, int handType) {
        this.scene = scene;
        initialRotation.rotateX((float) Math.toRadians(-90));
        initialRotation.rotateY((float) Math.toRadians(180));
        position = new Vector3f();
        reorientation = new Quaternionf();
        Vector3f modelPalmFacing, modelFingerPointing;
        if (handType == LeapHand.LEFT_HAND) {
            modelPalmFacing = new Vector3f(0.0f, 1.0f, 0.0f);
            modelFingerPointing = new Vector3f(-1.0f, 0.0f, 0.0f);
        } else {
            modelPalmFacing = new Vector3f(0.0f, -1.0f, 0.0f);
            modelFingerPointing = new Vector3f(1.0f, 0.0f, 0.0f);
        }

        modelPalmFacing.negate();
        Utils.lookRotate(modelFingerPointing.x, modelFingerPointing.y, modelFingerPointing
                .z, modelPalmFacing.x, modelPalmFacing.y, modelPalmFacing.z, reorientation);
        reorientation.invert();
    }

    public void setBone(GVRBone bone, SkeletalController controller) {
        this.bone = bone;
        this.controller = controller;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setRotation(Quaternionf rotation) {
        if (bone != null) {
            GVRTransform transform = scene.getMainCameraRig().getHeadTransform();
            scratchRotation.set(transform.getRotationX(), transform
                    .getRotationY(), transform.getRotationZ(), transform.getRotationW());
            rotation.mul(reorientation);
            initialRotation.mul(rotation, rotation);
            scratchRotation.mul(rotation, rotation);
            controller.updateGlobalRotation(bone, rotation);
        }
    }
}
