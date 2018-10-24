
#include "skin.h"
#include "skeleton.h"
#include "component.inl"
#include "engine/renderer/renderer.h"

namespace gvr
{
    Skin::Skin(Skeleton& skel)
    : Component(COMPONENT_TYPE_SKIN),
       mSkeleton(skel),
       mBonesBuffer(nullptr)
    { }

    Skin::~Skin()
    {
        if (mBonesBuffer)
        {
            delete mBonesBuffer;
        }
    };

    void Skin::setBoneMap(const int* bonemap, int numBones)
    {
        mBoneMap.resize(numBones);
        for (int i = 0; i < numBones; ++i)
        {
            mBoneMap.at(i) = bonemap[i];
        }
    }

    void Skin::bindBuffer(Renderer* renderer, Shader* shader)
    {
        if (mBonesBuffer)
        {
            mBonesBuffer->bindBuffer(shader, renderer);
        }
    }

    bool Skin::updateGPU(Renderer* renderer, Shader* shader)
    {
        int numBones = mBoneMap.size();

        if (numBones == 0)
        {
            return false;
        }
        if (mBonesBuffer == NULL)
        {
            mBonesBuffer = renderer->createUniformBlock("mat4 u_bone_matrix", BONES_UBO_INDEX,
                                                        "Bones_ubo", numBones);
            mBonesBuffer->setNumElems(numBones);
        }
        for (int i = 0; i < numBones; ++i)
        {
            int boneId = mBoneMap.at(i);
            glm::mat4* boneMatrix = mSkeleton.getSkinMatrix(boneId);
            mBonesBuffer->setRange(i, boneMatrix, 1);
        }
        mBonesBuffer->updateGPU(renderer);
        return true;
    }

}
