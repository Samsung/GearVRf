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

namespace gvr {
Mesh* AssimpImporter::getMesh(int index) {
    Mesh* mesh = new Mesh();

    if (assimp_importer_->GetScene() == 0) {
        LOGE("_ASSIMP_SCENE_NOT_FOUND_");
        delete mesh;
        return 0;
    }

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
        }
        mesh->set_tex_coords(std::move(tex_coords));
    }

    std::vector<unsigned short> triangles;
    for (int i = 0; i < ai_mesh->mNumFaces; ++i) {
        if (ai_mesh->mFaces[i].mNumIndices == 3) {
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[1]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);
        } else if (ai_mesh->mFaces[i].mNumIndices == 4) {
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[1]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);

            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[3]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
        }
    }
    mesh->set_triangles(std::move(triangles));

    return mesh;
}

bool AssimpImporter::create_instance(JNIEnv *env, const char* class_name,
        jobject& new_instance) {
    jclass java_class = env->FindClass(class_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID constructor_id = env->GetMethodID(java_class, "<init>", "()V");

    if (NULL == constructor_id) {
        return false;
    }

    new_instance = env->NewObject(java_class, constructor_id);
    env->DeleteLocalRef(java_class);

    if (NULL == new_instance) {
        return false;
    }

    return true;
}

bool AssimpImporter::create_instance(JNIEnv *env, const char* class_name,
        const char* signature,/* const*/jvalue* params, jobject& new_instance) {
    jclass java_class = env->FindClass(class_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID constructor_id = env->GetMethodID(java_class, "<init>",
            signature);

    if (NULL == constructor_id) {
        return false;
    }

    new_instance = env->NewObjectA(java_class, constructor_id, params);
    env->DeleteLocalRef(java_class);

    if (NULL == new_instance) {
        return false;
    }

    return true;
}

bool AssimpImporter::get_field(JNIEnv *env, jobject object,
        const char* field_name, const char* signature, jobject& field) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, signature);
    env->DeleteLocalRef(java_class);

    if (NULL == field_id) {
        return false;
    }

    field = env->GetObjectField(object, field_id);

    return true;
}

bool AssimpImporter::set_int_field(JNIEnv *env, jobject object,
        const char* field_name, jint value) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, "I");
    env->DeleteLocalRef(java_class);

    if (NULL == field_id) {
        return false;
    }

    env->SetIntField(object, field_id, value);

    return true;
}

bool AssimpImporter::set_float_field(JNIEnv *env, jobject object,
        const char* field_name, jfloat value) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, "F");
    env->DeleteLocalRef(java_class);

    if (NULL == field_id) {
        return false;
    }

    env->SetFloatField(object, field_id, value);

    return true;
}

bool AssimpImporter::set_object_field(JNIEnv *env, jobject object,
        const char* field_name, const char* signature, jobject value) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, signature);
    env->DeleteLocalRef(java_class);

    if (NULL == field_id) {
        return false;
    }

    env->SetObjectField(object, field_id, value);

    return true;
}

bool AssimpImporter::get_static_field(JNIEnv *env, const char* class_name,
        const char* field_name, const char* signature, jobject& field) {
    jclass java_class = env->FindClass(class_name);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, signature);

    if (NULL == field_id) {
        return false;
    }

    field = env->GetStaticObjectField(java_class, field_id);

    return true;
}

bool AssimpImporter::call(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetMethodID(java_class, method_name, signature);
    env->DeleteLocalRef(java_class);

    if (NULL == method_id) {
        return false;
    }

    jboolean return_value = env->CallBooleanMethod(object, method_id,
            params[0].l);

    return (bool) return_value;
}

bool AssimpImporter::callv(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetMethodID(java_class, method_name, signature);
    env->DeleteLocalRef(java_class);

    if (NULL == method_id) {
        return false;
    }

    env->CallVoidMethodA(object, method_id, params);

    return true;
}

bool AssimpImporter::call_static_object(JNIEnv *env, const char* type_name,
        const char* method_name, const char* signature,/* const*/jvalue* params,
        jobject& return_value) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetStaticMethodID(java_class, method_name,
            signature);

    if (NULL == method_id) {
        return false;
    }

    return_value = env->CallStaticObjectMethodA(java_class, method_id, params);

    return true;
}

bool AssimpImporter::copy_buffer(JNIEnv *env, jobject jassimp_mesh,
        const char* java_buffer_name, void* copy_data, size_t size) {
    jobject jassimp_buffer = NULL;

    if (!get_field(env, jassimp_mesh, java_buffer_name, "Ljava/nio/ByteBuffer;",
            jassimp_buffer)) {
        return false;
    }

    if (env->GetDirectBufferCapacity(jassimp_buffer) != size) {
        return false;
    }

    void* jassimp_buffer_ptr = env->GetDirectBufferAddress(jassimp_buffer);

    if (NULL == jassimp_buffer_ptr) {
        return false;
    }

    memcpy(jassimp_buffer_ptr, copy_data, size);

    return true;
}

bool AssimpImporter::copy_buffer_array(JNIEnv *env, jobject jassimp_mesh,
        const char* java_buffer_name, int index, void* copy_data, size_t size) {
    jobject jassimp_buffer_array = NULL;

    if (!get_field(env, jassimp_mesh, java_buffer_name,
            "[Ljava/nio/ByteBuffer;", jassimp_buffer_array)) {
        return false;
    }

    jobject jassimp_buffer = env->GetObjectArrayElement(
            (jobjectArray) jassimp_buffer_array, index);

    if (env->GetDirectBufferCapacity(jassimp_buffer) != size) {
        return false;
    }

    void* jassimp_buffer_ptr = env->GetDirectBufferAddress(jassimp_buffer);

    if (NULL == jassimp_buffer_ptr) {
        return false;
    }

    memcpy(jassimp_buffer_ptr, copy_data, size);

    return true;
}

bool AssimpImporter::load_meshes(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    for (unsigned int mesh_index = 0; mesh_index < assimp_scene->mNumMeshes;
            mesh_index++) {
        const aiMesh *assimp_mesh = assimp_scene->mMeshes[mesh_index];
        /* create mesh */
        jobject jassimp_mesh = NULL;

        if (!create_instance(env, "org/util/jassimp/AiMesh", jassimp_mesh)) {
            return false;
        }

        /* add mesh to m_meshes java.util.List */
        jobject jassimp_mesh_list = NULL;

        if (!get_field(env, jassimp_scene, "m_meshes", "Ljava/util/List;",
                jassimp_mesh_list)) {
            return false;
        }

        jvalue add_params[1];
        add_params[0].l = jassimp_mesh;
        if (!call(env, jassimp_mesh_list, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_params)) {
            return false;
        }

        /* set general mesh data in java */
        jvalue set_types_params[1];
        set_types_params[0].i = assimp_mesh->mPrimitiveTypes;
        if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                "setPrimitiveTypes", "(I)V", set_types_params)) {
            return false;
        }

        if (!set_int_field(env, jassimp_mesh, "m_materialIndex",
                assimp_mesh->mMaterialIndex)) {
            return false;
        }

        if (!set_object_field(env, jassimp_mesh, "m_name", "Ljava/lang/String;",
                env->NewStringUTF(assimp_mesh->mName.C_Str()))) {
            return false;
        }

        /* determine face buffer size */
        bool is_pure_triangle = assimp_mesh->mPrimitiveTypes
                == aiPrimitiveType_TRIANGLE;
        size_t face_buffer_size;
        if (is_pure_triangle) {
            face_buffer_size = assimp_mesh->mNumFaces * 3
                    * sizeof(unsigned int);
        } else {
            int vertex_references_number = 0;
            for (unsigned int face = 0; face < assimp_mesh->mNumFaces; face++) {
                vertex_references_number +=
                        assimp_mesh->mFaces[face].mNumIndices;
            }

            face_buffer_size = vertex_references_number * sizeof(unsigned int);
        }

        /* allocate buffers - we do this from java so they can be garbage collected */
        jvalue allocate_buffers_params[4];
        allocate_buffers_params[0].i = assimp_mesh->mNumVertices;
        allocate_buffers_params[1].i = assimp_mesh->mNumFaces;
        allocate_buffers_params[2].z = is_pure_triangle;
        allocate_buffers_params[3].i = (jint) face_buffer_size;
        if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                "allocateBuffers", "(IIZI)V", allocate_buffers_params)) {
            return false;
        }

        if (assimp_mesh->mNumVertices > 0) {
            /* push vertex data to java */
            if (!copy_buffer(env, jassimp_mesh, "m_vertices",
                    assimp_mesh->mVertices,
                    assimp_mesh->mNumVertices * sizeof(aiVector3D))) {
                return false;
            }
        }

        /* push face data to java */
        if (assimp_mesh->mNumFaces > 0) {
            if (is_pure_triangle) {
                char* face_buffer = (char*) malloc(face_buffer_size);

                size_t face_data_size = 3 * sizeof(unsigned int);
                for (unsigned int face = 0; face < assimp_mesh->mNumFaces;
                        face++) {
                    memcpy(face_buffer + face * face_data_size,
                            assimp_mesh->mFaces[face].mIndices, face_data_size);
                }

                bool res = copy_buffer(env, jassimp_mesh, "m_faces",
                        face_buffer, face_buffer_size);

                free(face_buffer);

                if (!res) {
                    return false;
                }
            } else {
                char* face_buffer = (char*) malloc(face_buffer_size);
                char* offset_buffer = (char*) malloc(
                        assimp_mesh->mNumFaces * sizeof(unsigned int));

                size_t face_buffer_pos = 0;
                for (unsigned int face = 0; face < assimp_mesh->mNumFaces;
                        face++) {
                    size_t face_buffer_offset = face_buffer_pos
                            / sizeof(unsigned int);
                    memcpy(offset_buffer + face * sizeof(unsigned int),
                            &face_buffer_offset, sizeof(unsigned int));

                    size_t face_data_size =
                            assimp_mesh->mFaces[face].mNumIndices
                                    * sizeof(unsigned int);
                    memcpy(face_buffer + face_buffer_pos,
                            assimp_mesh->mFaces[face].mIndices, face_data_size);
                    face_buffer_pos += face_data_size;
                }

                if (face_buffer_pos != face_buffer_size) {
                    /* this should really not happen */
                    env->FatalError("error copying face data");
                    exit(-1);
                }

                bool res = copy_buffer(env, jassimp_mesh, "m_faces",
                        face_buffer, face_buffer_size);
                res &= copy_buffer(env, jassimp_mesh, "m_faceOffsets",
                        offset_buffer,
                        assimp_mesh->mNumFaces * sizeof(unsigned int));

                free(face_buffer);
                free(offset_buffer);

                if (!res) {
                    return false;
                }
            }
        }

        /* push normals to java */
        if (assimp_mesh->HasNormals()) {
            jvalue allocate_data_channel_params[2];
            allocate_data_channel_params[0].i = 0;
            allocate_data_channel_params[1].i = 0;
            if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                    "allocateDataChannel", "(II)V",
                    allocate_data_channel_params)) {
                return false;
            }
            if (!copy_buffer(env, jassimp_mesh, "m_normals",
                    assimp_mesh->mNormals,
                    assimp_mesh->mNumVertices * 3 * sizeof(float))) {
                return false;
            }
        }

        /* push tangents to java */
        if (assimp_mesh->mTangents != NULL) {
            jvalue allocate_data_channel_params[2];
            allocate_data_channel_params[0].i = 1;
            allocate_data_channel_params[1].i = 0;
            if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                    "allocateDataChannel", "(II)V",
                    allocate_data_channel_params)) {
                return false;
            }
            if (!copy_buffer(env, jassimp_mesh, "m_tangents",
                    assimp_mesh->mTangents,
                    assimp_mesh->mNumVertices * 3 * sizeof(float))) {
                return false;
            }
        }

        /* push bitangents to java */
        if (assimp_mesh->mBitangents != NULL) {
            jvalue allocate_data_channel_params[2];
            allocate_data_channel_params[0].i = 2;
            allocate_data_channel_params[1].i = 0;
            if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                    "allocateDataChannel", "(II)V",
                    allocate_data_channel_params)) {
                return false;
            }
            if (!copy_buffer(env, jassimp_mesh, "m_bitangents",
                    assimp_mesh->mBitangents,
                    assimp_mesh->mNumVertices * 3 * sizeof(float))) {
                return false;
            }
        }

        /* push color sets to java */
        for (int c = 0; c < AI_MAX_NUMBER_OF_COLOR_SETS; c++) {
            if (assimp_mesh->mColors[c] != NULL) {
                jvalue allocate_data_channel_params[2];
                allocate_data_channel_params[0].i = 3;
                allocate_data_channel_params[1].i = c;
                if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                        "allocateDataChannel", "(II)V",
                        allocate_data_channel_params)) {
                    return false;
                }
                if (!copy_buffer_array(env, jassimp_mesh, "m_colorsets", c,
                        assimp_mesh->mColors[c],
                        assimp_mesh->mNumVertices * 4 * sizeof(float))) {
                    return false;
                }
            }
        }

        /* push tex coords to java */
        for (int c = 0; c < AI_MAX_NUMBER_OF_TEXTURECOORDS; c++) {
            if (assimp_mesh->mTextureCoords[c] != NULL) {
                jvalue allocate_data_channel_params[2];

                switch (assimp_mesh->mNumUVComponents[c]) {
                case 1:
                    allocate_data_channel_params[0].i = 4;
                    break;
                case 2:
                    allocate_data_channel_params[0].i = 5;
                    break;
                case 3:
                    allocate_data_channel_params[0].i = 6;
                    break;
                default:
                    return false;
                }

                allocate_data_channel_params[1].i = c;
                if (!callv(env, jassimp_mesh, "org/util/jassimp/AiMesh",
                        "allocateDataChannel", "(II)V",
                        allocate_data_channel_params)) {
                    return false;
                }

                /* gather data */
                size_t coord_buffer_size = assimp_mesh->mNumVertices
                        * assimp_mesh->mNumUVComponents[c] * sizeof(float);
                char* coord_buffer = (char*) malloc(coord_buffer_size);
                size_t coord_buffer_offset = 0;

                for (unsigned int v = 0; v < assimp_mesh->mNumVertices; v++) {
                    memcpy(coord_buffer + coord_buffer_offset,
                            &assimp_mesh->mTextureCoords[c][v],
                            assimp_mesh->mNumUVComponents[c] * sizeof(float));
                    coord_buffer_offset += assimp_mesh->mNumUVComponents[c]
                            * sizeof(float);
                }

                if (coord_buffer_offset != coord_buffer_size) {
                    /* this should really not happen */
                    env->FatalError("error copying coord data");
                    exit(-1);
                }

                bool res = copy_buffer_array(env, jassimp_mesh, "m_texcoords",
                        c, coord_buffer, coord_buffer_size);

                free(coord_buffer);

                if (!res) {
                    return false;
                }
            }
        }

        for (unsigned int b = 0; b < assimp_mesh->mNumBones; b++) {
            aiBone *assimp_bone = assimp_mesh->mBones[b];

            jobject jassimp_bone;
            if (!create_instance(env, "org/util/jassimp/AiBone",
                    jassimp_bone)) {
                return false;
            }

            /* add bone to bone list */
            jobject jassimp_bones_list = NULL;

            if (!get_field(env, jassimp_mesh, "m_bones", "Ljava/util/List;",
                    jassimp_bones_list)) {
                return false;
            }

            jvalue add_params[1];
            add_params[0].l = jassimp_bone;
            if (!call(env, jassimp_bones_list, "java/util/Collection", "add",
                    "(Ljava/lang/Object;)Z", add_params)) {
                return false;
            }

            /* set bone data */
            if (!set_object_field(env, jassimp_bone, "m_name",
                    "Ljava/lang/String;",
                    env->NewStringUTF(assimp_bone->mName.C_Str()))) {
                return false;
            }

            /* add bone weights */
            for (unsigned int w = 0; w < assimp_bone->mNumWeights; w++) {
                jobject jassimp_bone_weight;
                if (!create_instance(env, "org/util/jassimp/AiBoneWeight",
                        jassimp_bone_weight)) {
                    return false;
                }

                /* add boneweight to bone list */
                jobject jassimp_bones_weight_list = NULL;

                if (!get_field(env, jassimp_bone, "m_boneWeights",
                        "Ljava/util/List;", jassimp_bones_weight_list)) {
                    return false;
                }

                /* copy offset matrix */
                jfloatArray jassimp_offset_matrix = env->NewFloatArray(16);
                env->SetFloatArrayRegion(jassimp_offset_matrix, 0, 16,
                        (jfloat*) &assimp_bone->mOffsetMatrix);

                jvalue wrap_params[1];
                wrap_params[0].l = jassimp_offset_matrix;
                jobject jassimp_matrix;

                if (!call_static_object(env, "org/util/jassimp/Jassimp",
                        "wrapMatrix", "([F)Ljava/lang/Object;", wrap_params,
                        jassimp_matrix)) {
                    return false;
                }

                if (!set_object_field(env, jassimp_bone, "m_offsetMatrix",
                        "Ljava/lang/Object;", jassimp_matrix)) {
                    return false;
                }

                jvalue add_bone_weight_params[1];
                add_bone_weight_params[0].l = jassimp_bone_weight;
                if (!call(env, jassimp_bones_weight_list,
                        "java/util/Collection", "add", "(Ljava/lang/Object;)Z",
                        add_bone_weight_params)) {
                    return false;
                }

                if (!set_int_field(env, jassimp_bone_weight, "m_vertexId",
                        assimp_bone->mWeights[w].mVertexId)) {
                    return false;
                }

                if (!set_float_field(env, jassimp_bone_weight, "m_weight",
                        assimp_bone->mWeights[w].mWeight)) {
                    return false;
                }
            }
        }
        env->DeleteLocalRef(jassimp_mesh_list);
        env->DeleteLocalRef(jassimp_mesh);
    }
    return true;
}

bool AssimpImporter::load_scene_node(JNIEnv *env, const aiNode *assimp_node,
        jobject parent, jobject* loaded_node) {
    /* wrap matrix */
    jfloatArray jassimp_wrap_matrix = env->NewFloatArray(16);
    env->SetFloatArrayRegion(jassimp_wrap_matrix, 0, 16,
            (jfloat*) &assimp_node->mTransformation);

    jvalue wrap_matrix_params[1];
    wrap_matrix_params[0].l = jassimp_wrap_matrix;
    jobject jassimp_matrix;

    if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapMatrix",
            "([F)Ljava/lang/Object;", wrap_matrix_params, jassimp_matrix)) {
        return false;
    }

    /* create mesh references array */
    jintArray jassimp_mesh_ref_arr = env->NewIntArray(assimp_node->mNumMeshes);
    jint *temp = (jint*) malloc(sizeof(jint) * assimp_node->mNumMeshes);

    for (unsigned int i = 0; i < assimp_node->mNumMeshes; i++) {
        temp[i] = assimp_node->mMeshes[i];
    }
    env->SetIntArrayRegion(jassimp_mesh_ref_arr, 0, assimp_node->mNumMeshes,
            (jint*) temp);

    free(temp);

    /* convert name */
    jstring jassimp_node_name = env->NewStringUTF(assimp_node->mName.C_Str());

    /* wrap scene node */
    jvalue wrap_node_params[4];
    wrap_node_params[0].l = parent;
    wrap_node_params[1].l = jassimp_matrix;
    wrap_node_params[2].l = jassimp_mesh_ref_arr;
    wrap_node_params[3].l = jassimp_node_name;

    jobject jassimp_node;
    if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapSceneNode",
            "(Ljava/lang/Object;Ljava/lang/Object;[ILjava/lang/String;)Ljava/lang/Object;",
            wrap_node_params, jassimp_node)) {
        return false;
    }

    /* and recurse */
    for (unsigned int c = 0; c < assimp_node->mNumChildren; c++) {
        if (!load_scene_node(env, assimp_node->mChildren[c], jassimp_node)) {
            return false;
        }
    }

    if (NULL != loaded_node) {
        *loaded_node = jassimp_node;
    }

    return true;
}

bool AssimpImporter::load_scene_graph(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    if (NULL != assimp_scene->mRootNode) {
        jobject jassimp_root;

        if (!load_scene_node(env, assimp_scene->mRootNode, NULL,
                &jassimp_root)) {
            return false;
        }

        if (!set_object_field(env, jassimp_scene, "m_sceneRoot",
                "Ljava/lang/Object;", jassimp_root)) {
            return false;
        }
    }
    return true;
}

bool AssimpImporter::load_materials(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    for (unsigned int m = 0; m < assimp_scene->mNumMaterials; m++) {
        const aiMaterial* assimp_material = assimp_scene->mMaterials[m];
        jobject jassimp_material = NULL;

        if (!create_instance(env, "org/util/jassimp/AiMaterial",
                jassimp_material)) {
            return false;
        }

        /* add material to m_materials java.util.List */
        jobject jassimp_materials_list = NULL;

        if (!get_field(env, jassimp_scene, "m_materials", "Ljava/util/List;",
                jassimp_materials_list)) {
            return false;
        }

        jvalue add_material_params[1];
        add_material_params[0].l = jassimp_material;
        if (!call(env, jassimp_materials_list, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_material_params)) {
            return false;
        }

        /* set texture numbers */
        for (int texture_type_index = aiTextureType_DIFFUSE;
                texture_type_index < aiTextureType_UNKNOWN;
                texture_type_index++) {
            aiTextureType texture_type =
                    static_cast<aiTextureType>(texture_type_index);

            unsigned int total_textures = assimp_material->GetTextureCount(
                    texture_type);
            jvalue set_number_params[2];
            set_number_params[0].i = texture_type_index;
            set_number_params[1].i = total_textures;

            if (!callv(env, jassimp_material, "org/util/jassimp/AiMaterial",
                    "setTextureNumber", "(II)V", set_number_params)) {
                return false;
            }
        }

        for (unsigned int p = 0; p < assimp_material->mNumProperties; p++) {
            const aiMaterialProperty* assimp_material_property =
                    assimp_material->mProperties[p];
            jobject jassimp_material_property = NULL;

            jvalue constructor_params[5];
            constructor_params[0].l = env->NewStringUTF(
                    assimp_material_property->mKey.C_Str());
            constructor_params[1].i = assimp_material_property->mSemantic;
            constructor_params[2].i = assimp_material_property->mIndex;
            constructor_params[3].i = assimp_material_property->mType;

            /* special case conversion for color3 */
            if (NULL != strstr(assimp_material_property->mKey.C_Str(), "clr")
                    && assimp_material_property->mType == aiPTI_Float
                    && assimp_material_property->mDataLength
                            == 3 * sizeof(float)) {
                jobject jassimp_data = NULL;

                /* wrap color */
                jvalue wrap_color_params[3];
                wrap_color_params[0].f =
                        ((float*) assimp_material_property->mData)[0];
                wrap_color_params[1].f =
                        ((float*) assimp_material_property->mData)[1];
                wrap_color_params[2].f =
                        ((float*) assimp_material_property->mData)[2];
                if (!call_static_object(env, "org/util/jassimp/Jassimp",
                        "wrapColor3", "(FFF)Ljava/lang/Object;",
                        wrap_color_params, jassimp_data)) {
                    return false;
                }

                constructor_params[4].l = jassimp_data;
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIILjava/lang/Object;)V",
                        constructor_params, jassimp_material_property)) {
                    return false;
                }
            }
            /* special case conversion for color4 */
            else if (NULL
                    != strstr(assimp_material_property->mKey.C_Str(), "clr")
                    && assimp_material_property->mType == aiPTI_Float
                    && assimp_material_property->mDataLength
                            == 4 * sizeof(float)) {
                jobject jassimp_data = NULL;

                /* wrap color */
                jvalue wrap_color_params[4];
                wrap_color_params[0].f =
                        ((float*) assimp_material_property->mData)[0];
                wrap_color_params[1].f =
                        ((float*) assimp_material_property->mData)[1];
                wrap_color_params[2].f =
                        ((float*) assimp_material_property->mData)[2];
                wrap_color_params[3].f =
                        ((float*) assimp_material_property->mData)[3];
                if (!call_static_object(env, "org/util/jassimp/Jassimp",
                        "wrapColor4", "(FFFF)Ljava/lang/Object;",
                        wrap_color_params, jassimp_data)) {
                    return false;
                }

                constructor_params[4].l = jassimp_data;
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIILjava/lang/Object;)V",
                        constructor_params, jassimp_material_property)) {
                    return false;
                }
            } else if (assimp_material_property->mType == aiPTI_Float
                    && assimp_material_property->mDataLength == sizeof(float)) {
                jobject jassimp_data = NULL;

                jvalue new_float_params[1];
                new_float_params[0].f =
                        ((float*) assimp_material_property->mData)[0];
                if (!create_instance(env, "java/lang/Float", "(F)V",
                        new_float_params, jassimp_data)) {
                    return false;
                }

                constructor_params[4].l = jassimp_data;
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIILjava/lang/Object;)V",
                        constructor_params, jassimp_material_property)) {
                    return false;
                }
            } else if (assimp_material_property->mType == aiPTI_Integer
                    && assimp_material_property->mDataLength == sizeof(int)) {
                jobject jassimp_data = NULL;

                jvalue new_int_params[1];
                new_int_params[0].i =
                        ((int*) assimp_material_property->mData)[0];
                if (!create_instance(env, "java/lang/Integer", "(I)V",
                        new_int_params, jassimp_data)) {
                    return false;
                }

                constructor_params[4].l = jassimp_data;
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIILjava/lang/Object;)V",
                        constructor_params, jassimp_material_property)) {
                    return false;
                }
            } else if (assimp_material_property->mType == aiPTI_String) {
                /* skip length prefix */
                jobject jassimp_data = env->NewStringUTF(
                        assimp_material_property->mData + 4);

                constructor_params[4].l = jassimp_data;
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIILjava/lang/Object;)V",
                        constructor_params, jassimp_material_property)) {
                    return false;
                }
            } else {
                constructor_params[4].i = assimp_material_property->mDataLength;

                /* generic copy code, uses dump ByteBuffer on java side */
                if (!create_instance(env,
                        "org/util/jassimp/AiMaterial$Property",
                        "(Ljava/lang/String;IIII)V", constructor_params,
                        jassimp_material_property)) {
                    return false;
                }

                jobject jassimp_buffer = NULL;

                if (!get_field(env, jassimp_material_property, "m_data",
                        "Ljava/lang/Object;", jassimp_buffer)) {
                    return false;
                }

                if (env->GetDirectBufferCapacity(jassimp_buffer)
                        != assimp_material_property->mDataLength) {
                    LOGE("invalid direct buffer\n");
                    return false;
                }

                void* jassimp_buffer_ptr = env->GetDirectBufferAddress(
                        jassimp_buffer);

                if (NULL == jassimp_buffer_ptr) {
                    LOGE("could not access direct buffer\n");
                    return false;
                }

                memcpy(jassimp_buffer_ptr, assimp_material_property->mData,
                        assimp_material_property->mDataLength);
            }

            /* add property */
            jobject jassimp_properties = NULL;

            if (!get_field(env, jassimp_material, "m_properties",
                    "Ljava/util/List;", jassimp_properties)) {
                return false;
            }

            jvalue add_properties_params[1];
            add_properties_params[0].l = jassimp_material_property;
            if (!call(env, jassimp_properties, "java/util/Collection", "add",
                    "(Ljava/lang/Object;)Z", add_properties_params)) {
                return false;
            }
        }
    }
    return true;
}

bool AssimpImporter::load_animations(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    for (unsigned int anim_index = 0; anim_index < assimp_scene->mNumAnimations;
            anim_index++) {
        const aiAnimation *assimp_animation =
                assimp_scene->mAnimations[anim_index];
        jobject jassimp_animation;
        jvalue new_anim_params[3];
        new_anim_params[0].l = env->NewStringUTF(
                assimp_animation->mName.C_Str());
        new_anim_params[1].d = assimp_animation->mDuration;
        new_anim_params[2].d = assimp_animation->mTicksPerSecond;

        if (!create_instance(env, "org/util/jassimp/AiAnimation",
                "(Ljava/lang/String;DD)V", new_anim_params,
                jassimp_animation)) {
            return false;
        }

        /* add animation to m_animations java.util.List */
        jobject jassimp_animations_list = NULL;

        if (!get_field(env, jassimp_scene, "m_animations", "Ljava/util/List;",
                jassimp_animations_list)) {
            return false;
        }

        jvalue add_params[1];
        add_params[0].l = jassimp_animation;
        if (!call(env, jassimp_animations_list, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_params)) {
            return false;
        }

        for (unsigned int c = 0; c < assimp_animation->mNumChannels; c++) {
            const aiNodeAnim *assimp_node_animation =
                    assimp_animation->mChannels[c];

            jobject jassimp_node_animation;
            jvalue new_node_anim_params[6];
            new_node_anim_params[0].l = env->NewStringUTF(
                    assimp_node_animation->mNodeName.C_Str());
            new_node_anim_params[1].i = assimp_node_animation->mNumPositionKeys;
            new_node_anim_params[2].i = assimp_node_animation->mNumRotationKeys;
            new_node_anim_params[3].i = assimp_node_animation->mNumScalingKeys;
            new_node_anim_params[4].i = assimp_node_animation->mPreState;
            new_node_anim_params[5].i = assimp_node_animation->mPostState;

            if (!create_instance(env, "org/util/jassimp/AiNodeAnim",
                    "(Ljava/lang/String;IIIII)V", new_node_anim_params,
                    jassimp_node_animation)) {
                return false;
            }

            /* add node animation to m_animations java.util.List */
            jobject jassimp_node_animations_list = NULL;

            if (!get_field(env, jassimp_animation, "m_nodeAnims",
                    "Ljava/util/List;", jassimp_node_animations_list)) {
                return false;
            }

            jvalue add_params[1];
            add_params[0].l = jassimp_node_animation;
            if (!call(env, jassimp_node_animations_list, "java/util/Collection",
                    "add", "(Ljava/lang/Object;)Z", add_params)) {
                return false;
            }

            /* copy keys */
            if (!copy_buffer(env, jassimp_node_animation, "m_posKeys",
                    assimp_node_animation->mPositionKeys,
                    assimp_node_animation->mNumPositionKeys
                            * sizeof(aiVectorKey))) {
                return false;
            }

            if (!copy_buffer(env, jassimp_node_animation, "m_rotKeys",
                    assimp_node_animation->mRotationKeys,
                    assimp_node_animation->mNumRotationKeys
                            * sizeof(aiQuatKey))) {
                return false;
            }

            if (!copy_buffer(env, jassimp_node_animation, "m_scaleKeys",
                    assimp_node_animation->mScalingKeys,
                    assimp_node_animation->mNumScalingKeys
                            * sizeof(aiVectorKey))) {
                return false;
            }
        }
    }
    return true;
}

bool AssimpImporter::load_lights(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    for (unsigned int light_index = 0; light_index < assimp_scene->mNumLights;
            light_index++) {
        const aiLight *assimp_light = assimp_scene->mLights[light_index];
        /* wrap color nodes */
        jvalue wrap_color_params[3];
        wrap_color_params[0].f = assimp_light->mColorDiffuse.r;
        wrap_color_params[1].f = assimp_light->mColorDiffuse.g;
        wrap_color_params[2].f = assimp_light->mColorDiffuse.b;

        jobject jassimp_diffuse;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapColor3",
                "(FFF)Ljava/lang/Object;", wrap_color_params,
                jassimp_diffuse)) {
            return false;
        }

        wrap_color_params[0].f = assimp_light->mColorSpecular.r;
        wrap_color_params[1].f = assimp_light->mColorSpecular.g;
        wrap_color_params[2].f = assimp_light->mColorSpecular.b;

        jobject jassimp_specular;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapColor3",
                "(FFF)Ljava/lang/Object;", wrap_color_params,
                jassimp_specular)) {
            return false;
        }

        wrap_color_params[0].f = assimp_light->mColorAmbient.r;
        wrap_color_params[1].f = assimp_light->mColorAmbient.g;
        wrap_color_params[2].f = assimp_light->mColorAmbient.b;

        jobject jassimp_ambient;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapColor3",
                "(FFF)Ljava/lang/Object;", wrap_color_params,
                jassimp_ambient)) {
            return false;
        }

        /* wrap vec3 nodes */
        jvalue wrap_vec3_params[3];
        wrap_vec3_params[0].f = assimp_light->mPosition.x;
        wrap_vec3_params[1].f = assimp_light->mPosition.y;
        wrap_vec3_params[2].f = assimp_light->mPosition.z;

        jobject jassimp_position;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapVec3",
                "(FFF)Ljava/lang/Object;", wrap_vec3_params,
                jassimp_position)) {
            return false;
        }

        wrap_vec3_params[0].f = assimp_light->mPosition.x;
        wrap_vec3_params[1].f = assimp_light->mPosition.y;
        wrap_vec3_params[2].f = assimp_light->mPosition.z;

        jobject jassimp_direction;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapVec3",
                "(FFF)Ljava/lang/Object;", wrap_vec3_params,
                jassimp_direction)) {
            return false;
        }

        jobject jassimp_light;

        jvalue params[12];
        params[0].l = env->NewStringUTF(assimp_light->mName.C_Str());
        params[1].i = assimp_light->mType;
        params[2].l = jassimp_position;
        params[3].l = jassimp_direction;
        params[4].f = assimp_light->mAttenuationConstant;
        params[5].f = assimp_light->mAttenuationLinear;
        params[6].f = assimp_light->mAttenuationQuadratic;
        params[7].l = jassimp_diffuse;
        params[8].l = jassimp_specular;
        params[9].l = jassimp_ambient;
        params[10].f = assimp_light->mAngleInnerCone;
        params[11].f = assimp_light->mAngleOuterCone;

        if (!create_instance(env, "org/util/jassimp/AiLight",
                "(Ljava/lang/String;ILjava/lang/Object;Ljava/lang/Object;FFFLjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;FF)V",
                params, jassimp_light)) {
            return false;
        }

        /* add light to m_lights java.util.List */
        jobject jassimp_lights_list = NULL;

        if (!get_field(env, jassimp_scene, "m_lights", "Ljava/util/List;",
                jassimp_lights_list)) {
            return false;
        }

        jvalue add_params[1];
        add_params[0].l = jassimp_light;
        if (!call(env, jassimp_lights_list, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_params)) {
            return false;
        }
    }
    return true;
}

bool AssimpImporter::load_cameras(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    LOGE("converting %d cameras ...\n", assimp_scene->mNumCameras);

    for (unsigned int c = 0; c < assimp_scene->mNumCameras; c++) {
        const aiCamera *assimp_camera = assimp_scene->mCameras[c];
        /* wrap color nodes */
        jvalue wrap_position_params[3];
        wrap_position_params[0].f = assimp_camera->mPosition.x;
        wrap_position_params[1].f = assimp_camera->mPosition.y;
        wrap_position_params[2].f = assimp_camera->mPosition.z;

        jobject jassimp_position;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapVec3",
                "(FFF)Ljava/lang/Object;", wrap_position_params,
                jassimp_position)) {
            return false;
        }

        wrap_position_params[0].f = assimp_camera->mUp.x;
        wrap_position_params[1].f = assimp_camera->mUp.y;
        wrap_position_params[2].f = assimp_camera->mUp.z;

        jobject jassimp_camera_up;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapVec3",
                "(FFF)Ljava/lang/Object;", wrap_position_params,
                jassimp_camera_up)) {
            return false;
        }

        wrap_position_params[0].f = assimp_camera->mLookAt.x;
        wrap_position_params[1].f = assimp_camera->mLookAt.y;
        wrap_position_params[2].f = assimp_camera->mLookAt.z;

        jobject jassimp_camera_look_at;
        if (!call_static_object(env, "org/util/jassimp/Jassimp", "wrapVec3",
                "(FFF)Ljava/lang/Object;", wrap_position_params,
                jassimp_camera_look_at)) {
            return false;
        }

        jobject jassimp_camera;

        jvalue params[8];
        params[0].l = env->NewStringUTF(assimp_camera->mName.C_Str());
        params[1].l = jassimp_position;
        params[2].l = jassimp_camera_up;
        params[3].l = jassimp_camera_look_at;
        params[4].f = assimp_camera->mHorizontalFOV;
        params[5].f = assimp_camera->mClipPlaneNear;
        params[6].f = assimp_camera->mClipPlaneFar;
        params[7].f = assimp_camera->mAspect;

        if (!create_instance(env, "org/util/jassimp/AiCamera",
                "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;FFFF)V",
                params, jassimp_camera)) {
            return false;
        }

        /* add camera to m_cameras java.util.List */
        jobject jassimp_cameras_list = NULL;

        if (!get_field(env, jassimp_scene, "m_cameras", "Ljava/util/List;",
                jassimp_cameras_list)) {
            return false;
        }

        jvalue add_params[1];
        add_params[0].l = jassimp_camera;
        if (!call(env, jassimp_cameras_list, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_params)) {
            return false;
        }
    }
    return true;
}

jobject AssimpImporter::mesh_material(JNIEnv *env, int index) {
    aiMesh* assimp_mesh = assimp_importer_->GetScene()->mMeshes[index];
    aiMaterial* assimp_material =
            assimp_importer_->GetScene()->mMaterials[assimp_mesh->mMaterialIndex];
    jobject jassimp_material = NULL;

    if (!create_instance(env, "org/util/jassimp/AiMaterial",
            jassimp_material)) {
        return NULL;
    }

    /* set texture numbers */
    for (int texture_type_index = aiTextureType_DIFFUSE;
            texture_type_index < aiTextureType_UNKNOWN; texture_type_index++) {
        aiTextureType texture_type =
                static_cast<aiTextureType>(texture_type_index);

        unsigned int total_textures = assimp_material->GetTextureCount(
                texture_type);
        jvalue set_number_params[2];
        set_number_params[0].i = texture_type_index;
        set_number_params[1].i = total_textures;

        if (!callv(env, jassimp_material, "org/util/jassimp/AiMaterial",
                "setTextureNumber", "(II)V", set_number_params)) {
            return NULL;
        }
    }

    for (unsigned int p = 0; p < assimp_material->mNumProperties; p++) {
        const aiMaterialProperty* assimp_material_property =
                assimp_material->mProperties[p];
        jobject jassimp_material_property = NULL;
        jvalue constructor_params[5];
        constructor_params[0].l = env->NewStringUTF(
                assimp_material_property->mKey.C_Str());
        constructor_params[1].i = assimp_material_property->mSemantic;
        constructor_params[2].i = assimp_material_property->mIndex;
        constructor_params[3].i = assimp_material_property->mType;

        /* special case conversion for color3 */
        if (NULL != strstr(assimp_material_property->mKey.C_Str(), "clr")
                && assimp_material_property->mType == aiPTI_Float
                && assimp_material_property->mDataLength == 3 * sizeof(float)) {
            jobject jassimp_data = NULL;

            /* wrap color */
            jvalue wrap_color_params[3];
            wrap_color_params[0].f =
                    ((float*) assimp_material_property->mData)[0];
            wrap_color_params[1].f =
                    ((float*) assimp_material_property->mData)[1];
            wrap_color_params[2].f =
                    ((float*) assimp_material_property->mData)[2];
            if (!call_static_object(env, "org/util/jassimp/Jassimp",
                    "wrapColor3", "(FFF)Ljava/lang/Object;", wrap_color_params,
                    jassimp_data)) {
                return NULL;
            }

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        }
        /* special case conversion for color4 */
        else if (NULL != strstr(assimp_material_property->mKey.C_Str(), "clr")
                && assimp_material_property->mType == aiPTI_Float
                && assimp_material_property->mDataLength == 4 * sizeof(float)) {
            jobject jassimp_data = NULL;

            /* wrap color */
            jvalue wrap_color_params[4];
            wrap_color_params[0].f =
                    ((float*) assimp_material_property->mData)[0];
            wrap_color_params[1].f =
                    ((float*) assimp_material_property->mData)[1];
            wrap_color_params[2].f =
                    ((float*) assimp_material_property->mData)[2];
            wrap_color_params[3].f =
                    ((float*) assimp_material_property->mData)[3];
            if (!call_static_object(env, "org/util/jassimp/Jassimp",
                    "wrapColor4", "(FFFF)Ljava/lang/Object;", wrap_color_params,
                    jassimp_data)) {
                return NULL;
            }

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else if (assimp_material_property->mType == aiPTI_Float
                && assimp_material_property->mDataLength == sizeof(float)) {
            jobject jassimp_data = NULL;

            jvalue new_float_params[1];
            new_float_params[0].f =
                    ((float*) assimp_material_property->mData)[0];
            if (!create_instance(env, "java/lang/Float", "(F)V",
                    new_float_params, jassimp_data)) {
                return NULL;
            }

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else if (assimp_material_property->mType == aiPTI_Integer
                && assimp_material_property->mDataLength == sizeof(int)) {
            jobject jassimp_data = NULL;

            jvalue new_int_params[1];
            new_int_params[0].i = ((int*) assimp_material_property->mData)[0];
            if (!create_instance(env, "java/lang/Integer", "(I)V",
                    new_int_params, jassimp_data)) {
                return NULL;
            }

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else if (assimp_material_property->mType == aiPTI_String) {
            /* skip length prefix */
            jobject jassimp_data = env->NewStringUTF(
                    assimp_material_property->mData + 4);

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else {
            constructor_params[4].i = assimp_material_property->mDataLength;

            /* generic copy code, uses dump ByteBuffer on java side */
            if (!create_instance(env, "org/util/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIII)V", constructor_params,
                    jassimp_material_property)) {
                return NULL;
            }

            jobject jassimp_buffer = NULL;

            if (!get_field(env, jassimp_material_property, "m_data",
                    "Ljava/lang/Object;", jassimp_buffer)) {
                return NULL;
            }

            if (env->GetDirectBufferCapacity(jassimp_buffer)
                    != assimp_material_property->mDataLength) {
                LOGE("invalid direct buffer\n");
                return NULL;
            }

            void* jassimp_buffer_ptr = env->GetDirectBufferAddress(
                    jassimp_buffer);

            if (NULL == jassimp_buffer_ptr) {
                LOGE("could not access direct buffer\n");
                return NULL;
            }

            memcpy(jassimp_buffer_ptr, assimp_material_property->mData,
                    assimp_material_property->mDataLength);
        }

        /* add property */
        jobject jassimp_properties = NULL;

        if (!get_field(env, jassimp_material, "m_properties",
                "Ljava/util/List;", jassimp_properties)) {
            return NULL;
        }

        jvalue add_properties_params[1];
        add_properties_params[0].l = jassimp_material_property;
        if (!call(env, jassimp_properties, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_properties_params)) {
            return NULL;
        }
    }
    return jassimp_material;
}
}
