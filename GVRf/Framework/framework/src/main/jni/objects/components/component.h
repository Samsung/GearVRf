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
 * Things which can be attached to a scene object.
 ***************************************************************************/

#ifndef COMPONENT_H_
#define COMPONENT_H_

#include <memory>

#include "objects/hybrid_object.h"

namespace gvr {
class SceneObject;

class Component: public HybridObject {
public:
    Component() :
            HybridObject(), type_(0), owner_object_(0), enabled_(true) {
    }

    Component(long long type) :
            HybridObject(), type_(type), owner_object_(0), enabled_(true) {
    }

    Component(SceneObject* owner_object) :
            type_(0),
            enabled_(true),
            owner_object_(owner_object) {
    }

    Component(long long type, SceneObject* owner_object) :
            type_(type),
            enabled_(true),
            owner_object_(owner_object) {
    }

    virtual ~Component() {
        set_owner_object(NULL);
    }

    SceneObject* owner_object() const {
        return owner_object_;
    }

    virtual void set_owner_object(SceneObject* owner_object) {
        owner_object_ = owner_object;
    }

    void removeOwnerObject() {
        owner_object_ = 0;
    }

    long long getType() const {
        return type_;
    }

    bool enabled() const {
        return enabled_;
    }

    virtual void set_enable(bool enable) {
        enabled_ = enable;
    }

private:
    Component(const Component& component);
    Component(Component&& component);
    Component& operator=(const Component& component);
    Component& operator=(Component&& component);

protected:
    SceneObject* owner_object_;
    long long    type_;
    bool         enabled_;
};

}
#endif

