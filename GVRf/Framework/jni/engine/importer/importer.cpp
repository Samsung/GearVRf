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

#include "importer.h"

namespace gvr {
AssimpImporter* Importer::readFileFromAssets(char* buffer,
        long size, const char * hint_string) {
    Assimp::Importer* importer = new Assimp::Importer();
    importer->ReadFileFromMemory(buffer, size,
            aiProcess_JoinIdenticalVertices | aiProcess_FlipUVs, hint_string);
    return new AssimpImporter(importer);
}

AssimpImporter* Importer::readFileFromSDCard(const char * filename) {
    Assimp::Importer* importer = new Assimp::Importer();
    importer->ReadFile(filename,
            aiProcess_JoinIdenticalVertices | aiProcess_FlipUVs);
    return new AssimpImporter(importer);
}
}
