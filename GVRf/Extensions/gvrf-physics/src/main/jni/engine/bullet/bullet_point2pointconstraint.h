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


#ifndef BULLET_POINT2POINTCONSTRAINT_H
#define BULLET_POINT2POINTCONSTRAINT_H

#include "../physics_point2pointconstraint.h"
#include "bullet_object.h"

class btPoint2PointConstraint;

namespace gvr {

    class PhysicsRigidBody;
    class BulletRigidBody;

    class BulletPoint2PointConstraint : public PhysicsPoint2pointConstraint,
                                               BulletObject {

    public:
        explicit BulletPoint2PointConstraint(PhysicsRigidBody* rigidBodyB, float pivotInA[],
                                             float pivotInB[]);

        BulletPoint2PointConstraint(btPoint2PointConstraint *constraint);

        virtual ~BulletPoint2PointConstraint();

        virtual void* getUnderlying() {
            return this->mPoint2PointConstraint;
        }

        void setBreakingImpulse(float impulse);

        float getBreakingImpulse() const;

        void setPivotInA(PhysicsVec3 pivot);

        PhysicsVec3 getPivotInA() const { return mPivotInA; }

        void setPivotInB(PhysicsVec3 pivot);

        PhysicsVec3 getPivotInB() const { return mPivotInB; }

        void updateConstructionInfo();

    private:
        btPoint2PointConstraint *mPoint2PointConstraint;
        BulletRigidBody *mRigidBodyB;

        float mBreakingImpulse;
        PhysicsVec3 mPivotInA;
        PhysicsVec3 mPivotInB;
    };
}
#endif //BULLET_POINT2POINTCONSTRAINT_H
