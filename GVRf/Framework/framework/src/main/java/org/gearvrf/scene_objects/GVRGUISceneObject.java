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
 * Created by j.reynolds on 6/12/2017.
 */

public class GVRGUISceneObject extends GVRViewSceneObject {
    private static final String TAG = GVRGUISceneObject.class.getSimpleName();;

    private static final int MOTION_EVENT = 1;

    private static final int SUBDIVISION_MUTLIPLIER = 6;
    private static final int DEGREES_PER_SUBDIVISION = 8;

    private int frameWidth;
    private int frameHeight;

    private final static MotionEvent.PointerProperties[] pointerProperties;
    private final static MotionEvent.PointerCoords[] pointerCoordsArray;
    private final static MotionEvent.PointerCoords pointerCoords;
    private Handler mainThreadHandler;

    static {
        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        pointerProperties = new MotionEvent.PointerProperties[]{properties};
        pointerCoords = new MotionEvent.PointerCoords();
        pointerCoordsArray = new MotionEvent.PointerCoords[]{pointerCoords};
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * This constructor will generate a {@link GVRMesh} for you. The curved parameter specifies
     * whether the generated mesh should be planar or curved.
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param curved        indicates what kind of mesh will be generated for the GVRView
     */
    public <T extends View & GVRView> GVRGUISceneObject(GVRContext gvrContext, T gvrView, boolean curved) {
        this(gvrContext, gvrView, generateMesh(gvrContext, gvrView, curved));
    }

    /**
     * Constructor for GVRGUISceneObject
     *
     * @param gvrContext    current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param mesh          the mesh that the {@link GVRView} will be displayed on
     */
    public <T extends View & GVRView> GVRGUISceneObject(GVRContext gvrContext, final T gvrView, GVRMesh mesh) {
        super(gvrContext, gvrView, mesh);
        this.frameWidth = gvrView.getView().getWidth();
        this.frameHeight = gvrView.getView().getHeight();
        this.attachCollider(new GVRMeshCollider(mesh));
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
        this.setSensor(new GVRBaseSensor(gvrContext));
    }

    /**
     * Generates a mesh based on the arguments passed. To standardize the
     * output mesh dimensions, we make the longest side of the view 1 units
     * in length
     *
     * @param gvrContext    the current {@link GVRContext}
     * @param gvrView       the {@link GVRView} to be displayed on the GVRGUISceneObject
     * @param curved        indicates what kind of mesh will be generated for the GVRView
     */
    private static GVRMesh generateMesh(GVRContext gvrContext, GVRView gvrView, boolean curved){
        View view = gvrView.getView();
        int w = view.getWidth();
        int h = view.getHeight();

        if (curved){
            return generateCurvedMesh(gvrContext, w, h);
        }
        else {
            int largest = w > h ? w : h;
            return gvrContext.createQuad((float)w/largest*1.0f, (float)h/largest*1.0f);
        }

    }

    /**
     * Generates a curved mesh based on the width and height of a {@link GVRView}
     * @param gvrContext    current {@link GVRContext}
     * @param width         the width of a GVRView
     * @param height        the height of a GVRView
     * @return
     */
    private static GVRMesh generateCurvedMesh(GVRContext gvrContext, int width, int height){
        GVRMesh mesh = new GVRMesh(gvrContext);

        double ratio = (double)width/height;
        // Scale the number of subdivisions with the width of the view relative to its height
        int subdivisions = (int) Math.ceil(ratio*SUBDIVISION_MUTLIPLIER);
        // Let each subdivision represent a constant number of degrees on the arc
        int degrees = subdivisions*DEGREES_PER_SUBDIVISION;
        double startDegree = -degrees/2.0;
        double radius, h;
        // Choose a height and radius that ensure the mesh is at most 1 unit tall
        // or 1 unit wide (along the circumference)
        if(width > height) {
            radius = 1.0 / Math.toRadians(degrees);
            h = 1.0 / ratio;
        }
        else {
            h = 1.0;
            radius = ratio / Math.toRadians(degrees);
        }

        float yTop = (float)h/2;
        float yBottom = -yTop;

        float[] vertices = new float[(subdivisions+1)*6];
        float[] normals = new float[(subdivisions+1)*6];
        float[] texCoords= new float[(subdivisions+1)*4];
        char[] triangles = new char[subdivisions*6];

        /*
         * The following diagram illustrates the construction method
         * Let s be the number of subdivisions, then we create s pairs of vertices
         * like so
         *
         * {0}  {2}  {4} ... {2s-1}
         *                             |y+
         * {1}  {3}  {5} ... {2s}      |___x+
         *                          z+/
         */
        for(int i = 0; i <= subdivisions; i++){
            double angle = Math.toRadians(-90+startDegree + DEGREES_PER_SUBDIVISION*i);
            float x = (float) (radius * Math.cos(angle));
            float z = (float) ((radius * Math.sin(angle)) + radius);
            vertices[6*i] = x;
            vertices[6*i + 1] = yTop;
            vertices[6*i + 2] = z;
            normals[6*i] = -x;
            normals[6*i + 1] = 0.0f;
            normals[6*i + 2] = -z;
            texCoords[4*i] = (float)i/subdivisions;
            texCoords[4*i + 1] = 0.0f;

            vertices[6*i + 3] = x;
            vertices[6*i + 4] = yBottom;
            vertices[6*i + 5] = z;
            normals[6*i + 3] = -x;
            normals[6*i + 4] = 0.0f;
            normals[6*i + 5] = -z;
            texCoords[4*i + 2] = (float)i/subdivisions;
            texCoords[4*i + 3] = 1.0f;
        }

        /*
         * Referring to the diagram above, we create two triangles
         * for each pair of consecutive pairs of vertices
         * (e.g. we create two triangles with {0, 1} and {2, 3}
         *  and two triangles with {2, 3} and {4, 5})
         *
         * {0}--{2}--{4}-...-{2s-1}
         *  | ＼  | ＼ |        |       |y+
         * {1}--{3}--{5}-...-{2s}      |___x+
         *                          z+/
         */
        for(int i = 0; i < subdivisions; i++){
            triangles[6*i] = (char)(2*(i+1)+1);
            triangles[6*i+1] = (char) (2*(i));
            triangles[6*i+2] = (char) (2*(i)+1);
            triangles[6*i+3] = (char) (2*(i+1)+1);
            triangles[6*i+4] = (char) (2*(i+1));
            triangles[6*i+5] = (char) (2*(i));
        }

        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setIndices(triangles);
        return mesh;
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
                    pointerCoords.x = pickedObject.getTextureU() * frameWidth;
                    pointerCoords.y = pickedObject.getTextureV() * frameHeight;


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
