package org.gearvrf;

import android.graphics.PointF;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Controller.ConnectionStates;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vr.sdk.controller.Orientation;

import org.gearvrf.io.GVRGearCursorController;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class DayDreamControllerReader extends GVRGearCursorController.ControllerReaderStubs {

    private ControllerManager mControllerManager;
    private Controller mController;
    private int mConnectionState = ConnectionStates.DISCONNECTED;
    private FloatBuffer readbackBuffer = null;
    private GVRApplication mApplication;
    private final float OCULUS_SCALE = 256.0f;

    DayDreamControllerReader(GVRApplication application) {
        EventListener listener = new EventListener();
        mControllerManager = new ControllerManager(application.getActivity(), listener);
        mController = mControllerManager.getController();
        mController.setEventListener(listener);
        mControllerManager.start();
        mApplication = application;
    }

    @Override
    public boolean isTouched(int index){
        return mController.isTouching;
    }

    @Override
    public boolean isConnected(int index) {
        return mConnectionState == ConnectionStates.CONNECTED;
    }

    public void bufferInit(){
        ByteBuffer readbackBufferB = ByteBuffer.allocateDirect(4);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();
        GVRViewManager gvrViewManager = mApplication.getViewManager();
        DaydreamViewManager viewManager = (DaydreamViewManager)gvrViewManager;
        setNativeBuffer(viewManager.getNativeRenderer(), readbackBufferB);
        updateHandedness(viewManager.getNativeRenderer());
    }

    @Override
    public void updateRotation(Quaternionf quat,int index) {
        Orientation orientation = mController.orientation;
        quat.set(orientation.x, orientation.y, orientation.z,orientation.w);
        quat.rotateLocalY(-(float)Math.toRadians(45.0));  // this makes it to look same as other backends
    }

    @Override
    public void updatePosition(Vector3f vec,int index) {
        vec.set(mController.position[0], mController.position[1], mController.position[2]);
        vec.add(0.341f, -0.486f, -0.383f); // this makes it to look same as other backends
    }

    @Override
    public int getKey(int index) {
        if(mController.appButtonState)
            return GVRGearCursorController.CONTROLLER_KEYS.BUTTON_A.getNumVal();
        if(mController.clickButtonState)
            return GVRGearCursorController.CONTROLLER_KEYS.BUTTON_ENTER.getNumVal();
        if(mController.volumeUpButtonState)
            return GVRGearCursorController.CONTROLLER_KEYS.BUTTON_VOLUME_UP.getNumVal();
        if(mController.volumeDownButtonState)
            return GVRGearCursorController.CONTROLLER_KEYS.BUTTON_VOLUME_DOWN.getNumVal();
        if(mController.homeButtonState)
            return GVRGearCursorController.CONTROLLER_KEYS.BUTTON_HOME.getNumVal();

        return 0;
    }

    @Override
    public float getHandedness() {
        if(readbackBuffer == null)
            bufferInit();
        return readbackBuffer.get(0);
    }

    @Override
    public void updateTouchpad(PointF pt,int index) {
        pt.set(mController.touch.x * OCULUS_SCALE, mController.touch.y * OCULUS_SCALE);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mControllerManager.stop();
            mControllerManager = null;
        } finally {
            super.finalize();
        }
    }

    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener {

        @Override
        public void onConnectionStateChanged(int state) {
            mConnectionState = state;
        }
        @Override
        public void onApiStatusChanged(int var1){}
        @Override
        public void onRecentered() {}
        @Override
        public void onUpdate() {
            mController.update();
        }
    }
    public static native void setNativeBuffer(long nativeRenderer, ByteBuffer buffer);
    public static native void updateHandedness(long nativeRenderer);
}