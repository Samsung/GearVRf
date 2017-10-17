package org.gearvrf.scene_objects;

import android.os.Handler;
import android.os.Message;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.scene_objects.view.GVRView;

import java.util.List;


/**
 * This class extends the {@link GVRViewSceneObject} to make interaction with Android
 * Views simple. This class achieves the following:
 *
 * 1.  Allows any {@link org.gearvrf.GVRCursorController} to be used for interactions
 * with the {@link GVRView} displayed. This is achieved with an {@link ISensorEvents} listener
 * that uses the motion events set in {@link org.gearvrf.GVRCursorController}s to dispatch
 * appropriate touch events to the Android View contained in the {@link GVRView}.
 *
 * 2. Generates an appropriate mesh for displaying the {@link GVRView} passed to the
 * constructor. The mesh will always have the correct aspect ratio, and the user can
 * specify the curvature of the mesh if desired.
 *
 * To use this class, simply instantiate a new {@link GVRGUISceneObject} with the appropriate
 * arguments, and add the instance to the current {@link org.gearvrf.GVRScene} using
 * {@link org.gearvrf.GVRScene#addSceneObject}. Then use the application's
 * {@link org.gearvrf.GVRCursorController}(s) to interact with the {@link GVRGUISceneObject}
 * instance.
 */

public class GVRGUISceneObject extends GVRViewSceneObject {
    private static final String TAG = GVRGUISceneObject.class.getSimpleName();;

    private static final int MOTION_EVENT = 1;

    private int frameWidth;
    private int frameHeight;

    private final MotionEvent.PointerProperties[] pointerProperties;
    private final MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();;
    private final MotionEvent.PointerCoords[] pointerCoordsArray = new MotionEvent.PointerCoords[]{pointerCoords};;
    private Handler mainThreadHandler;

    static {
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * This constructor will generate a planar {@link GVRMesh} for you.
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     */
    public <T extends View> GVRGUISceneObject(GVRContext gvrContext, T gvrView) {
        this(gvrContext, gvrView, planarMesh(gvrContext, gvrView));
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * This constructor will generate a curved {@link GVRMesh} for you based on the
     * arguments passed.
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param radius        the radius of the circle to use to create the arc
     * @param centralAngle  the central angle of the arc
     */
    public <T extends View> GVRGUISceneObject(GVRContext gvrContext, T gvrView, float radius, float centralAngle) {
        this(gvrContext, gvrView, curvedMesh(gvrContext, gvrView, radius, centralAngle));
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * This constructor will generate a curved {@link GVRMesh} for you based on the
     * arguments passed. The central angle of the arc of the curve will be 45 degrees.
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param radius        the radius of the circle to use to create the arc
     */
    public <T extends View> GVRGUISceneObject(GVRContext gvrContext, T gvrView, float radius) {
        this(gvrContext, gvrView, curvedMesh(gvrContext, gvrView, radius, 45f));
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param mesh          the mesh that the {@link GVRView} will be displayed on
     */
    public <T extends View> GVRGUISceneObject(GVRContext gvrContext, final T gvrView, GVRMesh mesh) {
        super(gvrContext, gvrView, mesh);
        this.frameWidth = gvrView.getWidth();
        this.frameHeight = gvrView.getHeight();
        this.attachCollider(new GVRMeshCollider(gvrContext, mesh, true));
        this.mainThreadHandler = new Handler(gvrContext.getActivity().getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                // Dispatch motion event
                if (msg.what == MOTION_EVENT) {
                    MotionEvent motionEvent = (MotionEvent) msg.obj;
                    gvrView.dispatchTouchEvent(motionEvent);
                    gvrView.invalidate();
                    motionEvent.recycle();
                }
            }
        };
        this.getEventReceiver().addListener(GUIEventListener);
        this.attachComponent(new GVRBaseSensor(gvrContext));

        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        pointerProperties = new MotionEvent.PointerProperties[]{properties};
    }

    private static GVRMesh planarMesh(GVRContext gvrContext, View view){
        int w = view.getWidth();
        int h = view.getHeight();
        int largest = w > h ? w : h;
        return gvrContext.createQuad((float)w/largest*1.0f, (float)h/largest*1.0f);
    }

    private static GVRMesh curvedMesh(GVRContext gvrContext, View view, float radius, float centralAngle){
        int w = view.getWidth();
        int h = view.getHeight();
        return GVRMesh.createCurvedMesh(gvrContext, w, h, centralAngle, radius);
    }

    private ISensorEvents GUIEventListener = new ISensorEvents() {
        private static final float SCALE = 5.0f;
        private float savedMotionEventX, savedMotionEventY, savedHitPointX,
                savedHitPointY;

        @Override
        public void onSensorEvent(SensorEvent event) {
            List<MotionEvent> motionEvents = event.getCursorController().getMotionEvents();

            for (MotionEvent motionEvent : motionEvents) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    pointerCoords.x = savedHitPointX
                            + ((motionEvent.getX() - savedMotionEventX) * SCALE);
                    pointerCoords.y = savedHitPointY
                            + ((motionEvent.getY() - savedMotionEventY) * SCALE);
                } else {
                    GVRPicker.GVRPickedObject pickedObject = event.getPickedObject();
                    float[] texCoords = pickedObject.getTextureCoords();
                    pointerCoords.x = texCoords[0] * frameWidth;
                    pointerCoords.y = texCoords[1] * frameHeight;


                    if (motionEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        // save the coordinates on down
                        savedMotionEventX = motionEvent.getX();
                        savedMotionEventY = motionEvent.getY();

                        savedHitPointX = pointerCoords.x;
                        savedHitPointY = pointerCoords.y;
                    }
                }

                final MotionEvent clone = MotionEvent.obtain(
                        motionEvent.getDownTime(), motionEvent.getEventTime(),
                        motionEvent.getAction(), 1, pointerProperties,
                        pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);

                Message message = Message.obtain(mainThreadHandler, MOTION_EVENT, 0, 0,
                        clone);
                mainThreadHandler.sendMessage(message);
            }
        }
    };

}
