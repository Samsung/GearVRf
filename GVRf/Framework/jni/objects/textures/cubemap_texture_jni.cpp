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

#include "cubemap_texture.h"
#include "png_loader.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"

#include <png.h>

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCubemapTexture_bitmapListConstructor(JNIEnv * env,
		jobject obj, jobjectArray bitmapArray);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCubemapTexture_bitmapListConstructor(JNIEnv * env,
	jobject obj, jobjectArray bitmapArray) {
try {
	jlong res_texture = reinterpret_cast<jlong>(new std::shared_ptr<
			CubemapTexture>(new CubemapTexture(env, bitmapArray)));
	return res_texture;
} catch (const std::string &err) {
	printJavaCallStack(env, err);
	throw err;
}
}

}
