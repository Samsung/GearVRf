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

#include "bullet_conetwistconstraint.h"
#include "bullet_rigidbody.h"

#include <BulletDynamics/ConstraintSolver/btConeTwistConstraint.h>
#include <LinearMath/btScalar.h>

const char tag[] = "BulletConeTwistConstr";

namespace gvr {

    BulletConeTwistConstraint::BulletConeTwistConstraint(PhysicsRigidBody *rigidBodyA,
            PhysicsRigidBody *rigidBodyB, PhysicsVec3 pivot, PhysicsMat3x3 const &bodyRotation,
            PhysicsMat3x3 const &coneRotation)
    {
        btRigidBody *rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
        btRigidBody *rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

        // Original pivot is relative to body A (the one that swings)
        btVector3 p(pivot.x, pivot.y, pivot.z);

        btMatrix3x3 m(bodyRotation.vec[0], bodyRotation.vec[1], bodyRotation.vec[2],
                bodyRotation.vec[3], bodyRotation.vec[4], bodyRotation.vec[5],
                bodyRotation.vec[6], bodyRotation.vec[7], bodyRotation.vec[8]);
        btTransform fA(m, p);

        m.setValue(coneRotation.vec[0], coneRotation.vec[1], coneRotation.vec[2],
                coneRotation.vec[3], coneRotation.vec[4], coneRotation.vec[5],
                coneRotation.vec[6], coneRotation.vec[7], coneRotation.vec[8]);

        // Pivot for body B will be fixed in updateConstructionInfo()
        btTransform fB(m, p);

        mConeTwistConstraint = new btConeTwistConstraint(*rbA, *rbB, fA, fB);
    }

    BulletConeTwistConstraint::BulletConeTwistConstraint(btConeTwistConstraint *constraint)
    {
        mConeTwistConstraint = constraint;
        constraint->setUserConstraintPtr(this);
    }

    BulletConeTwistConstraint::~BulletConeTwistConstraint()
    {
        delete mConeTwistConstraint;
    }

    void BulletConeTwistConstraint::setSwingLimit(float limit)
    {
        mConeTwistConstraint->setLimit(4, limit);
        mConeTwistConstraint->setLimit(5, limit);
    }

    float BulletConeTwistConstraint::getSwingLimit() const
    {
        return mConeTwistConstraint->getLimit(4);
    }

    void BulletConeTwistConstraint::setTwistLimit(float limit)
    {
        mConeTwistConstraint->setLimit(3, limit);
    }

    float BulletConeTwistConstraint::getTwistLimit() const
    {
        return mConeTwistConstraint->getLimit(3);
    }

    void BulletConeTwistConstraint::setBreakingImpulse(float impulse)
    {
        mConeTwistConstraint->setBreakingImpulseThreshold(impulse);
    }

    float BulletConeTwistConstraint::getBreakingImpulse() const
    {
        return mConeTwistConstraint->getBreakingImpulseThreshold();
    }

    void BulletConeTwistConstraint::updateConstructionInfo()
    {
        void *user = mConeTwistConstraint->getUserConstraintPtr();
        if (user != nullptr && user != (void*)-1)
        {
            // Do not update loaded constraint
            return;
        }

        // Will fix BFrame because rigid bodies might be updated when added to the world. This
        // happens because rigid bodies are added tho the world in physics thread while this
        // constraint was created in another thread
        btVector3 p(mConeTwistConstraint->getAFrame().getOrigin());
        p = mConeTwistConstraint->getRigidBodyA().getWorldTransform().getOrigin() + p;
        p -= mConeTwistConstraint->getRigidBodyB().getWorldTransform().getOrigin();
        btTransform fB(mConeTwistConstraint->getBFrame().getBasis(), p);
        mConeTwistConstraint->setFrames(mConeTwistConstraint->getAFrame(), fB);
    }

}