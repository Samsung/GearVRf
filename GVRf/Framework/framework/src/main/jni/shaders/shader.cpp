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
 * A shader which an user can add in run-time.
 ***************************************************************************/

#include <jni_utils.h>
#include "shader.h"
#include "matrix_calc.h"

namespace gvr {
Shader::Shader(int id,
               const char* signature,
               const char* uniformDescriptor,
               const char* textureDescriptor,
               const char* vertexDescriptor,
               const char* vertex_shader,
               const char* fragment_shader,
               const char* matrixCalc)
    : mId(id), mSignature(signature),
      mUniformDesc(uniformDescriptor),
      mTextureDesc(textureDescriptor),
      mVertexDesc(vertexDescriptor),
      mVertexShader(vertex_shader),
      mFragmentShader(fragment_shader),
      mUseMatrixUniforms(false),
      mOutputBufferSize(0),
      mUseLights(false),
      mUseShadowMaps(false),
      mUseHasBones(false),
      mMatrixCalc(nullptr),
      mUseMaterialGPUBuffer(false)
{
    if (strstr(vertex_shader, "u_matrices") ||
        strstr(fragment_shader, "u_matrices"))
        mUseMatrixUniforms = true;
    if (strstr(vertex_shader, "Material_ubo") ||
        strstr(fragment_shader, "Material_ubo"))
        mUseMaterialGPUBuffer = true;
    if (strstr(signature, "$LIGHTSOURCES"))
        mUseLights = true;
    if (strstr(signature, "$SHADOWS"))
        mUseShadowMaps = true;
    if (strstr(vertex_shader, "Bones_ubo"))
        mUseHasBones = true;
    LOGD("SHADER: %s\n%s\n%s\n%s", signature, uniformDescriptor, textureDescriptor, vertexDescriptor);
    if (matrixCalc)
    {
        mMatrixCalc = new MatrixCalc(matrixCalc);
        mOutputBufferSize = mMatrixCalc->getNumOutputs();
    }
}

int Shader::calcMatrix(const glm::mat4* inputMatrices, glm::mat4* outputMatrices)
{
    if (mMatrixCalc == nullptr)
    {
        return 0;
    }
    if (mMatrixCalc->calculate(inputMatrices, outputMatrices))
    {
        return mMatrixCalc->getNumOutputs();
    }
    return -1;
}

int Shader::calcSize(const char* type)
{
    return DataDescriptor::calcSize(type) / sizeof(float);
}



} /* namespace gvr */
