/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#ifndef BONE_H_
#define BONE_H_

#include <string>
#include <vector>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"

#include "objects/components/component.h"
#include "objects/components/bone_weight.h"

#include "util/gvr_log.h"

namespace gvr {
class Bone: public Component {
public:
    Bone();
    virtual ~Bone();

    void setName(const char *name);
    void setBoneWeights(std::vector<BoneWeight*> &&boneWeights);

    void setOffsetMatrix(glm::mat4 &mat) {
        offsetMatrix_ = mat;
    }

    glm::mat4 getOffsetMatrix() {
        return offsetMatrix_;
    }

    void setFinalTransformMatrixPtr(glm::mat4 *ptr) {
        finalTransformMatrixPtr_ = ptr;
    }

    void setFinalTransformMatrix(glm::mat4 &mat) {
        *finalTransformMatrixPtr_ = mat;
    }

    glm::mat4 &getFinalTransformMatrix() {
        return *finalTransformMatrixPtr_;
    }

    static long long getComponentType() {
        return (long long) & getComponentType;
    }

private:
    Bone(const Bone& bone);
    Bone(Bone&& bone);
    Bone& operator=(const Bone& bone);
    Bone& operator=(Bone&& bone);

private:
    std::string name_;
    std::vector<BoneWeight*> boneWeights_;
    glm::mat4 offsetMatrix_;
    glm::mat4 *finalTransformMatrixPtr_;
};

}
#endif
