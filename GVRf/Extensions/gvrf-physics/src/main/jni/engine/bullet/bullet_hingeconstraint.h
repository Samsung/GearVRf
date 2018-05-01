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
// Created by c.bozzetto on 30/05/2017.
//

#ifndef EXTENSIONS_BULLET_HINGECONSTRAINT_H
#define EXTENSIONS_BULLET_HINGECONSTRAINT_H

#include "../physics_hingeconstraint.h"
#include "bullet_object.h"

class btHingeConstraint;

namespace gvr {

    class PhysicsRigidBody;
    class BulletRigidBody;
    class BulletHingeConstraint : public PhysicsHingeConstraint,
                                  BulletObject {

    public:
        explicit BulletHingeConstraint(PhysicsRigidBody *rigidBodyB, float const pivotInA[],
                                       float const pivotInB[], float const axisInA[],
                                       float const axisInB[]);

        BulletHingeConstraint(btHingeConstraint *constraint);

        virtual ~BulletHingeConstraint();

        void setLimits(float lower, float upper);

        float getLowerLimit() const;

        float getUpperLimit() const;

        void *getUnderlying() { return mHingeConstraint; }

        void setBreakingImpulse(float impulse);

        float getBreakingImpulse() const;

        void updateConstructionInfo();

    private:
        btHingeConstraint *mHingeConstraint;
        BulletRigidBody *mRigidBodyB;

        float mBreakingImpulse;
        float mTempLower;
        float mTempUpper;

        PhysicsVec3 mPivotInA;
        PhysicsVec3 mPivotInB;
        PhysicsVec3 mAxisInA;
        PhysicsVec3 mAxisInB;
    };
}
#endif //EXTENSIONS_BULLET_HINGECONSTRAINT_H
