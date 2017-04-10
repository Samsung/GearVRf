/***************************************************************************
 * JNI
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/components/bone.h"
#include "objects/mesh.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeVertexBoneData_get(JNIEnv * env, jclass clz, jlong mesh);

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeVertexBoneData_getFreeBoneSlot(JNIEnv * env, jclass clz, jlong ptr,
        jint vertexId);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeVertexBoneData_setVertexBoneWeight(JNIEnv * env, jclass clz, jlong ptr,
        jint vertexId, jint boneSlot, jint boneId, jfloat boneWeight);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeVertexBoneData_normalizeWeights(JNIEnv * env, jclass clz, jlong ptr);

} // extern "C"
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeVertexBoneData_get(JNIEnv * env, jclass clz, jlong jmesh) {
    Mesh *mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(&mesh->getVertexBoneData());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeVertexBoneData_getFreeBoneSlot(JNIEnv * env, jclass clz, jlong ptr,
        jint vertexId) {
    VertexBoneData *boneData = reinterpret_cast<VertexBoneData*>(ptr);
    return boneData->getFreeBoneSlot(vertexId);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeVertexBoneData_setVertexBoneWeight(JNIEnv * env, jclass clz, jlong ptr,
        jint vertexId, jint boneSlot, jint boneId, jfloat boneWeight) {
    VertexBoneData *boneData = reinterpret_cast<VertexBoneData*>(ptr);
    boneData->setVertexBoneWeight(vertexId, boneSlot, boneId, boneWeight);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeVertexBoneData_normalizeWeights(JNIEnv * env, jclass clz, jlong ptr) {
    VertexBoneData *boneData = reinterpret_cast<VertexBoneData*>(ptr);
    boneData->normalizeWeights();
}

} // namespace gvr
