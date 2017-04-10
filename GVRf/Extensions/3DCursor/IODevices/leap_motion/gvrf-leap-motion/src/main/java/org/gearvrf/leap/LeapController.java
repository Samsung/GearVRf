package org.gearvrf.leap;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * The main class that interfaces with the libLeapC library
 */
public class LeapController {
    private static final String TAG = LeapController.class.getSimpleName();
    private static int NUM_FLOATS = 303;
    private static int FLOAT_TO_BYTES = 4;
    private static int NUM_FINGERS = 5;
    private static int NUM_BONES = 4;
    private ByteBuffer readbackBufferB;
    private FloatBuffer readbackBuffer;
    private Thread thread;
    private Vector3f palmPosition = new Vector3f();
    private Quaternionf palmRotation = new Quaternionf();

    // temp object for bone rotations
    private Quaternionf boneRotation = new Quaternionf();

    private int frameNum, totalFrames;
    private LeapHand leftHand, rightHand;

    private static final Vector3f direction = new Vector3f();
    private static final Vector3f normal = new Vector3f();
    private static final Vector3f position = new Vector3f();

    private static Vector3f up = new Vector3f();
    private static Vector3f forward = new Vector3f();

    private float handType;

    /**
     * Initialize a connection to the Leap Sensor.
     *
     * @param context
     * @param scene
     */
    public LeapController(GVRContext context, GVRScene scene) {
        leftHand = new LeapHand(context, scene, "rigged_hand_left.fbx", LeapHand.LEFT_HAND);
        rightHand = new LeapHand(context, scene, "rigged_hand_right.fbx", LeapHand.RIGHT_HAND);
        leftHand.getSceneObject().setEnable(false);
        rightHand.getSceneObject().setEnable(false);

        readbackBufferB = ByteBuffer.allocateDirect(NUM_FLOATS * FLOAT_TO_BYTES);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();

        Log.d(TAG, "Creating a new thread");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // call into native to initialize the thread
                initialize(readbackBufferB);
            }
        });
        thread.start();
    }

    /**
     * Perform cleanup using this method.
     */
    public void close() {
        Log.d(TAG, "Interrupting thread");
        // Destroy the native thread.
        destroy();
        thread.interrupt();
    }

    /**
     * Get the scene object that represents the left hand.
     *
     * @return
     */
    public GVRSceneObject getLeftHandSceneObject() {
        return leftHand.getSceneObject();
    }

    /**
     * Get the scene object that represents the right hand.
     *
     * @return
     */
    public GVRSceneObject getRightHandSceneObject() {
        return rightHand.getSceneObject();
    }

    native void initialize(ByteBuffer readbackBuffer);

    native void destroy();

    static {
        System.loadLibrary("leap_controller");
    }

    void onFrame() {
        totalFrames = (int) readbackBuffer.get(0);
        boolean rightHandProcessed = false;
        boolean leftHandProcessed = false;
        int count = 1;
        LeapHand leapHand;

        for (frameNum = 0; frameNum < totalFrames; frameNum++) {
            handType = readbackBuffer.get(count++);
            if (handType == LeapHand.LEFT_HAND) {
                leapHand = leftHand;

                GVRSceneObject left = leftHand.getSceneObject();
                if (!left.isEnabled()) {
                    leftHand.getSceneObject().setEnable(true);
                }
                leftHandProcessed = true;
            } else {
                leapHand = rightHand;

                GVRSceneObject right = rightHand.getSceneObject();
                if (!right.isEnabled()) {
                    rightHand.getSceneObject().setEnable(true);
                }
                rightHandProcessed = true;
            }

            float pinchStrength = readbackBuffer.get(count++);

            direction.set(readbackBuffer.get(count), readbackBuffer.get(count + 1),
                    readbackBuffer.get(count + 2));
            count = count + 3;

            normal.set(readbackBuffer.get(count), readbackBuffer.get(count + 1),
                    readbackBuffer.get(count + 2));
            count = count + 3;

            palmPosition.set(readbackBuffer.get(count), readbackBuffer.get(count + 1),
                    readbackBuffer.get(count + 2));
            count = count + 3;

            Vector3f handYBasis = normal.negate();
            Vector3f handZBasis = direction.negate();

            up.set(handYBasis.x, handYBasis.y, handYBasis.z);
            forward.set(handZBasis.x, handZBasis.y, handZBasis.z);

            Utils.lookRotate(forward.x, forward.y, forward.z, up.x, up.y, up.z, palmRotation);

            leapHand.setPalm(palmPosition, palmRotation);

            for (int fingerNum = 0; fingerNum < NUM_FINGERS; fingerNum++) {
                LeapFinger ioFinger = leapHand.fingerMap.get(fingerNum);
                for (int boneNum = 0; boneNum < NUM_BONES; boneNum++) {

                    //Get position data
                    position.set(readbackBuffer.get(count), readbackBuffer.get(count + 1),
                            readbackBuffer.get(count + 2));

                    boneRotation.set(readbackBuffer.get(count + 4),
                            readbackBuffer.get(count + 5), readbackBuffer.get(count + 6),
                            readbackBuffer.get(count + 3));
                    LeapBone ioBone = ioFinger.boneMap.get(boneNum);
                    ioBone.setRotation(boneRotation);
                    count = count + 7;
                }
            }

            leapHand.update();
        }

        if (!rightHandProcessed) {
            GVRSceneObject right = rightHand.getSceneObject();
            if (right.isEnabled()) {
                rightHand.getSceneObject().setEnable(false);
            }
        }
        if (!leftHandProcessed) {
            GVRSceneObject left = leftHand.getSceneObject();
            if (left.isEnabled()) {
                leftHand.getSceneObject().setEnable(false);
            }
        }
    }
}
