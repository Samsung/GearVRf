/***************************************************************************
 * JNI
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "skin.h"
#include "skeleton.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeSkin_ctor(JNIEnv* env, jobject obj, jobject jskeleton);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeSkin_getComponentType(JNIEnv* env, jobject clz);

    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_animation_NativeSkin_setBoneMap(JNIEnv* env, jobject clz,
                                            jlong jskin, jintArray jboneMap);

} // extern "C"


JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeSkin_ctor(JNIEnv * env, jobject clz, jobject jskeleton)
{
    Skeleton* skel = reinterpret_cast<Skeleton*>(jskeleton);
    return reinterpret_cast<jlong>(new Skin(*skel));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeSkin_getComponentType(JNIEnv * env, jobject clz)
{
    return Skin::getComponentType();
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_animation_NativeSkin_setBoneMap(JNIEnv* env, jobject clz,
                                        jlong jskin, jintArray jboneMap)
{
    Skin* skin = reinterpret_cast<Skin*>(jskin);
    int n = env->GetArrayLength(jboneMap);
    jint* boneMap = env->GetIntArrayElements(jboneMap, JNI_FALSE);

    skin->setBoneMap(boneMap, n);
    env->ReleaseIntArrayElements(jboneMap, boneMap, JNI_ABORT);
    return true;
}

} // namespace gvr
