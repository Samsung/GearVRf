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
// Created by c.bozzetto on 31/05/2017.
//

#ifndef EXTENSIONS_PHYSICS_SLIDERCONSTRAINT_H
#define EXTENSIONS_PHYSICS_SLIDERCONSTRAINT_H

#include "physics_constraint.h"

namespace gvr {

    class PhysicsSliderConstraint : public PhysicsConstraint {
    public:
        virtual ~PhysicsSliderConstraint() {}

        virtual void setAngularLowerLimit(float limit) = 0;

        virtual float getAngularLowerLimit() const = 0;

        virtual void setAngularUpperLimit(float limit) = 0;

        virtual float getAngularUpperLimit() const = 0;

        virtual void setLinearLowerLimit(float limit) = 0;

        virtual float getLinearLowerLimit() const = 0;

        virtual void setLinearUpperLimit(float limit) = 0;

        virtual float getLinearUpperLimit() const = 0;

        int getConstraintType() const { return PhysicsConstraint::sliderConstraint; }
    };

}

#endif //EXTENSIONS_PHYSICS_SLIDERCONSTRAINT_H
