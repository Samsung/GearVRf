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

import org.gearvrf.utility.VrAppSettings;

/** A container for various services and pieces of data required for rendering. */
final class GVRRenderBundle implements IRenderBundle {
    private final GVRContext mGVRContext;
    private final GVRMaterialShaderManager mMaterialShaderManager;
    private final GVRPostEffectShaderManager mPostEffectShaderManager;
    private final GVRRenderTexture mPostEffectRenderTextureA;
    private final GVRRenderTexture mPostEffectRenderTextureB;

    GVRRenderBundle(GVRContext gvrContext, final int width, final int height) {
        mGVRContext = gvrContext;
        mMaterialShaderManager = new GVRMaterialShaderManager(gvrContext);
        mPostEffectShaderManager = new GVRPostEffectShaderManager(gvrContext);

        final VrAppSettings appSettings = mGVRContext.getActivity().getAppSettings();
        int sampleCount = appSettings.getEyeBufferParams().getMultiSamples() < 0 ? 0
                : appSettings.getEyeBufferParams().getMultiSamples();
        if (sampleCount > 1) {
            int maxSampleCount = GVRMSAA.getMaxSampleCount();
            if (sampleCount > maxSampleCount) {
                sampleCount = maxSampleCount;
            }
        }

        if (sampleCount <= 1) {
            mPostEffectRenderTextureA = new GVRRenderTexture(mGVRContext, width, height);
            mPostEffectRenderTextureB = new GVRRenderTexture(mGVRContext, width, height);
        } else {
            mPostEffectRenderTextureA = new GVRRenderTexture(mGVRContext, width, height, sampleCount);
            mPostEffectRenderTextureB = new GVRRenderTexture(mGVRContext, width, height, sampleCount);
        }
    }

    public GVRMaterialShaderManager getMaterialShaderManager() {
        return mMaterialShaderManager;
    }

    public GVRPostEffectShaderManager getPostEffectShaderManager() {
        return mPostEffectShaderManager;
    }

    public GVRRenderTexture getPostEffectRenderTextureA() {
        return mPostEffectRenderTextureA;
    }

    public GVRRenderTexture getPostEffectRenderTextureB() {
        return mPostEffectRenderTextureB;
    }
}
