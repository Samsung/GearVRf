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

//
// Created by c.bozzetto on 31/05/2017.
//

#ifndef EXTENSIONS_BULLET_SLIDERCONSTRAINT_H
#define EXTENSIONS_BULLET_SLIDERCONSTRAINT_H

#include "../physics_sliderconstraint.h"
#include "bullet_object.h"

class btSliderConstraint;

namespace gvr {

    class PhysicsRigidBody;
    class BulletRigidBody;

    class BulletSliderConstraint : public PhysicsSliderConstraint,
                                          BulletObject {
    public:
        explicit BulletSliderConstraint(PhysicsRigidBody *rigidBodyB);

        BulletSliderConstraint(btSliderConstraint *constraint);

        virtual ~BulletSliderConstraint();

        void setAngularLowerLimit(float limit);

        float getAngularLowerLimit() const;

        void setAngularUpperLimit(float limit);

        float getAngularUpperLimit() const;

        void setLinearLowerLimit(float limit);

        float getLinearLowerLimit() const;

        void setLinearUpperLimit(float limit);

        float getLinearUpperLimit() const;

        void setBreakingImpulse(float impulse);

        float getBreakingImpulse() const;

        void *getUnderlying() { return mSliderConstraint; }

        void updateConstructionInfo();

    private:
        btSliderConstraint *mSliderConstraint;
        BulletRigidBody *mRigidBodyB;

        float mBreakingImpulse;
        float mLowerAngularLimit;
        float mUpperAngularLimit;
        float mLowerLinearLimit;
        float mUpperLinearLimit;
    };

}

#endif //EXTENSIONS_BULLET_SLIDERCONSTRAINT_H
