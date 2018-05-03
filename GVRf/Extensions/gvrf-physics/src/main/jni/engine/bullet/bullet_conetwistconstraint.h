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
// Created by c.bozzetto on 06/06/2017.
//

#ifndef EXTENSIONS_BULLET_CONETWISTCONSTRAINT_H
#define EXTENSIONS_BULLET_CONETWISTCONSTRAINT_H

#include "../physics_common.h"
#include "../physics_conetwistconstraint.h"
#include "bullet_object.h"

class btConeTwistConstraint;
namespace gvr {

    class PhysicsRigidBody;
    class BulletRigidBody;

    class BulletConeTwistConstraint : public PhysicsConeTwistConstraint, BulletObject {
    public:
        explicit BulletConeTwistConstraint(PhysicsRigidBody *rigidBodyB, PhysicsVec3 pivot,
                                           PhysicsMat3x3 const &bodyRotation,
                                           PhysicsMat3x3 const &coneRotation);

        BulletConeTwistConstraint(btConeTwistConstraint *constraint);

        virtual ~BulletConeTwistConstraint();

        void setSwingLimit(float limit);

        float getSwingLimit() const;

        void setTwistLimit(float limit);

        float getTwistLimit() const;

        void* getUnderlying() {
            return this->mConeTwistConstraint;
        }

        void setBreakingImpulse(float impulse);

        float getBreakingImpulse() const;

        void updateConstructionInfo();
    private:

        btConeTwistConstraint *mConeTwistConstraint;
        BulletRigidBody *mRigidBodyB;

        float mBreakingImpulse;
        PhysicsVec3 mPivot;
        PhysicsMat3x3 mBodyRotation;
        PhysicsMat3x3 mConeRotation;

        float mSwingLimit;
        float mTwistLimit;
    };

}

#endif //EXTENSIONS_BULLET_CONETWISTCONSTRAINT_H
