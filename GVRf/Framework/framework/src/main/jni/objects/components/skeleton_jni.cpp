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
    Java_org_gearvrf_animation_NativeSkeleton_ctor(JNIEnv* env, jobject obj, jintArray boneparents);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_getComponentType(JNIEnv* env, jobject clz);

    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_setPose(JNIEnv* env, jobject clz,
                                            jlong jskel, jfloatArray jmatrices);
    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_animation_NativeSkeleton_setSkinPose(JNIEnv* env, jobject clz,
                                            jlong jskel, jfloatArray jmatrices);
} // extern "C"


JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeSkeleton_ctor(JNIEnv * env, jobject clz, jintArray jboneparents)
{
    jint numbones = env->GetArrayLength(jboneparents);
    jint* boneParents = env->GetIntArrayElements(jboneparents, JNI_FALSE);
    Skeleton* skel = new Skeleton(boneParents, numbones);
    env->ReleaseIntArrayElements(jboneparents, boneParents, JNI_ABORT);
    return reinterpret_cast<jlong>(skel);
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

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_animation_NativeSkeleton_setSkinPose(JNIEnv* env, jobject clz,
                                                  jlong jskel, jfloatArray jmatrices)
{
    Skeleton* skel = reinterpret_cast<Skeleton*>(jskel);
    int n = env->GetArrayLength(jmatrices);

    if (skel->getNumBones() != (n / 16))
    {
        return false;
    }
    jfloat* inputMatrices = env->GetFloatArrayElements(jmatrices, JNI_FALSE);

    skel->setSkinPose(inputMatrices);
    env->ReleaseFloatArrayElements(jmatrices, inputMatrices, JNI_ABORT);
    return true;
}

} // namespace gvr
