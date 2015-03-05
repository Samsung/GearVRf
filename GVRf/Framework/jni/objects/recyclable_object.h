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
 * Objects that have native resources that need to be closed or freed.
 ***************************************************************************/

#ifndef RECYCLABLE_OBJECT_H_
#define RECYCLABLE_OBJECT_H_

#include "objects/hybrid_object.h"

#include "util/gvr_log.h"

namespace gvr {

class RecyclableObject: public HybridObject {
public:
    RecyclableObject() :
            HybridObject() {
    }

    /** Close/free the native resource. It must be safe to call this more than once! */
    virtual void recycle() {
    }
};
}
#endif
