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
 * Textures.
 ***************************************************************************/

#ifndef TEXTURE_H_
#define TEXTURE_H_

#include "image.h"
#include "util/gvr_jni.h"

#define MAX_TEXTURE_PARAM_NUM 10

namespace gvr {
class Image;

/*
 * Packed representation of texture parameters.
 * Integer values for filtering and wrapping are
 * packed into a single byte. Maximum anisotropy,
 * a floating point value, is separate.
 * Texture parameters are supplied as an array
 * of values on input and converted to this
 * internal format.
 */
class TextureParameters
{
public:
    enum
    {
        CLAMP = 0,
        REPEAT = 1,
        MIRROR = 2,

        NEAREST = 0,
        LINEAR = 1,
        NEAREST_MIPMAP_NEAREST = 2,
        NEAREST_MIPMAP_LINEAR = 3,
        LINEAR_MIPMAP_NEAREST = 4,
        LINEAR_MIPMAP_LINEAR = 5,
    };

    TextureParameters() : MaxAnisotropy(1.0f)
    {
        Params.HashCode = 0;
        Params.BitFields.MinFilter = LINEAR_MIPMAP_NEAREST;
        Params.BitFields.MagFilter = LINEAR;
        Params.BitFields.WrapU = CLAMP;
        Params.BitFields.WrapV = CLAMP;
    }

    TextureParameters(const int* params)
    {
        setMinFilter(params[0]);
        setMagFilter(params[1]);
        setMaxAnisotropy((float) params[2]);
        setWrapU(params[3]);
        setWrapV(params[4]);
    }

    TextureParameters& operator=(const int* params)
    {
        setMinFilter(params[0]);
        setMagFilter(params[1]);
        setMaxAnisotropy((float) params[2]);
        setWrapU(params[3]);
        setWrapV(params[4]);
    }

    int getMinFilter() const { return Params.BitFields.MinFilter; }
    int getMagFilter() const { return Params.BitFields.MagFilter; }
    int getWrapU() const { return Params.BitFields.WrapU; }
    int getWrapV() const { return Params.BitFields.WrapV; }
    float getMaxAnisotropy() const { return MaxAnisotropy; }
    unsigned short getHashCode() const { return Params.HashCode; }
    void setMinFilter(int f) { Params.BitFields.MinFilter = f; }
    void setMagFilter(int f) { Params.BitFields.MagFilter = f; }
    void setWrapU(int wrap) { Params.BitFields.WrapU = wrap; }
    void setWrapV(int wrap) { Params.BitFields.WrapV = wrap; }
    void setMaxAnisotropy(float v) { MaxAnisotropy = v; }

protected:
    union
    {
        struct
        {
            unsigned int MinFilter : 3;
            unsigned int MagFilter : 3;
            unsigned int WrapU : 2;
            unsigned int WrapV : 2;
        } BitFields;
        unsigned short HashCode;
    } Params;
    float MaxAnisotropy;
};

class Texture : public HybridObject
{
public:
    /*
     * Texture types correspond to different subclasses of Texture.
     */
    enum TextureType
    {
        TEXTURE_2D = 1,
        TEXTURE_ARRAY,
        TEXTURE_EXTERNAL,
        TEXTURE_RENDER,
        TEXTURE_EXTERNAL_RENDERER
    };


    explicit Texture(int type = TEXTURE_2D);
    virtual ~Texture();
    void clearData(JNIEnv* env);
    void setImage(Image* image);
    void setImage(JNIEnv* env, jobject javaImage, Image* image);
    void updateTextureParameters(const int* texture_parameters, int n);

    int getType() const { return mType; }
    Image*  getImage()  { return mImage; }
    virtual int getId() { return mImage ? mImage->getId() : 0; }
    virtual bool isReady();

    const TextureParameters& getTexParams() const
    {
        return mTexParams;
    }

    bool transparency() {
        Image* image = getImage();
        return image && image->transparency();
    }

protected:
    JavaVM* mJava;
    jobject mJavaImage;
    Image*  mImage;
    int     mType;
    bool    mTexParamsDirty;
    TextureParameters   mTexParams;

private:
    Texture(const Texture& texture) = delete;
    Texture(Texture&& texture) = delete;
    Texture& operator=(const Texture& texture) = delete;
    Texture& operator=(Texture&& texture) = delete;
};

}

#endif
