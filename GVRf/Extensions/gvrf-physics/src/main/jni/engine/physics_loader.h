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

#ifndef EXTENSIONS_PHYSICS_LOADER_H
#define EXTENSIONS_PHYSICS_LOADER_H

#include <cstddef>

namespace gvr {

class PhysicsWorld;
class PhysicsRigidBody;
class PhysicsConstraint;

class PhysicsLoader
{
public:
    PhysicsLoader(char *buffer, size_t length, bool ignoreUpAxis) { }
    virtual ~PhysicsLoader() { }

    virtual PhysicsRigidBody* getNextRigidBody() = 0;

    virtual const char* getRigidBodyName(PhysicsRigidBody *body) const = 0;

    virtual PhysicsConstraint* getNextConstraint() = 0;

    virtual PhysicsRigidBody* getConstraintBodyA(PhysicsConstraint *constraint) = 0;

    virtual PhysicsRigidBody* getConstraintBodyB(PhysicsConstraint *constraint) = 0;
};

}

#endif //EXTENSIONS_PHYSICS_LOADER_H
