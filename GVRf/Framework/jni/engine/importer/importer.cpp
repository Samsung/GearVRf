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
AssimpImporter* Importer::readFileFromAssets(char* buffer, long size,
        const char * filename, int settings) {
    Assimp::Importer* importer = new Assimp::Importer();
    char* hint = 0;

    if (filename != 0) {
        hint = strrchr(filename, '.');
        if (hint && hint != filename) {
            hint = hint + 1;
        }
    }
    importer->ReadFileFromMemory(buffer, size, getImportFlags(settings), hint);

    return new AssimpImporter(importer);
}

AssimpImporter* Importer::readFileFromSDCard(const char * filename, int settings) {
    Assimp::Importer* importer = new Assimp::Importer();
    importer->ReadFile(filename, getImportFlags(settings));
    return new AssimpImporter(importer);
}

unsigned int Importer::getImportFlags(int settings) {
    int flags = aiProcess_JoinIdenticalVertices | aiProcess_FlipUVs | aiProcess_Triangulate;

    // TODO: Think of a better way to not do this with hardcoded values.
    // Cannot specify to generate both kind of normals.
    // Generating Smooth Normals has precedence over "hard" normals
    if (settings & 0x1) {
        flags |= aiProcess_GenSmoothNormals;
    } else if (settings & 0x2){
        flags |= aiProcess_GenNormals;
    }

    // Needed for most per pixel lighting as most artists don't export tangents.
    if (settings & 0x4) {
        flags |= aiProcess_CalcTangentSpace;
    }

    // This might increase loading time but it will reduce drawcalls and improve cache hit.
    if (settings & 0x8) {
        flags |= aiProcess_ImproveCacheLocality | aiProcess_OptimizeMeshes | aiProcess_OptimizeGraph;
    }

    return static_cast<unsigned int>(flags);
}
}
