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
#include "objects/uniform_block.h"
#include "glm/gtc/type_ptr.hpp"
#include <cctype>
#include "util/gvr_gl.h"
#include "vulkan/vulkanCore.h"
namespace gvr {

UniformBlock::UniformBlock(const std::string& descriptor) :
        //Descriptor(descriptor),
        TotalSize(0),
        UniformData(NULL)
{
    ownData = false;
    if (!descriptor.empty())
    {
        LOGE("setting descriptor");
        setDescriptor(descriptor);
    }
}

UniformBlock::UniformBlock() :
        TotalSize(0),
        UniformData(NULL)
{
    ownData = false;
}

bool UniformBlock::setInt(std::string name, int val)
{
    int size = sizeof(int);
    char* data = getData(name, size);
    if (data != NULL)
    {
        *((int*) data) = val;
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setFloat(std::string name, float val) {
    int size = sizeof(float);
    char* data = getData(name, size);
    if (data != NULL)
    {
        *((float*) data) = val;
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setVec(std::string name, const float* val, int n)
{
    int bytesize = n * sizeof(float);
    char* data = getData(name, bytesize);
    if (data != NULL)
    {
        memcpy(data, val, bytesize);
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setIntVec(std::string name, const int* val, int n) {
    int bytesize = n * sizeof(int);
    char* data = getData(name, bytesize);
    if (data != NULL)
    {
        memcpy(data, val, bytesize);
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setVec2(std::string name, const glm::vec2& val)
{
    int bytesize = 2 * sizeof(float);
    float* data = (float*)getData(name, bytesize);
    if (data != NULL)
    {
        data[0] = val.x;
        data[1] = val.y;
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setVec3(std::string name, const glm::vec3& val)
{
    int bytesize = 3 * sizeof(float);
    float* data = (float*)getData(name, bytesize);
    if (data != NULL)
    {
        data[0] = val.x;
        data[1] = val.y;
        data[2] = val.z;
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setVec4(std::string name, const glm::vec4& val)
{
    int bytesize = 4 * sizeof(float);
    float* data = (float*)getData(name, bytesize);
    if (data != NULL)
    {
        data[0] = val.x;
        data[1] = val.y;
        data[2] = val.z;
        data[3] = val.w;
        setDirty();
        return true;
    }
    return false;
}

bool UniformBlock::setMat4(std::string name, const float* val)
{
    int bytesize = 16 * sizeof(float);
    char* data = getData(name, bytesize);
    if (data != NULL)
    {
        memcpy(data, (val), bytesize);
        setDirty();
        return true;
    }
    return false;
}

const glm::vec2* UniformBlock::getVec2(std::string name) const
{
    int size = 2 * sizeof(float);
    const char* data = getData(name, size);
    if (data != NULL)
        return (reinterpret_cast<const glm::vec2*>(data));
    return NULL;
}

const glm::vec3* UniformBlock::getVec3(std::string name) const {
    int size = 3 * sizeof(float);
    const char* data = getData(name, size);
    if (data != NULL)
        return (reinterpret_cast<const glm::vec3*> (data));
    return NULL;
}

const glm::vec4* UniformBlock::getVec4(std::string name) const {
    int size = 4 * sizeof(float);
    const char* data = getData(name, size);
    if (data != NULL)
        return (reinterpret_cast<const glm::vec4*> (data));
    return NULL;
}

int UniformBlock::getInt(std::string name) const
{
    int size = sizeof(int);
    const char* data = getData(name, size);
    if (data != NULL)
        return *(reinterpret_cast<const int*> (data));
    return 0;
}

float UniformBlock::getFloat(std::string name) const
{
    int size = sizeof(float);
    const char* data = getData(name, size);
    if (data != NULL)
        return *(reinterpret_cast<const float*> (data));
    return 0.0f;
}

bool UniformBlock::getIntVec(std::string name, int* val, int n) const
{
    int size = n * sizeof(int);
    const char* data = getData(name, size);
    if (data != NULL)
    {
        memcpy((char*) val, data,size);
        return true;
    }
    LOGE("ERROR: UniformBlock element %s not found\n", name.c_str());
    return false;
}

bool UniformBlock::getVec(std::string name, float* val, int n) const
{
    int size =  n * sizeof(float);
    const char* data = getData(name, size);
    if (data != NULL)
    {
        memcpy((char*) val, data, n * sizeof(float));
        return true;
    }
    LOGE("ERROR: UniformBlock element %s not found\n", name.c_str());
    return false;
}

bool UniformBlock::getMat4(std::string name, glm::mat4& val) const
{
    int bytesize = 16 * sizeof(float);
    const char* data = getData(name, bytesize);
    if (data != NULL)
    {
        val = glm::make_mat4((const float*) data);
        return true;
    }
    return false;
}

void  UniformBlock::parseDescriptor()
{
    const char* p = Descriptor.c_str();
    const char* type_start;
    int type_size;
    const char* name_start;
    int name_size;
    int offset = 0;
    const int VEC4_BOUNDARY = (sizeof(float) * 4) - 1;
    TotalSize = 0;
    while (*p)
    {
        while (std::isspace(*p) || *p == ';'|| *p == ',')
            ++p;
        type_start = p;
        if (*p == 0)
            break;
        while (std::isalnum(*p))
            ++p;
        type_size = p - type_start;
        if (type_size == 0)
        {
            LOGE("UniformBlock: SYNTAX ERROR: expecting data type\n");
            break;
        }
        std::string type(type_start, type_size);
        while (std::isspace(*p))
            ++p;
        name_start = p;
        while (std::isalnum(*p) || (*p == '_'))
            ++p;
        name_size = p - name_start;

        // check if it is array
        int array_size = 0;

        if( (*p == '[')){
            ++p;
            while(std::isdigit(*p))   {
                array_size = array_size * 10 + ((*p) - 48);
                ++p;
            }

            ++p;
        }
        else
            array_size = 1;


        if (name_size == 0)
        {
            LOGE("UniformBlock: SYNTAX ERROR: expecting uniform name\n");
            break;
        }
        Uniform uniform;
        std::string name(name_start, name_size);
        int nvecs;

        uniform.Name = name;
        uniform.Type = type;
        uniform.Offset = offset;
        uniform.Size = calcSize(type) * array_size;                // get number of bytes


        if (uniform.Size == 0)
            continue;
        if (offset & VEC4_BOUNDARY)                   // pad to 4 float boundary?
            uniform.Offset = offset = (offset + VEC4_BOUNDARY) & ~VEC4_BOUNDARY;
        std::pair<std::string, Uniform> pair(name, uniform);
        std::pair< std::map<std::string, Uniform>::iterator, bool > ret = UniformMap.insert(pair);
        if (!ret.second)
        {
            LOGE("UniformBlock: ERROR: element %s specified twice\n", name.c_str());
            continue;
        }
        LOGD("UniformBlock: %s offset=%d size=%d\n", name.c_str(), uniform.Offset, uniform.Size);
        offset += uniform.Size;
        TotalSize += uniform.Size;
    }
    if (TotalSize > 0)
    {
        if (TotalSize & VEC4_BOUNDARY)                 // pad to 4 float boundary?
            TotalSize = (TotalSize + VEC4_BOUNDARY) & ~VEC4_BOUNDARY;
        LOGD("UniformBlock: allocating uniform block of %d bytes\n", TotalSize);
        UniformData = new char[TotalSize];
        ownData = true;
    }
    else
    {
        LOGE("UniformBlock: ERROR: no uniform block allocated\n");
    }
}

int UniformBlock::calcSize(std::string type) const
{
    if (type == "float") return sizeof(float);
    if (type == "float3") return 4 * sizeof(float);
    if (type == "float4") return 4 * sizeof(float);
    if (type == "float2") return 2 * sizeof(float);
    if (type == "int") return sizeof(int);
    if (type == "int3") return 4 * sizeof(int);
    if (type == "int4") return 4 * sizeof(int);
    if (type == "float2") return 2 * sizeof(int);
    if (type == "mat4") return 16 * sizeof(float);
    if (type == "mat3") return 12 * sizeof(float);
    LOGE("UniformBlock: SYNTAX ERROR: unknown type %s\n", type.c_str());
    return 0;
}

UniformBlock::Uniform* UniformBlock::getUniform(std::string name, int& bytesize)
{
    auto it = UniformMap.find(name);
    if (it == UniformMap.end())
    {
        LOGE("ERROR: UniformBlock element %s not found\n", name.c_str());
        return NULL;
    }
    Uniform& u = it->second;
    if (u.Size < bytesize)
    {
        LOGE("ERROR: UniformBlock element %s is %d bytes, should be %d bytes\n", name.c_str(), bytesize, u.Size);
        return NULL;
    }
    bytesize = u.Size;
    return &u;
}

const UniformBlock::Uniform* UniformBlock::getUniform(std::string name, int& bytesize) const
{
    auto it = UniformMap.find(name);
    if (it == UniformMap.end())
    {
        LOGE("ERROR: UniformBlock element %s not found\n", name.c_str());
        return NULL;
    }
    const Uniform& u = it->second;
    if (u.Size < bytesize)
    {
        LOGE("ERROR: UniformBlock element %s is %d bytes, should be %d bytes\n", name.c_str(), bytesize, u.Size);
        return NULL;
    }
    bytesize = u.Size;
    return &u;
}

const char* UniformBlock::getData(std::string name, int& bytesize) const
{
    const Uniform* u = getUniform(name, bytesize);
    if (u == NULL)
        return NULL;
    char* data = (char*) UniformData;
    data += u->Offset;
    return data;
}

char* UniformBlock::getData(std::string name, int& bytesize)
{
    Uniform* u = getUniform(name, bytesize);
    if (u == NULL)
        return NULL;
    char* data = (char*) UniformData;
    data += u->Offset;
    return data;
}


GLUniformBlock::GLUniformBlock(const std::string& descriptor) :
    UniformBlock(descriptor),
    GLBlockIndex(-1),
    GLBindingPoint(-1),
    GLOffset(0),
    GLBuffer(0)
{
}

GLUniformBlock::GLUniformBlock() :
    UniformBlock(),
    GLBlockIndex(-1),
    GLOffset(0),
    GLBuffer(0),
    GLBindingPoint(-1)
{

}

void GLUniformBlock::bindBuffer(GLuint programId)
{

    if (GLBindingPoint < 0)
        return;
    if (GLBlockIndex < 0)
    {
        GLBlockIndex = glGetUniformBlockIndex(programId, BlockName.c_str());
        if (GLBlockIndex < 0)
        {
            LOGE("UniformBlock: ERROR: cannot find block named %s\n", BlockName.c_str());
            return;
        }

        glGenBuffers(1, &GLBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, GLBuffer);
        glBufferData(GL_UNIFORM_BUFFER, TotalSize, NULL, GL_DYNAMIC_DRAW);
        glUniformBlockBinding(programId, GLBlockIndex, GLBindingPoint);
        glBindBufferBase(GL_UNIFORM_BUFFER, GLBindingPoint, GLBuffer);
        checkGlError("bindUBO ");
        LOGD("UniformBlock: %s bound to #%d at index %d buffer = %d\n", BlockName.c_str(), GLBindingPoint, GLBlockIndex, GLBuffer);
    }
    else {
        glBindBuffer(GL_UNIFORM_BUFFER, GLBuffer);
        glBindBufferBase(GL_UNIFORM_BUFFER, GLBindingPoint, GLBuffer);
    }
}

void GLUniformBlock::render(GLuint programId)
{
    auto it = Dirty.find(programId);

    if (it != Dirty.end() && !it->second)
        return;

  //  LOGE("it should not come hrere");
    Dirty[programId] = false;
    if (GLBuffer == 0 )
        bindBuffer(programId);
    if (GLBuffer >= 0)
    {
        glBindBuffer(GL_UNIFORM_BUFFER, GLBuffer);
        glBindBufferBase(GL_UNIFORM_BUFFER, GLBindingPoint, GLBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, GLOffset, TotalSize, UniformData);
    }
}

void GLUniformBlock::dump(GLuint programID, int blockIndex)
{
    // get size of name of the uniform block
    GLint nameLength;
    glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_NAME_LENGTH, &nameLength);

    // get name of uniform block
    GLchar blockName[nameLength];
    glGetActiveUniformBlockName(programID, blockIndex, nameLength, NULL, blockName);

    // get size of uniform block in bytes
    GLint byteSize;
    glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, &byteSize);

    // get number of uniform variables in uniform block
    GLint numberOfUniformsInBlock;
    glGetActiveUniformBlockiv(programID, blockIndex,
                         GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, &numberOfUniformsInBlock);

    // get indices of uniform variables in uniform block
    GLint uniformsIndices[numberOfUniformsInBlock];
    glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformsIndices);
    LOGD("UniformBlock: %s %d bytes\n", blockName, byteSize);

    // get parameters of all uniform variables in uniform block
    for (int uniformMember=0; uniformMember<numberOfUniformsInBlock; uniformMember++)
    {
       if (uniformsIndices[uniformMember] > 0)
       {
          // index of uniform variable
          GLuint tUniformIndex = uniformsIndices[uniformMember];

          uniformsIndices[uniformMember];
          GLint uniformNameLength, uniformOffset, uniformSize;
          GLint uniformType, arrayStride, matrixStride;

          // get length of name of uniform variable
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                      GL_UNIFORM_NAME_LENGTH, &uniformNameLength);
          // get name of uniform variable
          GLchar uniformName[uniformNameLength];
          glGetActiveUniform(programID, tUniformIndex, uniformNameLength,
                  NULL, NULL, NULL, uniformName);

          // get offset of uniform variable related to start of uniform block
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                         GL_UNIFORM_OFFSET, &uniformOffset);
          // get size of uniform variable (number of elements)
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                         GL_UNIFORM_SIZE, &uniformSize);
          // get type of uniform variable (size depends on this value)
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                         GL_UNIFORM_TYPE, &uniformType);
          // offset between two elements of the array
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                         GL_UNIFORM_ARRAY_STRIDE, &arrayStride);
          // offset between two vectors in matrix
          glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                         GL_UNIFORM_MATRIX_STRIDE, &matrixStride);

          // Size of uniform variable in bytes
          byteSize = uniformSize * sizeFromUniformType(uniformType);
          LOGD("UniformBlock: %s GL offset = %d, byteSize = %d\n", uniformName, uniformOffset, byteSize);
       }
    }
}

GLint GLUniformBlock::sizeFromUniformType(GLint type)
{
    GLint s;

    #define UNI_CASE(type, numElementsInType, elementType) \
       case type : s = numElementsInType * sizeof(elementType); break;

    switch (type)
    {
       UNI_CASE(GL_FLOAT, 1, GLfloat);
       UNI_CASE(GL_FLOAT_VEC2, 2, GLfloat);
       UNI_CASE(GL_FLOAT_VEC3, 3, GLfloat);
       UNI_CASE(GL_FLOAT_VEC4, 4, GLfloat);
       UNI_CASE(GL_INT, 1, GLint);
       UNI_CASE(GL_INT_VEC2, 2, GLint);
       UNI_CASE(GL_INT_VEC3, 3, GLint);
       UNI_CASE(GL_INT_VEC4, 4, GLint);
       UNI_CASE(GL_UNSIGNED_INT, 1, GLuint);
       UNI_CASE(GL_UNSIGNED_INT_VEC2, 2, GLuint);
       UNI_CASE(GL_UNSIGNED_INT_VEC3, 3, GLuint);
       UNI_CASE(GL_UNSIGNED_INT_VEC4, 4, GLuint);
       UNI_CASE(GL_BOOL, 1, GLboolean);
       UNI_CASE(GL_BOOL_VEC2, 2, GLboolean);
       UNI_CASE(GL_BOOL_VEC3, 3, GLboolean);
       UNI_CASE(GL_BOOL_VEC4, 4, GLboolean);
       UNI_CASE(GL_FLOAT_MAT2, 4, GLfloat);
       UNI_CASE(GL_FLOAT_MAT3, 9, GLfloat);
       UNI_CASE(GL_FLOAT_MAT4, 16, GLfloat);
       UNI_CASE(GL_FLOAT_MAT2x3, 6, GLfloat);
       UNI_CASE(GL_FLOAT_MAT2x4, 8, GLfloat);
       UNI_CASE(GL_FLOAT_MAT3x2, 6, GLfloat);
       UNI_CASE(GL_FLOAT_MAT3x4, 12, GLfloat);
       UNI_CASE(GL_FLOAT_MAT4x2, 8, GLfloat);
       UNI_CASE(GL_FLOAT_MAT4x3, 12, GLfloat);
       default : s = 0; break;
    }
    return s;
}

VulkanUniformBlock::VulkanUniformBlock(const std::string& descriptor) :
    UniformBlock(descriptor), buffer_init_(false){

    LOGE("pararameter");
}

VulkanUniformBlock::VulkanUniformBlock() :
    UniformBlock(), buffer_init_(false)
{
    LOGE("default");
}
void VulkanUniformBlock::updateBuffer(VkDevice &device,VulkanCore* vk){

  //  if(!buffer_init_)
  //      createBuffer(device, vk);

    VkResult ret = VK_SUCCESS;
    uint8_t *pData;

    ret = vkMapMemory(device, m_bufferInfo.mem, 0, m_bufferInfo.allocSize, 0, (void **) &pData);
    assert(!ret);

    memcpy(pData, UniformData, TotalSize);

    vkUnmapMemory(device, m_bufferInfo.mem);

}
void VulkanUniformBlock::createBuffer(VkDevice &device,VulkanCore* vk){

    VkResult err = VK_SUCCESS;
     memset(&m_bufferInfo, 0, sizeof(m_bufferInfo));
      //err = vkCreateBuffer(m_device, &bufferCreateInfo, NULL, &m_modelViewMatrixUniform.buf);
      err = vkCreateBuffer(device, gvr::BufferCreateInfo(TotalSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT), NULL, &m_bufferInfo.buf);
      assert(!err);
        LOGE("size of the buffer is %d" ,TotalSize);
      // Obtain the requirements on memory for this buffer
      VkMemoryRequirements mem_reqs;
      vkGetBufferMemoryRequirements(device, m_bufferInfo.buf, &mem_reqs);
      assert(!err);

      // And allocate memory according to those requirements
      VkMemoryAllocateInfo memoryAllocateInfo;
      memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
      memoryAllocateInfo.pNext = NULL;
      memoryAllocateInfo.allocationSize = 0;
      memoryAllocateInfo.memoryTypeIndex = 0;
      memoryAllocateInfo.allocationSize  = mem_reqs.size;
      bool pass = vk->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, &memoryAllocateInfo.memoryTypeIndex);
      assert(pass);

      // We keep the size of the allocation for remapping it later when we update contents
      m_bufferInfo.allocSize = memoryAllocateInfo.allocationSize;

      err = vkAllocateMemory(device, &memoryAllocateInfo, NULL, &m_bufferInfo.mem);
      assert(!err);

          // Bind our buffer to the memory
          err = vkBindBufferMemory(device, m_bufferInfo.buf, m_bufferInfo.mem, 0);
          assert(!err);

          m_bufferInfo.bufferInfo.buffer = m_bufferInfo.buf;
          m_bufferInfo.bufferInfo.offset = 0;
          m_bufferInfo.bufferInfo.range = TotalSize;

}
}

