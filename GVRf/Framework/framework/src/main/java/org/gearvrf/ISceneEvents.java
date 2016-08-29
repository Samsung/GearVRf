package org.gearvrf;

public interface ISceneEvents extends IEvents {
    /**
     * Called when the scene has been initialized.
     * @param gvrContext
     *         The GVRContext.
     * @param scene
     *         The GVRScene.
     */
    void onInit(GVRContext gvrContext, GVRScene scene);

    /**
     * Called after all handlers of onInit are completed.
     */
    void onAfterInit();
}
