
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
        mBoneNames.reserve(numbones);
        mBoneNames.resize(numbones);
        memcpy(mBoneParents, boneparents, numbones * sizeof(int));
    }

    Skeleton::~Skeleton()
    {
        delete[] mSkinMatrices;
        delete[] mBoneMatrices;
        delete[] mBoneParents;
    };

    void Skeleton::setBoneName(int boneIndex, const char* boneName)
    {
        if ((boneIndex >= 0) && (boneIndex < getNumBones()))
        {
            mBoneNames[boneIndex] = boneName;
        }
    }

    const char* Skeleton::getBoneName(int boneIndex) const
    {
        if ((boneIndex < 0) || (boneIndex >= mBoneNames.size()))
        {
            return nullptr;
        }
        return mBoneNames[boneIndex].c_str();
    }

    const int* Skeleton::getBoneParents() const
    {
        return mBoneParents;
    }

    int Skeleton::getBoneParent(int boneIndex) const
    {
        if ((boneIndex < 0) || (boneIndex >= mBoneNames.size()))
        {
            return -1;
        }
        return mBoneParents[boneIndex];
    }

    void Skeleton::setPose(const float* input)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(mBoneMatrices, input, mNumBones * sizeof(glm::mat4));
    }

    void Skeleton::getPose(float* output)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(output, mBoneMatrices, mNumBones * sizeof(glm::mat4));
    }

    void Skeleton::setSkinPose(const float* input)
    {
        std::lock_guard<std::mutex> lock(mLock);
        memcpy(mSkinMatrices, input, mNumBones * sizeof(glm::mat4));
    }

    const glm::mat4* Skeleton::getSkinMatrix(int boneId) const
    {
        if ((boneId < 0) || (boneId > getNumBones()))
        {
            return nullptr;
        }
        return &mSkinMatrices[boneId];
    }
}
