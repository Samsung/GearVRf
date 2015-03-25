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

#include <memory>

#include "assimp/Importer.hpp"
#include "assimp/scene.h"

#include "objects/hybrid_object.h"

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
        return assimp_importer_->GetScene()->mNumMeshes;
    }

    std::shared_ptr<Mesh> getMesh(int index);

private:
    Assimp::Importer* assimp_importer_;
};
}
#endif
