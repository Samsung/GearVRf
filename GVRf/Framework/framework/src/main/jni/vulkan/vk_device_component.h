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


#ifndef FRAMEWORK_VK_DEVICE_COMPONENT_H
#define FRAMEWORK_VK_DEVICE_COMPONENT_H


/* All classes which use resources whose creation requires the logical device
 * should extend from this. This is so that when the device is deleted, all the resources
 * associated with this device are also destroyed with it.
 *
 */

namespace gvr {
    class VKDeviceComponent {

    public:
        VKDeviceComponent();

        /*
         * All classes using vulkan resources must override this method
         * in order to clear up the resources when the device is deleted.
         */
        virtual void cleanup() = 0;
        virtual  ~VKDeviceComponent();

    };
}


#endif //FRAMEWORK_VK_DEVICE_COMPONENT_H
