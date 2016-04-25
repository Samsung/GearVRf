package org.gearvrf;

public class GVRDirectionalLight extends GVRHybridObject {

    public GVRDirectionalLight(GVRContext gvrContext) {
        super(gvrContext, NativeDirectionalLight.getNewDirectionalLight());

        setLightRenderMode(LightRenderMode.PERSPECTIVE);
        setShadowMapHandlerEdges(ShadowMapHandlerEdges.HANDLER_EDGES_DARK);
        setShadowMapEdgesLength(0.1f);
        setLightAmbientOnShadow(0.1f);
        setShadowSmoothSize(0);
        setShadowGradientCenter(2);
        setLightingShade(0);
        setBoardStratifiedSampling(1);
        setShadowSmoothDistance(1);
        setBias(0.001f);
    }

    public void setLightDirection(float x, float y, float z) {
        NativeDirectionalLight.setLightDirection(getNative(), x, y, z);
    }

    public void setLightPosition(float x, float y, float z) {
        NativeDirectionalLight.setLightPosition(getNative(), x, y, z);
    }

    /**
     * Set spot angle from perspective matrix light or size from orthogonal
     * matrix.
     * 
     * @param angle
     */
    public void setSpotangle(float angle) {
        NativeDirectionalLight.setSpotangle(getNative(), angle);
    }

    /**
     * Set blur level from shadow map
     * 
     * @param angle
     */
    public void setShadowSmoothSize(float angle) {
        NativeDirectionalLight.setShadowSmoothSize(getNative(), angle);
    }

    public enum LightRenderMode {
        PERSPECTIVE, ORTOGONAL,
    };

    public void setLightRenderMode(LightRenderMode mode) {
        NativeDirectionalLight.setLightRenderMode(getNative(), mode.ordinal());
    }

    public enum ShadowMapHandlerEdges {
        HANDLER_EDGES_NONE, HANDLER_EDGES_DARK, HANDLER_EDGES_LIGHT;
    };

    public void setShadowMapHandlerEdges(ShadowMapHandlerEdges mode) {
        NativeDirectionalLight.setShadowMapHandlerEdges(getNative(), mode.ordinal());
    }

    /////////////////////////////

    public enum ShadowMapHandlerMode {
        HIDE, SHOW, GRADIENT
    };

    @Deprecated
    public void setShadowMapHandlerMode(ShadowMapHandlerMode mode) {
        NativeDirectionalLight.setShadowMapHandlerMode(getNative(), mode.ordinal());
    }

    public void setShadowMapEdgesLength(float value) {
        NativeDirectionalLight.setShadowMapEdgesLength(getNative(), value);
    }

    public void setLightAmbientOnShadow(float value) {
        NativeDirectionalLight.setLightAmbientOnShadow(getNative(), value);
    }

    public void setShadowGradientCenter(float value) {
        NativeDirectionalLight.setShadowGradientCenter(getNative(), value);
    }

    /**
     * Set minimal lighting shade
     * 
     * @param value
     */
    public void setLightingShade(float value) {
        NativeDirectionalLight.setLightingShade(getNative(), value);
    }

    public void setBoardStratifiedSampling(float value) {
        NativeDirectionalLight.setBoardStratifiedSampling(getNative(), value);
    }

    public void setShadowSmoothDistance(float value) {
        NativeDirectionalLight.setShadowSmoothDistance(getNative(), value);
    }

    public void setBias(float value) {
        NativeDirectionalLight.setBias(getNative(), value);
    }
}

class NativeDirectionalLight {

    static native int setBias(long dlight, float value);

    static native int setShadowSmoothDistance(long dlight, float value);

    static native int setBoardStratifiedSampling(long dlight, float value);

    static native int setLightingShade(long dlight, float value);

    static native int setShadowGradientCenter(long dlight, float value);

    static native int setShadowMapEdgesLength(long dlight, float value);

    static native int setLightAmbientOnShadow(long dlight, float value);

    static native int setShadowMapHandlerEdges(long dlight, int mode);

    static native int setShadowSmoothSize(long dlight, float mode);

    static native int setShadowMapHandlerMode(long dlight, int mode);

    static native int setLightRenderMode(long dlight, int mode);

    static native int setSpotangle(long dlight, float angle);

    static native int setLightDirection(long dlight, float x, float y, float z);

    static native int setLightPosition(long dlight, float x, float y, float z);

    static native long getNewDirectionalLight();

}
