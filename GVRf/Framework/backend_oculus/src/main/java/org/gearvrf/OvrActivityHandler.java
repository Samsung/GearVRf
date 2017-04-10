package org.gearvrf;

interface OvrActivityHandler {
    public void onPause();

    public void onResume();

    public void onSetScript();

    public boolean onBack();

    public boolean onBackLongPress();

    void setViewManager(GVRViewManager viewManager);
}