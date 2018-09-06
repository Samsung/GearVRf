
#include "objects/components/skeleton.h"
#include "objects/components/component.inl"
#include "engine/renderer/renderer.h"

#define MAX_BONES 60

namespace gvr {
    Skeleton::Skeleton(int numbones)
       :  Component(COMPONENT_TYPE_SKELETON),
          mNumBones(numbones)
    {
        mMatrixData = new glm::mat4[numbones];
    }

    Skeleton::~Skeleton()
    {
        delete[] mMatrixData;
    };

    void Skeleton::setPose(const float* input)
    {
        memcpy(mMatrixData, input, mNumBones * sizeof(glm::mat4));
    }

    glm::mat4* Skeleton::getBoneMatrix(int boneId)
    {
        if ((boneId < 0) || (boneId > getNumBones()))
        {
            return nullptr;
        }
        return &mMatrixData[boneId];
    }

}
