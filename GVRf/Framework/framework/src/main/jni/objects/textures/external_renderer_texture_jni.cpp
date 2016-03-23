/***************************************************************************
 * JNI
 ***************************************************************************/

#include "external_renderer_texture.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_setData(JNIEnv * env,
        jobject obj, jlong ptr, jlong data);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_getData(JNIEnv * env,
        jobject obj, jlong ptr);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new ExternalRendererTexture());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_setData(JNIEnv * env,
        jobject obj, jlong ptr, jlong data) {
    reinterpret_cast<ExternalRendererTexture*>(ptr)->setData(data);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeExternalRendererTexture_getData(JNIEnv * env,
        jobject obj, jlong ptr) {
    return reinterpret_cast<ExternalRendererTexture*>(ptr)->getData();
}

}
