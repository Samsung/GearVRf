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
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeAssimpImporter_decomposeTransformationMatrix(JNIEnv * env,
        jobject obj, jfloatArray jtransformation_matrix);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getVKeysize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getQKeysize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getV3Dsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getfloatsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getintsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getuintsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getdoublesize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getlongsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jstring JNICALL
Java_org_util_jassimp_Jassimp_getErrorString(
        JNIEnv *env, jclass cls);
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

    if (!assimp_scene) {
        LOGE("import file returned null\n");
        goto error;
    }

    if (!AssimpImporter::create_instance(env, "org/util/jassimp/AiScene", jassimp_scene)) {
        goto error;
    }

    if (!AssimpImporter::load_scene_graph(env, assimp_scene, jassimp_scene)) {
        goto error;
    }

    /* jump over error handling section */
    goto end;

error:
    {
        jclass exception = env->FindClass("java/io/IOException");
        if (NULL == exception) {
            /* thats really a problem because we cannot throw in this case */
            env->FatalError("could not throw java.io.IOException");
        }
        env->ThrowNew(exception, aiGetErrorString());
    }

end:
    /*
     * NOTE: this releases all memory used in the native domain.
     * Ensure all data has been passed to java before!
     */
    aiReleaseImport(assimp_scene);
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
    return reinterpret_cast<jobject>(assimp_importer->mesh_material(env, current_node->mMeshes[index]));
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeAssimpImporter_decomposeTransformationMatrix(JNIEnv * env,
        jobject obj, jfloatArray jtransformation_matrix) {

    jfloat* tm = env->GetFloatArrayElements(jtransformation_matrix,0); // Transformation Matrix

    jfloatArray result;
    result = env->NewFloatArray(10);

    std::vector<float> data(10); // To store the decomposed matrix value

    aiMatrix4x4 transform(tm[0], tm[1], tm[2], tm[3],
                          tm[4], tm[5], tm[6], tm[7],
                          tm[8], tm[9], tm[10], tm[11],
                          tm[12], tm[13], tm[14], tm[15]);

    aiVector3t<float> scaling;
    aiQuaterniont<float> rotation;
    aiVector3t<float> position;
    transform.Decompose(scaling, rotation, position);

    // Scale factors
    data[0] = scaling.x;
    data[1] = scaling.y;
    data[2] = scaling.z;

    // Rotation factors
    data[3] = rotation.w;
    data[4] = rotation.x;
    data[5] = rotation.y;
    data[6] = rotation.z;

    // Position factors
    data[7] = position.x;
    data[8] = position.y;
    data[9] = position.z;

    env->SetFloatArrayRegion(result, 0, 10, &data[0]);
    return result;

}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getVKeysize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(aiVectorKey);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getQKeysize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(aiQuatKey);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getV3Dsize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(aiVector3D);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getfloatsize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(float);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getintsize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(int);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getuintsize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(unsigned int);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getdoublesize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(double);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_util_jassimp_Jassimp_getlongsize(JNIEnv *env,
        jclass cls) {
    const int res = sizeof(long);
    return res;
}

JNIEXPORT jstring JNICALL
Java_org_util_jassimp_Jassimp_getErrorString(
        JNIEnv *env, jclass cls) {
    const char *err = aiGetErrorString();

    if (NULL == err) {
        return env->NewStringUTF("");
    }

    return env->NewStringUTF(err);
}
}
