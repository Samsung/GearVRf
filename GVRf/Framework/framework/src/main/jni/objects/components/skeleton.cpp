
#include "objects/components/skeleton.h"
#include "objects/components/component.inl"
#include "engine/renderer/renderer.h"

#define MAX_BONES 60

namespace gvr {
    Skeleton::Skeleton(int* boneparents, int numbones)
       :  Component(COMPONENT_TYPE_SKELETON),
          mNumBones(numbones)
    {
        mSkinMatrices = new glm::mat4[numbones];
        mBoneMatrices = new glm::mat4[numbones];
        mBoneParents = new int[numbones];
        memcpy(mBoneParents, boneparents, numbones * sizeof(int));
    }

    Skeleton::~Skeleton()
    {
        delete[] mSkinMatrices;
        delete[] mBoneMatrices;
        delete[] mBoneParents;
    };

    void Skeleton::setPose(const float* input)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(mBoneMatrices, input, mNumBones * sizeof(glm::mat4));
    }

    void Skeleton::setSkinPose(const float* input)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(mSkinMatrices, input, mNumBones * sizeof(glm::mat4));
    }

    glm::mat4* Skeleton::getSkinMatrix(int boneId)
    {
        if ((boneId < 0) || (boneId > getNumBones()))
        {
            return nullptr;
        }
        return &mSkinMatrices[boneId];
    }

    void Skeleton::getBoneMatrices(glm::mat4* matrixData)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(matrixData, mBoneMatrices, sizeof(glm::mat4) * getNumBones());
    }
}
