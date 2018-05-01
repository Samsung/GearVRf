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

#ifndef EXTENSIONS_BULLET_GENERIC6DOFCONSTRAINT_H
#define EXTENSIONS_BULLET_GENERIC6DOFCONSTRAINT_H

#include "../physics_common.h"
#include "../physics_genericconstraint.h"
#include "bullet_object.h"

class btGeneric6DofConstraint;
namespace gvr {

    class PhysicsRigidBody;
    class BulletRigidBody;

    class BulletGeneric6dofConstraint : public PhysicsGenericConstraint, BulletObject {
    public:
        explicit BulletGeneric6dofConstraint(PhysicsRigidBody *rigidBodyB, float const joint[],
                                             float const rotationA[], float const rotationB[]);

        BulletGeneric6dofConstraint(btGeneric6DofConstraint *constraint);

        virtual ~BulletGeneric6dofConstraint();

        void setLinearLowerLimits(float limitX, float limitY, float limitZ);

        PhysicsVec3 getLinearLowerLimits() const;

        void setLinearUpperLimits(float limitX, float limitY, float limitZ);

        PhysicsVec3 getLinearUpperLimits() const;

        void setAngularLowerLimits(float limitX, float limitY, float limitZ);

        PhysicsVec3 getAngularLowerLimits() const;

        void setAngularUpperLimits(float limitX, float limitY, float limitZ);

        PhysicsVec3 getAngularUpperLimits() const;

        void *getUnderlying() { return mGeneric6DofConstraint;}

        void setBreakingImpulse(float impulse);

        float getBreakingImpulse() const;

        void updateConstructionInfo();

    private:

        btGeneric6DofConstraint *mGeneric6DofConstraint;
        BulletRigidBody *mRigidBodyB;

        float mBreakingImpulse;
        PhysicsVec3 mLinearLowerLimits;
        PhysicsVec3 mLinearUpperLimits;
        PhysicsVec3 mAngularLowerLimits;
        PhysicsVec3 mAngularUpperLimits;

        PhysicsVec3 mPosition;
        PhysicsMat3x3 mRotationA;
        PhysicsMat3x3 mRotationB;
    };

}

#endif //EXTENSIONS_BULLET_GENERIC6DOFCONSTRAINT_H
