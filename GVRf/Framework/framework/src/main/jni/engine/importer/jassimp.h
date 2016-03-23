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

#ifndef JASSIMP_H_
#define JASSIMP_H_

#include <android/bitmap.h>
#include <memory>
#include <vector>
#include <string>
#include <map>

#include "assimp/Importer.hpp"
#include "assimp/material.h"
#include "assimp/cimport.h"
#include "assimp/scene.h"
#include "util/gvr_log.h"
#include "glm/glm.hpp"

namespace gvr {

bool create_instance(JNIEnv *env, const char* class_name,
        jobject& new_instance);

bool create_instance(JNIEnv *env, const char* class_name, const char* signature,/* const*/
jvalue* params, jobject& new_instance);

bool get_field(JNIEnv *env, jobject object, const char* field_name,
        const char* signature, jobject& field);

bool set_object_field(JNIEnv *env, jobject object, const char* field_name,
        const char* signature, jobject value);

bool call_method(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params);

bool call_void_method(JNIEnv *env, jobject object, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params);

bool call_static_object(JNIEnv *env, const char* type_name,
        const char* method_name, const char* signature,/* const*/
        jvalue* params, jobject& return_value);

bool load_scene_node(JNIEnv *env, const aiNode *node, jobject parent,
        jobject* loaded_node = NULL);

bool load_scene_graph(JNIEnv *env, const aiScene* assimp_scene, jobject& scene);

jobject mesh_material(JNIEnv *env, const aiScene *assimp_scene, int index);

}
#endif
