/***************************************************************************
 * Bone for skeletal animation.
 ***************************************************************************/

#ifndef BONE_WEIGHT_H_
#define BONE_WEIGHT_H_

#include "objects/components/component.h"

namespace gvr {
class BoneWeight: public Component {
public:
	BoneWeight();
    virtual ~BoneWeight();

    void setVertexId(int vertexId);
    void setWeight(float weight);
    int getVertexId();
    float getWeight();

    static long long getComponentType() {
        return (long long) & getComponentType;
    }

private:
    BoneWeight(const BoneWeight& boneWeight);
    BoneWeight(BoneWeight&& boneWeight);
    BoneWeight& operator=(const BoneWeight& boneWeight);
    BoneWeight& operator=(BoneWeight&& boneWeight);

private:
    int vertexId_;
    float weight_;
};

}
#endif
