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

static const char tag[] = "BulletSliderConstrN";

namespace gvr {

    BulletSliderConstraint::BulletSliderConstraint(PhysicsRigidBody *rigidBodyB) {
        mRigidBodyB = reinterpret_cast<BulletRigidBody*>(rigidBodyB);
        mSliderConstraint = 0;

        mBreakingImpulse = SIMD_INFINITY;

        // Default values from btSliderConstraint
        mLowerAngularLimit = 0.0f;
        mUpperAngularLimit = 0.0f;
        mLowerLinearLimit = 1.0f;
        mUpperLinearLimit = -1.0f;
    }

    BulletSliderConstraint::BulletSliderConstraint(btSliderConstraint *constraint)
    {
        mSliderConstraint = constraint;
        mRigidBodyB = static_cast<BulletRigidBody*>(constraint->getRigidBodyB().getUserPointer());
        constraint->setUserConstraintPtr(this);
    }

    BulletSliderConstraint::~BulletSliderConstraint() {
        if (0 != mSliderConstraint) {
            delete mSliderConstraint;
        }
    }

    void BulletSliderConstraint::setAngularLowerLimit(float limit) {
        if (0 != mSliderConstraint) {
            mSliderConstraint->setLowerAngLimit(limit);
        }
        else {
            mLowerAngularLimit = limit;
        }
    }

    float BulletSliderConstraint::getAngularLowerLimit() const {
        if (0 != mSliderConstraint) {
            return mSliderConstraint->getLowerAngLimit();
        }
        else {
            return mLowerAngularLimit;
        }
    }

    void BulletSliderConstraint::setAngularUpperLimit(float limit) {
        if (0 != mSliderConstraint) {
            mSliderConstraint->setUpperAngLimit(limit);
        }
        else {
            mUpperAngularLimit = limit;
        }
    }

    float BulletSliderConstraint::getAngularUpperLimit() const {
        if (0 != mSliderConstraint) {
            return mSliderConstraint->getUpperAngLimit();
        }
        else {
            return mUpperAngularLimit;
        }
    }

    void BulletSliderConstraint::setLinearLowerLimit(float limit) {
        if (0 != mSliderConstraint) {
            mSliderConstraint->setLowerLinLimit(limit);
        }
        else {
            mLowerLinearLimit = limit;
        }
    }

    float BulletSliderConstraint::getLinearLowerLimit() const {
        if (0 != mSliderConstraint) {
            return mSliderConstraint->getLowerLinLimit();
        }
        else {
            return mLowerLinearLimit;
        }
    }

    void BulletSliderConstraint::setLinearUpperLimit(float limit) {
        if (0 != mSliderConstraint) {
            mSliderConstraint->setUpperLinLimit(limit);
        }
        else {
            mUpperLinearLimit = limit;
        }
    }

    void BulletSliderConstraint::setBreakingImpulse(float impulse) {
        if (0 != mSliderConstraint) {
            mSliderConstraint->setBreakingImpulseThreshold(impulse);
        }
        else {
            mBreakingImpulse = impulse;
        }
    }

    float BulletSliderConstraint::getBreakingImpulse() const {
        if (0 != mSliderConstraint) {
            return mSliderConstraint->getBreakingImpulseThreshold();
        }
        else {
            return mBreakingImpulse;
        }
    }

    float BulletSliderConstraint::getLinearUpperLimit() const {
        if (0 != mSliderConstraint) {
            return mSliderConstraint->getUpperLinLimit();
        }
        else {
            return mUpperLinearLimit;
        }
    }

void BulletSliderConstraint::updateConstructionInfo() {
    if (mSliderConstraint != nullptr) {
        return;
    }

    btRigidBody* rbA = ((BulletRigidBody*)this->owner_object()->getComponent(COMPONENT_TYPE_PHYSICS_RIGID_BODY))->getRigidBody();

    btTransform frameInA, frameInB;
    frameInA = btTransform::getIdentity();
    frameInB = btTransform::getIdentity();

    mSliderConstraint = new btSliderConstraint(*rbA, *mRigidBodyB->getRigidBody(), frameInA,
                                               frameInB, true);

    mSliderConstraint->setLowerAngLimit(mLowerAngularLimit);
    mSliderConstraint->setUpperAngLimit(mUpperAngularLimit);
    mSliderConstraint->setLowerLinLimit(mLowerLinearLimit);
    mSliderConstraint->setUpperLinLimit(mUpperLinearLimit);
    mSliderConstraint->setBreakingImpulseThreshold(mBreakingImpulse);
}

}