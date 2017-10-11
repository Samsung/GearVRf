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
#include <cstring>
#include <sstream>
#include <istream>
#include "objects/data_descriptor.h"
#include "util/gvr_log.h"

namespace gvr
{

    DataDescriptor::DataDescriptor(const char* descriptor) :
            mTotalSize(0),
            mIsDirty(false),
            mDescriptor(descriptor)
    {
        if (descriptor)
        {
            LOGV("DataDescriptor: %s", descriptor);
            parseDescriptor();
        }
        else
        {
            LOGE("DataDescriptor: Error: missing descriptor string");
        }
    }

    void DataDescriptor::forEachEntry(std::function<void(DataEntry&)> func)
    {
        for (auto it = mLayout.begin(); it != mLayout.end(); ++it)
        {
            func(*it);
        }
    }

    void DataDescriptor::forEachEntry(std::function<void(const DataEntry&)> func) const
    {
        for (auto it = mLayout.begin(); it != mLayout.end(); ++it)
        {
            func(*it);
        }
    }

    void DataDescriptor::forEach(std::function<void(const char*, const char*, int)> func)
    {
        const char* p = mDescriptor.c_str();
        const char* type_start;
        int type_size;
        const char* name_start;
        int name_size;
        int index = 0;

        while (*p)
        {
            while (std::isspace(*p) || std::ispunct(*p))
            {
                ++p;
            }
            type_start = p;
            if (*p == 0)
            {
                break;
            }
            while (std::isalnum(*p))    // types are alphanumeric
            {
                ++p;
            }
            type_size = p - type_start;
            if (type_size == 0)
            {
                break;
            }
            std::string type(type_start, type_size);
            while (std::isspace(*p))
            {
                ++p;
            }
            name_start = p;
            if (*p == '!')              // skip leading !
            {
                ++p;                    // indicates unused field
            }
            while (std::isalnum(*p) || (*p == '_') || (*p == '[') || (*p == ']'))
            {
                ++p;                    // names are alphanumeric, _ allowed, [] for arrays
            }
            name_size = p - name_start;
            if (name_size == 0)
            {
                break;
            }
            std::string name(name_start, name_size);
            int size = calcSize(type.c_str());
            func(name.c_str(), type.c_str(), size);
            ++index;
        }
    }

    const char* DataDescriptor::addName(const char* name, int len, DataEntry& entry)
    {
        if (len > 62)
        {
            len = 62;
            LOGE("DataDescriptor: %s too long, truncated to %s", name, entry.Name);
        }
        strncpy(entry.Name, name, len);
        entry.Name[len] = 0;
        entry.NameLength = (char) len;
        return entry.Name;
    }

    int DataDescriptor::findName(const char* name) const
    {
        int n = strlen(name);
        for (auto it = mLayout.begin(); it != mLayout.end(); ++it)
        {
            const DataEntry& entry = *it;
            if ((entry.NameLength == n) && (strcmp(entry.Name, name) == 0))
            {
                return it - mLayout.begin();
            }
        }
        return -1;
    }

    void  DataDescriptor::parseDescriptor()
    {
        int index = 0;
        forEach([this, index](const char* name, const char* type, int size) mutable
                {
                    // check if it is array
                    int array_size = 1;
                    const char* p = name;
                    const char* bracket = strchr(name, '[');
                    size_t namelen = strlen(name);

                    if (name == NULL)
                    {
                        LOGE("UniformBlock: SYNTAX ERROR: expecting uniform name\n");
                        return;
                    }
                    if (bracket)                // parse array size in brackets
                    {
                        namelen = bracket - name;
                        array_size = 0;
                        p += (bracket - name) + 1;
                        while (std::isdigit(*p))
                        {
                            int v = *p - '0';
                            array_size = array_size * 10 + v;
                            ++p;
                        }
                    }
                    DataEntry entry;
                    short byteSize = calcSize(type);

                    entry.Type = makeShaderType(type, byteSize);
                    byteSize *= array_size;     // multiply by number of array elements
                    entry.IsSet = false;
                    entry.Count = array_size;
                    entry.NotUsed = false;
                    entry.IsInt =  strstr(type,"int") != nullptr;
                    entry.IsMatrix = type[0] == 'm';
                    entry.Index = index++;
                    entry.Offset = mTotalSize;
                    entry.Size = byteSize;

                    if (*name == '!')           // ! indicates entry not used by shader
                    {
                        entry.NotUsed = true;
                        ++name;
                    }
                    addName(name, namelen, entry);
                    mLayout.push_back(entry);
                    mTotalSize += entry.Size;
                });
    }

    std::string DataDescriptor::makeShaderType(const char* type, int byteSize)
    {
        std::ostringstream stream;

        if ((byteSize > 4) && (byteSize <= 16))
        {
            if (type[0] == 'f')
            {
                stream << "vec" << (byteSize / 4);
            }
            else if (type[0] == 'i')
            {
                stream << "ivec" << (byteSize / 4);
            }
            else
            {
                stream << type;
            }
        }
        else
        {
            stream << type;
        }
        return stream.str();
    }

    short DataDescriptor::calcSize(const char* type)
    {
        int size = 1;
        int n = strlen(type);

        if (strncmp(type, "float", 5) == 0)
        {
            std::istringstream is(type + 5);
            is >> size;
            return size * sizeof(float);
        }
        else if (strncmp(type, "int", 3) == 0)
        {
            std::istringstream is(type + 3);
            is >> size;
            return size * sizeof(int);
        }
        else if (strncmp(type, "uint", 4) == 0)
        {
            std::istringstream is(type + 4);
            is >> size;
            return size * sizeof(int);
        }
        else if ((strncmp(type, "mat", 3) == 0) && (n <= 4))
        {
            if (type[3] == '3')
            {
                return 12 * sizeof(float);
            }
            else if (type[3] == '4')
            {
                return 16 * sizeof(float);
            }
        }
        return 0;
    }

    /*
     * This function is used inside the renderer so it is optimized
     * by checking the length of the entry name first before doing
     * the string compare. This works better for names which have
     * common prefixes.
     */
    const DataDescriptor::DataEntry* DataDescriptor::find(const char* name) const
    {
        if (name == nullptr)
        {
            return nullptr;
        }
        int i = findName(name);
        if (i >= 0)
        {
            return &mLayout[i];
        }
        return nullptr;
    }

    DataDescriptor::DataEntry* DataDescriptor::find(const char* name)
    {
        if (name == nullptr)
        {
            return nullptr;
        }
        int i = findName(name);
        if (i >= 0)
        {
            return &mLayout[i];
        }
        return nullptr;
    }

    int DataDescriptor::getByteSize(const char* name) const
    {
        const DataEntry* e = find(name);
        return (e && e->IsSet) ? e->Size : 0;
    }

}
