/***************************************************************************
 * Holds dynamic data for the bones
 ***************************************************************************/

#include <math.h>
#include "scene.h"
#include "objects/vertex_bone_data.h"
#include "objects/components/bone.h"

#define TOL 1e-6

namespace gvr {

VertexBoneData::VertexBoneData(Mesh *mesh)
: mesh(mesh)
, bones()
, boneMatrices()
, boneCounts()
, boneIndices()
, boneWeights()
{
}

void VertexBoneData::setBones(std::vector<Bone*>&& bonesVec) {
    bones = std::move(bonesVec);

    boneMatrices.clear();
    boneMatrices.resize(bones.size());

    if (bones.empty())
        return;

    int vertexNum(mesh->vertices().size());
    boneCounts.clear();
    boneCounts.resize(vertexNum);

    boneIndices.clear();
    boneIndices.resize(vertexNum);

    boneWeights.clear();
    boneWeights.resize(vertexNum);

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

    int res = boneCounts[vertexId];
    if (res < MAX_BONE_NUM_PER_VERTEX) {
        boneCounts[vertexId]++;
        return res;
    } else {
        return -1; // no free bone slot
    }
}

void VertexBoneData::setVertexBoneWeight(int vertexId, int boneSlot, int boneId, float boneWeight) {
    boneIndices[vertexId][boneSlot] = boneId;
    boneWeights[vertexId][boneSlot] = boneWeight;
}

void VertexBoneData::normalizeWeights() {
    int size = mesh->vertices().size();
    for (int i = 0; i < size; ++i) {
        float wtSum = 0.f;
        for (int j = 0; j < boneCounts[i]; ++j) {
            wtSum += boneWeights[i][j];
        }
        if (fabs(wtSum) > 1e-6) {
            for (int j = 0; j < boneCounts[i]; ++j) {
                boneWeights[i][j] /= wtSum;
            }
        }
    }
}

} // namespace gvr
