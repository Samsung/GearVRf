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
 * JNI
 ***************************************************************************/

#include "float_image.h"

namespace gvr {
extern "C"
{
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeFloatImage_update(JNIEnv* env, jobject obj, jlong jimage,
                                             jint width, jint height,
                                             jint pixelFormat, jfloatArray jdata);
};

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeFloatImage_update(JNIEnv* env, jobject obj, jlong jimage,
                                         jint width, jint height,
                                         jint pixelFormat, jfloatArray jdata)
{
    FloatImage* image = reinterpret_cast<FloatImage*>(jimage);
    jfloat* data = env->GetFloatArrayElements(jdata, 0);
    image->update(env, width, height, jdata, pixelFormat);
    env->ReleaseFloatArrayElements(jdata, data, 0);
}

}
