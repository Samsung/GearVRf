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
namespace gvr {
class Mesh;
class Scene;
class SceneObject;
class Material;
class PerspectiveCamera;
class Camera;
class CameraRig;
class RenderData;
class BaseTexture;

class AssimpImporter: public HybridObject {
public:
    AssimpImporter(Assimp::Importer* assimp_importer) :
            assimp_importer_(assimp_importer) {
    }

    ~AssimpImporter() {
        delete assimp_importer_;
    }

    unsigned int getNumberOfMeshes() {
        return assimp_importer_->GetScene()->mNumMeshes;
    }
    void scene_recursion(aiNode* assimp_node, const aiScene* assimp_scene, std::shared_ptr<Scene> 
gvr_scene_pointer, JNIEnv * env, jobject default_bitmap, jobject gvr_context, jmethodID 
method_ID, aiMatrix4x4 accumulated_transform);
    std::shared_ptr<Scene> load_scene(JNIEnv* env, jobject obj, jobject bitmap, jobject gvr_context);
    std::shared_ptr<Mesh> getMesh(int index);

private:
    Assimp::Importer* assimp_importer_;
};
}
#endif
