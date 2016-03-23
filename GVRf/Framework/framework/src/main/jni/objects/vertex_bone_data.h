
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
class Mesh;
class VertexBoneData {
public:
    VertexBoneData(Mesh *mesh);
    void setBones(std::vector<Bone*>&& bonesVec);

    int getNumBones() const {
        return bones.size();
    }

    glm::mat4 getFinalBoneTransform(int boneId) {
        return boneMatrices[boneId];
    }

    void setFinalBoneTransform(int boneId, glm::mat4 &transform) {
        boneMatrices[boneId] = transform;
    }

    int getFreeBoneSlot(int vertexId);
    void setVertexBoneWeight(int vertexId, int boneSlot, int boneId, float boneWeight);
    void normalizeWeights();

    struct BoneData {
        uint32_t ids[BONES_PER_VERTEX];
        float weights[BONES_PER_VERTEX];

        BoneData() {
            reset();
        }

        void reset() {
            for(int i = 0; i < BONES_PER_VERTEX; i++) {
                ids[i] = 0;
                weights[i]=0;
            }
        }

        int getFreeBoneSlot() {
            for (int i = 0; i < sizeof(ids) / sizeof(ids[0]); i++) {
                if (weights[i] == 0.0) {
                    return i;
                }
            }
            return -1;
        }
    } __attribute__((packed, aligned(4)));

public:
    std::vector<glm::mat4>  boneMatrices;
    std::vector<BoneData>   boneData;

private:
    Mesh *mesh;

    // Static bone data loaded from model
    std::vector<Bone*> bones;
};

} // namespace gvr
#endif
