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

#ifndef FLOAT_IMAGE_H
#define FLOAT_IMAGE_H

#include "image.h"
#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "util/jni_utils.h"

namespace gvr {
/*
 * Represents an image whose pixels are floating point pairs.
 */
    class FloatImage : public Image
    {
    public:
        FloatImage(int pixelFormat = GL_RG);
        virtual ~FloatImage();
        void update(JNIEnv* env, int width, int height, jfloatArray data, int pixelFormat = 0);

    protected:
        void clearData(JNIEnv* env);

    private:
        FloatImage(const FloatImage&) = delete;
        FloatImage(FloatImage&&) = delete;
        FloatImage& operator=(const FloatImage&) = delete;
        FloatImage& operator=(FloatImage&) = delete;

    protected:
        JavaVM* mJava;
        jfloatArray mData;
    };

}
#endif

