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

#include "bullet_rigidbody.h"
#include "bullet_sliderconstraint.h"
#include <BulletDynamics/ConstraintSolver/btSliderConstraint.h>
#include <LinearMath/btTransform.h>

static const char tag[] = "BulletSliderConstr";

namespace gvr {

    BulletSliderConstraint::BulletSliderConstraint(PhysicsRigidBody *rigidBodyA,
            PhysicsRigidBody *rigidBodyB, float const *rotationA, float const *rotationB)
    {
        btRigidBody* rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
        btRigidBody* rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

        btTransform frameInA(btMatrix3x3(rotationA[0], rotationA[1], rotationA[2], rotationA[3],
                rotationA[4], rotationA[5], rotationA[6], rotationA[7], rotationA[8]));

        btTransform frameInB(btMatrix3x3(rotationB[0], rotationB[1], rotationB[2], rotationB[3],
                rotationB[4], rotationB[5], rotationB[6], rotationB[7], rotationB[8]));

        mSliderConstraint = new btSliderConstraint(*rbA, *rbB, frameInA, frameInB, true);
    }

    BulletSliderConstraint::BulletSliderConstraint(btSliderConstraint *constraint)
    {
        mSliderConstraint = constraint;
        constraint->setUserConstraintPtr(this);
    }

    BulletSliderConstraint::~BulletSliderConstraint()
    {
        delete mSliderConstraint;
    }

    void BulletSliderConstraint::setAngularLowerLimit(float limit)
    {
        mSliderConstraint->setLowerAngLimit(limit);
    }

    float BulletSliderConstraint::getAngularLowerLimit() const
    {
        return mSliderConstraint->getLowerAngLimit();
    }

    void BulletSliderConstraint::setAngularUpperLimit(float limit)
    {
        mSliderConstraint->setUpperAngLimit(limit);
    }

    float BulletSliderConstraint::getAngularUpperLimit() const
    {
        return mSliderConstraint->getUpperAngLimit();
    }

    void BulletSliderConstraint::setLinearLowerLimit(float limit)
    {
        mSliderConstraint->setLowerLinLimit(limit);
    }

    float BulletSliderConstraint::getLinearLowerLimit() const
    {
        return mSliderConstraint->getLowerLinLimit();
    }

    void BulletSliderConstraint::setLinearUpperLimit(float limit)
    {
        mSliderConstraint->setUpperLinLimit(limit);
    }

    void BulletSliderConstraint::setBreakingImpulse(float impulse)
    {
        mSliderConstraint->setBreakingImpulseThreshold(impulse);
    }

    float BulletSliderConstraint::getBreakingImpulse() const
    {
        return mSliderConstraint->getBreakingImpulseThreshold();
    }

    float BulletSliderConstraint::getLinearUpperLimit() const
    {
        return mSliderConstraint->getUpperLinLimit();
    }
}