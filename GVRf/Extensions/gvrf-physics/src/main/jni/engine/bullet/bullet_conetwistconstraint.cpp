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

const char tag[] = "BulletConeTwistConstrN";

namespace gvr {
    BulletConeTwistConstraint::BulletConeTwistConstraint(PhysicsRigidBody *rigidBodyB,
                                                         PhysicsVec3 pivot,
                                                         PhysicsMat3x3 const &bodyRotation,
                                                         PhysicsMat3x3 const &coneRotation) {
        mConeTwistConstraint = 0;
        mRigidBodyB = reinterpret_cast<BulletRigidBody*>(rigidBodyB);

        mBreakingImpulse = SIMD_INFINITY;
        mPivot = pivot;
        mBodyRotation = bodyRotation;
        mConeRotation = coneRotation;
        mSwingLimit = SIMD_PI * 0.25f;
        mTwistLimit = SIMD_PI;
    }

    BulletConeTwistConstraint::BulletConeTwistConstraint(btConeTwistConstraint *constraint)
    {
        mConeTwistConstraint = constraint;
        mRigidBodyB = static_cast<BulletRigidBody*>(constraint->getRigidBodyB().getUserPointer());
        constraint->setUserConstraintPtr(this);
    }

    BulletConeTwistConstraint::~BulletConeTwistConstraint() {
        if (0 != mConeTwistConstraint) {
            delete mConeTwistConstraint;
        }
    }

    void BulletConeTwistConstraint::setSwingLimit(float limit) {
        if (0 != mConeTwistConstraint) {
            mConeTwistConstraint->setLimit(4, limit);
            mConeTwistConstraint->setLimit(5, limit);
        }
        else {
            mSwingLimit = limit;
        }
    }

    float BulletConeTwistConstraint::getSwingLimit() const {
        if (0 != mConeTwistConstraint) {
            return mConeTwistConstraint->getLimit(4);
        }
        else {
            return mSwingLimit;
        }
    }

    void BulletConeTwistConstraint::setTwistLimit(float limit) {
        if (0 != mConeTwistConstraint) {
            mConeTwistConstraint->setLimit(3, limit);
        }
        else {
            mTwistLimit = limit;
        }
    }

    float BulletConeTwistConstraint::getTwistLimit() const {
        if (0 != mConeTwistConstraint) {
            return mConeTwistConstraint->getLimit(3);
        }
        else {
            return mTwistLimit;
        }
    }

    void BulletConeTwistConstraint::setBreakingImpulse(float impulse) {
        if (0 != mConeTwistConstraint) {
            mConeTwistConstraint->setBreakingImpulseThreshold(impulse);
        }
        else {
            mBreakingImpulse = impulse;
        }
    }

    float BulletConeTwistConstraint::getBreakingImpulse() const {
        if (0 != mConeTwistConstraint) {
            return mConeTwistConstraint->getBreakingImpulseThreshold();
        }
        else {
            return mBreakingImpulse;
        }
    }

void BulletConeTwistConstraint::updateConstructionInfo() {
    if (mConeTwistConstraint != nullptr) {
        return;
    }

    btRigidBody *rbA = reinterpret_cast<BulletRigidBody*>(owner_object()
            ->getComponent(COMPONENT_TYPE_PHYSICS_RIGID_BODY))->getRigidBody();

    // Original pivot is relative to body A (the one that swings)
    btVector3 p(mPivot.x, mPivot.y, mPivot.z);

    btMatrix3x3 m(mBodyRotation.vec[0], mBodyRotation.vec[1], mBodyRotation.vec[2],
                  mBodyRotation.vec[3], mBodyRotation.vec[4], mBodyRotation.vec[5],
                  mBodyRotation.vec[6], mBodyRotation.vec[7], mBodyRotation.vec[8]);
    btTransform fA(m, p);

    m.setValue(mConeRotation.vec[0], mConeRotation.vec[1], mConeRotation.vec[2],
               mConeRotation.vec[3], mConeRotation.vec[4], mConeRotation.vec[5],
               mConeRotation.vec[6], mConeRotation.vec[7], mConeRotation.vec[8]);

    // Pivot for body B must be calculated
    p = rbA->getWorldTransform().getOrigin() + p;
    p -= mRigidBodyB->getRigidBody()->getWorldTransform().getOrigin();
    btTransform fB(m, p);

    mConeTwistConstraint = new btConeTwistConstraint(*rbA, *mRigidBodyB->getRigidBody(), fA, fB);
    mConeTwistConstraint->setLimit(mSwingLimit, mSwingLimit, mTwistLimit);
    mConeTwistConstraint->setBreakingImpulseThreshold(mBreakingImpulse);
}
}