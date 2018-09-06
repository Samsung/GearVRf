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

#include "mesh.h"

#include "util/gvr_log.h"
#include "util/gvr_jni.h"
#include "android/asset_manager_jni.h"

namespace gvr {
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMesh_ctorBuffers(JNIEnv* env, jobject obj, jlong vbuf, jlong ibuf);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVertexBuffer(JNIEnv* env,
                                                jobject obj, jlong jmesh, jlong vertices);
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setIndexBuffer(JNIEnv* env,
                                               jobject obj, jlong jmesh, jlong indices);
};

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMesh_ctorBuffers(JNIEnv* env, jobject obj, jlong jvertices, jlong jindices)
    {
        VertexBuffer* vbuf = reinterpret_cast<VertexBuffer*>(jvertices);
        IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jindices);
        LOGD("Mesh::ctorBuffer vertices = %p, indices = %p", vbuf, ibuf);
        Mesh* mesh = new Mesh(*vbuf);
        if (ibuf)
        {
            mesh->setIndexBuffer(ibuf);
        }
        return reinterpret_cast<jlong>(mesh);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVertexBuffer(JNIEnv * env,
                                            jobject obj, jlong jmesh, jlong jverts)
    {
        Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
        VertexBuffer* vbuf = reinterpret_cast<VertexBuffer*>(jverts);
        mesh->setVertexBuffer(vbuf);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setIndexBuffer(JNIEnv * env,
                                                jobject obj, jlong jmesh, jlong jindices)
    {
        Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
        IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jindices);
        mesh->setIndexBuffer(ibuf);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_getSphereBound(JNIEnv * env,
                                               jobject obj, jlong jmesh, jfloatArray jsphere) {
        Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
        const BoundingVolume& bvol = mesh->getBoundingVolume();
        float   sphere[4];

        sphere[0] = bvol.center().x;
        sphere[1] = bvol.center().y;
        sphere[2] = bvol.center().z;
        sphere[3] = bvol.radius();
        env->SetFloatArrayRegion(jsphere, 0, 4, sphere);
    }


}