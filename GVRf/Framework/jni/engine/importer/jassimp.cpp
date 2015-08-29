/*
 ---------------------------------------------------------------------------
 Open Asset Import Library
 ---------------------------------------------------------------------------
 Copyright (c) 2006-2012, assimp team
 All rights reserved.
 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:
 * Redistributions of source code must retain the above
 copyright notice, this list of conditions and the
 following disclaimer.
 * Redistributions in binary form must reproduce the above
 copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other
 materials provided with the distribution.
 * Neither the name of the assimp team, nor the names of its
 contributors may be used to endorse or promote products
 derived from this software without specific prior
 written permission of the assimp team.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ---------------------------------------------------------------------------
 */

#include "jassimp.h"

namespace gvr {

class DeleteLocalRef {
private:
    JNIEnv* mEnv;
    jobject& mObject;
public:
    DeleteLocalRef(JNIEnv* env, jobject& object) : mEnv(env), mObject(object) {};
    DeleteLocalRef(JNIEnv* env, jclass& clazz) : mEnv(env), mObject((jobject&) clazz) {};
    DeleteLocalRef(JNIEnv* env, jfloatArray& farray) : mEnv(env), mObject((jobject&) farray) {};
    DeleteLocalRef(JNIEnv* env, jintArray& iarray) : mEnv(env), mObject((jobject&) iarray) {};
    DeleteLocalRef(JNIEnv* env, jstring& str) : mEnv(env), mObject((jobject&) str) {};
    ~DeleteLocalRef() {
        if (mObject != NULL) {
            mEnv->DeleteLocalRef(mObject);
        }
    }
};


bool create_instance(JNIEnv *env, const char* class_name,
        jobject& new_instance) {
    jclass java_class = env->FindClass(class_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID constructor_id = env->GetMethodID(java_class, "<init>", "()V");

    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == constructor_id) {
        return false;
    }

    new_instance = env->NewObject(java_class, constructor_id);
    if (NULL == new_instance) {
        return false;
    }

    return true;
}

bool create_instance(JNIEnv *env, const char* class_name, const char* signature,/* const*/
jvalue* params, jobject& new_instance) {
    jclass java_class = env->FindClass(class_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID constructor_id = env->GetMethodID(java_class, "<init>",
            signature);

    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == constructor_id) {
        return false;
    }

    new_instance = env->NewObjectA(java_class, constructor_id, params);
    if (NULL == new_instance) {
        return false;
    }

    return true;
}

bool get_field(JNIEnv *env, jobject object, const char* field_name,
        const char* signature, jobject& field) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, signature);
    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == field_id) {
        return false;
    }

    field = env->GetObjectField(object, field_id);

    return true;
}

bool set_object_field(JNIEnv *env, jobject object, const char* field_name,
        const char* signature, jobject value) {
    jclass java_class = env->GetObjectClass(object);

    if (NULL == java_class) {
        return false;
    }

    jfieldID field_id = env->GetFieldID(java_class, field_name, signature);
    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == field_id) {
        return false;
    }

    env->SetObjectField(object, field_id, value);

    return true;
}

bool call_method(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetMethodID(java_class, method_name, signature);
    if (NULL == method_id) {
        return false;
    }

    jboolean return_value = env->CallBooleanMethod(object, method_id,
            params[0].l);
    DeleteLocalRef clazzRef(env, java_class);

    return (bool) return_value;
}

bool call_void_method(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetMethodID(java_class, method_name, signature);
    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == method_id) {
        return false;
    }

    env->CallVoidMethodA(object, method_id, params);

    return true;
}

bool call_static_object(JNIEnv *env, const char* type_name,
        const char* method_name, const char* signature,/* const*/jvalue* params,
        jobject& return_value) {
    jclass java_class = env->FindClass(type_name);

    if (NULL == java_class) {
        return false;
    }

    jmethodID method_id = env->GetStaticMethodID(java_class, method_name,
            signature);
    DeleteLocalRef clazzRef(env, java_class);
    if (NULL == method_id) {
        return false;
    }

    return_value = env->CallStaticObjectMethodA(java_class, method_id, params);

    return true;
}

bool load_scene_node(JNIEnv *env, const aiNode *assimp_node, jobject parent,
        jobject* loaded_node) {
    /* wrap matrix */
    jfloatArray jassimp_wrap_matrix = env->NewFloatArray(16);
    env->SetFloatArrayRegion(jassimp_wrap_matrix, 0, 16,
            (jfloat*) &assimp_node->mTransformation);

    jvalue wrap_matrix_params[1];
    wrap_matrix_params[0].l = jassimp_wrap_matrix;
    jobject jassimp_matrix;
    DeleteLocalRef jassimp_wrap_matrix_ref(env, jassimp_wrap_matrix);

    if (!call_static_object(env, "org/gearvrf/jassimp/Jassimp", "wrapMatrix",
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

    DeleteLocalRef jassimp_matrix_ref(env, jassimp_matrix);
    DeleteLocalRef jassimp_mesh_ref_arr_ref(env, jassimp_mesh_ref_arr);
    DeleteLocalRef jassimp_node_name_ref(env, jassimp_node_name);

    jobject jassimp_node;
    if (!call_static_object(env, "org/gearvrf/jassimp/Jassimp", "wrapSceneNode",
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

bool load_scene_graph(JNIEnv *env, const aiScene* assimp_scene,
        jobject& jassimp_scene) {
    if (NULL != assimp_scene->mRootNode) {
        jobject jassimp_root;
        DeleteLocalRef ref(env, jassimp_root);

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

jobject mesh_material(JNIEnv *env, const aiScene *assimp_scene, int index) {
    aiMesh* assimp_mesh = assimp_scene->mMeshes[index];
    aiMaterial* assimp_material =
            assimp_scene->mMaterials[assimp_mesh->mMaterialIndex];
    jobject jassimp_material = NULL;

    if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial",
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

        if (!call_void_method(env, jassimp_material,
                "org/gearvrf/jassimp/AiMaterial", "setTextureNumber", "(II)V",
                set_number_params)) {
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
            if (!call_static_object(env, "org/gearvrf/jassimp/Jassimp",
                    "wrapColor3", "(FFF)Ljava/lang/Object;", wrap_color_params,
                    jassimp_data)) {
                return NULL;
            }

            DeleteLocalRef jassimp_data_ref(env, jassimp_data);
            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
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
            if (!call_static_object(env, "org/gearvrf/jassimp/Jassimp",
                    "wrapColor4", "(FFFF)Ljava/lang/Object;", wrap_color_params,
                    jassimp_data)) {
                return NULL;
            }
            DeleteLocalRef jassimp_data_ref(env, jassimp_data);

            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
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

            DeleteLocalRef jassimp_data_ref(env, jassimp_data);
            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
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

            DeleteLocalRef jassimp_data_ref(env, jassimp_data);
            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else if (assimp_material_property->mType == aiPTI_String) {
            /* skip length prefix */
            jobject jassimp_data = env->NewStringUTF(
                    assimp_material_property->mData + 4);

            DeleteLocalRef jassimp_data_ref(env, jassimp_data);
            constructor_params[4].l = jassimp_data;
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIILjava/lang/Object;)V",
                    constructor_params, jassimp_material_property)) {
                return NULL;
            }
        } else {
            constructor_params[4].i = assimp_material_property->mDataLength;

            /* generic copy code, uses dump ByteBuffer on java side */
            if (!create_instance(env, "org/gearvrf/jassimp/AiMaterial$Property",
                    "(Ljava/lang/String;IIII)V", constructor_params,
                    jassimp_material_property)) {
                return NULL;
            }

            jobject jassimp_buffer = NULL;

            if (!get_field(env, jassimp_material_property, "m_data",
                    "Ljava/lang/Object;", jassimp_buffer)) {
                return NULL;
            }
            DeleteLocalRef jassimp_buffer_ref(env, jassimp_buffer);

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

        DeleteLocalRef jassimp_properties_ref(env, jassimp_properties);
        DeleteLocalRef jassimp_material_property_ref(env, jassimp_material_property);
        jvalue add_properties_params[1];
        add_properties_params[0].l = jassimp_material_property;
        if (!call_method(env, jassimp_properties, "java/util/Collection", "add",
                "(Ljava/lang/Object;)Z", add_properties_params)) {
            return NULL;
        }
    }
    return jassimp_material;
}
}
