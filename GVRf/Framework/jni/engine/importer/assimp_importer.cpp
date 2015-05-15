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
 * Contains a ai_scene of Assimp.
 ***************************************************************************/

#include "assimp_importer.h"

#include "objects/mesh.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/render_data.h"

namespace gvr {
std::shared_ptr<Mesh> AssimpImporter::getMesh(int index) {
    Mesh* mesh = new Mesh();

    aiMesh* ai_mesh = assimp_importer_->GetScene()->mMeshes[index];

    std::vector<glm::vec3> vertices;
    for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
        vertices.push_back(
                glm::vec3(ai_mesh->mVertices[i].x, ai_mesh->mVertices[i].y,
                        ai_mesh->mVertices[i].z));
    }
    mesh->set_vertices(std::move(vertices));

    if (ai_mesh->mNormals != 0) {
        std::vector<glm::vec3> normals;
        for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
            normals.push_back(
                    glm::vec3(ai_mesh->mNormals[i].x, ai_mesh->mNormals[i].y,
                            ai_mesh->mNormals[i].z));
        }
        mesh->set_normals(std::move(normals));
    }

    if (ai_mesh->mTextureCoords[0] != 0) {
        std::vector<glm::vec2> tex_coords;
        for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
            tex_coords.push_back(
                    glm::vec2(ai_mesh->mTextureCoords[0][i].x,
                            ai_mesh->mTextureCoords[0][i].y));
            // Sets the texture repeat flag to TRUE if either of the 
            // texture coordinate is greater than 1
            if((ai_mesh->mTextureCoords[0][i].x) > 1 || (ai_mesh->mTextureCoords[0][i].y) > 1) {
                mesh->setTextureRepeatFlag(true);
            }
        }
        mesh->set_tex_coords(std::move(tex_coords));
    }

    std::vector<unsigned short> triangles;
    for (int i = 0; i < ai_mesh->mNumFaces; ++i) {
        if (ai_mesh->mFaces[i].mNumIndices == 3) {
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[1]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);
        }
    }
    mesh->set_triangles(std::move(triangles));

    return std::shared_ptr < Mesh > (mesh);
}

void AssimpImporter::scene_recursion(aiNode* assimp_node, const aiScene* assimp_scene, std::shared_ptr<Scene> gvr_scene_pointer, JNIEnv * env, jobject default_bitmap, jobject gvr_context, jmethodID method_ID, aiMatrix4x4 accumulated_transform)
{
    for(int i=0; i < assimp_node->mNumMeshes; i++)
    {
        std::shared_ptr<SceneObject> gvr_scene_object(new SceneObject());

        // Mesh
        std::shared_ptr<Mesh> gvr_mesh = getMesh(assimp_node->mMeshes[i]);

        // New render data object.
        std::shared_ptr<RenderData> scene_object_render_data(new RenderData());

        // Set the mesh to the render data.
        scene_object_render_data->set_mesh(gvr_mesh);

        // Set material.
        aiMesh* assimp_mesh = assimp_scene->mMeshes[assimp_node->mMeshes[i]];
        aiMaterial* assimp_material = assimp_scene->mMaterials[assimp_mesh->mMaterialIndex];

        // Defines a shader with shader type 0. UNLIT_SHADER = 0
        std::shared_ptr<Material> gvr_material(new Material(Material::ShaderType::UNLIT_SHADER));

        // Actual texture image
        aiString assimp_texture_file_name;
        assimp_material->GetTexture(aiTextureType_DIFFUSE, i, &assimp_texture_file_name);
        jstring texture_file_name = env->NewStringUTF(assimp_texture_file_name.C_Str());

        // Default texture

        bool repeat_flag = false;

        if(gvr_mesh->getTextureRepeatFlag()) {
            repeat_flag = true;
        }

        std::shared_ptr<Texture> default_texture(new BaseTexture(env, default_bitmap, repeat_flag));

            if (method_ID == NULL) {
                // No such method in Java side
                // No interaction with Java side so apply default texture

                gvr_material->setTexture("main_texture", default_texture);
            } else {
                if ((assimp_material->GetTextureCount(aiTextureType_DIFFUSE)) <= 0) {
                    // No texture file found so apply default texture
                    gvr_material->setTexture("main_texture", default_texture);
                } else {
                    // About to enter Java side.
                    jobject actual_texture_bitmap = env->CallObjectMethod(gvr_context, method_ID, texture_file_name);
                    if(actual_texture_bitmap == NULL)
                    {
                        // Null bitmap for texture file for mesh
                        // Applying default texture
                        gvr_material->setTexture("main_texture", default_texture);
                    }
                    else
                    {
                        // Back to native side from Java side
                        // Applying actual texture
                        std::shared_ptr<Texture> actual_texture(new BaseTexture(env, actual_texture_bitmap, repeat_flag));
                        gvr_material->setTexture("main_texture", actual_texture);
                    }
                }
            }

        // Set the material to the render data
        scene_object_render_data->set_material(gvr_material);

        // Transformation
        std::shared_ptr<Transform> gvr_transform(new Transform());
        gvr_transform->set_owner_object(gvr_scene_object);

        // Accumulated transformations of the node
        aiMatrix4x4 assimp_transform = assimp_node->mTransformation * accumulated_transform;
        aiVector3t<float> assimp_node_scaling;
        aiQuaterniont<float> assimp_node_rotation;
        aiVector3t<float> assimp_node_position;
        assimp_transform.Decompose(assimp_node_scaling, assimp_node_rotation, assimp_node_position);
        int assimp_mesh_index = i;
        gvr_transform->set_position(assimp_node_position.x, assimp_node_position.y, assimp_node_position.z);
        gvr_transform->set_rotation(assimp_node_rotation.w, assimp_node_rotation.x, assimp_node_rotation.y, assimp_node_rotation.z);
        gvr_transform->set_scale(assimp_node_scaling.x, assimp_node_scaling.y, assimp_node_scaling.z);
        gvr_scene_object->attachTransform(gvr_scene_object, gvr_transform);
        gvr_scene_object->attachRenderData(gvr_scene_object, scene_object_render_data);

        //Attaches the Scene Object to the Scene.
        gvr_scene_pointer->addSceneObject(gvr_scene_object);
    }

    for(int i=0;i<assimp_node->mNumChildren;i++)
    {
        scene_recursion(assimp_node->mChildren[i], assimp_scene, gvr_scene_pointer, env, default_bitmap, gvr_context, method_ID, accumulated_transform);
    }
}


std::shared_ptr<Scene> AssimpImporter::load_scene(JNIEnv* env, jobject obj, jobject default_bitmap, jobject gvr_context)
{
    //  Scene
    std::shared_ptr<Scene> gvr_scene_pointer(new Scene());

    // Left Camera
    std::shared_ptr<Camera> left_camera(new PerspectiveCamera());
    left_camera->set_render_mask(RenderData::RenderMaskBit::Left);

    //  Right Camera
    std::shared_ptr<Camera> right_camera(new PerspectiveCamera());
    right_camera->set_render_mask(RenderData::RenderMaskBit::Right);

    //  Left Camera object and its transform
    std::shared_ptr<SceneObject> left_camera_object(new SceneObject());
    std::shared_ptr<Transform> transform_left_camera_object(new Transform());
    transform_left_camera_object->set_owner_object(left_camera_object);

    // Camera Rig
    std::shared_ptr<CameraRig> camera_rig(new CameraRig());

    // Get the default separation between left and right camera
    float camera_separation_distance_ = camera_rig->default_camera_separation_distance();

    //  Sets transform for the Left and the Right Camera on the Camera Rig
    glm::vec3 left_camera_position = glm::vec3(-camera_separation_distance_/2, 0.0f, 0.0f);
    glm::vec3 right_camera_position = glm::vec3(camera_separation_distance_/2, 0.0f, 0.0f);

    transform_left_camera_object->set_position(left_camera_position);
    left_camera_object->attachTransform(left_camera_object, transform_left_camera_object);
    left_camera_object->attachCamera(left_camera_object, left_camera);

    //  Right Camera object and its transform
    std::shared_ptr<SceneObject> right_camera_object(new SceneObject());
    std::shared_ptr<Transform> transform_right_camera_object(new Transform());
    transform_right_camera_object->set_owner_object(right_camera_object);
    transform_right_camera_object->set_position(right_camera_position);
    right_camera_object->attachTransform(right_camera_object, transform_right_camera_object);
    right_camera_object->attachCamera(right_camera_object, right_camera);

    //  Camera Rig and its transform
    std::shared_ptr<SceneObject> camera_rig_object(new SceneObject());
    std::shared_ptr<Transform> transform_camera_rig_object(new Transform());
    camera_rig_object->attachTransform(camera_rig_object, transform_camera_rig_object);
    
    camera_rig->attachLeftCamera(left_camera);
    camera_rig->attachRightCamera(right_camera);
    camera_rig_object->attachCameraRig(camera_rig_object, camera_rig);

    //  Adds the Camera Rig Scene Object to the Scene
    gvr_scene_pointer->addSceneObject(camera_rig_object);

    //  Adds the Left and the Right Camera to the Camera Rig
    camera_rig_object->addChildObject(camera_rig_object, left_camera_object);
    camera_rig_object->addChildObject(camera_rig_object, right_camera_object);

    //  Sets the Camera Rig as the main camera rig for the scene
    gvr_scene_pointer->set_main_camera_rig(camera_rig);

    //  Get the pointer to the scene
    const aiScene *assimp_scene_pointer = assimp_importer_->GetScene();

    // Default transformation
    aiMatrix4x4 identity_matrix;


    // Get bitmap method id
    jclass java_bitmap_method_class = env->GetObjectClass(gvr_context);
    jmethodID method_ID = env->GetMethodID(java_bitmap_method_class, "loadBitmap", "(Ljava/lang/String;)Landroid/graphics/Bitmap;");

    // Start the scene recursion for all the nodes in the hierarchy
    scene_recursion(assimp_scene_pointer->mRootNode, assimp_scene_pointer, gvr_scene_pointer, env, default_bitmap, gvr_context, method_ID, 
    identity_matrix);

    //  Returns the scene pointer
    return std::shared_ptr<Scene>(gvr_scene_pointer);
}
}
