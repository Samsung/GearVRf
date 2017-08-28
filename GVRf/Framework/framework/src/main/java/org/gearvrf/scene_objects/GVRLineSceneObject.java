package org.gearvrf.scene_objects;

import android.opengl.GLES30;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderTemplate;

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
        final GVRRenderData renderData = getRenderData().setDrawMode(GLES30.GL_LINES);
        renderData.setShaderTemplate(GVRPhongShader.class);

        final GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.BeingGenerated.ID);
        renderData.disableLight();
        renderData.setMaterial(material);

        GVRMaterialShaderManager shadermanager = gvrContext.getMaterialShaderManager();
        GVRShaderTemplate gvrShaderTemplate = shadermanager.retrieveShaderTemplate(GVRPhongShader.class);
        gvrShaderTemplate.bindShader(gvrContext, material);
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
