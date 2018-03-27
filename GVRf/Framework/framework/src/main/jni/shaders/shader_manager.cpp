#include "util/gvr_log.h"

#include "shader_manager.h"
#include "shader.h"
#include "engine/renderer/renderer.h"

namespace gvr {
    ShaderManager::~ShaderManager()
    {
#ifdef DEBUG_SHADER
        LOGE("SHADER: deleting ShaderManager");
#endif
        for (auto it = shadersByID.begin(); it != shadersByID.end(); ++it) {
            Shader *shader = it->second;
            delete shader;
        }
        shadersByID.clear();
        shadersBySignature.clear();
    }

    int ShaderManager::addShader(const char* signature,
                                 const char* uniformDescriptor,
                                 const char* textureDescriptor,
                                 const char* vertexDescriptor,
                                 const char* vertex_shader,
                                 const char* fragment_shader)
    {
        Shader* shader = findShader(signature);
        if (shader != NULL)
        {
            return shader->getShaderID();
        }
        std::lock_guard<std::mutex> lock(lock_);
        int id = ++latest_shader_id_;
        shader = Renderer::getInstance()->createShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertex_shader, fragment_shader);
        shadersBySignature[signature] = shader;
        shadersByID[id] = shader;
#ifdef DEBUG_SHADER
        LOGD("SHADER: added shader %d %s", id, signature);
#endif
        return id;
    }

    Shader* ShaderManager::findShader(const char* signature)
    {
        std::lock_guard<std::mutex> lock(lock_);
        auto it = shadersBySignature.find(signature);
        if (it != shadersBySignature.end())
        {
            Shader* shader = it->second;
            const std::string& sig = shader->signature();
#ifdef DEBUG_SHADER
            LOGV("SHADER: findShader %s -> %d", sig.c_str(), shader->getShaderID());
#endif
            return shader;
        }
        else
        {
            return NULL;
        }
    }

    Shader* ShaderManager::getShader(int id)
    {
        std::lock_guard<std::mutex> lock(lock_);
        auto it = shadersByID.find(id);
        if (it != shadersByID.end())
        {
            Shader* shader = it->second;
            const std::string& sig = shader->signature();
#ifdef DEBUG_SHADER
            LOGV("SHADER: getShader %d -> %s", id, sig.c_str());
#endif
            return shader;
        }
        else
        {
#ifdef DEBUG_SHADER
            LOGE("SHADER: getShader %d NOT FOUND", id);
#endif
            return NULL;
        }
    }

    void ShaderManager::dump()
    {
        for (auto it = shadersByID.begin(); it != shadersByID.end(); ++it)
        {
            Shader* shader = it->second;
            long id = shader->getShaderID();
            const std::string& sig = shader->signature();
            LOGD("SHADER: #%ld %s", id, sig.c_str());
        }
    }
}