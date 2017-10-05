package org.gearvrf;

interface OvrActivityHandlerRenderingCallbacks {
    public void onSurfaceCreated();

    public void onSurfaceChanged(int width, int height);

    public void onBeforeDrawEyes();

    public void onDrawEye(int eye);

    public void onAfterDrawEyes();
}