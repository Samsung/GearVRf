/***************************************************************************
 * JNI
 ***************************************************************************/

#include "objects/components/render_target.h"

#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr {
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_ctor(JNIEnv *env, jobject obj, jobject jtexture);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_getComponentType(JNIEnv *env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_setTexture(JNIEnv *env, jobject obj, jlong ptr, jobject texture);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_setCamera(JNIEnv *env, jobject obj, jlong ptr, jlong camera);
};


JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_ctor(JNIEnv *env, jobject obj, jobject jtexture)
{
    RenderTexture* texture = reinterpret_cast<RenderTexture*>(jtexture);
    return reinterpret_cast<jlong>(new RenderTarget(texture));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_setTexture(JNIEnv *env, jobject obj, jlong ptr, jobject jtexture)
{
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    RenderTexture* texture = reinterpret_cast<RenderTexture*>(jtexture);
    target->setTexture(texture);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_setCamera(JNIEnv *env, jobject obj, jlong ptr, jlong jcamera)
{
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    target->setCamera(camera);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_getComponentType(JNIEnv * env, jobject obj)
{
    return RenderTarget::getComponentType();
}

}