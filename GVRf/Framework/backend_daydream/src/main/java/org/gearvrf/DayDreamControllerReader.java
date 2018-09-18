package org.gearvrf;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Controller.ConnectionStates;
import com.google.vr.sdk.controller.ControllerManager;

import org.gearvrf.io.GVRGearCursorController;
import org.joml.Math;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

class DayDreamControllerReader extends GVRGearCursorController.ControllerReaderStubs {

    private ControllerManager mControllerManager;
    private Controller mController;
    private int mConnectionState = ConnectionStates.DISCONNECTED;
    private FloatBuffer readbackBuffer = null;
    private final DaydreamViewManager mViewManager;
    private final float OCULUS_SCALE = 256.0f;

    DayDreamControllerReader(final DaydreamViewManager viewManager) {
        mViewManager = viewManager;

        EventListener listener = new EventListener();
        mControllerManager = new ControllerManager(mViewManager.getApplication().getActivity(), listener);
        mController = mControllerManager.getController();
        mController.setEventListener(listener);
        mControllerManager.start();

        bufferInit();
    }

    @Override
    public void getEvents(int controllerID, ArrayList<GVRGearCursorController.ControllerEvent> controllerEvents) {
        final GVRGearCursorController.ControllerEvent event = GVRGearCursorController.ControllerEvent.obtain();

        event.handedness = readbackBuffer.get(0);
        event.pointF.set(mController.touch.x * OCULUS_SCALE, mController.touch.y * OCULUS_SCALE);

        event.touched = mController.isTouching;
        event.rotation.set(mController.orientation.x, mController.orientation.y, mController.orientation.z, mController.orientation.w);
        event.rotation.rotateLocalY(-(float)Math.toRadians(45.0));  // this makes it to look same as other backends
        event.position.set(mController.position[0], mController.position[1], mController.position[2]);
        event.position.add(0.341f, -0.486f, -0.383f); // this makes it to look same as other backends
        event.key = getKey();

        controllerEvents.add(event);
    }

    @Override
    public boolean isConnected(int index) {
        return mConnectionState == ConnectionStates.CONNECTED;
    }

    public void bufferInit(){
        ByteBuffer readbackBufferB = ByteBuffer.allocateDirect(4);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();

        setNativeBuffer(mViewManager.getNativeRenderer(), readbackBufferB);
        updateHandedness(mViewManager.getNativeRenderer());
    }

    private int getKey() {
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
