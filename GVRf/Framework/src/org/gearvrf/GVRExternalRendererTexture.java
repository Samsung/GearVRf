package org.gearvrf;

/**
 * Wrapper for a (pseudo) texture to be used with an external
 * renderer. Data to be passed to the external renderer can be
 * attached to this texture object 
 */
public class GVRExternalRendererTexture extends GVRTexture {
    /**
     * @param gvrContext Current gvrContext
     */
    public GVRExternalRendererTexture(GVRContext gvrContext) {
        super(gvrContext, NativeExternalRendererTexture.ctor());
    }

    public void setData(long data) {
        NativeExternalRendererTexture.setData(getNative(), data);
    }

    public long getData() {
        return NativeExternalRendererTexture.getData(getNative());
    }
}

class NativeExternalRendererTexture {
    static native long ctor();
    static native void setData(long ptr, long data);
    static native long getData(long ptr);
}

