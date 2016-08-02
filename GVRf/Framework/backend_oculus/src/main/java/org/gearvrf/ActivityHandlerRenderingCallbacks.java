package org.gearvrf;

interface ActivityHandlerRenderingCallbacks {
    public void onSurfaceCreated();

    public void onSurfaceChanged(int width, int height);

    public void onBeforeDrawEyes();

    public void onDrawEye(int eye);

    public void onAfterDrawEyes();
}