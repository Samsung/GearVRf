package org.gearvrf;

import java.util.List;

/**
 * <span style="color:red; font-weight: bold">For internal use only.</span>
 * 
 * <p>
 * Provides methods for rendering scenes
 */
abstract class GVRMonoRenderer {
    /**
     * Renders the given scene for the specified camera into the supplied
     * texture.
     * 
     * @param scene
     *            The {@link GVRScene scene} to render.
     * @param camera
     *            The {@link GVRCamera camera} to render the scene for.
     * @param renderTexture
     *            The framebuffer {@link GVRRenderTexture texture} to render the
     *            scene into.
     * @param renderBundle
     *            Options and data for the renderer's use.
     * @param listPostEffectData
     *            Data for {@link GVRPostEffectMap custom} post-effect
     *            shaders.
     */
    static void renderCamera(GVRScene scene, GVRCamera camera,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            GVRRenderBundle renderBundle) {

        NativeMonoRenderer.renderCamera(scene.getPtr(), camera.getPtr(),
                viewportX, viewportY, viewportWidth, viewportHeight,
                renderBundle.getMaterialShaderManager().getPtr(), 
                renderBundle.getPostEffectShaderManager().getPtr(), 
                renderBundle.getPostEffectRenderTextureA().getPtr(),
                renderBundle.getPostEffectRenderTextureB().getPtr());
    }
}

class NativeMonoRenderer {
    public static native void renderCamera(long scene, long camera,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            long shaderManager,
            long postEffectShaderManager, 
            long postEffectRenderTextureA,
            long postEffectRenderTextureB);
}