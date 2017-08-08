/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/***************************************************************************
 * JNI
 ***************************************************************************/

#include "mesh_collider.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMeshCollider_ctorMesh(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMeshCollider_ctorMeshPicking(JNIEnv * env,
             jobject obj, jlong jmesh, jboolean pickCoordinates);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMeshCollider_ctor(JNIEnv * env,
            jobject obj, jboolean useBounds);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMeshCollider_setMesh(JNIEnv * env,
            jobject obj, jlong jmesh_collider, jlong jmesh);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeMeshCollider_ctorMesh(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(new MeshCollider(mesh));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeMeshCollider_ctorMeshPicking(JNIEnv * env,
                                             jobject obj, jlong jmesh, jboolean pickCoordinates) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(new MeshCollider(mesh, pickCoordinates));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMeshCollider_setMesh(JNIEnv * env,
        jobject obj, jlong jmesh_collider, jlong jmesh) {
    MeshCollider* meshcollider = reinterpret_cast<MeshCollider*>(jmesh_collider);
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    meshcollider->set_mesh(mesh);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeMeshCollider_ctor(JNIEnv * env,
        jobject obj, jboolean useBounds) {
    return reinterpret_cast<jlong>(new MeshCollider(useBounds));
}
}
