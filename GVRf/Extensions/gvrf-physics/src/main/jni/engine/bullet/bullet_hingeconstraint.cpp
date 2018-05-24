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

#include "bullet_hingeconstraint.h"
#include "bullet_rigidbody.h"
#include <BulletDynamics/ConstraintSolver/btHingeConstraint.h>

const char tag[] = "BulletHingeConstr";

namespace gvr {

    BulletHingeConstraint::BulletHingeConstraint(PhysicsRigidBody *rigidBodyA,
            PhysicsRigidBody *rigidBodyB, const float *pivotInA, const float *pivotInB,
            const float *axisInA, const float *axisInB)
    {
        btVector3 pInA(pivotInA[0], pivotInA[1], pivotInA[2]);
        btVector3 pInB(pivotInB[0], pivotInB[1], pivotInB[2]);
        btVector3 aInA(axisInA[0], axisInA[1], axisInA[2]);
        btVector3 aInB(axisInB[0], axisInB[1], axisInB[2]);
        btRigidBody *rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
        btRigidBody *rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

        mHingeConstraint = new btHingeConstraint(*rbA, *rbB, pInA, pInB, aInA, aInB);

        // By default angular limits are inactive
    }

    BulletHingeConstraint::BulletHingeConstraint(btHingeConstraint *constraint)
    {
        mHingeConstraint = constraint;
        constraint->setUserConstraintPtr(this);
    }

    BulletHingeConstraint::~BulletHingeConstraint()
    {
        delete mHingeConstraint;
    }

    void BulletHingeConstraint::setLimits(float lower, float upper)
    {
        mHingeConstraint->setLimit(lower, upper);
    }

    float BulletHingeConstraint::getLowerLimit() const
    {
        return mHingeConstraint->getLowerLimit();
    }

    float BulletHingeConstraint::getUpperLimit() const
    {
        return mHingeConstraint->getUpperLimit();
    }

    void BulletHingeConstraint::setBreakingImpulse(float impulse)
    {
        mHingeConstraint->setBreakingImpulseThreshold(impulse);
    }

    float BulletHingeConstraint::getBreakingImpulse() const
    {
        return mHingeConstraint->getBreakingImpulseThreshold();
    }
}