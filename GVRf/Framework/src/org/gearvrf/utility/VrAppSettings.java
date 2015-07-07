/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.utility;

/**
 * A class that represents overall parameters that we can set with Oculus
 * library.
 */
public class VrAppSettings {
    public static class ModeParms {

        // If true, warn and allow the app to continue at 30fps when
        // throttling occurs.
        // If false, display the level 2 error message which requires
        // the user to undock.
        public boolean allowPowerSave;
        // When an application with multiple activities moves backwards on
        // the activity stack, the activity window it returns to is no longer
        // flagged as fullscreen. As a result, Android will also render
        // the decor view, which wastes a significant amount of bandwidth.
        // By setting this flag, the fullscreen flag is reset on the window.
        // Unfortunately, this causes Android life cycle events that mess up
        // several NativeActivity codebases like Stratum and UE4, so this
        // flag should only be set for select applications with multiple
        // activities. Use "adb shell dumpsys SurfaceFlinger" to verify
        // that there is only one HWC next to the FB_TARGET.
        public boolean resetWindowFullScreen;
        // These are fixed clock levels.
        public int cpuLevel, gpuLevel;

        /**
         * Check if it allows power save mode.
         * 
         * @return if power save mode is available for this app.
         */

        public boolean isAllowPowerSave() {
            return allowPowerSave;
        }

        /**
         * Set if it allows power save mode
         * 
         * @param allowPowerSave
         *            To set if it allows power save mode.
         */
        public void setAllowPowerSave(boolean allowPowerSave) {
            this.allowPowerSave = allowPowerSave;
        }

        /**
         * Check if it will reset the fullscreen flag.
         * 
         * @return if it will reset the fullscreen flag.
         */
        public boolean isResetWindowFullScreen() {
            return resetWindowFullScreen;
        }

        /**
         * Set if it will reset the fullscreen flag.
         * 
         * @param resetWindowFullScreen
         *            To set if it will reset the fullscreen flag.
         */
        public void setResetWindowFullScreen(boolean resetWindowFullScreen) {
            this.resetWindowFullScreen = resetWindowFullScreen;
        }

        /**
         * Get current CPU clock level.
         * 
         * @return Current CPU clock level.
         */
        public int getCpuLevel() {
            return cpuLevel;
        }

        /**
         * Set CPU clock level.
         * 
         * @param cpuLevel
         *            Current CPU clock level.
         */
        public void setCpuLevel(int cpuLevel) {
            this.cpuLevel = cpuLevel;
        }

        /**
         * Get current GPU clock level.
         * 
         * @return Current GPU clock level.
         */
        public int getGpuLevel() {
            return gpuLevel;
        }

        /**
         * Set GPU clock level.
         * 
         * @param gpuLevel
         *            Current GPU clock level.
         */
        public void setGpuLevel(int gpuLevel) {
            this.gpuLevel = gpuLevel;
        }

        public ModeParms() {
            allowPowerSave = true;
            resetWindowFullScreen = true;
            cpuLevel = gpuLevel = 2;
        }

    }

    public static class EyeBufferParms {

        // For dimshadow rendering, we would need an option to
        // make the depth buffer a texture instead of a renderBufferStorage,
        // which requires an extension on Gl ES 2.0, but would be fine on 3.0.
        // It would also require a flag to allow the resolve of depth instead
        // of invalidating it.
        public DepthFormat depthFormat;

        // Determines how the time warp samples the eye buffers.
        // Defaults to TEXTURE_FILTER_BILINEAR.
        public TextureFilter textureFilter;

        // Color format of current vr app.
        // Defaults to FMT_8888.

        public ColorFormat colorFormat;
        // Setting the resolution higher than necessary will cause aliasing
        // when presented to the screen, since we do not currently generate
        // mipmaps for the eye buffers, but lowering the resolution can
        // significantly improve the application frame rate.
        public int resolution;

        // For double wide UE4
        public int widthScale;

        // Multisample anti-aliasing is almost always desirable for VR, even
        // if it requires a drop in resolution.
        public int multiSamples;

        public enum DepthFormat {
            DEPTH_0(0), // useful for overlay planes
            DEPTH_16(1), DEPTH_24(2), DEPTH_24_STENCIL_8(3);
            private final int value;

            private DepthFormat(int value) {
                this.value = value;
            }

            /**
             * Get numerical value of current DepthFormat.
             * 
             * @return numerical value of current DepthFormat.
             */
            public int getValue() {
                return value;
            }
        }

        public enum TextureFilter {
            TEXTURE_FILTER_NEAREST(0), // Causes significant aliasing, only for
                                       // performance testing.
            TEXTURE_FILTER_BILINEAR(1), // This should be used under almost all
                                        // circumstances.
            TEXTURE_FILTER_ANISO_2(2), // Anisotropic filtering can in some
                                       // cases
                                       // reduce aliasing.
            TEXTURE_FILTER_ANISO_4(3);

            private final int value;

            private TextureFilter(int value) {
                this.value = value;
            }

            /**
             * Get numerical value of current TextureFilter.
             * 
             * @return numerical value of current TextureFilter.
             */
            public int getValue() {
                return value;
            }
        }

        public enum ColorFormat {
            COLOR_565(0),

            COLOR_5551(1), // single bit alpha useful for overlay planes

            COLOR_4444(2),

            COLOR_8888(3),

            COLOR_8888_sRGB(4);
            private final int value;

            private ColorFormat(int value) {
                this.value = value;
            }

            /**
             * Get numerical value of current ColorFormat.
             * 
             * @return numerical value of current ColorFormat.
             */
            public int getValue() {
                return value;
            }
        }

        /**
         * Get current DepthFormat.
         * 
         * @return Current DepthFormat.
         */
        public DepthFormat getDepthFormat() {
            return depthFormat;
        }

        /**
         * Set DepthFormat.
         * 
         * @param depthFormat
         *            DepthFormat to set.
         */
        public void setDepthFormat(DepthFormat depthFormat) {
            this.depthFormat = depthFormat;
        }

        /**
         * Get current textureFilter.
         * 
         * @return Current textureFilter.
         */
        public TextureFilter getTextureFilter() {
            return textureFilter;
        }

        /**
         * Set TextureFilter.
         * 
         * @param textureFilter
         *            TextureFilter to set.
         */
        public void setTextureFilter(TextureFilter textureFilter) {
            this.textureFilter = textureFilter;
        }

        /**
         * Get current ColorFormat.
         * 
         * @return Current ColorFormat.
         */
        public ColorFormat getColorFormat() {
            return colorFormat;
        }

        /**
         * Set ColorFormat.
         * 
         * @param colorFormat
         *            ColorFormat to set.
         */
        public void setColorFormat(ColorFormat colorFormat) {
            this.colorFormat = colorFormat;
        }

        /**
         * Get current resolution.
         * 
         * @return Current resolution.
         */
        public int getResolution() {
            return resolution;
        }

        /**
         * Set resolution.
         * 
         * @param resolution
         *            Resolution to set.
         */
        public void setResolution(int resolution) {
            this.resolution = resolution;
        }

        /**
         * Get current width scale.
         * 
         * @return Current width scale.
         */
        public int getWidthScale() {
            return widthScale;
        }

        /**
         * Set width scale.
         * 
         * @param widthScale
         *            width scale to set.
         */
        public void setWidthScale(int widthScale) {
            this.widthScale = widthScale;
        }

        /**
         * Get current multi sampling level.
         * 
         * @return Current multi sampling level.
         */
        public int getMultiSamples() {
            return multiSamples;
        }

        /**
         * Set multi sampling level.
         * 
         * @param multiSamples
         *            Multi sampling level to set.
         */
        public void setMultiSamples(int multiSamples) {
            this.multiSamples = multiSamples;
        }

        public EyeBufferParms() {
            multiSamples = 2;
            widthScale = 1;
            resolution = -1;
            depthFormat = DepthFormat.DEPTH_24;
            colorFormat = ColorFormat.COLOR_8888;
            textureFilter = TextureFilter.TEXTURE_FILTER_BILINEAR;
        }

    }

    // -----------------------------------------------------------------
    // Head Model
    // -----------------------------------------------------------------
    public static class HeadModelParms {
        float interpupillaryDistance; // Distance between eyes.
        float eyeHeight; // Eye height relative to the eyeHeightground.
        float headModelDepth; // Eye offset forward from the head center at
                              // eyeHeight.
        float headModelHeight; // Neck joint offset down from the head center at
                               // eyeHeight.

        /**
         * Get current distance between eyes.
         * 
         * @return current distance between eyes.
         */
        public float getInterpupillaryDistance() {
            return interpupillaryDistance;
        }

        /**
         * Set distance between eyes.
         * 
         * @param interpupillaryDistance
         *            Set distance between eyes.
         */
        public void setInterpupillaryDistance(float interpupillaryDistance) {
            this.interpupillaryDistance = interpupillaryDistance;
        }

        /**
         * Get current eye height relative to the eyeHeightground.
         * 
         * @return current eye height relative to the eyeHeightground.
         */
        public float getEyeHeight() {
            return eyeHeight;
        }

        /**
         * Set eye height relative to the eyeHeightground.
         * 
         * @param eyeHeight
         *            current eye height relative to the eyeHeightground.
         */
        public void setEyeHeight(float eyeHeight) {
            this.eyeHeight = eyeHeight;
        }

        /**
         * Get current Eye offset forward from the head center at eyeHeight.
         * 
         * @return current Eye offset forward from the head center at eyeHeight.
         */
        public float getHeadModelDepth() {
            return headModelDepth;
        }

        /**
         * Set Eye offset forward from the head center at eyeHeight.
         * 
         * @param headModelDepth
         *            Eye offset forward from the head center at eyeHeight.
         */

        public void setHeadModelDepth(float headModelDepth) {
            this.headModelDepth = headModelDepth;
        }

        /**
         * Get current neck joint offset down from the head center at eyeHeight.
         * 
         * @return current neck joint offset down from the head center at
         *         eyeHeight.
         */
        public float getHeadModelHeight() {
            return headModelHeight;
        }

        /**
         * Set neck joint offset down from the head center at eyeHeight.
         * 
         * @param headModelHeight
         *            Neck joint offset down from the head center at eyeHeight.
         */
        public void setHeadModelHeight(float headModelHeight) {
            this.headModelHeight = headModelHeight;
        }

        public HeadModelParms() {
            this.interpupillaryDistance = Float.NaN;
            this.eyeHeight = Float.NaN;
            this.headModelDepth = Float.NaN;
            this.headModelHeight = Float.NaN;
        }
    }

    // If it will show loading icon in the vr app.
    public boolean showLoadingIcon;

    // If it is monoscopic mode.
    public boolean renderMonoMode;

    // If it will use srgb frame buffer.
    public boolean useSrgbFramebuffer; // EGL_GL_COLORSPACE_KHR,
                                       // EGL_GL_COLORSPACE_SRGB_KHR

    // If it will use protected frame buffer
    public boolean useProtectedFramebuffer; // EGL_PROTECTED_CONTENT_EXT,
                                            // EGL_TRUE

    // Current frame buffer's pixels width.
    public int framebufferPixelsWide;

    // Current frame buffer's pixels height.
    public int framebufferPixelsHigh;

    public ModeParms modeParms;
    public EyeBufferParms eyeBufferParms;
    public HeadModelParms headModelParms;

    /**
     * Check if current app shows loading icon
     * 
     * @return if current app shows loading icon
     */
    public boolean isShowLoadingIcon() {
        return showLoadingIcon;
    }

    /**
     * Set if current app shows loading icon
     * 
     * @param showLoadingIcon
     *            if current app shows loading icon
     */
    public void setShowLoadingIcon(boolean showLoadingIcon) {
        this.showLoadingIcon = showLoadingIcon;
    }

    /**
     * Check if current app is monoscopic mode
     * 
     * @return if current app is monoscopic mode
     */
    public boolean isRenderMonoMode() {
        return renderMonoMode;
    }

    /**
     * Set if current app is monoscopic mode
     * 
     * @param renderMonoMode
     *            if current app is monoscopic mode
     */
    public void setRenderMonoMode(boolean renderMonoMode) {
        this.renderMonoMode = renderMonoMode;
    }

    /**
     * Check if current app is using srgb frame buffer
     * 
     * @return if current app is using srgb frame buffer
     */
    public boolean isUseSrgbFramebuffer() {
        return useSrgbFramebuffer;
    }

    /**
     * Set if current app is using srgb frame buffer
     * 
     * @param useSrgbFramebuffer
     *            if current app is using srgb frame buffer
     */
    public void setUseSrgbFramebuffer(boolean useSrgbFramebuffer) {
        this.useSrgbFramebuffer = useSrgbFramebuffer;
    }

    /**
     * Check if current app is using protected frame buffer
     * 
     * @return if current app is using protected frame buffer
     */
    public boolean isUseProtectedFramebuffer() {
        return useProtectedFramebuffer;
    }

    /**
     * Set if current app is using srgb protected buffer
     * 
     * @param useProtectedFramebuffer
     *            if current app is using srgb protected buffer
     */
    public void setUseProtectedFramebuffer(boolean useProtectedFramebuffer) {
        this.useProtectedFramebuffer = useProtectedFramebuffer;
    }

    /**
     * Get frame buffer's number of pixels in width.
     * 
     * @return Frame buffer's number of pixels in width.
     */
    public int getFramebufferPixelsWide() {
        return framebufferPixelsWide;
    }

    /**
     * Set frame buffer's number of pixels in width.
     * 
     * @param framebufferPixelsWide
     *            Frame buffer's number of pixels in width.
     */
    public void setFramebufferPixelsWide(int framebufferPixelsWide) {
        this.framebufferPixelsWide = framebufferPixelsWide;
    }

    /**
     * Get frame buffer's number of pixels in height.
     * 
     * @return Frame buffer's number of pixels in height.
     */
    public int getFramebufferPixelsHigh() {
        return framebufferPixelsHigh;
    }

    /**
     * Set frame buffer's number of pixels in height.
     * 
     * @param framebufferPixelsHigh
     *            Frame buffer's number of pixels in height.
     */
    public void setFramebufferPixelsHigh(int framebufferPixelsHigh) {
        this.framebufferPixelsHigh = framebufferPixelsHigh;
    }

    /**
     * Get current overall Mode Parameters.
     * 
     * @return Current overall Mode Parameters.
     */
    public ModeParms getModeParms() {
        return modeParms;
    }

    /**
     * Set overall Mode Parameters.
     * 
     * @param modeParms
     *            New overall Mode Parameters.
     */
    public void setModeParms(ModeParms modeParms) {
        this.modeParms = modeParms;
    }

    /**
     * Get current overall eye buffer parameters.
     * 
     * @return Current overall eye buffer parameters.
     */
    public EyeBufferParms getEyeBufferParms() {
        return eyeBufferParms;
    }

    /**
     * Set overall eye buffer parameters.
     * 
     * @param eyeBufferParms
     *            New overall eye buffer parameters.
     */
    public void setEyeBufferParms(EyeBufferParms eyeBufferParms) {
        this.eyeBufferParms = eyeBufferParms;
    }

    /**
     * Get current overall head mode parameters.
     * 
     * @return Current overall head mode parameters.
     */
    public HeadModelParms getHeadModelParms() {
        return headModelParms;
    }

    /**
     * Set overall head mode parameters.
     * 
     * @param headModelParms
     *            New overall head mode parameters.
     */
    public void setHeadModelParms(HeadModelParms headModelParms) {
        this.headModelParms = headModelParms;
    }

    public VrAppSettings() {
        showLoadingIcon = true;
        renderMonoMode = false;
        useSrgbFramebuffer = false;
        useProtectedFramebuffer = false;
        framebufferPixelsWide = -1;
        framebufferPixelsHigh = -1;
        modeParms = new ModeParms();
        eyeBufferParms = new EyeBufferParms();
        headModelParms = new HeadModelParms();
    }
}
