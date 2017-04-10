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


#ifndef CONFIGURATION_MANAGER_H_
#define CONFIGURATION_MANAGER_H_

namespace gvr {

    class ConfigurationManager {
    public:
        ConfigurationManager();

        virtual ~ConfigurationManager();

        void configureRendering(bool useStencil);
        int getMaxLights();

    private:
        ConfigurationManager(const ConfigurationManager &configuration_manager);

        ConfigurationManager(ConfigurationManager &&configuration_manager);

        ConfigurationManager &operator=(const ConfigurationManager &configuration_manager);

        ConfigurationManager &operator=(ConfigurationManager &&configuration_manager);

        void calculateMaxLights();

    private:
        int max_lights;

    };
}
#endif
