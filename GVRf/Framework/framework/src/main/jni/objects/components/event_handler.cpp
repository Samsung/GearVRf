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

#include "objects/hybrid_object.h"
#include "objects/components/render_data.h"
#include "objects/components/event_handler.h"
#include <unordered_set>

namespace gvr {

void Listener::notify_listeners(bool dirty){
    for(auto& it: listeners_)
        it->set_renderdata_dirty(dirty);
    }
}