package org.gearvrf;

public interface ISceneEvents extends ILifeCycleEvents {
    /**
     * Called when the scene has been initialized.
     * @param gvrContext
     *         The GVRContext.
     * @param scene
     *         The GVRScene.
     */
    void onInit(GVRContext gvrContext, GVRScene scene);
}
