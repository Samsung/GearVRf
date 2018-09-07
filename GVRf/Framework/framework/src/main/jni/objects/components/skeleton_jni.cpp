/***************************************************************************
 * JNI
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "objects/components/skeleton.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_ctor(JNIEnv* env, jobject obj, int numbones);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_getComponentType(JNIEnv* env, jobject clz);

    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_setPose(JNIEnv* env, jobject clz,
                                            jlong jskel, jfloatArray jmatrices);

} // extern "C"


JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeSkeleton_ctor(JNIEnv * env, jobject clz, int numbones)
{
    return reinterpret_cast<jlong>(new Skeleton(numbones));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeSkeleton_getComponentType(JNIEnv * env, jobject clz)
{
    return Skeleton::getComponentType();
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_animation_NativeSkeleton_setPose(JNIEnv* env, jobject clz,
                                        jlong jskel, jfloatArray jmatrices)
{
    Skeleton* skel = reinterpret_cast<Skeleton*>(jskel);
    int n = env->GetArrayLength(jmatrices);

    if (skel->getNumBones() != (n / 16))
    {
        return false;
    }
    jfloat* inputMatrices = env->GetFloatArrayElements(jmatrices, JNI_FALSE);

    skel->setPose(inputMatrices);
    env->ReleaseFloatArrayElements(jmatrices, inputMatrices, JNI_ABORT);
    return true;
}

} // namespace gvr
