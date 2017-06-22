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
 * Exports a scene to file.
 ***************************************************************************/

#include "exporter.h"

#include "objects/scene_object.h"
#include "util/gvr_log.h"
#include "objects/components/render_data.h"

#include <assimp/Exporter.hpp>
#include <assimp/scene.h>

#include <vector>

namespace gvr {

/* Returns the extension of filename */
const std::string getFileExtension(const std::string &filename) {
    return filename.substr(filename.find_last_of('.') + 1);
}

/* Returns Assimp's format description to filename */
const char * findFormatDescription(Assimp::Exporter &exporter,
        const std::string &filename) {
    if (!exporter.GetExportFormatCount()) {
        LOGE("Unexpected failure! Missing number of supported formats.");
        return 0;
    }

    auto const extension = getFileExtension(filename);
    std::string supported_extensions;

    for (int i = 0; i < exporter.GetExportFormatCount(); i++) {
        if (!strcmp(extension.c_str(), exporter.GetExportFormatDescription(i)->fileExtension))
            return exporter.GetExportFormatDescription(i)->fileExtension;

        // List of supported extensions to a better log
        supported_extensions.append(exporter.GetExportFormatDescription(i)->fileExtension);
        supported_extensions.append(" ");
    }

    LOGW("Format '%s' is not supported! Please use some of the following format(s): %s",
            extension.c_str(), supported_extensions.c_str());
    return 0;
}

void glm2aiMatrix4x4(aiMatrix4x4 &p, glm::mat4 m) {
    p.a1 = m[0][0]; p.a2 = m[1][0]; p.a3 = m[2][0]; p.a4 = m[3][0];
    p.b1 = m[0][1]; p.b2 = m[1][1]; p.b3 = m[2][1]; p.b4 = m[3][1];
    p.c1 = m[0][2]; p.c2 = m[1][2]; p.c3 = m[2][2]; p.c4 = m[3][2];
    p.d1 = m[0][3]; p.d2 = m[1][3]; p.d3 = m[2][3]; p.d4 = m[3][3];
}

/* Sets the transformations of GVRSceneObject to aiNode of i Mesh */
void gvr2aiNode(SceneObject &gvrobj, aiNode &ainode, int i) {
    ainode.mNumMeshes = 1;
    ainode.mMeshes = new unsigned int[ainode.mNumMeshes];
    ainode.mMeshes[0] = i;
    ainode.mNumChildren = 0;

    glm2aiMatrix4x4(ainode.mTransformation, gvrobj.transform()->getModelMatrix());
}

/* Converts the GVRMesh to aiMesh */
void gvr2aiMesh(Mesh &gvrmesh, aiMesh &aimesh) {
    int nverts = gvrmesh.getVertexCount();
    float* verts = static_cast<float*>(&(aimesh.mVertices[0].x));
    float* norms = static_cast<float*>(&(aimesh.mNormals[0].x));

    aimesh.mMaterialIndex = 0;
    aimesh.mVertices = new aiVector3D[nverts];
    aimesh.mNormals = new aiVector3D[nverts];
    aimesh.mNumVertices = nverts;
    aimesh.mTextureCoords[0] = new aiVector3D[nverts];
    aimesh.mNumUVComponents[0] = nverts;
    gvrmesh.getVertices(verts, nverts * 3);
    gvrmesh.getNormals(norms, nverts * 3);
    gvrmesh.forAllVertices("a_texcoord", [aimesh](int iter, const float* uvs)
    {
        aimesh.mTextureCoords[0][iter] = aiVector3D(uvs[0], uvs[1], 0);
    });
    int nIndices = gvrmesh.getIndexCount();
    aimesh.mNumFaces = (unsigned int)(nIndices / 3);
    aimesh.mFaces = new aiFace[aimesh.mNumFaces];
        gvrmesh.forAllIndices([aimesh](int iter, int index)
                              {
                                  int faceIndex = iter / 3;
                                  aiFace &face = aimesh.mFaces[faceIndex];
                                  if (face.mIndices == NULL)
                                  {
                                      face.mIndices = new unsigned int[3];
                                      face.mNumIndices = 3;
                                  }
                                  face.mIndices[iter % 3] = index;
                              }
        );
}

/* Converts the given GVRScene to aiScene */
void gvr2aiScene(Scene &gvrscene, aiScene &aiscene) {
    std::vector<SceneObject*> scene_objects(gvrscene.getWholeSceneObjects());

    aiscene.mRootNode = new aiNode();

    aiscene.mNumMaterials = 1;
    aiscene.mMaterials = new aiMaterial*[aiscene.mNumMaterials];
    aiscene.mMaterials[0] = new aiMaterial();

    aiscene.mNumMeshes = scene_objects.size();
    aiscene.mMeshes = new aiMesh*[aiscene.mNumMeshes];

    aiscene.mRootNode->mMeshes = NULL;
    aiscene.mRootNode->mNumMeshes = 0;
    aiscene.mRootNode->mChildren = new aiNode*[aiscene.mNumMeshes];

    int i = 0;

    for ( auto itr = scene_objects.begin(); itr != scene_objects.end(); ++itr ) {

        if ((*itr) == NULL || (*itr)->render_data() == NULL
                || (*itr)->render_data()->mesh() == NULL) {
            continue;
        }

        aiMesh *aimesh = new aiMesh();
        aiNode *ainode = new aiNode();

        gvr2aiMesh(*(*itr)->render_data()->mesh(), *aimesh);

        gvr2aiNode(**itr, *ainode, i);

        if ((*itr)->name().empty())
            aimesh->mName.length = sprintf(aimesh->mName.data, "Mesh%03d", i);
        else
            aimesh->mName = (*itr)->name();

        aiscene.mMeshes[i] = aimesh;
        aiscene.mRootNode->mChildren[i] = ainode;

        i++;
    }

    aiscene.mNumMeshes = i;
    aiscene.mRootNode->mNumChildren = aiscene.mNumMeshes;
}

/* Exports the scene to the given filename */
int Exporter::writeToFile(Scene *scene, const std::string filename) {
    if (!scene || filename.empty()) {
        LOGW("Exporting to invalid filename or current scene is invalid.");
        return -1;
    }

    Assimp::Exporter exporter;
    const char *format_description_id = findFormatDescription(exporter, filename);

    if (!format_description_id) {
        LOGW("Failure to find supported format description to %s", filename.c_str());
        return -1;
    }

    aiScene aiscene;

    gvr2aiScene(*scene, aiscene);

    if (aiscene.mNumMeshes > 0) {
        LOGD("Exporting scene to %s\n", filename.c_str());
        exporter.Export(&aiscene, format_description_id, filename);
    }

    return 0;
}
}
