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
 * Imports a scene file using Assimp.
 ***************************************************************************/

#ifndef IMPORTER_H_
#define IMPORTER_H_

#include <memory>

#include "assimp/scene.h"
#include "assimp/Importer.hpp"
#include "assimp/postprocess.h"

#include "engine/importer/assimp_importer.h"

namespace gvr {
class Importer {
private:
    Importer();

public:

    static AssimpImporter* readFileFromAssets(char* buffer, long size, const char * filename, int settings);
    static AssimpImporter* readFileFromSDCard(const char * filename, int settings);
};
}
#endif
