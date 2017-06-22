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
#include <sstream>
#include "util/gvr_gl.h"

namespace gvr
{

    UniformBlock::UniformBlock(const char* descriptor, int bindingPoint, const char* blockName) :
            DataDescriptor(descriptor),
            mBlockName(blockName),
            mOwnData(false),
            mUseBuffer(true),
            mBindingPoint(bindingPoint),
            mUniformData(NULL)
    {
        if (mTotalSize > 0)
        {
            mTotalSize = (mTotalSize + 15) & ~0x0F;
            mUniformData = new char[mTotalSize];
            memset(mUniformData, 0, mTotalSize);
            mOwnData = true;
            LOGV("UniformBlock: allocating uniform block %s of %d  bytes", blockName, mTotalSize);
        }
        else
        {
            LOGE("UniformBlock: ERROR: no uniform block allocated\n");
        }
    }


    bool UniformBlock::setInt(const char* name, int val)
    {
        int size = sizeof(int);
        char *data = getData(name, size);
        if (data != NULL)
        {
            *((int *) data) = val;
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setFloat(const char* name, float val)
    {
        int size = sizeof(float);
        char *data = getData(name, size);
        if (data != NULL)
        {
            *((float *) data) = val;
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setFloatVec(const char* name, const float *val, int n)
    {
        int bytesize = n * sizeof(float);
        char *data = getData(name, bytesize);
        if (data != NULL)
        {
            memcpy(data, val, bytesize);
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setIntVec(const char* name, const int *val, int n)
    {
        int bytesize = n * sizeof(int);
        char *data = getData(name, bytesize);
        if (data != NULL)
        {
            memcpy(data, val, bytesize);
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setVec2(const char* name, const glm::vec2 &val)
    {
        int bytesize = 2 * sizeof(float);
        float *data = (float *) getData(name, bytesize);
        if (data != NULL)
        {
            data[0] = val.x;
            data[1] = val.y;
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setVec3(const char* name, const glm::vec3 &val)
    {
        int bytesize = 3 * sizeof(float);
        float *data = (float *) getData(name, bytesize);
        if (data != NULL)
        {
            data[0] = val.x;
            data[1] = val.y;
            data[2] = val.z;
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setVec4(const char* name, const glm::vec4 &val)
    {
        int bytesize = 4 * sizeof(float);
        float *data = (float *) getData(name, bytesize);
        if (data != NULL)
        {
            data[0] = val.x;
            data[1] = val.y;
            data[2] = val.z;
            data[3] = val.w;
            markDirty();
            return true;
        }
        return false;
    }

    bool UniformBlock::setMat4(const char* name, const glm::mat4 &val)
    {
        const float *mtxdata = glm::value_ptr(val);
        int bytesize = 16 * sizeof(float);
        char *data = getData(name, bytesize);
        if (data != NULL)
        {
            memcpy(data, mtxdata, bytesize);
            markDirty();
            return true;
        }
        return false;
    }

    const glm::vec2* UniformBlock::getVec2(const char* name) const
    {
        int size = 2 * sizeof(float);
        const char *data = getData(name, size);
        if (data != NULL)
            return (reinterpret_cast<const glm::vec2 *>(data));
        return NULL;
    }

    const glm::vec3* UniformBlock::getVec3(const char* name) const
    {
        int size = 3 * sizeof(float);
        const char *data = getData(name, size);
        if (data != NULL)
            return (reinterpret_cast<const glm::vec3 *> (data));
        return NULL;
    }

    const glm::vec4* UniformBlock::getVec4(const char* name) const
    {
        int size = 4 * sizeof(float);
        const char *data = getData(name, size);
        if (data != NULL)
            return (reinterpret_cast<const glm::vec4 *> (data));
        return NULL;
    }

    bool UniformBlock::getInt(const char* name, int &v) const
    {
        int size = sizeof(int);
        const char *data = getData(name, size);
        if (data != NULL)
        {
            v = *(reinterpret_cast<const int *> (data));
            return true;
        }
        return false;
    }

    bool UniformBlock::getFloat(const char* name, float &v) const
    {
        int size = sizeof(float);
        const char *data = getData(name, size);
        if (data != NULL)
        {
            v = *(reinterpret_cast<const float *> (data));
            return true;
        }
        return false;
    }

    bool UniformBlock::getIntVec(const char* name, int *val, int n) const
    {
        int size = n * sizeof(int);
        const char *data = getData(name, size);
        if (data != NULL)
        {
            memcpy((char *) val, data, size);
            return true;
        }
        LOGE("ERROR: UniformBlock element %s not found\n", name);
        return false;
    }

    bool UniformBlock::getFloatVec(const char* name, float *val, int n) const
    {
        int size = n * sizeof(float);
        const char *data = getData(name, size);
        if (data != NULL)
        {
            memcpy((char *) val, data, n * sizeof(float));
            return true;
        }
        LOGE("ERROR: UniformBlock element %s not found\n", name);
        return false;
    }

    bool UniformBlock::getMat4(const char* name, glm::mat4 &val) const
    {
        int bytesize = 16 * sizeof(float);
        const char *data = getData(name, bytesize);
        if (data != NULL)
        {
            val = glm::make_mat4((const float *) data);
            return true;
        }
        return false;
    }


    const char* UniformBlock::getData(const char* name, int &bytesize) const
    {
        const DataEntry* u = find(name);
        if ((u == NULL) || !u->IsSet)
            return NULL;
        char* data = (char*) mUniformData;
        if (data == NULL)
            return NULL;
        data += u->Offset;
        bytesize = u->Size;
        return data;
    }

    char* UniformBlock::getData(const char* name, int &bytesize)
    {
        DataEntry* u = find(name);
        if (u == NULL)
            return NULL;
        char* data = (char*) mUniformData;

        if (data == NULL)
            return NULL;
        data += u->Offset;
        bytesize = u->Size;
        u->IsSet = true;
        return data;
    }

    std::string UniformBlock::makeShaderLayout()
    {
        std::ostringstream stream;
        if (mUseBuffer)
        {
            stream << "layout (std140) uniform " << getBlockName() << " {" << std::endl;
            DataDescriptor::forEachEntry([&stream](const DataEntry& entry) mutable
            {
                stream << "   " << entry.Type << " " << entry.Name << ";" << std::endl;
            });
            stream << "};" << std::endl;
        }
        else
        {
            DataDescriptor::forEachEntry([&stream, this](const DataEntry& entry) mutable
            {
                if (entry.IsSet)
                {
                    stream << "uniform " << entry.Type << " " << entry.Name << ";" << std::endl;
                }
            });
        }
        return stream.str();
    }

    std::string UniformBlock::toString()
    {
        std::ostringstream os;
        forEachEntry([this, &os](const DataEntry& e) mutable
        {
            os << e.Name << ":" << e.Offset;
            for (int i = 0; i < e.Size / sizeof(float); i++)
            {
                char *d = ((char*) mUniformData) + e.Offset;
                os << " ";
                if (!e.IsSet)
                {
                    os << "NOT SET";
                }
                else if (e.Name[0] == 'i')
                {
                    os << *(((int *) d) + i);
                }
                else
                {
                    os << *(((float *) d) + i);
                }
            }
            os << ';' << std::endl;
        });
        return os.str();
    }
}

