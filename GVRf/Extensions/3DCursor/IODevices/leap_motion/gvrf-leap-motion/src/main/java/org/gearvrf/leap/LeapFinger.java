package org.gearvrf.leap;

import android.util.SparseArray;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRScene;

class LeapFinger {
    static final int TYPE_THUMB = 0;
    static final int TYPE_INDEX = 1;
    static final int TYPE_MIDDLE = 2;
    static final int TYPE_RING = 3;
    static final int TYPE_PINKY = 4;

    public static String getString(int i) {
        switch (i) {
            case TYPE_THUMB:
                return "TYPE_THUMB";
            case TYPE_INDEX:
                return "TYPE_INDEX";
            case TYPE_MIDDLE:
                return "TYPE_MIDDLE";
            case TYPE_RING:
                return "TYPE_RING";
            case TYPE_PINKY:
                return "TYPE_PINKY";
            default:
                return "TYPE_THUMB";
        }
    }

    SparseArray<LeapBone> boneMap;
    private SkeletalController controller;
    int type;

    LeapFinger(int type, SkeletalController controller, GVRScene scene, int handType) {
        this.type = type;
        this.controller = controller;
        boneMap = new SparseArray<LeapBone>(4);
        boneMap.put(LeapBone.TYPE_DISTAL, new LeapBone(scene, handType));
        boneMap.put(LeapBone.TYPE_INTERMEDIATE, new LeapBone(scene, handType));
        boneMap.put(LeapBone.TYPE_METACARPAL, new LeapBone(scene, handType));
        boneMap.put(LeapBone.TYPE_PROXIMAL, new LeapBone(scene, handType));
    }

    void addBone(int bone, GVRBone gvrBone) {
        LeapBone ioBone = boneMap.get(bone);
        ioBone.setBone(gvrBone, controller);
    }
}
