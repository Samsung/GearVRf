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

#ifndef GL_SHADER_H_
#define GL_SHADER_H_

#include "shaders/shader.h"
#include "gl/gl_program.h"

namespace gvr {
    class Mesh;
    class GLMaterial;
    class LightList;

/**
 * Contains information about the vertex attributes, textures and
 * uniforms used by the shader source and the sources for the
 * vertex and fragment shaders.
 *
 * Shaders are only created by the ShaderManager when addShader is called.
 */
class GLShader : public Shader
{

/*
 * Creates a native shader description.
 * The actual GL program is not made until the first call to render()
 * @param id                ShaderManager ID for the shader
 * @param signature         Unique signature string
 * @param uniformDescriptor String giving the names and types of shader material uniforms
 *                          This does NOT include uniforms used by light sources
 * @param textureDescriptor String giving the names and types of texture samplers
 * @param vertexDescriptor  String giving the names and types of vertex attributes
 * @param vertexShader      String with GLSL source for vertex shader
 * @param fragmentShader    String with GLSL source for fragment shader
 * @see ShaderManager::addShader
 */
public:
    explicit GLShader(int id, const char* signature,
            const char* uniformDescriptor,
            const char* textureDescriptor,
            const char* vertexDescriptor,
            const char* vertexShader,
            const char* fragmentShader);
    virtual ~GLShader();

    virtual bool useShader(bool);

    /*
     * Returns the GL program ID for the native shader
     */
    GLuint getProgramId()
    {
        if (mProgram)
        {
            return mProgram->id();
        }
        else
        {
            return -1;
        }
    }
    virtual void bindLights(LightList&, Renderer*);
    void convertToGLShaders();
    void findTextures();
    void findUniforms(const DataDescriptor& desc, int bindingPoint);
    void findUniforms(const Light& light, int locationOffset);
    int getUniformLoc(int index, int bindingPoint) const;
    int getTextureLoc(int index) const;
    static std::string makeLayout(const DataDescriptor& desc, const char* blockName, bool useGPUBuffer);

protected:
    void initialize(bool);

private:
    GLShader(const GLShader& shader) = delete;
    GLShader(GLShader&& shader) = delete;
    GLShader& operator=(const GLShader& shader) = delete;
    GLShader& operator=(GLShader&& shader) = delete;

    GLProgram* mProgram;
    bool mIsReady;
    std::vector<int> mShaderLocs[LAST_UBO_INDEX + 1];
    std::vector<int> mTextureLocs;
};

}
#endif
