/***************************************************************************
 * JNI
 ***************************************************************************/

#include "bone_weight.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBoneWeight_ctor(JNIEnv * env, jobject clz);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBoneWeight_setVertexId(JNIEnv * env, jobject clz, jlong ptr,
        int vertexId);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBoneWeight_setWeight(JNIEnv * env, jobject clz, jlong ptr,
        jfloat weight);

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeBoneWeight_getVertexId(JNIEnv * env, jobject clz, jlong ptr);

JNIEXPORT float JNICALL
Java_org_gearvrf_NativeBoneWeight_getWeight(JNIEnv * env, jobject clz, jlong ptr);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBoneWeight_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new BoneWeight());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBoneWeight_setVertexId(JNIEnv * env, jobject clz, jlong ptr,
        int vertexId) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    boneWeight->setVertexId(vertexId);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBoneWeight_setWeight(JNIEnv * env, jobject clz, jlong ptr,
        jfloat weight) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    boneWeight->setWeight(weight);
}

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeBoneWeight_getVertexId(JNIEnv * env, jobject clz, jlong ptr,
        int vertexId) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    return boneWeight->getVertexId();
}

JNIEXPORT float JNICALL
Java_org_gearvrf_NativeBoneWeight_getWeight(JNIEnv * env, jobject clz, jlong ptr,
        jfloat weight) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    return boneWeight->getWeight();
}

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeBoneWeight_getVertexId(JNIEnv * env, jobject clz, jlong ptr) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    return boneWeight->getVertexId();
}

JNIEXPORT float JNICALL
Java_org_gearvrf_NativeBoneWeight_getWeight(JNIEnv * env, jobject clz, jlong ptr) {
    BoneWeight *boneWeight = reinterpret_cast<BoneWeight *>(ptr);
    return boneWeight->getWeight();
}

}
