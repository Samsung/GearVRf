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
package org.gearvrf;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

/** A container for various services and pieces of data required for rendering. */
class GVRRenderBundle {
    protected GVRContext mGVRContext;
    private  GVRShaderManager mShaderManager;
    private  GVRRenderTexture mPostEffectRenderTextureA = null;
    private  GVRRenderTexture mPostEffectRenderTextureB = null;
    private  GVRRenderTarget mEyeCaptureRenderTarget = null;

    protected int  mSampleCount;
    protected int mWidth, mHeight;
    protected GVRRenderTarget[] mLeftEyeRenderTarget = new GVRRenderTarget[3];
    protected GVRRenderTarget [] mRightEyeRenderTarget = new GVRRenderTarget[3];
    protected GVRRenderTarget [] mMultiviewRenderTarget = new GVRRenderTarget[3];


    private GVRRenderTexture mEyeCapturePostEffectRenderTextureA = null;
    private GVRRenderTexture mEyeCapturePostEffectRenderTextureB = null;

    GVRRenderBundle(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mShaderManager = new GVRShaderManager(gvrContext);

        final VrAppSettings appSettings = mGVRContext.getApplication().getAppSettings();
        mSampleCount = appSettings.getEyeBufferParams().getMultiSamples() < 0 ? 0
                : appSettings.getEyeBufferParams().getMultiSamples();
        if (mSampleCount > 1) {
            int maxSampleCount = GVRMSAA.getMaxSampleCount();
            if (mSampleCount > maxSampleCount && maxSampleCount > 1) {
                mSampleCount = maxSampleCount;
            }
        }
        mWidth = mGVRContext.getApplication().getAppSettings().getEyeBufferParams().getResolutionWidth();
        mHeight = mGVRContext.getApplication().getAppSettings().getEyeBufferParams().getResolutionHeight();
    }
    public void createRenderTarget(int index, GVRViewManager.EYE eye, GVRRenderTexture renderTexture){

        if(eye == GVRViewManager.EYE.MULTIVIEW)
            mMultiviewRenderTarget[index] = new GVRRenderTarget(renderTexture, mGVRContext.getMainScene(), true);

        else if(eye == GVRViewManager.EYE.LEFT)
            mLeftEyeRenderTarget[index] = new GVRRenderTarget(renderTexture, mGVRContext.getMainScene());
        else
            mRightEyeRenderTarget[index] = new GVRRenderTarget(renderTexture, mGVRContext.getMainScene(),mLeftEyeRenderTarget[index]);
    }


    public void createRenderTargetChain(boolean use_multiview){

        for(int i=0; i< 3; i++){
            if(use_multiview){
                addRenderTarget(mMultiviewRenderTarget[i].getNative(), GVRViewManager.EYE.MULTIVIEW.ordinal(), i);
            }
            else {
                addRenderTarget(mLeftEyeRenderTarget[i].getNative(), GVRViewManager.EYE.LEFT.ordinal(), i);
                addRenderTarget(mRightEyeRenderTarget[i].getNative(), GVRViewManager.EYE.RIGHT.ordinal(), i);
            }

        }
        for(int i=0; i< 3; i++){
            int index = (i+1) % 3;
            if(use_multiview)
                mMultiviewRenderTarget[i].attachRenderTarget(mMultiviewRenderTarget[index]);
            else {
                mLeftEyeRenderTarget[i].attachRenderTarget(mLeftEyeRenderTarget[index]);
                mRightEyeRenderTarget[i].attachRenderTarget(mRightEyeRenderTarget[index]);
            }
        }
    }
    public GVRRenderTarget getEyeCaptureRenderTarget() {
        if(mEyeCaptureRenderTarget == null){
            mEyeCaptureRenderTarget  = new GVRRenderTarget(new GVRRenderTexture(mGVRContext, mWidth, mHeight, mSampleCount), mGVRContext.getMainScene());
            mEyeCaptureRenderTarget.setCamera(mGVRContext.getMainScene().getMainCameraRig().getCenterCamera());
        }
        return  mEyeCaptureRenderTarget;
    }
    void beginRendering(int bufferIdx, GVRViewManager.EYE eye) {
        getRenderTexture(bufferIdx,eye).beginRendering();
    }
    public GVRRenderTexture getRenderTexture(int bufferIdx, GVRViewManager.EYE eye){

        if (eye == GVRViewManager.EYE.LEFT)
            return mLeftEyeRenderTarget[bufferIdx].getTexture();
        if (eye == GVRViewManager.EYE.RIGHT)
            return mRightEyeRenderTarget[bufferIdx].getTexture();
        if (eye == GVRViewManager.EYE.MULTIVIEW)
            return mMultiviewRenderTarget[bufferIdx].getTexture();

        Log.e("GVRRendleBundle", "incorrect Eye type");
        return null;
    }
    void endRendering(int bufferIdx, GVRViewManager.EYE eye) {
        getRenderTexture(bufferIdx,eye).endRendering();
    }

    public GVRRenderTarget getRenderTarget(GVRViewManager.EYE eye, int index){
        if(eye == GVRViewManager.EYE.LEFT)
            return mLeftEyeRenderTarget[index];
        if(eye == GVRViewManager.EYE.RIGHT)
            return mRightEyeRenderTarget[index];

        return mMultiviewRenderTarget[index];
    }
    public GVRRenderTexture getEyeCapturePostEffectRenderTextureA(){
        if(mEyeCapturePostEffectRenderTextureA == null)
            mEyeCapturePostEffectRenderTextureA  = new GVRRenderTexture(mGVRContext, mWidth , mHeight, mSampleCount, 1);
        return mEyeCapturePostEffectRenderTextureA;
    }

    public GVRRenderTexture getEyeCapturePostEffectRenderTextureB(){
        if(mEyeCapturePostEffectRenderTextureB == null)
            mEyeCapturePostEffectRenderTextureB = new GVRRenderTexture(mGVRContext, mWidth , mHeight, mSampleCount, 1);

        return mEyeCapturePostEffectRenderTextureB;
    }
    public GVRShaderManager getShaderManager() {
        return mShaderManager;
    }

    public GVRRenderTexture getPostEffectRenderTextureA() {
        if(mPostEffectRenderTextureA == null)
            mPostEffectRenderTextureA = new GVRRenderTexture(mGVRContext, mWidth , mHeight, mSampleCount, mGVRContext.getApplication().getAppSettings().isMultiviewSet() ? 2 : 1);
        return mPostEffectRenderTextureA;
    }

    public GVRRenderTexture getPostEffectRenderTextureB() {
        if(mPostEffectRenderTextureB == null)
            mPostEffectRenderTextureB = new GVRRenderTexture(mGVRContext, mWidth , mHeight, mSampleCount, mGVRContext.getApplication().getAppSettings().isMultiviewSet() ? 2 : 1);

        return mPostEffectRenderTextureB;
    }

    public void addRenderTarget(GVRRenderTarget target, GVRViewManager.EYE eye, int index)
    {
        addRenderTarget(target.getNative(), eye.ordinal(), index);
    }

    protected static native long getRenderTextureNative(long ptr);
    protected static native void addRenderTarget(long renderTarget, int eye, int index);
}
