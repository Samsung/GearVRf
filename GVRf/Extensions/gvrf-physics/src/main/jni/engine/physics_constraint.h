/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef EXTENSIONS_PHYSICS_CONSTRAINT_H
#define EXTENSIONS_PHYSICS_CONSTRAINT_H

#include "../objects/scene_object.h"

namespace gvr {

    struct JointFeedback {
        float torqueA[3] = {0.0f, 0.0f, 0.0f};
        float forceA[3] = {0.0f, 0.0f, 0.0f};
        float torqueB[3] = {0.0f, 0.0f, 0.0f};
        float forceB[3] = {0.0f, 0.0f, 0.0f};

    };

    class PhysicsConstraint : public Component {
    public:
        PhysicsConstraint() : Component(PhysicsConstraint::getComponentType()){}

        virtual ~PhysicsConstraint() {}

        static long long getComponentType() {
            return COMPONENT_TYPE_PHYSICS_CONSTRAINT;
        }

        virtual int getConstraintType() const = 0;

    //virtual float getAppliedImpulse() const = 0;
        //virtual float getBreakingImpulseThreshold() const = 0;
        //virtual void setBreakingImpulseThreshold(float n) = 0;
    //virtual void getJointFeedback(JointFeedback* feedback) = 0;
    //virtual void setJointFeedback(JointFeedback const * feedback) = 0;
        virtual void *getUnderlying() = 0;

        virtual void setBreakingImpulse(float impulse) = 0;
        virtual float getBreakingImpulse() const = 0;
        virtual void updateConstructionInfo() = 0;

        enum ConstraintType {
            fixedConstraint = 1,
            point2pointConstraint = 2,
            sliderConstraint = 3,
            hingeConstraint = 4,
            coneTwistConstraint = 5,
            genericConstraint = 6,
        };
    };

}
#endif //EXTENSIONS_PHYSICS_CONSTRAINT_H
