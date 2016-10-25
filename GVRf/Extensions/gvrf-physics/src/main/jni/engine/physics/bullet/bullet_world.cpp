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

#include "bullet_world.h"
#include "bullet_rigidbody.h"
#include "util/gvr_log.h"

#include <BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h>
#include <BulletCollision/BroadphaseCollision/btDbvtBroadphase.h>
#include <BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h>

namespace gvr {

BulletWorld::BulletWorld() {
    initialize();
}

BulletWorld::~BulletWorld() {
    finalize();
}

void BulletWorld::initialize() {
    // Default setup for memory, collision setup.
    mCollisionConfiguration = new btDefaultCollisionConfiguration();

    /// Default collision dispatcher.
    mDispatcher = new btCollisionDispatcher(mCollisionConfiguration);

    ///btDbvtBroadphase is a good general purpose broadphase. You can also try out btAxis3Sweep.
    mOverlappingPairCache = new btDbvtBroadphase();

    ///the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
    mSolver = new btSequentialImpulseConstraintSolver;

    mPhysicsWorld = new btDiscreteDynamicsWorld(mDispatcher, mOverlappingPairCache, mSolver,
                                                mCollisionConfiguration);

    mPhysicsWorld->setGravity(btVector3(0, -10, 0));
}

void BulletWorld::finalize() {
    for (int i = mPhysicsWorld->getNumCollisionObjects() - 1; i >= 0; i--) {
        btCollisionObject *obj = mPhysicsWorld->getCollisionObjectArray()[i];
        if (obj) {
            mPhysicsWorld->removeCollisionObject(obj);
            delete obj;
        }
    }

    //delete dynamics world
    delete mPhysicsWorld;

    //delete solver
    delete mSolver;

    //delete broadphase
    delete mOverlappingPairCache;

    //delete dispatcher
    delete mDispatcher;

    delete mCollisionConfiguration;
}

void BulletWorld::addRigidBody(PhysicsRigidBody *body) {
    mPhysicsWorld->addRigidBody((static_cast<BulletRigidBody *>(body))->getRigidBody());
}

void BulletWorld::removeRigidBody(PhysicsRigidBody *body) {
    mPhysicsWorld->removeRigidBody((static_cast<BulletRigidBody *>(body))->getRigidBody());
}

void BulletWorld::step(float timeStep) {
    mPhysicsWorld->stepSimulation(timeStep);
}

void BulletWorld::listCollisions(std::vector <ContactPoint> &contactPoints) {
    int numManifolds = mPhysicsWorld->getDispatcher()->getNumManifolds();

    for (int i = 0; i < numManifolds; i++) {
        ContactPoint contactPt;

        btPersistentManifold *contactManifold = mPhysicsWorld->getDispatcher()->getManifoldByIndexInternal(
                i);

        contactPt.body0 = (BulletRigidBody *) (contactManifold->getBody0()->getUserPointer());
        contactPt.body1 = (BulletRigidBody *) (contactManifold->getBody1()->getUserPointer());
        contactPt.normal[0] = contactManifold->getContactPoint(0).m_normalWorldOnB.getX();
        contactPt.normal[1] = contactManifold->getContactPoint(0).m_normalWorldOnB.getY();
        contactPt.normal[2] = contactManifold->getContactPoint(0).m_normalWorldOnB.getZ();
        contactPt.distance = contactManifold->getContactPoint(0).getDistance();

        contactPoints.push_back(contactPt);
        //TODO more collision atributes
    }
}
}

