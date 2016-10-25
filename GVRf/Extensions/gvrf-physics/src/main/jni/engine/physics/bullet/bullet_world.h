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

/***************************************************************************
 * Bullet implementation of 3D world
 ***************************************************************************/

#ifndef BULLET_WORLD_H_
#define BULLET_WORLD_H_

#include "../physics3d/physics_3dworld.h"

#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>

#include "glm/glm.hpp"

namespace gvr {

class BulletWorld : public Physics3DWorld {
 public:
    BulletWorld();

    ~BulletWorld();

    void addRigidBody(PhysicsRigidBody *body);

    void removeRigidBody(PhysicsRigidBody *body);

    void step(float timeStep);

    void listCollisions(
            std::vector <ContactPoint> &contactPoints); //index counts how many iteration it's on list, if <0 should be taken out

 private:
    void initialize();

    void finalize();

 private:
    btDynamicsWorld *mPhysicsWorld;
    btCollisionConfiguration *mCollisionConfiguration;
    btCollisionDispatcher *mDispatcher;
    btSequentialImpulseConstraintSolver *mSolver;
    btBroadphaseInterface *mOverlappingPairCache;

    btNearCallback *gTmpFilter;
    int gNearCallbackCount = 0;
    void *gUserData = 0;
};

}

#endif /* BULLET_WORLD_H_ */
