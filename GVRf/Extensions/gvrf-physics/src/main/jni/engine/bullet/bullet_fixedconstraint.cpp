//
// Created by Juliana Figueira on 5/9/17.
//

#include "bullet_fixedconstraint.h"
#include "bullet_rigidbody.h"
#include "bullet_gvr_utils.h"
#include <BulletDynamics/ConstraintSolver/btFixedConstraint.h>

#include <android/log.h>

static const char tag[] = "BulletFixedConstr";

namespace gvr {

BulletFixedConstraint::BulletFixedConstraint(PhysicsRigidBody* rigidBodyA,
                                             PhysicsRigidBody* rigidBodyB)
{
    btRigidBody* rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
    btRigidBody* rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

    // Frames will be fixed at updateConstructionInfo()
    mFixedConstraint = new btFixedConstraint(*rbA, *rbB, rbB->getWorldTransform(),
            rbA->getWorldTransform());
}

BulletFixedConstraint::BulletFixedConstraint(btFixedConstraint *constraint)
{
    mFixedConstraint = constraint;
    constraint->setUserConstraintPtr(this);
}

BulletFixedConstraint::~BulletFixedConstraint()
{
    delete mFixedConstraint;
}

void BulletFixedConstraint::updateConstructionInfo()
{
    void *user = mFixedConstraint->getUserConstraintPtr();
    if (user != nullptr && user != (void*)-1)
    {
        // Do not update loaded constraint
        return;
    }

    // Will fix BFrame because rigid bodies might be updated when added to the world. This
    // happens because rigid bodies are added tho the world in physics thread while this
    // constraint was created in another thread
    mFixedConstraint->setFrames(mFixedConstraint->getRigidBodyB().getWorldTransform(),
            mFixedConstraint->getRigidBodyA().getWorldTransform());
}

void BulletFixedConstraint::setBreakingImpulse(float impulse)
{
    mFixedConstraint->setBreakingImpulseThreshold(impulse);
}

float BulletFixedConstraint::getBreakingImpulse() const
{
    return mFixedConstraint->getBreakingImpulseThreshold();
}

}