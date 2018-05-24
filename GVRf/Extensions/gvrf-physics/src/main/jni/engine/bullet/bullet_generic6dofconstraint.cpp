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
// Created by c.bozzetto on 09/06/2017.
//

#include "bullet_generic6dofconstraint.h"
#include "bullet_rigidbody.h"
#include "bullet_gvr_utils.h"

#include <BulletDynamics/Dynamics/btRigidBody.h>
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>

static const char tag[] = "BulletGenericConstr";

namespace gvr {

    BulletGeneric6dofConstraint::BulletGeneric6dofConstraint(PhysicsRigidBody *rigidBodyA,
            PhysicsRigidBody *rigidBodyB, float const joint[], float const rotationA[],
            float const rotationB[])
    {
        btRigidBody *rbA = reinterpret_cast<BulletRigidBody*>(rigidBodyA)->getRigidBody();
        btRigidBody *rbB = reinterpret_cast<BulletRigidBody*>(rigidBodyB)->getRigidBody();

        btVector3 p(joint[0], joint[1], joint[2]);
        btMatrix3x3 m(rotationA[0], rotationA[1], rotationA[2], rotationA[3], rotationA[4],
                rotationA[5], rotationA[6], rotationA[7], rotationA[8]);
        btTransform fA(m, p);

        // This frame will be fixed at updateConstructionInfo()
        m.setValue(rotationB[0], rotationB[1], rotationB[2], rotationB[3], rotationB[4],
                rotationB[5], rotationB[6], rotationB[7], rotationB[8]);
        btTransform fB(m, p);

        mGeneric6DofConstraint = new btGeneric6DofConstraint(*rbA, *rbB, fA, fB, false);
    }

    BulletGeneric6dofConstraint::BulletGeneric6dofConstraint(btGeneric6DofConstraint *constraint)
    {
        mGeneric6DofConstraint = constraint;
        constraint->setUserConstraintPtr(this);
    }

    BulletGeneric6dofConstraint::~BulletGeneric6dofConstraint()
    {
        delete mGeneric6DofConstraint;
    }

    void BulletGeneric6dofConstraint::setLinearLowerLimits(float limitX, float limitY, float limitZ)
    {
        mGeneric6DofConstraint->setLinearLowerLimit(btVector3(limitX, limitY, limitZ));
    }

    PhysicsVec3 BulletGeneric6dofConstraint::getLinearLowerLimits() const
    {
        btVector3 t;
        mGeneric6DofConstraint->getLinearLowerLimit(t);
        return PhysicsVec3(t.x(), t.y(), t.z());
    }

    void BulletGeneric6dofConstraint::setLinearUpperLimits(float limitX, float limitY, float limitZ)
    {
        mGeneric6DofConstraint->setLinearUpperLimit(btVector3(limitX, limitY, limitZ));
    }

    PhysicsVec3 BulletGeneric6dofConstraint::getLinearUpperLimits() const
    {
        btVector3 t;
        mGeneric6DofConstraint->getLinearUpperLimit(t);
        return PhysicsVec3(t.x(), t.y(), t.z());
    }

    void BulletGeneric6dofConstraint::setAngularLowerLimits(float limitX, float limitY,
            float limitZ)
    {
        mGeneric6DofConstraint->setAngularLowerLimit(btVector3(limitX, limitY, limitZ));
    }

    PhysicsVec3 BulletGeneric6dofConstraint::getAngularLowerLimits() const
    {
        btVector3 t;
        mGeneric6DofConstraint->getAngularLowerLimit(t);
        return PhysicsVec3(t.x(), t.y(), t.z());
    }

    void BulletGeneric6dofConstraint::setAngularUpperLimits(float limitX, float limitY,
            float limitZ)
    {
        mGeneric6DofConstraint->setAngularUpperLimit(btVector3(limitX, limitY, limitZ));
    }

    PhysicsVec3 BulletGeneric6dofConstraint::getAngularUpperLimits() const
    {
        btVector3 t;
        mGeneric6DofConstraint->getAngularUpperLimit(t);
        return PhysicsVec3(t.x(), t.y(), t.z());
    }

    void BulletGeneric6dofConstraint::setBreakingImpulse(float impulse)
    {
        mGeneric6DofConstraint->setBreakingImpulseThreshold(impulse);
    }

    float BulletGeneric6dofConstraint::getBreakingImpulse() const
    {
        return mGeneric6DofConstraint->getBreakingImpulseThreshold();
    }

    void BulletGeneric6dofConstraint::updateConstructionInfo()
    {
        void *user = mGeneric6DofConstraint->getUserConstraintPtr();
        if (user != nullptr && user != (void*)-1)
        {
            // Do not update loaded constraint
            return;
        }

        // Will fix BFrame because rigid bodies might be updated when added to the world. This
        // happens because rigid bodies are added tho the world in physics thread while this
        // constraint was created in another thread
        btVector3 p(mGeneric6DofConstraint->getFrameOffsetA().getOrigin());
        p = mGeneric6DofConstraint->getRigidBodyA().getWorldTransform().getOrigin() + p;
        p -= mGeneric6DofConstraint->getRigidBodyB().getWorldTransform().getOrigin();
        btTransform fB(mGeneric6DofConstraint->getFrameOffsetB().getBasis(), p);
        mGeneric6DofConstraint->setFrames(mGeneric6DofConstraint->getFrameOffsetA(), fB);
    }
}