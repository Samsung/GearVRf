/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#include "bone_weight.h"

#include "glm/gtc/matrix_inverse.hpp"

#include "util/gvr_log.h"

namespace gvr {
BoneWeight::BoneWeight()
: Component(BoneWeight::getComponentType())
, vertexId_(0)
, weight_(0)
{
}

BoneWeight::~BoneWeight() {
}

void BoneWeight::setVertexId(int vertexId) {
    vertexId_ = vertexId;
}

void BoneWeight::setWeight(float weight) {
    weight_ = weight;
}

int BoneWeight::getVertexId() {
    return vertexId_;
}

float BoneWeight::getWeight() {
    return weight_;
}

}
