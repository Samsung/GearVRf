/***************************************************************************
 * JNI
 ***************************************************************************/

#include "cubemap_texture.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCompressedCubemapTexture_compressedTextureArrayConstructor(JNIEnv * env,
        jobject obj, jint internalFormat, jint width, jint height, jint imageSize,
        jobjectArray textureArray, jintArray joffsetArray, jintArray jtexture_parameters);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCompressedCubemapTexture_compressedTextureArrayConstructor(JNIEnv * env,
    jobject obj, jint internalFormat, jint width, jint height, jint imageSize,
    jobjectArray textureArray, jintArray joffsetArray, jintArray jtexture_parameters) {
    if (env->GetArrayLength(textureArray) != 6) {
        std::string error =
        "new CubemapTexture() failed! Input texture list's length is not 6.";
        throw error;
    }
    if (env->GetArrayLength(joffsetArray) != 6) {
        std::string error =
        "new CubemapTexture() failed! Texture offset list's length is not 6.";
        throw error;
    }
    try {
        jint* texture_offsets = env->GetIntArrayElements(joffsetArray, 0);
        jint* texture_parameters = env->GetIntArrayElements(jtexture_parameters,0);
        jlong rv = reinterpret_cast<jlong>(new CubemapTexture(env,
                internalFormat, width, height, imageSize,
                textureArray, texture_offsets, texture_parameters));
        env->ReleaseIntArrayElements(jtexture_parameters, texture_parameters, 0);
        env->ReleaseIntArrayElements(joffsetArray, texture_offsets, 0);
        return rv;
    } catch (const std::string &err) {
        printJavaCallStack(env, err);
        throw err;
    }
}

}
