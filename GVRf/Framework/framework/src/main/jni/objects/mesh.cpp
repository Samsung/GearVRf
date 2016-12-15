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
 * The mesh for rendering.
 ***************************************************************************/

#include "mesh.h"

#include "assimp/Importer.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "objects/helpers.h"

namespace gvr {
std::vector<std::string> Mesh::dynamicAttribute_Names_ = {"a_bone_indices", "a_bone_weights"};
    std::vector<std::string> static getTokens(const std::string &input){
        std::vector <std::string> tokens;
        int prev = 0;
        for(uint i = 0; i < input.length(); i++){
            if(input[i] == '#'){
                std::string token = input.substr(prev, i-prev);
                if(token.find("a_texcoord")!=std::string::npos)
                    tokens.push_back(token);
                prev = i+1;
            }
            else{

            }
        }

        return tokens;
    }

Mesh* Mesh::createBoundingBox() {

    Mesh* mesh = new Mesh(std::string("float3 a_position "));

    getBoundingVolume(); // Make sure bounding_volume is valid

    glm::vec3 min_corner = bounding_volume.min_corner();
    glm::vec3 max_corner = bounding_volume.max_corner();

    float min_x = min_corner[0];
    float min_y = min_corner[1];
    float min_z = min_corner[2];
    float max_x = max_corner[0];
    float max_y = max_corner[1];
    float max_z = max_corner[2];

    mesh->vertices_.push_back(glm::vec3(min_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, max_z));

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(3);

    mesh->indices_.push_back(1);
    mesh->indices_.push_back(3);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(5);

    mesh->indices_.push_back(4);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(6);

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(0);
    mesh->indices_.push_back(4);
    mesh->indices_.push_back(6);

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(0);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(4);

    mesh->indices_.push_back(2);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(3);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(7);

    return mesh;
}

// an array of size:6 with Xmin, Ymin, Zmin and Xmax, Ymax, Zmax values
const BoundingVolume& Mesh::getBoundingVolume() {
    if (have_bounding_volume_) {
        return bounding_volume;
    }
    bounding_volume.reset();
    for (auto it = vertices_.begin(); it != vertices_.end(); ++it) {
        bounding_volume.expand(*it);
    }

    have_bounding_volume_ = true;
    return bounding_volume;
}

void Mesh::getTransformedBoundingBoxInfo(glm::mat4 *Mat,
        float *transformed_bounding_box) {

    if (have_bounding_volume_ == false) {
        getBoundingVolume();
    }

    glm::mat4 M = *Mat;
    float a, b;

    //Inspired by Graphics Gems - TransBox.c
    //Transform the AABB to the correct position in world space
    //Generate a new AABB from the non axis aligned bounding box

    transformed_bounding_box[0] = M[3].x;
    transformed_bounding_box[3] = M[3].x;

    transformed_bounding_box[1] = M[3].y;
    transformed_bounding_box[4] = M[3].y;

    transformed_bounding_box[2] = M[3].z;
    transformed_bounding_box[5] = M[3].z;

    glm::vec3 min_corner = bounding_volume.min_corner();
    glm::vec3 max_corner = bounding_volume.max_corner();

    for (int i = 0; i < 3; i++) {
        //x coord
        a = M[i].x * min_corner.x;
        b = M[i].x * max_corner.x;
        if (a < b) {
            transformed_bounding_box[0] += a;
            transformed_bounding_box[3] += b;
        } else {
            transformed_bounding_box[0] += b;
            transformed_bounding_box[3] += a;
        }

        //y coord
        a = M[i].y * min_corner.y;
        b = M[i].y * max_corner.y;
        if (a < b) {
            transformed_bounding_box[1] += a;
            transformed_bounding_box[4] += b;
        } else {
            transformed_bounding_box[1] += b;
            transformed_bounding_box[4] += a;
        }

        //z coord
        a = M[i].z * min_corner.z;
        b = M[i].z * max_corner.z;
        if (a < b) {
            transformed_bounding_box[2] += a;
            transformed_bounding_box[5] += b;
        } else {
            transformed_bounding_box[2] += b;
            transformed_bounding_box[5] += a;
        }
    }
}
int calcSize(std::string type)
{
    if (type == "float") return 1;
    if (type == "vec3") return 3;
    if (type == "vec4") return 4;
    if (type == "vec2") return 2;
    if (type == "float3") return 3;
    if (type == "float4") return 4;
    if (type == "float2") return 2;

    if (type == "int") return 1;
    if (type == "int3") return 4;
    if (type == "int4") return 4;
    if (type == "float2") return 2;
    if (type == "mat4") return 16;
    if (type == "mat3") return 12;
    return 0;
}

    void Mesh::getAttribData( std::string& descriptor,std::vector<GLAttributeMapping>& bindings, int& total_size){
        GLAttributeMapping binding;
        int vertices_len = 0;
        int attrib_index =0;
        binding.data = vertices_.data();
        vertices_len = vertices_.size();

        binding.data_type = "vec3";
        binding.size = calcSize(binding.data_type);
        binding.offset = total_size * sizeof(float);
        total_size +=calcSize(binding.data_type) ;
        binding.index = attrib_index++;
        bindings.push_back(binding);

        ////
        const std::vector<glm::vec2>& texcord = getVec2Vector("a_texcoord");

        if(vertices_len && vertices_len != texcord.size()){
            LOGE("ERROR: length of vector is not same as of vertices");
        }
        binding.data_type = "vec2";
        binding.size = calcSize(binding.data_type);
        binding.offset = total_size * sizeof(float);
        total_size +=calcSize(binding.data_type) ;
        binding.index = attrib_index++;
        binding.data = texcord.data();
        bindings.push_back(binding);


        if(descriptor.find("a_normal")!=std::string::npos || descriptor.find("normalTexture")!=std::string::npos){
            if(vertices_len && vertices_len != normals_.size()){
                LOGE("ERROR: length of tex cords is not same as of vertices");
            }
            binding.offset = total_size * sizeof(float);
            total_size +=calcSize(binding.data_type) ;
            binding.index = attrib_index++;
            binding.data = normals_.data();
            bindings.push_back(binding);
        }
        if(descriptor.find("normalTexture")!=std::string::npos) {
            const std::vector<glm::vec3>& curr = getVec3Vector("a_tangent");
            if(vertices_len && vertices_len != curr.size()){
                LOGE("ERROR: length of vector is not same as of vertices");
            }
            binding.offset = total_size * sizeof(float);
            total_size +=calcSize(binding.data_type) ;
            binding.index = attrib_index++;
            binding.data = curr.data();
            bindings.push_back(binding);

            // add bitangent
            const std::vector<glm::vec3>& curr1 = getVec3Vector("a_bitangent");
            if(vertices_len && vertices_len != curr1.size()){
                LOGE("ERROR: length of vector is not same as of vertices");
            }
            binding.offset = total_size * sizeof(float);
            total_size +=calcSize(binding.data_type) ;
            binding.index = attrib_index++;
            binding.data = curr1.data();
            bindings.push_back(binding);

        }
        std::vector<std::string> tokens = getTokens(descriptor);
        for(auto& it: tokens) {

            const std::vector<glm::vec2>& texcord = getVec2Vector(it);
            if(vertices_len && vertices_len != texcord.size()){
                LOGE("ERROR: length of vector is not same as of vertices");
            }
            binding.data_type = "vec2";
            binding.size = calcSize(binding.data_type);
            binding.offset = total_size * sizeof(float);
            total_size +=calcSize(binding.data_type) ;
            binding.index = attrib_index++;
            binding.data = texcord.data();
            bindings.push_back(binding);
        }

    }

VkFormat getDataType(std::string& type){
    if(type.compare("float")==0)
        return VK_FORMAT_R32_SFLOAT;

    if(type.compare("vec2")==0 || type.compare("float2")==0)
        return VK_FORMAT_R32G32_SFLOAT;

    if(type.compare("float3")==0 || type.compare("vec3")==0)
        return VK_FORMAT_R32G32B32_SFLOAT;

    if(type.compare("float4")==0 || type.compare("vec4")==0)
        return VK_FORMAT_R32G32B32A32_SFLOAT;

}

// call this from renderCamera, get attribute descriptor from shader
void Mesh::generateVKBuffers(std::string descriptor, VkDevice& m_device, VulkanCore* vulkanCore ){
        if (!vao_dirty_)
            return;
        int total_size = 0;
    getAttribData(descriptor, attrMapping, total_size);
    std::vector<GLfloat> buffer;
    createBuffer(buffer, vertices_.size());
        memset(&m_vertices, 0, sizeof(m_vertices));
        m_vertices.vi_bindings = new VkVertexInputBindingDescription[attrMapping.size()];
        m_vertices.vi_attrs = new VkVertexInputAttributeDescription[attrMapping.size()];
        VkResult   err;
        bool   pass;

        // Our m_vertices member contains the types required for storing
        // and defining our vertex buffer within the graphics pipeline

        // Create our buffer object.
        VkBufferCreateInfo bufferCreateInfo = {};
        bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
        bufferCreateInfo.pNext = nullptr;
        bufferCreateInfo.size = buffer.size() * sizeof(float) ;//sizeof(vb);//
        bufferCreateInfo.usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
        bufferCreateInfo.flags = 0;
        //err = vkCreateBuffer(m_device, &bufferCreateInfo, nullptr, &m_vertices.buf);
        err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(buffer.size() * sizeof(float), VK_BUFFER_USAGE_VERTEX_BUFFER_BIT), nullptr, &m_vertices.buf);
        GVR_VK_CHECK(!err);

        // Obtain the memory requirements for this buffer.
        VkMemoryRequirements mem_reqs;
        vkGetBufferMemoryRequirements(m_device, m_vertices.buf, &mem_reqs);
        GVR_VK_CHECK(!err);

        // And allocate memory according to those requirements.
        VkMemoryAllocateInfo memoryAllocateInfo = {};
        memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        memoryAllocateInfo.pNext = nullptr;
        memoryAllocateInfo.allocationSize = 0;
        memoryAllocateInfo.memoryTypeIndex = 0;
        memoryAllocateInfo.allocationSize  = mem_reqs.size;
        pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, &memoryAllocateInfo.memoryTypeIndex);
        GVR_VK_CHECK(pass);

        VkDeviceMemory mem_staging_vert;
        VkBuffer buf_staging_vert;
        err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(buffer.size() * sizeof(float), VK_BUFFER_USAGE_VERTEX_BUFFER_BIT), nullptr, &buf_staging_vert);
        GVR_VK_CHECK(!err);

        //err = vkAllocateMemory(m_device, &memoryAllocateInfo, nullptr, &m_vertices.mem);
        err = vkAllocateMemory(m_device, &memoryAllocateInfo, nullptr, &mem_staging_vert);
        GVR_VK_CHECK(!err);

        // Now we need to map the memory of this new allocation so the CPU can edit it.
        void *data;
        //err = vkMapMemory(m_device, m_vertices.mem, 0, memoryAllocateInfo.allocationSize, 0, &data);
        err = vkMapMemory(m_device, mem_staging_vert, 0, memoryAllocateInfo.allocationSize, 0, &data);
        GVR_VK_CHECK(!err);

        // Copy our triangle verticies and colors into the mapped memory area.
        //memcpy(data, vb, sizeof(vb));
        memcpy(data, buffer.data(), buffer.size()*sizeof(float));


        // Unmap the memory back from the CPU.
        vkUnmapMemory(m_device, mem_staging_vert);
        //vkUnmapMemory(m_device, m_vertices.mem);
        err = vkBindBufferMemory(m_device, buf_staging_vert, mem_staging_vert, 0);
        GVR_VK_CHECK(!err);

        // Create Device memory optimal
        pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, &memoryAllocateInfo.memoryTypeIndex);
        GVR_VK_CHECK(pass);
        err = vkAllocateMemory(m_device, &memoryAllocateInfo, nullptr, &m_vertices.mem);
        GVR_VK_CHECK(!err);
        // Bind our buffer to the memory.
        err = vkBindBufferMemory(m_device, m_vertices.buf, m_vertices.mem, 0);
        GVR_VK_CHECK(!err);

        VkCommandBuffer trnCmdBuf = vulkanCore->GetTransientCmdBuffer();
        VkCommandBufferBeginInfo beginInfo = {};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
        vkBeginCommandBuffer(trnCmdBuf, &beginInfo);
        VkBufferCopy copyRegion = {};
        copyRegion.srcOffset = 0; // Optional
        copyRegion.dstOffset = 0; // Optional
        copyRegion.size = bufferCreateInfo.size;
        vkCmdCopyBuffer(trnCmdBuf, buf_staging_vert, m_vertices.buf, 1, &copyRegion);
        vkEndCommandBuffer(trnCmdBuf);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = &trnCmdBuf;

        vkQueueSubmit(vulkanCore->getVkQueue(), 1, &submitInfo, VK_NULL_HANDLE);
        vkQueueWaitIdle(vulkanCore->getVkQueue());
        vkFreeCommandBuffers(m_device, vulkanCore->getTransientCmdPool(), 1, &trnCmdBuf);


        // The vertices need to be defined so that the pipeline understands how the
        // data is laid out. This is done by providing a VkPipelineVertexInputStateCreateInfo
        // structure with the correct information.
        m_vertices.vi.sType = VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
        m_vertices.vi.pNext = nullptr;
        // check this
        m_vertices.vi.vertexBindingDescriptionCount = attrMapping.size();
        m_vertices.vi.pVertexBindingDescriptions = m_vertices.vi_bindings;
        m_vertices.vi.vertexAttributeDescriptionCount = attrMapping.size();
        m_vertices.vi.pVertexAttributeDescriptions = m_vertices.vi_attrs;
        m_vertices.vi_bindings[0].binding = 0;
        m_vertices.vi_bindings[0].stride = total_size * sizeof(float); //sizeof(vb[0]);//
        m_vertices.vi_bindings[0].inputRate = VK_VERTEX_INPUT_RATE_VERTEX;

        for(int i=0; i< attrMapping.size(); i++){
           // check this

            m_vertices.vi_attrs[i].binding = GVR_VK_VERTEX_BUFFER_BIND_ID;
            m_vertices.vi_attrs[i].location = attrMapping[i].index;
            m_vertices.vi_attrs[i].format = getDataType(attrMapping[i].data_type); //float3
            m_vertices.vi_attrs[i].offset = attrMapping[i].offset;
        }

        m_indices.count = static_cast<uint32_t>(indices_.size());

         uint32_t indexBufferSize = m_indices.count *  sizeof(unsigned short);//sizeof(uint32_t);//*

             VkBufferCreateInfo indexbufferInfo = {};
             indexbufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
             indexbufferInfo.size = indexBufferSize;
             indexbufferInfo.usage = VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

             // Copy index data to a buffer visible to the host
             //err = vkCreateBuffer(m_device, &indexbufferInfo, nullptr, &m_indices.buffer);
             err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT), nullptr, &m_indices.buffer);
             GVR_VK_CHECK(!err);

             VkDeviceMemory mem_staging_indi;
             VkBuffer buf_staging_indi;
             err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT), nullptr, &buf_staging_indi);
             GVR_VK_CHECK(!err);


             vkGetBufferMemoryRequirements(m_device, m_indices.buffer, &mem_reqs);
             memoryAllocateInfo.allocationSize = mem_reqs.size;
             pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, &memoryAllocateInfo.memoryTypeIndex);
             GVR_VK_CHECK(pass);

             err = vkAllocateMemory(m_device, &memoryAllocateInfo, nullptr, &mem_staging_indi);
             GVR_VK_CHECK(!err);
             err = vkMapMemory(m_device, mem_staging_indi, 0, indexBufferSize, 0, &data);
             GVR_VK_CHECK(!err);
             //memcpy(data, indexBuffer.data(), indexBufferSize);
             memcpy(data, indices_.data(), indexBufferSize);
             vkUnmapMemory(m_device, mem_staging_indi);

             //err = vkBindBufferMemory(m_device, m_indices.buffer, m_indices.memory, 0);
             err = vkBindBufferMemory(m_device, buf_staging_indi, mem_staging_indi, 0);
             GVR_VK_CHECK(!err);

         // Create Device memory optimal
             pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, &memoryAllocateInfo.memoryTypeIndex);
             GVR_VK_CHECK(pass);
             err = vkAllocateMemory(m_device, &memoryAllocateInfo, nullptr, &m_indices.memory);
             GVR_VK_CHECK(!err);

     // Bind our buffer to the memory.
         err = vkBindBufferMemory(m_device, m_indices.buffer, m_indices.memory, 0);
         GVR_VK_CHECK(!err);

         trnCmdBuf = vulkanCore->GetTransientCmdBuffer();
         beginInfo = {};
         beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
         beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
         vkBeginCommandBuffer(trnCmdBuf, &beginInfo);
         copyRegion = {};
         copyRegion.srcOffset = 0; // Optional
         copyRegion.dstOffset = 0; // Optional
         copyRegion.size = indexBufferSize;
         vkCmdCopyBuffer(trnCmdBuf, buf_staging_indi, m_indices.buffer, 1, &copyRegion);
         vkEndCommandBuffer(trnCmdBuf);

         submitInfo = {};
         submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
         submitInfo.commandBufferCount = 1;
         submitInfo.pCommandBuffers = &trnCmdBuf;

         vkQueueSubmit(vulkanCore->getVkQueue(), 1, &submitInfo, VK_NULL_HANDLE);
         vkQueueWaitIdle(vulkanCore->getVkQueue());
         vkFreeCommandBuffers(m_device, vulkanCore->getTransientCmdPool(), 1, &trnCmdBuf);

        vao_dirty_ = false;

}
void Mesh::createAttributeMapping(int programId,
        int& totalStride, int& attrLen)
{
    totalStride = attrLen = 0;
    if (programId == -1)
    {
        // If program id has not been set, return.
        return;
    }
    GLint numActiveAtributes;
    glGetProgramiv(programId, GL_ACTIVE_ATTRIBUTES, &numActiveAtributes);
    GLchar attrName[512];
    GLAttributeMapping attrData;

    for (int i = 0; i < numActiveAtributes; i++)
    {
        GLsizei length;
        GLint size;
        GLenum type;
        glGetActiveAttrib(programId, i, 512, &length, &size, &type, attrName);
        if (std::find(dynamicAttribute_Names_.begin(), dynamicAttribute_Names_.end(), attrName) != dynamicAttribute_Names_.end())
        {
            // Skip dynamic attributes. Currently only bones are dynamic attributes which changes each frame.
            // They are handled seperately.
        }
        else
        {
            attrData.type = GL_FLOAT;
            int loc = glGetAttribLocation(programId, attrName);
            attrData.index = loc;
            attrData.data = NULL;
            attrData.offset = totalStride;
            bool addData = true;
            int len = 0;

            // Two things to note --
            // 1. The 3 builtin buffers are still seperate from the maps used for the other attributes
            // 2. The attribute index *has* to be 0, 1 and 2 for position, tex_coords and normal. The
            // index from querying via glGetActiveAttrib cannot be used. Needs analysis.
            if (strcmp(attrName, "a_position") == 0)
            {
                attrData.size = 3;
                len = vertices_.size();
                attrData.data = vertices_.data();
            }
            else if (strcmp(attrName, "a_normal") == 0)
            {
                attrData.size = 3;
                len = normals_.size();
                attrData.data = normals_.data();
            }
            else
            {

                switch (type)
                {
                    case GL_FLOAT:
                        attrData.size = 1;
                        {
                            const std::vector<float>& curr = getFloatVector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC2:
                        attrData.size = 2;
                        {
                            const std::vector<glm::vec2>& curr = getVec2Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC3:
                        attrData.size = 3;
                        {
                            const std::vector<glm::vec3>& curr = getVec3Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC4:
                        attrData.size = 4;
                        {
                            const std::vector<glm::vec4>& curr = getVec4Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    default:
                        addData = false;
                        LOGE("Looking up %s failed ", attrName);
                            break;
                }
            }
            if (addData)
            {
                totalStride += attrData.size;
                attrMapping.push_back(attrData);
                if (attrLen == 0)
                    attrLen = len;
                else
                {
                    if (len != attrLen)
                        LOGE(" $$$$*** Attib length does not match %d vs %d", len, attrLen);
                }
            }
        }
    }
}

void Mesh::createBuffer(std::vector<GLfloat>& buffer, int attrLength)
{
    for (int i = 0; i < attrLength; i++)
    {
        for (auto it = attrMapping.begin(); it != attrMapping.end(); ++it)
        {
            GLAttributeMapping currAttr = *it;
            const float* ptr = (float*) currAttr.data;
            for (int k = 0; k < currAttr.size; k++)
            {
                buffer.push_back(ptr[i * currAttr.size + k]);
            }
        }
    }
}


const GLuint Mesh::getVAOId(int programId) {
    if (programId == -1)
    {
        LOGI("!! %p Prog Id -- %d ", this, programId);
        return 0;
    }
    if (vao_dirty_)
    {
        generateVAO(programId);
    }
    auto it = program_ids_.find(programId);
    if (it != program_ids_.end())
    {
        GLVaoVboId id = it->second;
        return id.vaoID;
    }
    vao_dirty_ = true;
    generateVAO(programId);
    it = program_ids_.find(programId);
    if (it != program_ids_.end())
    {
        GLVaoVboId id = it->second;
        return id.vaoID;
    }
    LOGI("!! %p Error in creating VAO  for Prog Id -- %d", this, programId);
    return 0;
}
// generate vertex array object
void Mesh::generateVAO(int programId) {
    GLuint tmpID;

    if (!vao_dirty_) {
         return;
    }
    obtainDeleter();

    if (vertices_.size() == 0 && normals_.size() == 0) {
        std::string error = "no vertex data yet, shouldn't call here. ";
        throw error;
    }
    if (0 != normals_.size() && vertices_.size() != normals_.size()) {
        LOGW("mesh: number of vertices and normals do not match! vertices %d, normals %d", vertices_.size(), normals_.size());
    }

    GLuint vaoID_;
    GLuint triangle_vboID_;
    GLuint static_vboID_;
    auto it = program_ids_.find(programId);
    if (it != program_ids_.end())
    {

        GLVaoVboId ids = it->second;
        vaoID_ = ids.vaoID;
        triangle_vboID_ = ids.triangle_vboID;
        static_vboID_ = ids.static_vboID;
    }
    else
    {
        glGenVertexArrays(1, &vaoID_);
        glGenBuffers(1, &triangle_vboID_);
        glGenBuffers(1, &static_vboID_);
    }


    glBindVertexArray(vaoID_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangle_vboID_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,
            sizeof(unsigned short) * indices_.size(), &indices_[0],
            GL_STATIC_DRAW);
    numTriangles_ = indices_.size() / 3;

    attrMapping.clear();
    int totalStride;
    int attrLength;
    createAttributeMapping(programId, totalStride, attrLength);

    std::vector<GLfloat> buffer;
    createBuffer(buffer, attrLength);
    glBindBuffer(GL_ARRAY_BUFFER, static_vboID_);

    glBufferData(GL_ARRAY_BUFFER, sizeof(GLfloat) * buffer.size(),
            &buffer[0], GL_STATIC_DRAW);
    int localCnt = 0;
    for ( std::vector<GLAttributeMapping>::iterator it = attrMapping.begin(); it != attrMapping.end(); ++it)
    {
        GLAttributeMapping currData = *it;
        glVertexAttribPointer(currData.index, currData.size, currData.type, 0, totalStride * sizeof(GLfloat), (GLvoid*) (currData.offset * sizeof(GLfloat)));
        glEnableVertexAttribArray(currData.index);
    }


    // done generation
    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    if (it == program_ids_.end())
    {
        GLVaoVboId id;
        id.vaoID = vaoID_;
        id.static_vboID = static_vboID_;
        id.triangle_vboID = triangle_vboID_;
        program_ids_[programId] = id;
    }
    vao_dirty_ = false;
}

void Mesh::getAttribNames(std::set<std::string> &attrib_names) {
    	 if(vertices_.size() > 0)
    		 attrib_names.insert("a_position");
    	 if(normals_.size() > 0)
    		 attrib_names.insert("a_normal");

    	 if(hasBones()){
    		 attrib_names.insert("a_bone_indices");
    		 attrib_names.insert("a_bone_weights");
    	 }

    	 for(auto it : vec2_vectors_){
    		 attrib_names.insert(it.first);
    		 LOGE("vec2 vector %s",(it.first).c_str());
    	 }
    	 for(auto it : vec3_vectors_){
    		 attrib_names.insert(it.first);
    	 }
    	 for(auto it : vec4_vectors_){
    		 attrib_names.insert(it.first);
    	 }
    	 for(auto it : float_vectors_){
    		 attrib_names.insert(it.first);
    	 }

    }

void Mesh::generateBoneArrayBuffers(GLuint programId) {
    if (!bone_data_dirty_) {
        return;
    }


    // delete
    if (boneVboID_ != GVR_INVALID) {
        deleter_->queueBuffer(boneVboID_);
        boneVboID_ = GVR_INVALID;
    }

    int nVertices = vertices().size();
    if (!vertexBoneData_.getNumBones() || !nVertices) {
        LOGV("no bones or vertices");
        return;
    }

    auto it = program_ids_.find(programId);
    if (it == program_ids_.end())
    {
        LOGV("Invalid program Id for bones");
        return;
    }
    GLVaoVboId id = it->second;
    glBindVertexArray(id.vaoID);

    // BoneID
    GLuint boneVboID;
    glGenBuffers(1, &boneVboID);
    glBindBuffer(GL_ARRAY_BUFFER, boneVboID);
    glBufferData(GL_ARRAY_BUFFER,
            sizeof(vertexBoneData_.boneData[0]) * vertexBoneData_.boneData.size(),
            &vertexBoneData_.boneData[0], GL_STATIC_DRAW);
    glEnableVertexAttribArray(getBoneIndicesLoc());
    glVertexAttribIPointer(getBoneIndicesLoc(), 4, GL_INT, sizeof(VertexBoneData::BoneData), (const GLvoid*) 0);

    // BoneWeight
    glEnableVertexAttribArray(getBoneWeightsLoc());
    glVertexAttribPointer(getBoneWeightsLoc(), 4, GL_FLOAT, GL_FALSE, sizeof(VertexBoneData::BoneData),
            (const GLvoid*) (sizeof(VertexBoneData::BoneData::ids)));

    boneVboID_ = boneVboID;

    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void Mesh::add_dirty_flag(const std::shared_ptr<bool>& dirty_flag) {
    dirty_flags_.insert(dirty_flag);
}

void Mesh::dirty() {
    dirtyImpl(dirty_flags_);
}

}
