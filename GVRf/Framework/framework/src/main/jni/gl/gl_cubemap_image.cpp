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

/***************************************************************************
 * Cube map texture made by six bitmaps.
 ***************************************************************************/

#include <gvr_gl.h>
#include "gl/gl_cubemap_image.h"
#include "gl_bitmap_image.h"
namespace gvr {
class TextureParameters;
void GLCubemapImage::update(int texid)
{
    if (mJava == NULL)
    {
        return;
    }
    if (mBitmaps != NULL)
    {
        updateFromBitmap(texid);
        clearData(getCurrentEnv(mJava));
        LOGV("Texture: GLCubemapImage::update(%d, bitmaps)", texid);
    }
    else if (mTextures != NULL)
    {
        updateFromMemory(texid);
        clearData(getCurrentEnv(mJava));
        LOGV("Texture: GLCubemapImage::update(%d, textures)", texid);
    }
}

void GLCubemapImage::updateFromBitmap(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jobjectArray bmapArray = static_cast<jobjectArray>(mBitmaps);
    if (bmapArray == NULL)
    {
        LOGE("CubemapImage::updateFromBitmap bitmap array NULL");
        return;
    }
    // Clean up upon scope exit. The SCOPE_EXIT utility is used
    // to avoid duplicated code in the throw case and normal
    // case.
    SCOPE_EXIT( clearData(env); );

    for (int i = 0; i < 6; i++)
    {
        jobject bitmap = env->GetObjectArrayElement(bmapArray, i);
        jobject bmapref = env->NewLocalRef(bitmap);
        GLBitmapImage::updateFromBitmap(env, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, bitmap, false, mFormat);
        env->DeleteLocalRef(bmapref);
    }
    if(!mIsCompressed && mTexParams.getMinFilter() >=  TextureParameters::NEAREST_MIPMAP_NEAREST)
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
}

void GLCubemapImage::updateFromMemory(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jobjectArray texArray = static_cast<jobjectArray>(mTextures);
    if (texArray == NULL)
    {
        LOGE("CubemapImage::updateFromMemory texture array NULL");
        return;
    }

    // Clean up upon scope exit
    SCOPE_EXIT( clearData(env); );
    for (int i = 0; i < 6; i++)
    {
        jbyteArray byteArray = static_cast<jbyteArray>(env->GetObjectArrayElement(texArray, i));

        if (byteArray == NULL)
        {
            LOGE("CubemapImage::updateFromMemory texture %d is NULL", i);
        }
        else
        {
            jbyte* pixels = env->GetByteArrayElements(byteArray, 0);
            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, mFormat, mWidth,
                                   mHeight, 0, mImageSize, pixels + getDataOffset(i));
            checkGLError("GLCubemapImage::updateFromMemory");
            env->ReleaseByteArrayElements(byteArray, pixels, 0);
        }
        env->DeleteLocalRef(byteArray);
    }
}

}
