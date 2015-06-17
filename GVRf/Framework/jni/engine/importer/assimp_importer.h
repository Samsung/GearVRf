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
 * Contains a ai_scene of Assimp.
 ***************************************************************************/

#ifndef ASSIMP_SCENE_H_
#define ASSIMP_SCENE_H_

#include <android/bitmap.h>
#include <memory>
#include <vector>
#include <string>
#include <map>

#include "objects/components/perspective_camera.h"
#include "objects/components/camera_rig.h"
#include "objects/textures/base_texture.h"
#include "objects/components/transform.h"
#include "objects/components/component.h"
#include "objects/components/camera.h"
#include "objects/hybrid_object.h"
#include "objects/scene_object.h"
#include "assimp/Importer.hpp"
#include "objects/material.h"
#include "assimp/material.h"
#include "objects/scene.h"
#include "assimp/scene.h"
#include "util/gvr_log.h"
#include "glm/glm.hpp"
#include "assimp/cimport.h"

namespace gvr {
class Mesh;

class AssimpImporter: public HybridObject {
public:
    AssimpImporter(Assimp::Importer* assimp_importer) :
            assimp_importer_(assimp_importer) {
    }

    ~AssimpImporter() {
        delete assimp_importer_;
    }

    unsigned int getNumberOfMeshes() {
        if (assimp_importer_->GetScene() != 0) {
            return assimp_importer_->GetScene()->mNumMeshes;
        }
        LOGE("_ASSIMP_SCENE_NOT_FOUND_");
        return 0;
    }

    Mesh* getMesh(int index);
    const aiScene* getAssimpScene() {
        return assimp_importer_->GetScene();
    }

    jobject mesh_material(JNIEnv *env, int index);

    static bool create_instance(JNIEnv *env, const char* class_name,
            jobject& new_instance);
    static bool create_instance(JNIEnv *env, const char* class_name,
            const char* signature,/* const*/jvalue* params,
            jobject& new_instance);
    static bool get_field(JNIEnv *env, jobject object, const char* field_name,
            const char* signature, jobject& field);
    static bool set_int_field(JNIEnv *env, jobject object,
            const char* field_name, jint value);
    static bool set_float_field(JNIEnv *env, jobject object,
            const char* field_name, jfloat value);
    static bool set_object_field(JNIEnv *env, jobject object,
            const char* field_name, const char* signature, jobject value);
    static bool get_static_field(JNIEnv *env, const char* class_name,
            const char* field_name, const char* signature, jobject& field);
    static bool call(JNIEnv *env, jobject object, const char* type_name,
            const char* method_name, const char* signature,/* const*/
            jvalue* params);
    static bool callv(JNIEnv *env, jobject object, const char* type_name,
            const char* method_name, const char* signature,/* const*/
            jvalue* params);
    static bool call_static_object(JNIEnv *env, const char* type_name,
            const char* method_name, const char* signature,/* const*/
            jvalue* params, jobject& return_value);
    static bool copy_buffer(JNIEnv *env, jobject mesh, const char* buffer_name,
            void* data, size_t size);
    static bool copy_buffer_array(JNIEnv *env, jobject mesh,
            const char* buffer_name, int index, void* data, size_t size);
    static bool load_meshes(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);
    static bool load_scene_node(JNIEnv *env, const aiNode *node, jobject parent,
            jobject* loaded_node = NULL);
    static bool load_scene_graph(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);
    static bool load_materials(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);
    static bool load_animations(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);
    static bool load_lights(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);
    static bool load_cameras(JNIEnv *env, const aiScene* assimp_scene,
            jobject& scene);

private:
    Assimp::Importer* assimp_importer_;
};
}
#endif
