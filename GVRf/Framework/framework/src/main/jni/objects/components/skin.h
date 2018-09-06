#ifndef SKIN_H_
#define SKIN_H_

#include <vector>
#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "objects/components/component.h"
#include "objects/components/skeleton.h"

namespace gvr {
class Renderer;
class Shader;
class UniformBlock;

class Skin : public Component
{
public:
    Skin(Skeleton& skel) ;
    virtual ~Skin();

    static long long getComponentType()
    {
        return COMPONENT_TYPE_SKIN;
    }

    int getNumBones() const { return mBoneMap.size(); }

    void setBoneMap(const int* bonemap, int numBones);

    void bindBuffer(Renderer* renderer, Shader* shader);
    bool updateGPU(Renderer* renderer, Shader* shader);

private:
    Skin(const Skin& sksel) = delete;
    Skin(Skin&& s) = delete;
    Skin& operator=(const Skin& s) = delete;
    Skin& operator=(Skin&& s) = delete;

private:
    Skeleton& mSkeleton;
    std::vector<int> mBoneMap;
    UniformBlock* mBonesBuffer;
};

}
#endif
