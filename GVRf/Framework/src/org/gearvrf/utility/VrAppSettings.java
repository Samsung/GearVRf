package org.gearvrf.utility;

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

        public boolean isAllowPowerSave() {
            return allowPowerSave;
        }

        public void setAllowPowerSave(boolean allowPowerSave) {
            this.allowPowerSave = allowPowerSave;
        }

        public boolean isResetWindowFullScreen() {
            return resetWindowFullScreen;
        }

        public void setResetWindowFullScreen(boolean resetWindowFullScreen) {
            this.resetWindowFullScreen = resetWindowFullScreen;
        }

        public int getCpuLevel() {
            return cpuLevel;
        }

        public void setCpuLevel(int cpuLevel) {
            this.cpuLevel = cpuLevel;
        }

        public int getGpuLevel() {
            return gpuLevel;
        }

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

        // Adreno and Tegra benefit from 16 bit depth buffers
        public DepthFormat depthFormat;

        // Determines how the time warp samples the eye buffers.
        // Defaults to TEXTURE_FILTER_BILINEAR.
        public TextureFilter textureFilter;
        // 16 bit color eye buffers can improve performance noticeably, but any
        // dithering effects will be distorted by the warp to screen.
        //
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
            DEPTH_0(0), DEPTH_16(1), DEPTH_24(2), DEPTH_24_STENCIL_8(3);
            private final int value;

            private DepthFormat(int value) {
                this.value = value;
            }

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

            public int getValue() {
                return value;
            }
        }

        public enum ColorFormat {
            COLOR_565(0), COLOR_5551(1), // single bit alpha useful for overlay
                                         // planes
            COLOR_4444(2), COLOR_8888(3), COLOR_8888_sRGB(4);
            private final int value;

            private ColorFormat(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        public DepthFormat getDepthFormat() {
            return depthFormat;
        }

        public void setDepthFormat(DepthFormat depthFormat) {
            this.depthFormat = depthFormat;
        }

        public TextureFilter getTextureFilter() {
            return textureFilter;
        }

        public void setTextureFilter(TextureFilter textureFilter) {
            this.textureFilter = textureFilter;
        }

        public ColorFormat getColorFormat() {
            return colorFormat;
        }

        public void setColorFormat(ColorFormat colorFormat) {
            this.colorFormat = colorFormat;
        }

        public int getResolution() {
            return resolution;
        }

        public void setResolution(int resolution) {
            this.resolution = resolution;
        }

        public int getWidthScale() {
            return widthScale;
        }

        public void setWidthScale(int widthScale) {
            this.widthScale = widthScale;
        }

        public int getMultiSamples() {
            return multiSamples;
        }

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

    public static class HeadModelParms {
        float InterpupillaryDistance; // Distance between eyes.
        float EyeHeight; // Eye height relative to the ground.
        float HeadModelDepth; // Eye offset forward from the head center at
                              // EyeHeight.
        float HeadModelHeight; // Neck joint offset down from the head center at
                               // EyeHeight.

        public float getInterpupillaryDistance() {
            return InterpupillaryDistance;
        }

        public void setInterpupillaryDistance(float interpupillaryDistance) {
            InterpupillaryDistance = interpupillaryDistance;
        }

        public float getEyeHeight() {
            return EyeHeight;
        }

        public void setEyeHeight(float eyeHeight) {
            EyeHeight = eyeHeight;
        }

        public float getHeadModelDepth() {
            return HeadModelDepth;
        }

        public void setHeadModelDepth(float headModelDepth) {
            HeadModelDepth = headModelDepth;
        }

        public float getHeadModelHeight() {
            return HeadModelHeight;
        }

        public void setHeadModelHeight(float headModelHeight) {
            HeadModelHeight = headModelHeight;
        }

        public HeadModelParms() {
            this.InterpupillaryDistance = Float.NaN;
            this.EyeHeight = Float.NaN;
            this.HeadModelDepth = Float.NaN;
            this.HeadModelHeight = Float.NaN;
        }
    }

    public boolean showLoadingIcon;
    public boolean renderMonoMode;
    public boolean useSrgbFramebuffer; // EGL_GL_COLORSPACE_KHR,
    // EGL_GL_COLORSPACE_SRGB_KHR
    public boolean useProtectedFramebuffer; // EGL_PROTECTED_CONTENT_EXT,
                                            // EGL_TRUE
    public int framebufferPixelsWide;
    public int framebufferPixelsHigh;

    public ModeParms modeParms;
    public EyeBufferParms eyeBufferParms;
    public HeadModelParms headModelParms;


    public boolean isShowLoadingIcon() {
        return showLoadingIcon;
    }

    public void setShowLoadingIcon(boolean showLoadingIcon) {
        this.showLoadingIcon = showLoadingIcon;
    }

    public boolean isRenderMonoMode() {
        return renderMonoMode;
    }

    public void setRenderMonoMode(boolean renderMonoMode) {
        this.renderMonoMode = renderMonoMode;
    }

    public boolean isUseSrgbFramebuffer() {
        return useSrgbFramebuffer;
    }

    public void setUseSrgbFramebuffer(boolean useSrgbFramebuffer) {
        this.useSrgbFramebuffer = useSrgbFramebuffer;
    }

    public boolean isUseProtectedFramebuffer() {
        return useProtectedFramebuffer;
    }

    public void setUseProtectedFramebuffer(boolean useProtectedFramebuffer) {
        this.useProtectedFramebuffer = useProtectedFramebuffer;
    }

    public int getFramebufferPixelsWide() {
        return framebufferPixelsWide;
    }

    public void setFramebufferPixelsWide(int framebufferPixelsWide) {
        this.framebufferPixelsWide = framebufferPixelsWide;
    }

    public int getFramebufferPixelsHigh() {
        return framebufferPixelsHigh;
    }

    public void setFramebufferPixelsHigh(int framebufferPixelsHigh) {
        this.framebufferPixelsHigh = framebufferPixelsHigh;
    }

    public ModeParms getModeParms() {
        return modeParms;
    }

    public void setModeParms(ModeParms modeParms) {
        this.modeParms = modeParms;
    }

    public EyeBufferParms getEyeBufferParms() {
        return eyeBufferParms;
    }

    public void setEyeBufferParms(EyeBufferParms eyeBufferParms) {
        this.eyeBufferParms = eyeBufferParms;
    }

    public HeadModelParms getHeadModelParms() {
        return headModelParms;
    }

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
