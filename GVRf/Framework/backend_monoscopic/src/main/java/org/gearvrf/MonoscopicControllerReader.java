package org.gearvrf;

import android.graphics.PointF;

import org.joml.Quaternionf;
import org.joml.Vector3f;

class MonoscopicControllerReader extends org.gearvrf.io.GVRGearCursorController.ControllerReaderStubs {

    MonoscopicControllerReader() {
    }

    @Override
    public boolean isConnected(int index) {
        return false;
    }

    @Override
    public boolean isTouched(int index) {
        return false;
    }

    @Override
    public void updateRotation(Quaternionf quat,int index) {
    }

    @Override
    public void updatePosition(Vector3f vec,int index) {
    }

    @Override
    public int getKey(int index) {
        return 0;
    }

    @Override
    public float getHandedness() {
        return 0;
    }

    @Override
    public void updateTouchpad(PointF pt,int index) {
    }

    @Override
    protected void finalize() throws Throwable {
    }

}


