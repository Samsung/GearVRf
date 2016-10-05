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

import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

/**
 * A class that represents overall parameters that we can set with Oculus
 * library.
 */
public class VrAppSettings {
    public static class ModeParams {

        // To set power save mode is allowed. If true, then the app will run
        // at 30 fps when power is low. Otherwise will show a error message
        // instead.
        public boolean allowPowerSave;

        // If true, the fullscreen flag of the activity window will be on when a
        // vr
        // activity return from background to foreground. It will benefit the
        // performance
        // since it won't draw DecorView as background. However it will mess up
        // codebases
        // which depends on native activity like Stratum and Unreal Engine.
        public boolean resetWindowFullScreen;

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

        public ModeParams() {
            allowPowerSave = true;
            resetWindowFullScreen = true;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(" allowPowerSave = " + this.allowPowerSave);
            res.append(" resetWindowFullScreen = " + this.resetWindowFullScreen);
            return res.toString();
        }

    }

    public static class EyeBufferParams {

        // Data format for depth buffer
        public DepthFormat depthFormat;

        // Color format of current vr app.
        // Defaults to FMT_8888.
        public ColorFormat colorFormat;

        // Resolution for eye buffer width.
        public int resolutionWidth;

        // Resolution for eye buffer height.
        public int resolutionHeight;
        
        //Is depth buffer resolved to a texture or not.
        boolean resolveDepth;

        // Level for multi sampling.
        // Default to 2.
        public int multiSamples;

        // fov-y
        public float mFovY;

        public enum DepthFormat {
            DEPTH_0(0), // No depth buffer

            DEPTH_16(1), // 16-bit depth buffer

            DEPTH_24(2), // 24-bit depth buffer

            DEPTH_24_STENCIL_8(3);// 32-bit depth buffer

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

        public enum ColorFormat {
            COLOR_565(0), // 5-bit red, 6-bit for green, 5-bit blue

            COLOR_5551(1), // one bit from green right now for alpha channel.

            COLOR_4444(2), // 4-bit red, 4-bit green, 4-bit blue, 4-bit alpha
                           // channel.

            COLOR_8888(3), // 8-bit red, 8-bit green, 8-bit blue, 8-bit alpha
                           // channel.

            COLOR_8888_sRGB(4), // SRGB color format
            COLOR_RGBA16F(5);   // GL_RGBA16F

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
         * Get current resolution width.
         * 
         * @return Current resolution width.
         */
        public int getResolutionWidth() {
            return resolutionWidth;
        }

        /**
         * Get current resolution height.
         * 
         * @return Current resolution height.
         */
        public int getResolutionHeight() {
            return resolutionHeight;
        }

        /**
         * Set resolution width.
         * 
         * @param resolutionWidth
         *            Resolution width to set.
         */
        public void setResolutionWidth(int resolutionWidth) {
            this.resolutionWidth = resolutionWidth;
        }

        /**
         * Set resolution height.
         * 
         * @param resolutionHeight
         *            Resolution height to set.
         */
        public void setResolutionHeight(int resolutionHeight) {
            this.resolutionHeight = resolutionHeight;
        }

        /**
         *  Check if depth buffer is currently resolved to a texture 
         * 
         * @return if depth buffer is currently resolved to a texture
         */
        public boolean isResolveDepth() {
            return resolveDepth;
        }

        /**
         * Set if depth buffer is currently resolved to a texture 
         * 
         * @param resolveDepth
         *            if depth buffer is currently resolved to a texture
         */
        public void enableResolveDepth(boolean resolveDepth) {
            this.resolveDepth = resolveDepth;
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

        /**
         * Set current multi FovY.
         * 
         * @param fovy
         *            FovY degree to set
         */
        public void setFovY(float fovy) {
            this.mFovY = fovy;
        }

        /**
         * Get current multi FovY.
         * 
         * @return Current fovY degree.
         */
        public float getFovY() {
            return mFovY;
        }

        public EyeBufferParams() {
            multiSamples = 2;
            resolveDepth = false;
            resolutionWidth = -1;
            resolutionHeight = -1;
            depthFormat = DepthFormat.DEPTH_24;
            colorFormat = ColorFormat.COLOR_8888;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(" multiSamples = " + this.multiSamples);
            res.append(" resolveDepth = " + this.resolveDepth);
            res.append(" resolutionWidth = " + this.resolutionWidth);
            res.append(" resolutionHeight = " + this.resolutionHeight);
            res.append(" depthFormat = " + this.depthFormat.name());
            res.append(" colorFormat = " + this.colorFormat.name());
            res.append(" fovY = " + this.mFovY);
            return res.toString();
        }
    }

    // -----------------------------------------------------------------
    // Head Model
    // -----------------------------------------------------------------
    public static class HeadModelParams {
        float interpupillaryDistance; // Distance from left eye to right eye.
        float eyeHeight; // Distance from ground to eye.
        float headModelDepth; // Offset of head center ahead of eyes based on
                              // eyeHeight.
        float headModelHeight; // Distance from neck joint to eyes based on
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

        public HeadModelParams() {
            this.interpupillaryDistance = Float.NaN;
            this.eyeHeight = Float.NaN;
            this.headModelDepth = Float.NaN;
            this.headModelHeight = Float.NaN;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(" interpuillaryDistance = "
                    + this.interpupillaryDistance);
            res.append(" eyeHeight = " + this.eyeHeight);
            res.append(" headModelDepth = " + this.headModelDepth);
            res.append(" headModelHeight = " + this.headModelHeight);
            return res.toString();
        }
    }

    // -----------------------------------------------------------------
    // This class is to judge if the app will run under a special mono scopic
    // mode.
    // If it is the case, many parameters like headmodelparms and modeparms
    // won't
    // take effect
    // -----------------------------------------------------------------

    public static class MonoscopicModeParams {
        private boolean isMonoscopicMode;// Is the app mono scopic rendering
                                         // mode?
        private boolean isMonoFullScreen;// If it is mono scopic, will it be
                                         // fullscreen or simple quad?

        /**
         * Set if current app is mono scopic.
         *
         * @param isMono
         *            if current app is mono scopic
         */
        public void setMonoscopicMode(boolean isMono) {
            this.isMonoscopicMode = isMono;
        }

        /**
         * Check if current app is mono scopic.
         * 
         * @return if current app is mono scopic.
         */
        public boolean isMonoscopicMode() {
            return isMonoscopicMode;
        }

        /**
         * Set if current app is full screen under mono scopic mode.
         * 
         * @param isFullScreen
         *            if current app is full screen under mono scopic mode.
         */
        public void setMonoFullScreenMode(boolean isFullScreen) {
            this.isMonoFullScreen = isFullScreen;
        }

        /**
         * Check if current app is full screen under mono scopic mode.
         * 
         * @return if current app is full screen under mono scopic mode.
         */
        public boolean isMonoFullScreenMode() {
            return isMonoFullScreen;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(" isMonoscopicMode = " + isMonoscopicMode);
            result.append(" isMonoFullScreenMode = " + isMonoFullScreen);
            return result.toString();
        }

        public MonoscopicModeParams() {
            isMonoscopicMode = isMonoFullScreen = false;
        }
    }

    public static class PerformanceParams {

        // Set fixed cpu clock level and gpu clock level.
        public int cpuLevel, gpuLevel;

        public PerformanceParams() {
            cpuLevel = gpuLevel = 2;
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

        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append(" cpuLevel = " + this.cpuLevel);
            res.append(" gpuLevel = " + this.gpuLevel);
            return res.toString();
        }
    }

    public static int DEFAULT_FBO_RESOLUTION = 1024;

    // If it will show loading icon in the vr app.
    public boolean showLoadingIcon;

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

    // Use this flag to enable the gaze cursor controller whenever
    // the phone is docked.
    boolean useGazeCursorController;

    // Use this flag to enable the AndroidWearTouchpad in the scene
    boolean useAndroidWearTouchpad;

    // Use multiview feature
    boolean useMultiview;

    public final ModeParams modeParams;
    public final EyeBufferParams eyeBufferParams;
    public final HeadModelParams headModelParams;
    public final MonoscopicModeParams monoscopicModeParams;
    public final PerformanceParams performanceParams;

    /**
     * Set if user wants to use multiview or not
     * 
     * @param useMultiview
     *            
     */
    public void setUseMultiview(boolean useMultiview){
        this.useMultiview = useMultiview;
    }
    /**
     * Check if user has set usemultiview flag
     * 
     * @return if user has set usemultiview flag
     */
    public boolean isMultiviewSet() {
        return useMultiview;
    }

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
     * Check if current app is using the gaze cursor controller
     * 
     * @return if current app is using the gaze cursor controller
     */
    public boolean useGazeCursorController() {
        return useGazeCursorController;
    }

    /**
     * Set if current app is using the gaze cursor controller
     *
     * @param useGazeCursorController
     *            if current app is using the gaze cursor controller
     */
    public void setUseGazeCursorController(boolean useGazeCursorController) {
        this.useGazeCursorController = useGazeCursorController;
    }

    /**
     * Check if current app is using the Android Wear Touchpad.
     *
     * @return if current app is using the AndroidWearTouchpad
     */
    public boolean useAndroidWearTouchpad() {
        return useAndroidWearTouchpad;
    }

    /**
     * Set if current app uses the Android Wear Touchpad
     *
     * @param useAndroidWearTouchpad
     *            if current app is using the AndroidWearTouchpad
     */
    public void setUseAndroidWearTouchpad(boolean useAndroidWearTouchpad) {
        this.useAndroidWearTouchpad = useAndroidWearTouchpad;
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
    public ModeParams getModeParams() {
        return modeParams;
    }

    /**
     * Get current overall eye buffer parameters.
     * 
     * @return Current overall eye buffer parameters.
     */
    public EyeBufferParams getEyeBufferParams() {
        return eyeBufferParams;
    }

    /**
     * Get current overall head mode parameters.
     * 
     * @return Current overall head mode parameters.
     */
    public HeadModelParams getHeadModelParams() {
        return headModelParams;
    }

    /**
     * Get current overall mono scopic mode parameters.
     * 
     * @return Current overall mono scopic mode parameters.
     */
    public MonoscopicModeParams getMonoscopicModeParams() {
        return monoscopicModeParams;
    }

    /**
     * Get overall performance parameters.
     * 
     * @return Current overall performance parameters.
     */
    public PerformanceParams getPerformanceParams() {
        return performanceParams;
    }

    public VrAppSettings() {
        showLoadingIcon = true;
        useMultiview = false;
        useSrgbFramebuffer = false;
        useProtectedFramebuffer = false;
        framebufferPixelsWide = -1;
        framebufferPixelsHigh = -1;
        modeParams = new ModeParams();
        eyeBufferParams = new EyeBufferParams();
        headModelParams = new HeadModelParams();
        monoscopicModeParams = new MonoscopicModeParams();
        performanceParams = new PerformanceParams();
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("showLoadingIcon = " + showLoadingIcon);
        res.append(" useSrgbFramebuffer = " + useSrgbFramebuffer);
        res.append(" useProtectedFramebuffer = " + useProtectedFramebuffer);
        res.append(" useMultiview = " + useMultiview);
        res.append(" framebufferPixelsWide = " + this.framebufferPixelsWide);
        res.append(" framebufferPixelsHigh = " + this.framebufferPixelsHigh);
        res.append(modeParams.toString());
        res.append(eyeBufferParams.toString());
        res.append(headModelParams.toString());
        res.append(monoscopicModeParams.toString());
        res.append(performanceParams.toString());
        return res.toString();
    }

    /**
     * Doesn't do a thing
     * @deprecated
     */
    static public void setShowDebugLog(boolean b) {

    }
}
