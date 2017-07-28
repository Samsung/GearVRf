/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#include <algorithm>

#include "component.h"
#include "component.inl"
#include "bone.h"

#include "glm/gtc/matrix_inverse.hpp"

#include "util/gvr_log.h"

namespace gvr {
glm::mat4 Bone::identityMatrix_;

Bone::Bone()
  : Component(Bone::getComponentType())
  , name_()
  , offsetMatrix_()
  , finalTransformMatrixPtr_(nullptr)
{
}

Bone::~Bone() {
}

void Bone::setName(const char *name) {
	name_ = std::string(name);
}

}
