package org.gearvrf.scene_objects;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

/**
 * Created by j.reynolds on 7/10/2017.
 */

/***
 * A {@link GVRSceneObject} representing a line or ray
 */
public class GVRLineSceneObject extends GVRSceneObject {

    /**
     * The simple constructor; creates a line of length 1.
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRLineSceneObject(GVRContext gvrContext){
        this(gvrContext, 1.0f);
    }

    /**
     * Creates a line based on the passed {@code length} argument
     *
     * @param gvrContext    current {@link GVRContext}
     * @param length        length of the line/ray
     */
    public GVRLineSceneObject(GVRContext gvrContext, float length){
        super(gvrContext, generateLine(gvrContext, length));
        this.getRenderData().setDrawMode(android.opengl.GLES30.GL_LINES);
    }

    private static GVRMesh generateLine(GVRContext gvrContext, float length){
        GVRMesh mesh = new GVRMesh(gvrContext);
        float[] vertices = {
                0,          0,          0,
                0,          0,          -length
        };
        mesh.setVertices(vertices);
        return mesh;
    }
}
