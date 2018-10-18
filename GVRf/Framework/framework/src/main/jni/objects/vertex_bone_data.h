
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
 * The vertex_bone_data for rendering.
 ***************************************************************************/

#ifndef VERTEX_BONE_DATA_H_
#define VERTEX_BONE_DATA_H_

#include <map>
#include <memory>
#include <vector>
#include <stdint.h>
#include <string>

#include "glm/glm.hpp"
#include "glm/geometric.hpp"
#include "util/gvr_log.h"

#define MAX_BONES 60
#define BONES_PER_VERTEX 4

namespace gvr {
class Bone;
class VertexBoneData final {
public:
    VertexBoneData();
    void setBones(std::vector<Bone*>&& bonesVec);

    int getNumBones() const {
        return bones.size();
    }

    glm::mat4 getFinalBoneTransform(int boneId) {
        return boneMatrices[boneId];
    }

    std::vector<glm::mat4>& getBoneMatrices() {
        return boneMatrices;
    }

    void setFinalBoneTransform(int boneId, glm::mat4 &transform) {
        boneMatrices[boneId] = transform;
    }

public:
    std::vector<glm::mat4>  boneMatrices;

private:
    // Static bone data loaded from model
    std::vector<Bone*> bones;
};

} // namespace gvr
#endif
