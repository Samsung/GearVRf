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

#include "jassimp.h"

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

private:
    Assimp::Importer* assimp_importer_;
};
}
#endif
