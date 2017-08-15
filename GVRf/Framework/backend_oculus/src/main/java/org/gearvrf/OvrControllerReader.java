package org.gearvrf;

import android.graphics.PointF;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class OvrControllerReader implements GearCursorController.ControllerReader {

    private FloatBuffer readbackBuffer;
    private final long mPtr;

    OvrControllerReader(long ptrActivityNative) {
        ByteBuffer readbackBufferB = ByteBuffer.allocateDirect(DATA_SIZE * BYTE_TO_FLOAT);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();
        mPtr = OvrNativeGearController.ctor(readbackBufferB);
        OvrNativeGearController.nativeInitializeGearController(ptrActivityNative, mPtr);
    }

    @Override
    public boolean isConnected() {
        return readbackBuffer.get(INDEX_CONNECTED) == 1.0f;
    }
    @Override
    public GearCursorController.GazeBehavior getGazeBehavior() {
        return GearCursorController.GazeBehavior.Never;
    }
    @Override
    public void setGazeBehavior(GearCursorController.GazeBehavior gazeBehavior) {}
    @Override
    public void setAddedElbowHeight(float elbowHeight){}
    @Override
    public void setAddedElbowDepth(float elbowDepth){}
    @Override
    public void setPointerTiltAngle(float tiltAngle){}
    @Override
    public void setHandedness(GearCursorController.Handedness newHandedness){}
    @Override
    public void setUseAccelerometer(boolean newUseAccelerometer){}
    @Override
    public  void setFadeDistanceFromFace(float distanceFromFace){}
    @Override
    public  void setTooltipMinDistanceFromFace(float distanceFromFace){}
    @Override
    public Vector3f getPointerPositionOffset(){
        return new Vector3f(0f,0f,0f);
    }
    @Override
    public float getAddedElbowHeight(){
        return 0f;
    }
    @Override
    public float getAddedElbowDepth(){
        return 0f;
    }
    @Override
    public float getPointerTiltAngle(){
        return 0f;
    }
    @Override
    public boolean getUseAccelerometer(){
        return false;
    }
    @Override
    public float getFadeDistanceFromFace(){
        return 0f;
    }
    @Override
    public float getTooltipMinDistanceFromFace(){
        return 0f;
    }
    @Override
    public float getControllerAlphaValue(){
        return 0f;
    }
    @Override
    public float getTooltipAlphaValue(){
        return 0f;
    }
    @Override
    public void updateRotation(Quaternionf quat) {
        quat.set(readbackBuffer.get(INDEX_ROTATION + 1),
                readbackBuffer.get(INDEX_ROTATION + 2),
                readbackBuffer.get(INDEX_ROTATION + 3),
                readbackBuffer.get(INDEX_ROTATION));
    }

    @Override
    public void updatePosition(Vector3f vec) {
        vec.set(readbackBuffer.get(INDEX_POSITION),
                readbackBuffer.get(INDEX_POSITION + 1),
                readbackBuffer.get(INDEX_POSITION + 2));
    }

    @Override
    public int getKey() {
        return (int) readbackBuffer.get(INDEX_BUTTON);
    }

    @Override
    public float getHandedness() {
        return readbackBuffer.get(INDEX_HANDEDNESS);
    }

    @Override
    public void updateTouchpad(PointF pt) {
        pt.set(readbackBuffer.get(INDEX_TOUCHPAD), readbackBuffer.get(INDEX_TOUCHPAD + 1));
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            OvrNativeGearController.delete(mPtr);
        } finally {
            super.finalize();
        }
    }

    private static final int INDEX_CONNECTED = 0;
    private static final int INDEX_HANDEDNESS = 1;
    private static final int INDEX_POSITION = 2;
    private static final int INDEX_ROTATION = 5;
    private static final int INDEX_BUTTON = 9;
    private static final int INDEX_TOUCHPAD = 10;

    private static final int DATA_SIZE = 12;
    private static final int BYTE_TO_FLOAT = 4;
}

class OvrNativeGearController {
    static native long ctor(ByteBuffer buffer);

    static native void delete(long jConfigurationManager);

    static native void nativeInitializeGearController(long ptr, long controllerPtr);
}
