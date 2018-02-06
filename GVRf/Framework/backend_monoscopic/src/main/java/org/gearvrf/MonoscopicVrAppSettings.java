package org.gearvrf;

import org.gearvrf.utility.VrAppSettings;

final class MonoscopicVrAppSettings extends VrAppSettings {
    public MonoscopicVrAppSettings() {
        super();
        sceneParams = new SceneParams();
    }

    public static class SceneParams {
        public int viewportX;

        public int viewportY;

        public int viewportWidth;

        public int viewportHeight;

        public SceneParams() {
            viewportX = 0;
            viewportY = 0;
            viewportWidth = 0;
            viewportHeight = 0;
        }
    }

    public SceneParams getSceneParams() {
        return sceneParams;
    }

    public final SceneParams sceneParams;
}
