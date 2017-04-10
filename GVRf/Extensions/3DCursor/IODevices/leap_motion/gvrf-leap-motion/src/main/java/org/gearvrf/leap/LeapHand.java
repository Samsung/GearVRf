package org.gearvrf.leap;

import android.util.Log;
import android.util.SparseArray;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;

class LeapHand {
    private static final String TAG = LeapHand.class.getSimpleName();
    static int LEFT_HAND = 0;
    static int RIGHT_HAND = 1;
    SparseArray<LeapFinger> fingerMap;
    private GVRModelSceneObject hand;
    private GVRBone palm;

    private SkeletalController controller;
    private Quaternionf reorientation;
    private GVRScene scene;
    private Quaternionf initialRotation = new Quaternionf();
    // creating the rotation object to reduce load on GC
    private Quaternionf scratchRotation = new Quaternionf();

    LeapHand(GVRContext context, GVRScene scene, String model, int handType) {
        this.scene = scene;
        initialRotation.rotateX((float) Math.toRadians(-90));
        initialRotation.rotateY((float) Math.toRadians(180));
        Vector3f modelPalmFacing, modelFingerPointing;
        reorientation = new Quaternionf();
        if (handType == LEFT_HAND) {
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

        try {
            hand = context.loadModel(model);
            controller = new SkeletalController(hand);
        } catch (IOException e) {
            Log.e(TAG, "Error loading model " + model);
        }
        fingerMap = new SparseArray<LeapFinger>(5);

        fingerMap.put(LeapFinger.TYPE_INDEX, new LeapFinger(LeapFinger.TYPE_INDEX, controller,
                scene, handType));
        fingerMap.put(LeapFinger.TYPE_MIDDLE, new LeapFinger(LeapFinger.TYPE_MIDDLE, controller,
                scene, handType));
        fingerMap.put(LeapFinger.TYPE_RING, new LeapFinger(LeapFinger.TYPE_RING, controller,
                scene, handType));
        fingerMap.put(LeapFinger.TYPE_PINKY, new LeapFinger(LeapFinger.TYPE_PINKY, controller,
                scene, handType));
        fingerMap.put(LeapFinger.TYPE_THUMB, new LeapFinger(LeapFinger.TYPE_THUMB, controller,
                scene, handType));

        // traverse the tree for bones
        parseTree(hand, 0);
    }

    private void parseTree(GVRSceneObject sceneObject, int level) {
        if (sceneObject == null) {
            return;
        }

        //disable the light
        if (sceneObject.getLight() != null) {
            sceneObject.detachLight();
        }

        GVRRenderData renderData = sceneObject.getRenderData();
        if (renderData != null) {
            GVRMesh mesh = renderData.getMesh();
            if (mesh != null) {
                List<GVRBone> bones = sceneObject.getRenderData().getMesh().getBones();
                for (GVRBone bone : bones) {
                    setupPalm(bone);
                    LeapFinger ioFinger = getFinger(bone);
                    getBone(ioFinger, bone);
                }
            }
        }

        for (GVRSceneObject child : sceneObject.getChildren()) {
            parseTree(child, level + 1);
        }
    }

    private void setupPalm(GVRBone bone) {
        if (bone.getName().contains("Palm")) {
            palm = bone;
        }
    }

    private LeapFinger getFinger(GVRBone gvrBone) {
        String name = gvrBone.getName();
        if (name.contains("thumb")) {
            return fingerMap.get(LeapFinger.TYPE_THUMB);
        } else if (name.contains("index")) {
            return fingerMap.get(LeapFinger.TYPE_INDEX);
        } else if (name.contains("middle")) {
            return fingerMap.get(LeapFinger.TYPE_MIDDLE);
        } else if (name.contains("ring")) {
            return fingerMap.get(LeapFinger.TYPE_RING);
        } else {
            return fingerMap.get(LeapFinger.TYPE_PINKY);
        }
    }

    private void getBone(LeapFinger ioFinger, GVRBone gvrBone) {
        String name = gvrBone.getName();
        if (ioFinger.type == LeapFinger.TYPE_THUMB) {
            if (name.contains("_meta")) {
                ioFinger.addBone(LeapBone.TYPE_PROXIMAL, gvrBone);
            } else if (name.contains("_a")) {
                ioFinger.addBone(LeapBone.TYPE_INTERMEDIATE, gvrBone);
            } else if (name.contains("_b")) {
                ioFinger.addBone(LeapBone.TYPE_DISTAL, gvrBone);
            } else {
                // do nothing
            }
        } else {
            if (name.contains("_meta")) {
                ioFinger.addBone(LeapBone.TYPE_METACARPAL, gvrBone);
            } else if (name.contains("_a")) {
                ioFinger.addBone(LeapBone.TYPE_PROXIMAL, gvrBone);
            } else if (name.contains("_b")) {
                ioFinger.addBone(LeapBone.TYPE_INTERMEDIATE, gvrBone);
            } else if (name.contains("_c")) {
                ioFinger.addBone(LeapBone.TYPE_DISTAL, gvrBone);
            } else {
                // do nothing
            }
        }
    }

    LeapFinger getFinger(int count) {
        return fingerMap.get(count);
    }

    void update() {
        controller.update();
    }

    void setPalm(Vector3f position, Quaternionf rotation) {
        GVRTransform transform = scene.getMainCameraRig().getHeadTransform();
        scratchRotation.set(transform.getRotationX(), transform.getRotationY(), transform
                .getRotationZ(), transform.getRotationW());
        rotation.mul(reorientation);

        initialRotation.mul(rotation, rotation);
        scratchRotation.mul(rotation, rotation);
        position.div(10.0f);

        initialRotation.transform(position, position);
        scratchRotation.transform(position, position);
        controller.updateGlobalRotation(palm, rotation);
        controller.updateGlobalPosition(palm, position);
    }

    GVRSceneObject getSceneObject() {
        return hand;
    }
}
