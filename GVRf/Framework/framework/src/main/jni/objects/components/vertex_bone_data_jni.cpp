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
    Java_org_gearvrf_NativeVertexBoneData_get(JNIEnv* env, jclass clz, jlong mesh);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeVertexBoneData_get(JNIEnv * env, jclass clz, jlong jmesh) {
    Mesh *mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(&mesh->getVertexBoneData());
}


} // namespace gvr
