/***************************************************************************
 * JNI
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/components/bone.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBone_ctor(JNIEnv * env, jobject obj);

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBone_getComponentType(JNIEnv * env, jobject clz);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setName(JNIEnv * env, jobject clz, jlong ptr,
        jstring name);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setBoneWeights(JNIEnv * env, jobject clz, jlong ptr,
        jlongArray jArrayBoneWeights);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setOffsetMatrix(JNIEnv * env, jobject clz, jlong ptr,
        jfloatArray jOffsetMatrix);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeBone_getOffsetMatrix(JNIEnv * env, jobject clz, jlong ptr);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setFinalTransformMatrix(JNIEnv * env, jobject clz, jlong ptr,
        jfloatArray jOffsetMatrix);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeBone_getFinalTransformMatrix(JNIEnv * env, jobject clz, jlong ptr);

} // extern "C"
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBone_ctor(JNIEnv * env, jobject clz) {
    return reinterpret_cast<jlong>(new Bone());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBone_getComponentType(JNIEnv * env, jobject clz) {
    return Bone::getComponentType();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setName(JNIEnv * env, jobject clz, jlong ptr,
        jstring name) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);
    if (!name || !env->GetStringLength(name)) {
        bone->setName("");
        return;
    }

    const char* charName = env->GetStringUTFChars(name, NULL);
    bone->setName(charName);
    env->ReleaseStringUTFChars(name, charName);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setBoneWeights(JNIEnv * env, jobject clz, jlong ptr,
        jlongArray jArrayBoneWeights) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);
    int arrlen;
    if (!jArrayBoneWeights || !(arrlen = env->GetArrayLength(jArrayBoneWeights))) {
        bone->setBoneWeights(std::vector<BoneWeight*>());
        return;
    }

    jlong* ptr_arr = env->GetLongArrayElements(jArrayBoneWeights, JNI_FALSE);
    std::vector<BoneWeight*> ptr_vec(arrlen);
    for (int i = 0; i < arrlen; ++i)
        ptr_vec[i] = reinterpret_cast<BoneWeight*>(ptr_arr[i]);
    bone->setBoneWeights(std::move(ptr_vec));
    env->ReleaseLongArrayElements(jArrayBoneWeights, ptr_arr, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setOffsetMatrix(JNIEnv * env, jobject clz, jlong ptr,
        jfloatArray jOffsetMatrix) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);
    if (!jOffsetMatrix)
        return;

    jfloat* mat_arr = env->GetFloatArrayElements(jOffsetMatrix, JNI_FALSE);
    glm::mat4 matrix = glm::make_mat4x4(mat_arr);
    bone->setOffsetMatrix(matrix);
    env->ReleaseFloatArrayElements(jOffsetMatrix, mat_arr, JNI_ABORT);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeBone_getOffsetMatrix(JNIEnv * env, jobject clz, jlong ptr) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);

    glm::mat4 matrix = bone->getOffsetMatrix();
    jsize size = sizeof(matrix) / sizeof(jfloat);
    if (size != 16) {
        LOGE("sizeof(matrix) / sizeof(jfloat) != 16");
        throw "sizeof(matrix) / sizeof(jfloat) != 16";
    }
    jfloatArray jmatrix = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jmatrix, 0, size, glm::value_ptr(matrix));

    return jmatrix;
}


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBone_setFinalTransformMatrix(JNIEnv * env, jobject clz, jlong ptr,
        jfloatArray jTransform) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);
    if (!jTransform)
        return;

    jfloat* mat_arr = env->GetFloatArrayElements(jTransform, JNI_FALSE);
    glm::mat4 matrix(glm::make_mat4x4(mat_arr));
    bone->setFinalTransformMatrix(matrix);
    env->ReleaseFloatArrayElements(jTransform, mat_arr, JNI_ABORT);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeBone_getFinalTransformMatrix(JNIEnv * env, jobject clz, jlong ptr) {
    Bone* bone = reinterpret_cast<Bone*>(ptr);

    glm::mat4 matrix = bone->getFinalTransformMatrix();
    jsize size = sizeof(matrix) / sizeof(jfloat);
    if (size != 16) {
        LOGE("sizeof(matrix) / sizeof(jfloat) != 16");
        throw "sizeof(matrix) / sizeof(jfloat) != 16";
    }
    jfloatArray jmatrix = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jmatrix, 0, size, glm::value_ptr(matrix));

    return jmatrix;
}

} // namespace gvr
