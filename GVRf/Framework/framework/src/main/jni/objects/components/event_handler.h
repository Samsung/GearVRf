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


#ifndef FRAMEWORK_EVENTHANDLER_H
#define FRAMEWORK_EVENTHANDLER_H

#include "objects/hybrid_object.h"
#include "objects/components/render_data.h"
#include <unordered_set>
namespace gvr {
class RenderData;
class Listener{

public:
    void add_listener(RenderData* render_data){
        if(render_data)
            listeners_.insert(render_data);
    }
    void add_listener(Listener* listener){
        for(auto& it: listener->listeners_)
            listeners_.insert(it);
    }
    void remove_listener(RenderData* render_data){
        if(render_data)
            listeners_.erase(render_data);
    }
    void remove_listener(Listener* listener){
        for(auto& it: listener->listeners_)
            listeners_.erase(it);
    }
    void notify_listeners(bool dirty);

private:
    std::unordered_set<RenderData*>listeners_;
};


}
#endif //FRAMEWORK_EVENTHANDLER_H
