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

#include "assimp_importer.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeAssimpImporter_getNumberOfMeshes(
        JNIEnv * env, jobject obj, jlong jassimp_importer);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeAssimpImporter_getMesh(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jint index);
JNIEXPORT jobject JNICALL
Java_org_gearvrf_NativeAssimpImporter_getAssimpScene(JNIEnv * env,
        jobject obj, jlong jassimp_importer);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeAssimpImporter_getNodeMesh(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jstring jnode_name, jint index);
JNIEXPORT jobject JNICALL
Java_org_gearvrf_NativeAssimpImporter_getMeshMaterial(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jstring jnode_name, jint index);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeAssimpImporter_getNumberOfMeshes(
        JNIEnv * env, jobject obj, jlong jassimp_importer) {
    AssimpImporter* assimp_importer =
            reinterpret_cast<AssimpImporter*>(jassimp_importer);
    return assimp_importer->getNumberOfMeshes();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeAssimpImporter_getMesh(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jint index) {
    AssimpImporter* assimp_importer =
            reinterpret_cast<AssimpImporter*>(jassimp_importer);
    return reinterpret_cast<jlong>(assimp_importer->getMesh(index));
}

JNIEXPORT jobject JNICALL
Java_org_gearvrf_NativeAssimpImporter_getAssimpScene(
        JNIEnv * env, jobject obj, jlong jassimp_importer) {
    AssimpImporter* assimp_importer =
            reinterpret_cast<AssimpImporter*>(jassimp_importer);
    jobject jassimp_scene = NULL;
    const aiScene *assimp_scene = assimp_importer->getAssimpScene();
    create_instance(env, "org/gearvrf/jassimp/AiScene", jassimp_scene);
    load_scene_graph(env, assimp_scene, jassimp_scene);
    return reinterpret_cast<jobject>(jassimp_scene);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeAssimpImporter_getNodeMesh(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jstring jnode_name, jint index) {
    AssimpImporter* assimp_importer =
            reinterpret_cast<AssimpImporter*>(jassimp_importer);
    const char *node_name = env->GetStringUTFChars(jnode_name, 0);
    aiNode* root_node = assimp_importer->getAssimpScene()->mRootNode;
    aiNode* current_node = root_node->FindNode(node_name);
    env->ReleaseStringUTFChars(jnode_name, node_name);
    return reinterpret_cast<jlong>(assimp_importer->getMesh(current_node->mMeshes[index]));

}

JNIEXPORT jobject JNICALL
Java_org_gearvrf_NativeAssimpImporter_getMeshMaterial(JNIEnv * env,
        jobject obj, jlong jassimp_importer, jstring jnode_name, jint index) {
    AssimpImporter* assimp_importer =
            reinterpret_cast<AssimpImporter*>(jassimp_importer);
    const char *node_name = env->GetStringUTFChars(jnode_name, 0);
    aiNode* root_node = assimp_importer->getAssimpScene()->mRootNode;
    aiNode* current_node = root_node->FindNode(node_name);
    env->ReleaseStringUTFChars(jnode_name, node_name);
    const aiScene *assimp_scene = assimp_importer->getAssimpScene();
    return reinterpret_cast<jobject>(mesh_material(env, assimp_scene, current_node->mMeshes[index]));
}
}
