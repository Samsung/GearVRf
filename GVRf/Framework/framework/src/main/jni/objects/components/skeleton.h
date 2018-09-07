/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#ifndef SKELETON_H_
#define SKELETON_H_

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "objects/components/component.h"

namespace gvr {
class Renderer;
class Shader;

class Skeleton : public Component
{
public:
    Skeleton(int numbones);

    virtual ~Skeleton();

    static long long getComponentType()
    {
        return COMPONENT_TYPE_SKELETON;
    }

    int getNumBones() const { return mNumBones; }

    void setPose(const float* input);
    glm::mat4* getBoneMatrix(int boneId);

private:
    Skeleton(const Skeleton& skel) = delete;
    Skeleton(Skeleton&& skel) = delete;
    Skeleton& operator=(const Skeleton& skel) = delete;
    Skeleton& operator=(Skeleton&& skel) = delete;

private:
    int mNumBones;
    glm::mat4*  mMatrixData;
};

}
#endif
