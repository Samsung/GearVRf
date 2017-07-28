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

VertexBoneData::VertexBoneData()
: bones(),
  boneMatrices()
{
}

void VertexBoneData::setBones(std::vector<Bone*>&& bonesVec) {
    bones = std::move(bonesVec);

    boneMatrices.clear();
    boneMatrices.resize(bones.size());

    if (bones.empty())
        return;

    auto itMat = boneMatrices.begin();
    for (auto it = bones.begin(); it != bones.end(); ++it, ++itMat) {
        (*it)->setFinalTransformMatrixPtr(&*itMat);
    }
}

} // namespace gvr
