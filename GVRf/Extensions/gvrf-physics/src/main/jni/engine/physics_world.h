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
 * Represents a physics 2D or 3D world
 ***************************************************************************/

#ifndef PHYSICS_WORLD_H_
#define PHYSICS_WORLD_H_

#include "physics_common.h"
#include "physics_rigidbody.h"
#include "physics_constraint.h"
#include "../objects/scene_object.h"
#include <list>

namespace gvr {

struct ContactPoint {
	PhysicsRigidBody* body0 = 0;
	PhysicsRigidBody* body1 = 0;
	float normal[3] = {0.0f, 0.0f, 0.0f};
	float distance = 0.0f;
	bool isHit = true;
};

class PhysicsWorld : public Component {
public:
    PhysicsWorld() : Component(PhysicsWorld::getComponentType()){}

    virtual ~PhysicsWorld() {}

    static long long getComponentType() {
        return COMPONENT_TYPE_PHYSICS_WORLD;
    }

    virtual void addConstraint(PhysicsConstraint *constraint) = 0;

    virtual void removeConstraint(PhysicsConstraint *constraint) = 0;

    virtual void startDrag(SceneObject *dragger, PhysicsRigidBody *target,
		float relX, float relY, float relZ) = 0;

    virtual void stopDrag() = 0;

    virtual void addRigidBody(PhysicsRigidBody *body) = 0;

    virtual void addRigidBody(PhysicsRigidBody *body, int collisiontype, int collidesWith) = 0;

    virtual void removeRigidBody(PhysicsRigidBody *body) = 0;

    virtual void step(float timeStep, int maxSubSteps) = 0;

    virtual void listCollisions(std::list<ContactPoint>& contactPoints) = 0;

    virtual void setGravity(float gx, float gy, float gz) = 0;

    virtual PhysicsVec3 getGravity() const = 0;
};

}

#endif /* PHYSICS_WORLD_H_ */
