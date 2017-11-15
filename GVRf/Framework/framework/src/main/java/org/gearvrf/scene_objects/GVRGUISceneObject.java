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
}
