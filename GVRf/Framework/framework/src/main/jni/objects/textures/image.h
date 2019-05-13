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


#ifndef IMAGE_H_
#define IMAGE_H_

#include <string>
#include <mutex>
#include <vector>
#include "objects/hybrid_object.h"
#include "util/gvr_log.h"
#include "gl/gl_headers.h"  // for GL_TEXTURE_xxx

namespace gvr {
class Texture;
class TextureParameters;

class Image : public HybridObject
{

public:
    /*
     * Indicates the type of the image.
     * Usually the type corresponds to a different
     * subclass of Image.
     * @see BitmapImage
     * @see CubemapImage
     * @see FloatImage
     */
    enum ImageType
    {
        NONE = 0,
        BITMAP = 1,
        CUBEMAP,
        FLOAT_BITMAP,
        ARRAY,
        EXTERNAL
    };

    enum ImageState
    {
        UNINITIALIZED = 0,
        HAS_DATA = 1,
        UPDATE_PENDING = 2,
    };

    virtual ~Image() { }

    Image() :
            HybridObject(), mState(UNINITIALIZED), mType(NONE), mFormat(0), mIsCompressed(false),
            mXOffset(0), mYOffset(0), mWidth(0), mHeight(0), mDepth(1), mImageSize(0), mUpdateLock(),
            mLevels(0)
    {
    }

    explicit Image(ImageType type, int format) :
            HybridObject(), mState(UNINITIALIZED), mType(type), mFormat(format),mIsCompressed(false),
            mXOffset(0), mYOffset(0), mWidth(0), mHeight(0), mDepth(1), mImageSize(0), mUpdateLock(),
            mLevels(0)
    {
    }

    explicit Image(ImageType type, short width, short height, int imagesize, int format, short levels) :
            HybridObject(), mType(type), mState(UNINITIALIZED), mUpdateLock(), mIsCompressed(false),
            mXOffset(0), mYOffset(0), mWidth(width), mHeight(height), mDepth(1), mImageSize(imagesize),
            mFormat(format), mLevels(levels)
    {
    }

    virtual int getId() = 0;
    virtual bool isReady() = 0;
    virtual void texParamsChanged(const TextureParameters&) = 0;
    virtual bool transparency() { return false; }

    bool hasData() const { return mState == HAS_DATA; }
    short getWidth() const { return mWidth; }
    short getHeight() const { return mHeight; }
    short getDepth() const { return mDepth; }
    short getLevels() const { return mLevels; }
    short getType() const { return mType; }
    int getFormat() const { return mFormat; }
    const char* getFileName() const  { return mFileName.c_str(); }

    void setFileName(const char* fname)
    {
        mFileName = fname;
    }

    int getDataOffset(int level)
    {
        if (!mDataOffsets.empty() && (level >= 0) && (level < mDataOffsets.size()))
        {
            return mDataOffsets[level];
        }
        return 0;
    }

    void setDataOffsets(const int* offsets, int n)
    {
        mDataOffsets.resize(n);
        for (int i = 0; i < n; ++i)
        {
            mDataOffsets[i] = offsets[i];
        }
    }

    bool checkForUpdate(int texid)
    {
        if (texid && updatePending())
        {
            std::lock_guard<std::mutex> lock(mUpdateLock);
            update(texid);
            updateComplete();
        }
        return hasData();
    }


protected:
    void signalUpdate()
    {
        mState = UPDATE_PENDING;
    }

    bool updatePending() const { return mState == UPDATE_PENDING; }
    void updateComplete()
    {
        mState = HAS_DATA;
    }
    virtual void update(int texid) { }

    std::mutex mUpdateLock;
    short   mType;
    short   mLevels;
    int     mXOffset;
    int     mYOffset;
    int     mWidth;
    int     mHeight;
    short   mDepth;
    short   mState;
    int     mImageSize;
    bool    mIsCompressed;
    int     mFormat;
    std::string mFileName;
    std::vector<int>    mDataOffsets;

private:
    Image(const Image& image) = delete;
    Image(Image&& image) = delete;
    Image& operator=(const Image& image) = delete;
    Image& operator=(Image&& image) = delete;
};

}

#endif
