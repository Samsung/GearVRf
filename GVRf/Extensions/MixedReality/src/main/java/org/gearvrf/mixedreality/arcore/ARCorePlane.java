package org.gearvrf.mixedreality.arcore;


import android.opengl.GLES30;

import com.google.ar.core.Plane;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;


class ARCorePlane extends GVRPlane {
    private Plane mARPlane;
    private ARCorePose mPose;
    private GVRSceneObject mSceneObject = null;

    public ARCorePlane(GVRContext gvrContext, int indexColor) {
        super(gvrContext);
        mPose = new ARCorePose();
        // TODO: Create method to user set plane object
        mSceneObject = createPlaneMesh(gvrContext, 1.0f, 1.0f, indexColor);
        setEnable(false);
        addChildObject(mSceneObject);
    }

    @Override
    protected void setCenter(float[] pose) {
        mCenterPose = pose;
    }

    @Override
    protected void setType() {
        // FIXME: Create a enum type
        //mType = mARPlane.getType();
    }

    @Override
    protected void setTrackingState() {
        // FIXME: Create a enum tracking state
        //mTrackingState = mARPlane.getTrackingState();
    }

    @Override
    protected void setWidth(float width) {
        mWidth = width;
    }

    @Override
    protected void setHeight(float height) {
        mHeight = height;
    }


    public void update(Plane plane, float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
        mARPlane = plane;
        mPose.update(plane.getCenterPose(), ARViewMatrix, GVRCamMatrix, scale);
        getTransform().setModelMatrix(mPose.getGVRModelMatrixMatrix());

        setCenter(mPose.getPoseMatrix());
        setType();
        setTrackingState();
        setWidth(mARPlane.getExtentX());
        setHeight(mARPlane.getExtentZ());

        mSceneObject.getTransform().setScale(mWidth, 1.0f, mHeight);
    }

    private GVRSceneObject createPlaneMesh(GVRContext gvrContext, float width, float height, int indexColor) {

        final float[] vertices = {
                width * -0.5f, -0.01f, height * 0.5f,
                width * -0.5f, -0.01f, height * -0.5f,
                width * 0.5f, -0.01f, height * 0.5f,
                width * 0.5f, -0.01f, height * -0.5f};

        final float[] texCoords = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};

        char[] triangles = {0, 1, 2, 1, 3, 2};

        GVRMesh mesh = new GVRMesh(gvrContext,
                "float3 a_position float2 a_texcoord");

        mesh.setVertices(vertices);
        mesh.setTexCoords(texCoords);
        mesh.setIndices(triangles);

        GVRSceneObject quad = new GVRSceneObject(gvrContext, mesh);

        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
        int color = indexColor % PLANE_COLORS_RGBA.length;
        mat.setDiffuseColor(PLANE_COLORS_RGBA[color].red,
                PLANE_COLORS_RGBA[color].green,
                PLANE_COLORS_RGBA[color].blue,
                PLANE_COLORS_RGBA[color].alpha);

        quad.getRenderData().setMaterial(mat);
        quad.getRenderData().setAlphaBlend(true);
        quad.getRenderData().setDrawMode(GLES30.GL_TRIANGLES);
        quad.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.Front);

        return quad;
    }

    private static final RGB[] PLANE_COLORS_RGBA = {
            new RGB(0.5f, 0f, 0f, 0.2f),
            new RGB(0.0f, 0.5f, 0f, 0.2f),
            new RGB(0.0f, 0f, 0.5f, 0.2f),
            new RGB(0.5f, 0.5f, 0f, 0.2f),
            new RGB(0.5f, 0f, 0.5f, 0.2f),
            new RGB(0.0f, 0.5f, 0.5f, 0.2f),
    };

    private static class RGB {
        float red;
        float green;
        float blue;
        float alpha;

        RGB(float r, float g, float b, float a) {
            red = r;
            green = g;
            blue = b;
            alpha = a;
        }
    }
}
