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

#include "util/gvr_log.h"

namespace gvr {
class Bone: public Component {
public:
    Bone();
    virtual ~Bone();

    void setName(const char *name);

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
        if (finalTransformMatrixPtr_)
        {
            return *finalTransformMatrixPtr_;
        }
        return identityMatrix_;
    }

    static long long getComponentType() {
        return COMPONENT_TYPE_BONE;
    }

private:
    Bone(const Bone& bone) = delete;
    Bone(Bone&& bone) = delete;
    Bone& operator=(const Bone& bone) = delete;
    Bone& operator=(Bone&& bone) = delete;

private:
    static glm::mat4 identityMatrix_;
    std::string name_;
    glm::mat4 offsetMatrix_;
    glm::mat4 *finalTransformMatrixPtr_;
};

}
#endif
