/***************************************************************************
 * Holds dynamic data for the bones
 ***************************************************************************/

#include <math.h>
#include "scene.h"
#include "objects/vertex_bone_data.h"
#include "objects/components/bone.h"
#include "util/gvr_log.h"

#define TOL 1e-6

namespace gvr {

VertexBoneData::VertexBoneData(Mesh *mesh)
: mesh(mesh)
, bones()
, boneMatrices()
, boneData()
{
}

void VertexBoneData::setBones(std::vector<Bone*>&& bonesVec) {
    bones = std::move(bonesVec);

    boneMatrices.clear();
    boneMatrices.resize(bones.size());

    if (bones.empty())
        return;

    int vertexNum(mesh->vertices().size());
    boneData.clear();
    boneData.resize(vertexNum);

    auto itMat = boneMatrices.begin();
    for (auto it = bones.begin(); it != bones.end(); ++it, ++itMat) {
        (*it)->setFinalTransformMatrixPtr(&*itMat);
    }
}

int VertexBoneData::getFreeBoneSlot(int vertexId) {
    int vertexNum(mesh->vertices().size());
    if (vertexId < 0 || vertexId > vertexNum) {
        LOGD("Bad vertex id %d vertices %d", vertexId, vertexNum);
        return -1;
    }

    return boneData[vertexId].getFreeBoneSlot();
}

#define unlikely(x) __builtin_expect(!!(x), 0)
void VertexBoneData::setVertexBoneWeight(int vertexId, int boneSlot, int boneId, float boneWeight) {
    if (unlikely(BONES_PER_VERTEX <= boneSlot || 0 > boneSlot)) {
        FAIL("index out of bounds; boneSlot: %d", boneSlot);
    }
    if (unlikely(MAX_BONES <= boneId || 0 > boneId)) {
        FAIL("index out of bounds; boneId: %d", boneId);
    }
    if (unlikely(boneData.size() <= vertexId)) {
        FAIL("index out of bounds; vertexId: %d", vertexId);
    }

    BoneData& boneDataElement = boneData[vertexId];
    boneDataElement.ids[boneSlot] = boneId;
    boneDataElement.weights[boneSlot] = boneWeight;
}

void VertexBoneData::normalizeWeights() {
    if (bones.empty())
        return;

    int size = mesh->vertices().size();
    for (int i = 0; i < size; ++i) {
        float wtSum = 0.f;
        for (int j = 0; j < BONES_PER_VERTEX; ++j) {
            wtSum += boneData[i].weights[j];
        }
        if (fabs(wtSum) > TOL) {
            for (int j = 0; j < BONES_PER_VERTEX; ++j) {
                boneData[i].weights[j] /= wtSum;
            }
        }
    }
}

} // namespace gvr
