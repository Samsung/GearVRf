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

#include <mutex>
#include "objects/hybrid_object.h"
#include "gl/gl_headers.h"  // for GL_TEXTURE_xxx

namespace gvr {

class Image : public HybridObject {

public:
    virtual ~Image() { }

    Image(int type, int format) :
            HybridObject(), mType(type), mPendingUpdate(0),
            mWidth(0), mHeight(0), mImageSize(0),
            mFormat(format), mLevels(0), mDataOffsets(NULL)
    {

    }

    Image(int type, int width, int height, int imagesize, int format, int levels) :
            HybridObject(), mType(type), mPendingUpdate(0),
            mWidth(width), mHeight(height), mImageSize(imagesize),
            mFormat(format), mLevels(levels), mDataOffsets(NULL)
    {
        if (levels > 1)
        {
            mDataOffsets = new int[levels];
        }
    }

    int getWidth() const { return mWidth; }
    int getHeight() const { return mHeight; }
    int getLevels() const { return mLevels; }
    int getType() const { return mType; }
    int getFormat() const { return mFormat; }
    int getDataOffset(int level)
    {
        if ((mDataOffsets != NULL) && (level >= 0) && (level < mLevels))
        {
            return mDataOffsets[level];
        }
        return 0;
    }

    bool setDataOffset(int level, int offset)
    {
        if ((mDataOffsets != NULL) && (level >= 0) && (level < mLevels))
        {
            mDataOffsets[level] = offset;
            return true;
        }
        return false;
    }

    bool setDataOffsets(const int* offsets, int n)
    {
        if (n != mLevels)
        {
            return false;
        }
        mUpdateLock.lock();
        for (int i = 0; i < n; ++i)
        {
            mDataOffsets[i] = offsets[i];
        }
        mUpdateLock.unlock();
    }

    bool checkForUpdate(int texid)
    {
        if (mPendingUpdate)
        {
            mUpdateLock.lock();
            update(texid);
            mPendingUpdate = false;
            mUpdateLock.unlock();
            return true;
        }
        return false;
    }

protected:
    virtual void update(int texid) { }

    std::mutex mUpdateLock;
    int     mType;
    int     mWidth;
    int     mHeight;
    int     mImageSize;
    int     mFormat;
    int     mLevels;
    int*    mDataOffsets;
    bool    mPendingUpdate;


private:
    Image(const Image& image);
    Image(Image&& image);
    Image& operator=(const Image& image);
    Image& operator=(Image&& image);
};

}

#endif
