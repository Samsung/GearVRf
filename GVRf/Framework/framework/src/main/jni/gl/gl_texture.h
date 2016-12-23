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
 * RAII class for GL textures.
 ***************************************************************************/

#ifndef GL_TEXTURE_H_
#define GL_TEXTURE_H_

#ifndef GL_EXT_texture_filter_anisotropic
#define GL_EXT_texture_filter_anisotropic 1
#define GL_TEXTURE_MAX_ANISOTROPY_EXT     0x84FE
#define GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT 0x84FF
#endif /* GL_EXT_texture_filter_anisotropic */

#include <cstdlib>
#include "objects/textures/image.h"
#include "objects/textures/texture.h"
#include "gl/gl_headers.h"
#include "util/gvr_log.h"
#include "engine/memory/gl_delete.h"

namespace gvr {
class GLTexture : public Texture
{
public:
    explicit GLTexture()  {
    }

    explicit GLTexture(int texture_id) :
            id_(texture_id) { }

    virtual ~GLTexture()
    {
        if (0 != id_ && deleter_)
        {
            deleter_->queueTexture(id_);
        }
    }

    virtual bool isReady()
    {
        int target = getTarget();
        if (!target)
        {
            return false;
        }
        update();
        if (mImage && id_ && mImage->checkForUpdate(id_))
        {
            setReady(true);
            return true;
        }
        return Texture::isReady();
    }

    GLuint id()
    {
        return id_;
    }

    virtual bool update()
    {
        if  (mPendingTask == Texture::TASK_INIT_PARAMS)
        {
            int target = getTarget();
            deleter_ = getDeleterForThisThread();

            // Sets the new MIN FILTER
            GLenum min_filter_type_ = mTexParams[0];

            // Sets the MAG FILTER
            GLenum mag_filter_type_ = mTexParams[1];

            // Sets the wrap parameter for texture coordinate S
            GLenum wrap_s_type_ = mTexParams[3];

            // Sets the wrap parameter for texture coordinate S
            GLenum wrap_t_type_ = mTexParams[4];

            glGenTextures(1, &id_);
            glBindTexture(target, id_);

            // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
            if (mTexParams[2] > 1.0f)
            {
                glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) mTexParams[2]);
            }
            glTexParameteri(target, GL_TEXTURE_WRAP_S, wrap_s_type_);
            glTexParameteri(target, GL_TEXTURE_WRAP_T, wrap_t_type_);
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min_filter_type_);
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
        } // switch
        mPendingTask = 0;
        return true;
    }

private:
    GLTexture(const GLTexture& gl_texture);
    GLTexture(GLTexture&& gl_texture);
    GLTexture& operator=(const GLTexture& gl_texture);
    GLTexture& operator=(GLTexture&& gl_texture);

protected:
    GLuint id_ = 0;
    GlDelete* deleter_;

};

}
#endif
