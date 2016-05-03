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
    Java_org_gearvrf_NativeMesh_ctor(JNIEnv* env, jobject obj);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getVertices(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVertices(JNIEnv * env,
            jobject obj, jlong jmesh, jfloatArray vertices);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getNormals(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setNormals(JNIEnv * env,
            jobject obj, jlong jmesh, jfloatArray normals);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getTexCoords(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setTexCoords(JNIEnv * env,
            jobject obj, jlong jmesh, jfloatArray tex_coords);
    JNIEXPORT jcharArray JNICALL
    Java_org_gearvrf_NativeMesh_getTriangles(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setTriangles(JNIEnv * env,
            jobject obj, jlong jmesh, jcharArray triangles);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getFloatVector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key);

    JNIEXPORT jcharArray JNICALL
    Java_org_gearvrf_NativeMesh_getIndices(JNIEnv * env,
            jobject obj, jlong jmesh);
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setIndices(JNIEnv * env,
            jobject obj, jlong jmesh, jcharArray indices);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setFloatVector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key, jfloatArray float_vector);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getVec2Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVec2Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key, jfloatArray vec2_vector);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getVec3Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVec3Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key, jfloatArray vec3_vector);
    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeMesh_getVec4Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setVec4Vector(JNIEnv * env,
            jobject obj, jlong jmesh, jstring key, jfloatArray vec4_vector);
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeMesh_getBoundingBox(JNIEnv * env,
            jobject obj, jlong jmesh);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_setBones(JNIEnv * env,
            jobject obj, jlong jmesh, jlongArray jBonePtrArray);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeMesh_getSphereBound(JNIEnv * env,
            jobject obj, jlong jmesh, jfloatArray jsphere);

    JNIEXPORT jobjectArray JNICALL
    Java_org_gearvrf_NativeMesh_getAttribNames(JNIEnv * env,
            jobject obj, jlong jmesh);
};

JNIEXPORT jobjectArray JNICALL
Java_org_gearvrf_NativeMesh_getAttribNames(JNIEnv * env,
        jobject obj, jlong jmesh)
{
   jobjectArray ret;
   int i=0;
   std::set<std::string> attrib_names;
   Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
   mesh->getAttribNames(attrib_names);
   ret= (jobjectArray)env->NewObjectArray(attrib_names.size(),
        env->FindClass("java/lang/String"),
        env->NewStringUTF(""));
   for(auto it :attrib_names) {
       env->SetObjectArrayElement(
       ret,i++,env->NewStringUTF(it.c_str()));
   }
   return(ret);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeMesh_ctor(JNIEnv* env, jobject obj) {
    return reinterpret_cast<jlong>(new Mesh());
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getVertices(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const std::vector<glm::vec3>& vertices = mesh->vertices();
    jfloatArray jvertices = env->NewFloatArray(vertices.size() * 3);
    env->SetFloatArrayRegion(jvertices, 0, vertices.size() * 3,
            reinterpret_cast<const jfloat*>(vertices.data()));
    return jvertices;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setVertices(JNIEnv * env,
        jobject obj, jlong jmesh, jfloatArray vertices) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jvertices_pointer = env->GetFloatArrayElements(vertices, 0);
    glm::vec3* vertices_pointer =
            reinterpret_cast<glm::vec3*>(jvertices_pointer);
    int vertices_length = static_cast<int>(env->GetArrayLength(vertices))
            / (sizeof(glm::vec3) / sizeof(jfloat));
    std::vector<glm::vec3> native_vertices;
    for (int i = 0; i < vertices_length; ++i) {
        native_vertices.push_back(vertices_pointer[i]);
    }
    mesh->set_vertices(native_vertices);
    env->ReleaseFloatArrayElements(vertices, jvertices_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getNormals(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const std::vector<glm::vec3>& normals = mesh->normals();
    jfloatArray jnormals = env->NewFloatArray(normals.size() * 3);
    env->SetFloatArrayRegion(jnormals, 0, normals.size() * 3,
            reinterpret_cast<const jfloat*>(normals.data()));
    return jnormals;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setNormals(JNIEnv * env,
        jobject obj, jlong jmesh, jfloatArray normals) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jnormals_pointer = env->GetFloatArrayElements(normals, 0);
    glm::vec3* normals_pointer = reinterpret_cast<glm::vec3*>(jnormals_pointer);
    int normals_length = static_cast<int>(env->GetArrayLength(normals))
            / (sizeof(glm::vec3) / sizeof(jfloat));
    std::vector<glm::vec3> native_normals;
    for (int i = 0; i < normals_length; ++i) {
        native_normals.push_back(normals_pointer[i]);
    }
    mesh->set_normals(native_normals);
    env->ReleaseFloatArrayElements(normals, jnormals_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getTexCoords(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const std::vector<glm::vec2>& uvs = mesh->tex_coords();
    jfloatArray juvs = env->NewFloatArray(uvs.size() * 2);
    env->SetFloatArrayRegion(juvs, 0, uvs.size() * 2,
            reinterpret_cast<const jfloat*>(uvs.data()));
    return juvs;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setTexCoords(JNIEnv * env,
        jobject obj, jlong jmesh, jfloatArray tex_coords) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jtex_coords_pointer = env->GetFloatArrayElements(tex_coords, 0);
    glm::vec2* tex_coords_pointer =
            reinterpret_cast<glm::vec2*>(jtex_coords_pointer);
    int tex_coords_length = static_cast<int>(env->GetArrayLength(tex_coords))
            / (sizeof(glm::vec2) / sizeof(jfloat));
    std::vector<glm::vec2> native_tex_coords;
    for (int i = 0; i < tex_coords_length; ++i) {
        native_tex_coords.push_back(tex_coords_pointer[i]);
    }
    mesh->set_tex_coords(native_tex_coords);
    env->ReleaseFloatArrayElements(tex_coords, jtex_coords_pointer, 0);
}

JNIEXPORT jcharArray JNICALL
Java_org_gearvrf_NativeMesh_getTriangles(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const std::vector<unsigned short>& triangles = mesh->triangles();
    jcharArray jtriangles = env->NewCharArray(triangles.size());
    env->SetCharArrayRegion(jtriangles, 0, triangles.size(), triangles.data());
    return jtriangles;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setTriangles(JNIEnv * env,
        jobject obj, jlong jmesh, jcharArray triangles) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jchar* jtriangles_pointer = env->GetCharArrayElements(triangles, 0);
    unsigned short* triangles_pointer =
            static_cast<unsigned short*>(jtriangles_pointer);
    int triangles_length = env->GetArrayLength(triangles);
    std::vector<unsigned short> native_triangles;
    for (int i = 0; i < triangles_length; ++i) {
        native_triangles.push_back(triangles_pointer[i]);
    }
    mesh->set_triangles(native_triangles);
    env->ReleaseCharArrayElements(triangles, jtriangles_pointer, 0);
}

JNIEXPORT jcharArray JNICALL
Java_org_gearvrf_NativeMesh_getIndices(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const std::vector<unsigned short>& indices = mesh->indices();
    jcharArray jindices = env->NewCharArray(indices.size());
    env->SetCharArrayRegion(jindices, 0, indices.size(), indices.data());
    return jindices;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setIndices(JNIEnv * env,
        jobject obj, jlong jmesh, jcharArray indices) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jchar* jindices_pointer = env->GetCharArrayElements(indices, 0);
    unsigned short* indices_pointer =
            static_cast<unsigned short*>(jindices_pointer);
    int indices_length = env->GetArrayLength(indices);
    std::vector<unsigned short> native_indices;
    for (int i = 0; i < indices_length; ++i) {
        native_indices.push_back(indices_pointer[i]);
    }
    mesh->set_indices(native_indices);
    env->ReleaseCharArrayElements(indices, jindices_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getFloatVector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    const std::vector<float>& float_vector = mesh->getFloatVector(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    jfloatArray jfloat_vector = env->NewFloatArray(float_vector.size());
    env->SetFloatArrayRegion(jfloat_vector, 0, float_vector.size(),
            reinterpret_cast<const jfloat*>(float_vector.data()));
    return jfloat_vector;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setFloatVector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key, jfloatArray float_vector) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jfloat_vector_pointer = env->GetFloatArrayElements(float_vector, 0);
    float* float_vector_pointer =
            reinterpret_cast<float*>(jfloat_vector_pointer);
    int float_vector_length =
            static_cast<int>(env->GetArrayLength(float_vector));
    std::vector<float> native_float_vector;
    for (int i = 0; i < float_vector_length; ++i) {
        native_float_vector.push_back(float_vector_pointer[i]);
    }
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    mesh->setFloatVector(native_key, native_float_vector);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(float_vector, jfloat_vector_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getVec2Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    const std::vector<glm::vec2>& vec2_vector = mesh->getVec2Vector(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    jfloatArray jvec2_vector = env->NewFloatArray(vec2_vector.size() * 2);
    env->SetFloatArrayRegion(jvec2_vector, 0, vec2_vector.size() * 2,
            reinterpret_cast<const jfloat*>(vec2_vector.data()));
    return jvec2_vector;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setVec2Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key, jfloatArray vec2_vector) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jvec2_vector_pointer = env->GetFloatArrayElements(vec2_vector, 0);
    glm::vec2* vec2_vector_pointer =
            reinterpret_cast<glm::vec2*>(jvec2_vector_pointer);
    int vec2_vector_length = static_cast<int>(env->GetArrayLength(vec2_vector))
            / (sizeof(glm::vec2) / sizeof(jfloat));
    std::vector<glm::vec2> native_vec2_vector;
    for (int i = 0; i < vec2_vector_length; ++i) {
        native_vec2_vector.push_back(vec2_vector_pointer[i]);
    }
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    mesh->setVec2Vector(native_key, native_vec2_vector);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(vec2_vector, jvec2_vector_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getVec3Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    const std::vector<glm::vec3>& vec3_vector = mesh->getVec3Vector(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    jfloatArray jvec3_vector = env->NewFloatArray(vec3_vector.size() * 3);
    env->SetFloatArrayRegion(jvec3_vector, 0, vec3_vector.size() * 3,
            reinterpret_cast<const jfloat*>(vec3_vector.data()));
    return jvec3_vector;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setVec3Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key, jfloatArray vec3_vector) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jvec3_vector_pointer = env->GetFloatArrayElements(vec3_vector, 0);
    glm::vec3* vec3_vector_pointer =
            reinterpret_cast<glm::vec3*>(jvec3_vector_pointer);
    int vec3_vector_length = static_cast<int>(env->GetArrayLength(vec3_vector))
            / (sizeof(glm::vec3) / sizeof(jfloat));
    std::vector<glm::vec3> native_vec3_vector;
    for (int i = 0; i < vec3_vector_length; ++i) {
        native_vec3_vector.push_back(vec3_vector_pointer[i]);
    }
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    mesh->setVec3Vector(native_key, native_vec3_vector);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(vec3_vector, jvec3_vector_pointer, 0);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeMesh_getVec4Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    const std::vector<glm::vec4>& vec4_vector = mesh->getVec4Vector(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    jfloatArray jvec4_vector = env->NewFloatArray(vec4_vector.size() * 4);
    env->SetFloatArrayRegion(jvec4_vector, 0, vec4_vector.size() * 4,
            reinterpret_cast<const jfloat*>(vec4_vector.data()));
    return jvec4_vector;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setVec4Vector(JNIEnv * env,
        jobject obj, jlong jmesh, jstring key, jfloatArray vec4_vector) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    jfloat* jvec4_vector_pointer = env->GetFloatArrayElements(vec4_vector, 0);
    glm::vec4* vec4_vector_pointer =
            reinterpret_cast<glm::vec4*>(jvec4_vector_pointer);
    int vec4_vector_length = static_cast<int>(env->GetArrayLength(vec4_vector))
            / (sizeof(glm::vec4) / sizeof(jfloat));
    std::vector<glm::vec4> native_vec4_vector;
    for (int i = 0; i < vec4_vector_length; ++i) {
        native_vec4_vector.push_back(vec4_vector_pointer[i]);
    }
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    mesh->setVec4Vector(native_key, native_vec4_vector);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(vec4_vector, jvec4_vector_pointer, 0);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeMesh_getBoundingBox(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(mesh->createBoundingBox());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeMesh_setBones(JNIEnv * env, jobject obj, jlong jmesh,
        jlongArray jBonePtrArray) {
	Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
	int arrlen;
	if (!jBonePtrArray || !(arrlen = env->GetArrayLength(jBonePtrArray))) {
	    mesh->setBones(std::vector<Bone*>());
	    return;
	}

	jlong* bonesPtr = env->GetLongArrayElements(jBonePtrArray, JNI_FALSE);
	std::vector<Bone*> bonesVec(arrlen);
	for (int i = 0; i < arrlen; ++i) {
	    bonesVec[i] = reinterpret_cast<Bone*>(bonesPtr[i]);
	}
	mesh->setBones(std::move(bonesVec));

	env->ReleaseLongArrayElements(jBonePtrArray, bonesPtr, JNI_ABORT);
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
