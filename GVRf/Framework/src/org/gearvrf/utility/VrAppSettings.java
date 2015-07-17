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

        //To set power save mode is allowed. If true, then the app will run
        //at 30 fps when power is low. Otherwise will show a error message
        //instead.
        public boolean allowPowerSave;
        
        //If true, the fullscreen flag of the activity window will be on when a vr
        //activity return from background to foreground. It will benefit the performance 
        // since it won't draw DecorView as background. However it will mess up codebases
        // which depends on native activity like Stratum and Unreal Engine.
        public boolean resetWindowFullScreen;
        
        // Set fixed cpu clock level and gpu clock level.
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
        
        @Override
        public String toString(){
            StringBuilder res = new StringBuilder();
            res.append(" allowPowerSave = " + this.allowPowerSave);
            res.append(" resetWindowFullScreen = " + this.resetWindowFullScreen);
            res.append(" cpuLevel = " + this.cpuLevel);
            res.append(" gpuLevel = " + this.gpuLevel);
            return res.toString();
        }

    }

    public static class EyeBufferParms {

        //Data format for depth buffer
        public DepthFormat depthFormat;

        // Determines the texture filtering method.
        // It is actually how the time warp samples the eye buffers.
        public TextureFilter textureFilter;

        // Color format of current vr app.
        // Defaults to FMT_8888.
        public ColorFormat colorFormat;
        
        // Resolution for each eye buffer.
        public int resolution;

        // Level of width scaling.
        public int widthScale;
        
        // Level for multi sampling.
        // Default to 2.
        public int multiSamples;
        
        //fov-y
        public float mFovY;

        public enum DepthFormat {
            DEPTH_0(0), //No depth buffer
            
            DEPTH_16(1), //16-bit depth buffer
            
            DEPTH_24(2), //24-bit depth buffer
            
            DEPTH_24_STENCIL_8(3);//32-bit depth buffer
            
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
            TEXTURE_FILTER_NEAREST(0), //Nearest-neighbor interpolation
                                       //Fastest, but will cause lots of aliasing
                                       //and artifacts
            TEXTURE_FILTER_BILINEAR(1), //Bilinear filtering.
                                        //Should be the most common one to use.
            TEXTURE_FILTER_ANISO_2(2), // 2:1 Anisotropic filtering
                                       // Will reduce aliasing, but will cause
                                       // worse performance.
            TEXTURE_FILTER_ANISO_4(3); // 4:1 Anisotropic filtering
                                       // Texture filtering with best quality.
                                       // But also with worst performance.
            
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
            COLOR_565(0), // 5-bit red, 6-bit for green, 5-bit blue

            COLOR_5551(1), // one bit from green right now for alpha channel.

            COLOR_4444(2),//4-bit red, 4-bit green, 4-bit blue, 4-bit alpha channel.

            COLOR_8888(3),//8-bit red, 8-bit green, 8-bit blue, 8-bit alpha channel.

            COLOR_8888_sRGB(4);// SRGB color format
            
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
        
        /**
         * Set current multi FovY.
         * 
         * @param fovy
         *            FovY degree to set
         */
        public void setFovY(float fovy){
            this.mFovY = fovy;
        }
        
        /**
         * Get current multi FovY.
         * 
         * @return Current fovY degree.
         */
        public float getFovY(){
            return mFovY;
        }

        public EyeBufferParms() {
            multiSamples = 2;
            widthScale = 1;
            resolution = -1;
            depthFormat = DepthFormat.DEPTH_24;
            colorFormat = ColorFormat.COLOR_8888;
            textureFilter = TextureFilter.TEXTURE_FILTER_BILINEAR;
            mFovY = 90.0f;
        }
        
        @Override
        public String toString(){
            StringBuilder res = new StringBuilder();
            res.append(" multiSamples = " + this.multiSamples);
            res.append(" widthScale = " + this.widthScale);
            res.append(" resolution = " + this.resolution);
            res.append(" depthFormat = " + this.depthFormat.name());
            res.append(" colorFormat = " + this.colorFormat.name());
            res.append(" textureFilter = " + this.textureFilter.name());
            return res.toString();
        }
    }

    // -----------------------------------------------------------------
    // Head Model
    // -----------------------------------------------------------------
    public static class HeadModelParms {
        float interpupillaryDistance; //Distance from left eye to right eye.
        float eyeHeight; // Distance from ground to eye.
        float headModelDepth; // Offset of head center ahead of eyes based on eyeHeight.
        float headModelHeight; // Distance from neck joint to eyes based on eyeHeight.

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
        
        @Override
        public String toString(){
            StringBuilder res = new StringBuilder();
            res.append(" interpuillaryDistance = " + this.interpupillaryDistance);
            res.append(" eyeHeight = " + this.eyeHeight);
            res.append(" headModelDepth = " + this.headModelDepth);
            res.append(" headModelHeight = " + this.headModelHeight);
            return res.toString();
        }
    }
    // -----------------------------------------------------------------
    // This class is to judge if the app will run under a special mono scopic mode.
    // If it is the case, many parameters like headmodelparms and modeparms won't 
    // take effect
    // -----------------------------------------------------------------
   
    public static class MonoScopicModeParms{
        private boolean isMonoScopicMode;// Is the app mono scopic rendering mode?
        private boolean isMonoFullScreen;// If it is mono scopic, will it be fullscreen or simple quad?
        
        /**
         * Set if current app is mono scopic.
         * 
         * @param isMono
         *            if current app is mono scopic
         */
        public void setMonoScopicMode(boolean isMono){
            this.isMonoScopicMode = isMono;
        }
        
        /**
         * Check if current app is mono scopic.
         * 
         * @return if current app is mono scopic.
         */
        public boolean isMonoScopicMode(){
            return isMonoScopicMode;
        }
        
        /**
         * Set if current app is full screen under mono scopic mode.
         * 
         * @param isFullScreen
         *            if current app is full screen under mono scopic mode.
         */
        public void setMonoFullScreenMode(boolean isFullScreen){
            this.isMonoFullScreen = isFullScreen;
        }
        
        /**
         * Check if current app is full screen under mono scopic mode.
         * 
         * @return if current app is full screen under mono scopic mode.
         */
        public boolean isMonoFullScreenMode(){
            return isMonoFullScreen;
        }
        
        public MonoScopicModeParms(){
            isMonoScopicMode = isMonoFullScreen = false;
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

    public ModeParms modeParms;
    public EyeBufferParms eyeBufferParms;
    public HeadModelParms headModelParms;
    public MonoScopicModeParms monoScopicModeParms;

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

    /**
     * Get current overall mono scopic mode parameters.
     * 
     * @return Current overall mono scopic mode parameters.
     */
    public MonoScopicModeParms getMonoScopicModeParms(){
        return monoScopicModeParms;
    }
    
    /**
     * Set overall mono scopic mode parameters.
     * 
     * @param monoScopicModeParms
     *            New overall mono scopic mode parameters.
     */
    public void setMonoScopicModeParms(MonoScopicModeParms monoScopicModeParms){
        this.monoScopicModeParms = monoScopicModeParms;
    }
    
    public VrAppSettings() {
        showLoadingIcon = true;
        useSrgbFramebuffer = false;
        useProtectedFramebuffer = false;
        framebufferPixelsWide = -1;
        framebufferPixelsHigh = -1;
        modeParms = new ModeParms();
        eyeBufferParms = new EyeBufferParms();
        headModelParms = new HeadModelParms();
        monoScopicModeParms = new MonoScopicModeParms();
    }
    
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append("showLoadingIcon = " + showLoadingIcon);
        res.append(" useSrgbFramebuffer = " + useSrgbFramebuffer);
        res.append(" useProtectedFramebuffer = " + useProtectedFramebuffer);
        res.append(" framebufferPixelsWide = " + this.framebufferPixelsWide);
        res.append(" framebufferPixelsHigh = " + this.framebufferPixelsHigh);
        res.append(modeParms.toString());
        res.append(eyeBufferParms.toString());
        res.append(this.headModelParms.toString());
        return res.toString();
    }
}
