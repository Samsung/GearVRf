package org.gearvrf;

import org.gearvrf.io.GVRGearCursorController;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

final class OvrControllerReader extends GVRGearCursorController.ControllerReaderStubs {

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
    public void getEvents(int controllerID, ArrayList<GVRGearCursorController.ControllerEvent> controllerEvents) {
        final GVRGearCursorController.ControllerEvent event = GVRGearCursorController.ControllerEvent.obtain();

        event.handedness = readbackBuffer.get(INDEX_HANDEDNESS);
        event.pointF.set(readbackBuffer.get(INDEX_TOUCHPAD), readbackBuffer.get(INDEX_TOUCHPAD + 1));

        event.touched = readbackBuffer.get(INDEX_TOUCHED) == 1.0f;
        event.rotation.set(readbackBuffer.get(INDEX_ROTATION + 1),
                readbackBuffer.get(INDEX_ROTATION + 2),
                readbackBuffer.get(INDEX_ROTATION + 3),
                readbackBuffer.get(INDEX_ROTATION));
        event.position.set(readbackBuffer.get(INDEX_POSITION),
                readbackBuffer.get(INDEX_POSITION + 1),
                readbackBuffer.get(INDEX_POSITION + 2));
        event.key = (int) readbackBuffer.get(INDEX_BUTTON);

        controllerEvents.add(event);
    }

    @Override
    public boolean isConnected(int id) {
        return readbackBuffer.get(INDEX_CONNECTED) == 1.0f;
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
    private static final int INDEX_TOUCHED = 2;
    private static final int INDEX_POSITION = 3;
    private static final int INDEX_ROTATION = 6;
    private static final int INDEX_BUTTON = 10;
    private static final int INDEX_TOUCHPAD = 11;

    private static final int DATA_SIZE = 13;
    private static final int BYTE_TO_FLOAT = 4;
}

class OvrNativeGearController {
    static native long ctor(ByteBuffer buffer);

    static native void delete(long jConfigurationManager);

    static native void nativeInitializeGearController(long ptr, long controllerPtr);
}