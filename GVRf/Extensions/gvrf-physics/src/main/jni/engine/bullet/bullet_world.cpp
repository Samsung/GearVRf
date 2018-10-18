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
#include <algorithm>
#include "bullet_world.h"
#include "bullet_rigidbody.h"

#include <BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h>
#include <BulletCollision/BroadphaseCollision/btDbvtBroadphase.h>
#include <BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h>

#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>

#include <android/log.h>

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

void BulletWorld::addConstraint(PhysicsConstraint *constraint) {
    constraint->updateConstructionInfo();
    btTypedConstraint *_constr = reinterpret_cast<btTypedConstraint*>(constraint->getUnderlying());
    mPhysicsWorld->addConstraint(_constr);
}

void BulletWorld::removeConstraint(PhysicsConstraint *constraint) {
    mPhysicsWorld->removeConstraint(reinterpret_cast<btTypedConstraint*>(constraint->getUnderlying()));
}

void BulletWorld::addRigidBody(PhysicsRigidBody *body) {
    btRigidBody *b = (static_cast<BulletRigidBody *>(body))->getRigidBody();
    body->updateConstructionInfo();
    mPhysicsWorld->addRigidBody(b);
}

void BulletWorld::addRigidBody(PhysicsRigidBody *body, int collisiontype, int collidesWith) {
    body->updateConstructionInfo();
    mPhysicsWorld->addRigidBody((static_cast<BulletRigidBody *>(body))->getRigidBody(),
                                collidesWith, collisiontype);
}

void BulletWorld::removeRigidBody(PhysicsRigidBody *body) {
    mPhysicsWorld->removeRigidBody((static_cast<BulletRigidBody *>(body))->getRigidBody());
}

void BulletWorld::step(float timeStep, int maxSubSteps) {
    mPhysicsWorld->stepSimulation(timeStep, maxSubSteps);
}

/**
 * Returns by reference the list of new and ceased collisions
 *  that will be the objects of ONENTER and ONEXIT events.
 */
void BulletWorld::listCollisions(std::list <ContactPoint> &contactPoints) {

/*
 * Creates a list of all the current collisions on the World
 * */
    std::map<std::pair<long,long>, ContactPoint> currCollisions;
    int numManifolds = mPhysicsWorld->getDispatcher()->getNumManifolds();
    btPersistentManifold *contactManifold;

    for (int i = 0; i < numManifolds; i++) {
        ContactPoint contactPt;

        contactManifold = mPhysicsWorld->getDispatcher()->
                getManifoldByIndexInternal(i);

        contactPt.body0 = (BulletRigidBody *) (contactManifold->getBody0()->getUserPointer());
        contactPt.body1 = (BulletRigidBody *) (contactManifold->getBody1()->getUserPointer());
        contactPt.normal[0] = contactManifold->getContactPoint(0).m_normalWorldOnB.getX();
        contactPt.normal[1] = contactManifold->getContactPoint(0).m_normalWorldOnB.getY();
        contactPt.normal[2] = contactManifold->getContactPoint(0).m_normalWorldOnB.getZ();
        contactPt.distance = contactManifold->getContactPoint(0).getDistance();
        contactPt.isHit = true;

        std::pair<long, long> collisionPair((long)contactPt.body0, (long)contactPt.body1);
        std::pair<std::pair<long, long>, ContactPoint> newPair(collisionPair, contactPt);
        currCollisions.insert(newPair);

        /*
         * If one of these current collisions is not on the list with all the previous
         * collision, then it should be on the return list, because it is an onEnter event
         * */
        auto it = prevCollisions.find(collisionPair);
        if ( it == prevCollisions.end()) {
            contactPoints.push_front(contactPt);
        } 
        contactManifold = 0;
    }

    /*
     * After going through all the current list, go through all the previous collisions list,
     * if one of its collisions is not on the current collision list, then it should be
     * on the return list, because it is an onExit event
     * */
    for (auto it = prevCollisions.begin(); it != prevCollisions.end(); ++it) {
        if (currCollisions.find(it->first) == currCollisions.end()) {
            ContactPoint cp = it->second;
            cp.isHit = false;
            contactPoints.push_front(cp);
        }
    }

/*
 * Save all the current collisions on the previous collisions list for the next iteration
 * */
    prevCollisions.clear();
    prevCollisions.swap(currCollisions);

}


void BulletWorld::setGravity(float x, float y, float z) {
    mPhysicsWorld->setGravity(btVector3(x, y, z));
}

void BulletWorld::setGravity(glm::vec3 gravity) {
    mPhysicsWorld->setGravity(btVector3(gravity.x, gravity.y, gravity.z));
}

PhysicsVec3 BulletWorld::getGravity() const
{
    btVector3 g = mPhysicsWorld->getGravity();

    PhysicsVec3 gravity;
    gravity.x = g.getX();
    gravity.y = g.getY();
    gravity.z = g.getZ();

    return gravity;
}

}

