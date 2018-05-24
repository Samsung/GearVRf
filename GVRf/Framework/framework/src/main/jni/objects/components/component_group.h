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
 * Can hold a set of colliders attached to a scene object.
 ***************************************************************************/

#ifndef COMPONENT_GROUP_H_
#define COMPONENT_GROUP_H_

#include <algorithm>
#include <memory>
#include <vector>

#include "glm/glm.hpp"

#include "component.h"

namespace gvr {

class ComponentGroup: public Component
{
public:
    ComponentGroup(long long type);
    virtual ~ComponentGroup();

    void addChildComponent(Component* child);
    void removeChildComponent(Component* child);

private:
    ComponentGroup(const ComponentGroup& group) = delete;
    ComponentGroup(ComponentGroup&& group) = delete;
    ComponentGroup& operator=(const ComponentGroup& group) = delete;
    ComponentGroup& operator=(ComponentGroup&& group) = delete;

protected:
    std::vector<Component*> children_;
};
}

#endif
