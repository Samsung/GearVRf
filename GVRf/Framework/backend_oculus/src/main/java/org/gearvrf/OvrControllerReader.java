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