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

const char tag[] = "BulletHingeConstrN";

namespace gvr {

    BulletHingeConstraint::BulletHingeConstraint(PhysicsRigidBody *rigidBodyB, const float *pivotInA,
                                                 const float *pivotInB, const float *axisInA,
                                                 const float *axisInB) {
        mHingeConstraint = 0;
        mRigidBodyB = reinterpret_cast<BulletRigidBody*>(rigidBodyB);
        mBreakingImpulse = SIMD_INFINITY;
        mPivotInA.set(pivotInA);
        mPivotInB.set(pivotInB);
        mAxisInA.set(axisInA);
        mAxisInB.set(axisInB);

        // By default angular limit is inactive
        mTempLower = 2.0f;
        mTempUpper = 0.0f;
    }

    BulletHingeConstraint::BulletHingeConstraint(btHingeConstraint *constraint)
    {
        mHingeConstraint = constraint;
        mRigidBodyB = static_cast<BulletRigidBody*>(constraint->getRigidBodyB().getUserPointer());
        constraint->setUserConstraintPtr(this);
    }

    BulletHingeConstraint::~BulletHingeConstraint() {
        if (0 != mHingeConstraint) {
            delete mHingeConstraint;
        }
    }

    void BulletHingeConstraint::setLimits(float lower, float upper) {
        if (0 == mHingeConstraint) {
            mTempLower = lower;
            mTempUpper = upper;
        }
        else {
            mHingeConstraint->setLimit(lower, upper);
        }
    }

    float BulletHingeConstraint::getLowerLimit() const {
        if (0 == mHingeConstraint) {
            return mTempLower;
        }
        else {
            return mHingeConstraint->getLowerLimit();
        }
    }

    float BulletHingeConstraint::getUpperLimit() const {
        if (0 == mHingeConstraint) {
            return mTempUpper;
        }
        else {
            return mHingeConstraint->getUpperLimit();
        }
    }

    void BulletHingeConstraint::setBreakingImpulse(float impulse) {
        if (0 != mHingeConstraint) {
            mHingeConstraint->setBreakingImpulseThreshold(impulse);
        }
        else {
            mBreakingImpulse = impulse;
        }
    }

    float BulletHingeConstraint::getBreakingImpulse() const {
        if (0 != mHingeConstraint) {
            return mHingeConstraint->getBreakingImpulseThreshold();
        }
        else {
            return mBreakingImpulse;
        }
    }

    void BulletHingeConstraint::updateConstructionInfo() {
//        if (mHingeConstraint != 0) {
//            delete (mHingeConstraint);
//        }

        if (mHingeConstraint == nullptr)
        {
            btVector3 pivotInA(mPivotInA.x, mPivotInA.y, mPivotInA.z);
            btVector3 pivotInB(mPivotInB.x, mPivotInB.y, mPivotInB.z);
            btVector3 axisInA(mAxisInA.x, mAxisInA.y, mAxisInA.z);
            btVector3 axisInB(mAxisInB.x, mAxisInB.y, mAxisInB.z);
            btRigidBody *rbA = ((BulletRigidBody *) owner_object()->getComponent(
                    COMPONENT_TYPE_PHYSICS_RIGID_BODY))->getRigidBody();

            mHingeConstraint = new btHingeConstraint(*rbA, *mRigidBodyB->getRigidBody(), pivotInA,
                                                     pivotInB, axisInA, axisInB);
            mHingeConstraint->setLimit(mTempLower, mTempUpper);
            mHingeConstraint->setBreakingImpulseThreshold(mBreakingImpulse);
        }
    }
}