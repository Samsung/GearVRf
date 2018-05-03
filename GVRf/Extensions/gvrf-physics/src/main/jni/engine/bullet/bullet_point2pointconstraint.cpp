//
// Created by Juliana Figueira on 5/9/17.
//

#include "bullet_point2pointconstraint.h"
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
#include "bullet_rigidbody.h"
#include "bullet_world.h"

static const char tag[] = "BulletP2pConstrN";

namespace gvr {

    BulletPoint2PointConstraint::BulletPoint2PointConstraint(PhysicsRigidBody* rigidBodyB,
            float pivotInA[], float pivotInB[]) {
        mPoint2PointConstraint = 0;
        mRigidBodyB = reinterpret_cast<BulletRigidBody*>(rigidBodyB);
        mBreakingImpulse = SIMD_INFINITY;
        mPivotInA.set(pivotInA);
        mPivotInB.set(pivotInB);
    };

    // This constructor is only used when loading physics from bullet file
    BulletPoint2PointConstraint::BulletPoint2PointConstraint(btPoint2PointConstraint *constraint) {
        mPoint2PointConstraint = constraint;
        mRigidBodyB = static_cast<BulletRigidBody*>(constraint->getRigidBodyB().getUserPointer());
        constraint->setUserConstraintPtr(this);
    }

    BulletPoint2PointConstraint::~BulletPoint2PointConstraint() {
        if (0 != mPoint2PointConstraint) {
            delete mPoint2PointConstraint;
        }
    };

    void BulletPoint2PointConstraint::setPivotInA(PhysicsVec3 pivot)
    {
        mPivotInA = pivot;

        btVector3 p(pivot.x, pivot.y, pivot.z);
        mPoint2PointConstraint->setPivotA(p);
    }

    void BulletPoint2PointConstraint::setPivotInB(PhysicsVec3 pivot)
    {
        mPivotInB = pivot;

        btVector3 p(pivot.x, pivot.y, pivot.z);
        mPoint2PointConstraint->setPivotB(p);
    }

    void BulletPoint2PointConstraint::setBreakingImpulse(float impulse) {
        if (0 != mPoint2PointConstraint) {
            mPoint2PointConstraint->setBreakingImpulseThreshold(impulse);
        }
        else {
            mBreakingImpulse = impulse;
        }
    }

    float BulletPoint2PointConstraint::getBreakingImpulse() const {
        if (0 != mPoint2PointConstraint) {
            return mPoint2PointConstraint->getBreakingImpulseThreshold();
        }
        else {
            return mBreakingImpulse;
        }
    }


void BulletPoint2PointConstraint::updateConstructionInfo() {
//    if (mPoint2PointConstraint != 0) {
//        delete (mPoint2PointConstraint);
//    }

    if (mPoint2PointConstraint == nullptr) {
        btVector3 pivotInA(mPivotInA.x, mPivotInA.y, mPivotInA.z);
        btVector3 pivotInB(mPivotInB.x, mPivotInB.y, mPivotInB.z);
        btRigidBody *rbA = ((BulletRigidBody *) owner_object()->
                getComponent(COMPONENT_TYPE_PHYSICS_RIGID_BODY))->getRigidBody();

        mPoint2PointConstraint = new btPoint2PointConstraint(*rbA, *mRigidBodyB->getRigidBody(),
                                                             pivotInA, pivotInB);
        mPoint2PointConstraint->setBreakingImpulseThreshold(mBreakingImpulse);
    }
}

}
