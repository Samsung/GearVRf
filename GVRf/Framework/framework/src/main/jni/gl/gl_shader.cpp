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

#include "gl/gl_shader.h"
#include "gl/gl_material.h"
#include "objects/light.h"
#include "engine/renderer/renderer.h"
#include "gl_light.h"

namespace gvr {

    GLShader::GLShader(int id,
               const char* signature,
               const char* uniformDescriptor,
               const char* textureDescriptor,
               const char* vertexDescriptor,
               const char* vertexShader,
               const char* fragmentShader)
    : Shader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertexShader, fragmentShader),
      mProgram(NULL),
      mIsReady(false)
{ }


GLShader::~GLShader()
{
    if (mProgram)
    {
        delete mProgram;
    }
}

void getTokens(std::unordered_map<std::string, int>& tokens, std::string& line)
{
    std::string delimiters = " ;+-/*%()<>!={}\n";
    std::unordered_set<char>delim;
    for(int i=0; i<delimiters.length(); i++)
    {
        delim.insert(delimiters[i]);
    }
    int start  =0;
    for (int i=0; i<line.length(); i++)
    {
        if (delim.find(line[i]) != delim.end())
        {
            if ((i - start) > 0)
                tokens[line.substr(start, i - start)] = start;
            start = i + 1;
        }
    }
}

bool checkSamplers(std::unordered_map<std::string, int>& tokens){
    std::string samplers [] = { "sampler2D", "sampler2DArray", "samplerCube"};
    for(auto i: samplers)
        if(tokens.find(i)!= tokens.end())
            return true;

    return false;
}

void modifyShader(std::string& shader)
{
    std::istringstream shaderStream(shader);
    std::string line;
    std::getline(shaderStream, line);
    std::string mod_shader("#version 300 es\n");

    std::unordered_map<std::string, int>::iterator it;
    std::unordered_map<std::string, int>::iterator it1;
    while (std::getline(shaderStream, line))
    {
        if (line.find("GL_ARB_separate_shader_objects") != std::string::npos ||
            line.find("GL_ARB_shading_language_420pack") != std::string::npos)
            continue;

        std::unordered_map<std::string, int> tokens;
        getTokens(tokens, line);

        if ((it = tokens.find("uniform")) != tokens.end() && checkSamplers(tokens)){
            int layout_pos = tokens["layout"];
            mod_shader += ((layout_pos > 0) ? line.substr((0, layout_pos)) : "") + line.substr(it->second) + "\n";

        }
        else if ((it = tokens.find("layout")) != tokens.end() && tokens.find("uniform")==tokens.end() && tokens.find("num_views") == tokens.end()) {
            it1 = tokens.find("in");
            if (it1 == tokens.end())
                it1 = tokens.find("out");
            int pos = it->second;

            if(it1 != tokens.end())
            mod_shader += ((pos > 0) ? line.substr(0, pos) : "") + line.substr(it1->second) + "\n";
        }
        else
            {
            mod_shader += line + "\n";
        }
    }
    shader = mod_shader;
}

void GLShader::convertToGLShaders()
{
    if (mVertexShader.find("#version 400") == std::string::npos)
        return;
    modifyShader(mVertexShader);
    modifyShader(mFragmentShader);

}

void GLShader::initialize(bool is_multiview)
{
    convertToGLShaders();
    mProgram = new GLProgram(mVertexShader.c_str(), mFragmentShader.c_str());
    if (is_multiview && !(strstr(mVertexShader.c_str(), "GL_OVR_multiview2")))
    {
        std::string error = "Your shaders are not multiview";
        LOGE("Your shaders are not multiview");
        throw error;
    }
    mVertexShader.clear();
    mFragmentShader.clear();
}

bool GLShader::useShader(bool is_multiview)
{
    if (nullptr == mProgram)
    {
        initialize(is_multiview);
    }
    GLint programID = getProgramId();
    if (programID <= 0)
    {
        LOGE("SHADER: shader could not be generated %s", mSignature.c_str());
        return false;
    }
#ifdef DEBUG_SHADER
    LOGV("SHADER: rendering with program %d", programID);
#endif
    glUseProgram(programID);

    if(!mTextureLocs.size())
        findTextures();
    if (!useMaterialGPUBuffer())
    {
        findUniforms(mUniformDesc, MATERIAL_UBO_INDEX);
    }
    return true;
}

void GLShader::bindLights(LightList& lights, Renderer* renderer)
{
    int locOffset = 0;

    if ((mShaderLocs[LIGHT_UBO_INDEX].size() == 0) || lights.isDirty())
    {
        mShaderLocs[LIGHT_UBO_INDEX].resize(lights.getNumUniforms());
        lights.forEachLight([this, renderer, locOffset](Light& light) mutable
        {
            UniformBlock& lightData = light.uniforms().uniforms();

            findUniforms(light, locOffset);
            lightData.bindBuffer(this, renderer, locOffset);
            locOffset += lightData.getNumEntries();
        });
    }
    else
    {
        lights.forEachLight([this, renderer, locOffset](Light& light) mutable
        {
            UniformBlock& lightData = light.uniforms().uniforms();

            lightData.bindBuffer(this, renderer, locOffset);
            locOffset += lightData.getNumEntries();
        });
    }
}

/**
 * Gets the GL shader location of a uniform based on its index
 * in the Material uniformdescriptor.
 * @param index 0-based uniform index
 * @return GL shader location, -1 if shader does not use the uniform
 * @see GLMaterial::bindToShader ShaderData::getUniformDescriptor
 */
int GLShader::getUniformLoc(int index, int bindingPoint) const
{
    const std::vector<int>& locs = mShaderLocs[bindingPoint];
    if (index < locs.size())
    {
        return locs[index];
    }
    return -1;
}

/**
 * Gets the GL shader location of a texture based on its index
 * in the Material texture descriptor.
 * @param index 0-based texture index
 * @return GL shader location, -1 if shader does not use the texture
 * @see GLMaterial::bindToShader ShaderData::getTextureDescriptor
 */
int GLShader::getTextureLoc(int index) const
{
    if (index < mTextureLocs.size())
    {
        return mTextureLocs[index];
    }
    return -1;
}

/**
 * Finds the shader locations of all uniforms and textures from a given material.
 * The input material descriptor has all the possible textures and uniforms
 * that can be used by this shader. (Any material used with this shader
 * will have the same descriptor.)
 *
 * This function uses the descriptor of the input material to find and save
 * the GL shader locations of each texture and uniform. The locations are
 * saved into vectors - mTextureLocs and mUniformLocs. Each vector has an
 * entry for all of the uniforms/textures in the input material
 * (not just the ones used by this shader). If the shader does not
 * reference a particular uniform or texture, that location will be -1.
 * This function must be called after the GL shader program has
 * been selected as the current program.
 * @param material  can be any Material which uses this shader
 * @see #getUniformLoc
 */
void GLShader::findUniforms(const DataDescriptor& desc, int bindingPoint)
{
    std::vector<int>& uniformLocs = mShaderLocs[bindingPoint];

    if (uniformLocs.size() > 0)
    {
        return;
    }
    uniformLocs.resize(desc.getNumEntries(), -1);
    desc.forEachEntry([&](const DataDescriptor::DataEntry& entry) mutable
    {
        if (entry.NotUsed)
        {
            return;
        }
        int loc = glGetUniformLocation(getProgramId(), entry.Name);
        if (loc >= 0)
        {
            uniformLocs[entry.Index] = loc;
#ifdef DEBUG_SHADER
            LOGV("SHADER: program %d uniform %s loc %d", getProgramId(), entry.Name, loc);
#endif
        }
        else
        {
#ifdef DEBUG_SHADER
            LOGV("SHADER: uniform %s has no location in shader %d", entry.Name, getProgramId());
#endif
        }
    });
    checkGLError("GLShader::findUniforms");
}

/**
 * Finds the shader locations of all uniforms and textures from a given material.
 * The input material descriptor has all the possible textures and uniforms
 * that can be used by this shader. (Any material used with this shader
 * will have the same descriptor.)
 *
 * This function uses the descriptor of the input material to find and save
 * the GL shader locations of each texture and uniform. The locations are
 * saved into vectors - mTextureLocs and mUniformLocs. Each vector has an
 * entry for all of the uniforms/textures in the input material
 * (not just the ones used by this shader). If the shader does not
 * reference a particular uniform or texture, that location will be -1.
 * This function must be called after the GL shader program has
 * been selected as the current program.
 * @param material  can be any Material which uses this shader
 * @see #getUniformLoc
 */
void GLShader::findUniforms(const Light& light, int locationOffset)
{
    const UniformBlock& lightBlock = light.uniforms().uniforms();
    const GLLight* glLight = static_cast<const GLLight*>(&light);

    lightBlock.forEachEntry([this, glLight, locationOffset](const DataDescriptor::DataEntry& entry) mutable
    {
        if (entry.NotUsed)
        {
            return;
        }
        std::string name = glLight->getLightName();
        name += '.';
        name += entry.Name;
        int loc = glGetUniformLocation(getProgramId(), name.c_str());
        if (loc >= 0)
        {
            mShaderLocs[LIGHT_UBO_INDEX][entry.Index + locationOffset] = loc;
#ifdef DEBUG_SHADER
            LOGV("SHADER: program %d uniform %s loc %d", getProgramId(), entry.Name, loc);
#endif
        }
        else
        {
#ifdef DEBUG_SHADER
            LOGV("SHADER: uniform %s has no location in shader %d", entry.Name, getProgramId());
#endif
        }
    });
    checkGLError("GLShader::findUniforms");
}

/**
 * Finds the shader locations of all textures used by this shader.
 * The shader's texture descriptor  has all the possible textures
 * that can be used by this shader.
 *
 * This function uses the texture descriptor to find and save
 * the GL shader locations of each texture used by the shader.
 * The locations are saved into mTextureLocs which has an
 * entry for each texture in the descriptor
 * (not just the ones used by this shader). If the shader does not
 * reference a particular texture, that location will be -1.
 * This function must be called after the GL shader program has
 * been selected as the current program.
 * @see #getTextureLoc
 */
void GLShader::findTextures()
{
    mTextureLocs.resize(mTextureDesc.getNumEntries(), -1);
    mTextureDesc.forEachEntry([this](const DataDescriptor::DataEntry& entry) mutable
    {
        if (entry.NotUsed)
        {
            return;
        }
        int loc = glGetUniformLocation(getProgramId(), entry.Name);
        if (loc >= 0)
        {
            mTextureLocs[entry.Index] = loc;
#ifdef DEBUG_SHADER
            LOGV("SHADER: program %d texture %s loc %d", getProgramId(), entry.Name, loc);
#endif
        }
        else
        {
#ifdef DEBUG_SHADER
            LOGV("SHADER: texture %s has no location in shader %d", entry.Name, getProgramId());
#endif
        }
    });
    checkGLError("GLShader::findTextures");
}

std::string GLShader::makeLayout(const DataDescriptor& desc, const char* blockName, bool useGPUBuffer)
{
    std::ostringstream stream;
    if (useGPUBuffer)
    {
        stream << "\nlayout (std140) uniform " << blockName << std::endl << "{" << std::endl;
        desc.forEachEntry([&stream](const DataDescriptor::DataEntry& entry) mutable
        {
            int nelems = entry.Count;
            stream << "   " << entry.Type << " " << entry.Name;
            if (nelems > 1)
            {
                stream << "[" << nelems << "]";
            }
            stream << ";" << std::endl;
        });
        stream << "};" << std::endl;
    }
    else
    {
        desc.forEachEntry([&stream](const DataDescriptor::DataEntry& entry) mutable
        {
            if (entry.IsSet)
            {
                int nelems = entry.Count;
                stream << "uniform " << entry.Type << " " << entry.Name;
                if (nelems > 1)
                {
                    stream << "[" << nelems << "]";
                }
                stream << ";" << std::endl;
            }
        });
    }
    return stream.str();
}

} /* namespace gvr */
