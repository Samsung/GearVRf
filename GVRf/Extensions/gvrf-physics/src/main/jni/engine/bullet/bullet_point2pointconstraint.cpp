//
// Created by Juliana Figueira on 5/9/17.
//

#include "bullet_point2pointconstraint.h"
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
#include "bullet_rigidbody.h"
#include "bullet_world.h"

static const char tag[] = "BulletP2pConstr";

namespace gvr {

    BulletPoint2PointConstraint::BulletPoint2PointConstraint(PhysicsRigidBody* rigidBodyA,
            PhysicsRigidBody* rigidBodyB, float pivotInA[], float pivotInB[])
    {
        btVector3 pInA(pivotInA[0], pivotInA[1], pivotInA[2]);
        btVector3 pInB(pivotInB[0], pivotInB[1], pivotInB[2]);
        btRigidBody *rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
        btRigidBody *rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

        mPoint2PointConstraint = new btPoint2PointConstraint(*rbA, *rbB, pInA, pInB);
    };

    // This constructor is only used when loading physics from bullet file
    BulletPoint2PointConstraint::BulletPoint2PointConstraint(btPoint2PointConstraint *constraint)
    {
        mPoint2PointConstraint = constraint;
        constraint->setUserConstraintPtr(this);
    }

    BulletPoint2PointConstraint::~BulletPoint2PointConstraint()
    {
        delete mPoint2PointConstraint;
    };

    void BulletPoint2PointConstraint::setPivotInA(PhysicsVec3 pivot)
    {
        btVector3 p(pivot.x, pivot.y, pivot.z);
        mPoint2PointConstraint->setPivotA(p);
    }

    PhysicsVec3 BulletPoint2PointConstraint::getPivotInA() const
    {
        return PhysicsVec3(mPoint2PointConstraint->getPivotInA().m_floats);
    }

    void BulletPoint2PointConstraint::setPivotInB(PhysicsVec3 pivot)
    {
        btVector3 p(pivot.x, pivot.y, pivot.z);
        mPoint2PointConstraint->setPivotB(p);
    }

    PhysicsVec3 BulletPoint2PointConstraint::getPivotInB() const
    {
        return PhysicsVec3(mPoint2PointConstraint->getPivotInB().m_floats);
    }

    void BulletPoint2PointConstraint::setBreakingImpulse(float impulse)
    {
        mPoint2PointConstraint->setBreakingImpulseThreshold(impulse);
    }

    float BulletPoint2PointConstraint::getBreakingImpulse() const
    {
        return mPoint2PointConstraint->getBreakingImpulseThreshold();
    }
}
