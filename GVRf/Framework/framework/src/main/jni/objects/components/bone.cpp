/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#include <algorithm>

#include "objects/components/component.h"
#include "objects/components/bone.h"

#include "glm/gtc/matrix_inverse.hpp"

#include "util/gvr_log.h"

namespace gvr {
Bone::Bone()
  : Component(Bone::getComponentType())
  , name_()
  , boneWeights_()
  , offsetMatrix_()
  , finalTransformMatrixPtr_()
{
}

Bone::~Bone() {
}

void Bone::setName(const char *name) {
	name_ = std::string(name);
}

void Bone::setBoneWeights(std::vector<BoneWeight*>&& boneWeights) {
    boneWeights_ = std::move(boneWeights);
}

}
