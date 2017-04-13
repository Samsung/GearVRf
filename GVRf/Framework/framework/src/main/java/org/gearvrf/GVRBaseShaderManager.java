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


package org.gearvrf;

import android.content.res.AssetManager;
import android.content.res.Resources;

import org.gearvrf.utility.TextFile;
import org.gearvrf.utility.VrAppSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the implementation shared between GVRMaterialShaderManager
 * and GVRPostEffectShaderManager.
 */
public abstract class GVRBaseShaderManager extends GVRHybridObject
        implements GVRShaderManagers {

    protected GVRBaseShaderManager(GVRContext gvrContext, long pointer) {
        super(gvrContext, pointer);
    }

    /**
     * Adds a shader from sources in the assets directory. Assumes the shaders use GLSL ES version
     * 100.
     * @param pathPrefix
     *            Optional (may be {@code null}) prefix for shader filenames. If
     *            present, will be prepended to {@code vertexShader} and
     *            {@code fragmentShader}, thus allowing you to build a tree of
     *            shaders in your assets directory, where each node contains
     *            vertex and fragment shaders with the same filename. If
     *            {@code null}, {@code vertexShader} and {@code fragmentShader}
     *            are the path names of files in the assets directory.
     * @param vertexShader_asset
     *            Filename of a vertex shader, relative to the assets directory
     * @param fragmentShader_asset
     *            Filename of a fragment shader, relative to the assets
     *            directory
     * @return ID of new shader added
     */
    @Override
    public GVRShaderId addShader(String pathPrefix, String vertexShader_asset,
            String fragmentShader_asset) {
        return addShader(pathPrefix, vertexShader_asset, fragmentShader_asset, GLSLESVersion.V100);
    }

    /**
     * Adds a shader from sources in the assets directory.
     * @param pathPrefix
     *            Optional (may be {@code null}) prefix for shader filenames. If
     *            present, will be prepended to {@code vertexShader} and
     *            {@code fragmentShader}, thus allowing you to build a tree of
     *            shaders in your assets directory, where each node contains
     *            vertex and fragment shaders with the same filename. If
     *            {@code null}, {@code vertexShader} and {@code fragmentShader}
     *            are the path names of files in the assets directory.
     * @param vertexShader_asset
     *            Filename of a vertex shader, relative to the assets directory
     * @param fragmentShader_asset
     *            Filename of a fragment shader, relative to the assets
     *            directory
     * @param glslesVersion GLSL ES version used by the shaders
     * @return ID of new shader added
     */
    public GVRShaderId addShader(String pathPrefix, String vertexShader_asset,
                                 String fragmentShader_asset, GLSLESVersion glslesVersion) {
        Resources resources = getResources();
        try {
            InputStream vertexShader = open(resources, pathPrefix,
                    vertexShader_asset);
            InputStream fragmentShader = open(resources, pathPrefix,
                    fragmentShader_asset);
            return newShader(vertexShader, fragmentShader, glslesVersion);
        } catch (IOException e) {
            e.printStackTrace(); // give user a clue
            return null;
        }
    }

    /**
     * Adds a shader from sources in res/raw. Assumes the shaders use GLSL ES version 100.
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     */
    @Override
    public GVRShaderId newShader(int vertexShader_resRaw, int fragmentShader_resRaw) {
        return newShader(vertexShader_resRaw, fragmentShader_resRaw, GLSLESVersion.V100);
    }

    /**
     * Adds a shader from sources in res/raw.
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     * @param glslesVersion GLSL ES version used by the shaders
     */
    public GVRShaderId newShader(int vertexShader_resRaw, int fragmentShader_resRaw, GLSLESVersion glslesVersion) {
        Resources resources = getResources();
        InputStream vertexShader = open(resources, vertexShader_resRaw);
        InputStream fragmentShader = open(resources, fragmentShader_resRaw);
        return newShader(vertexShader, fragmentShader, glslesVersion);
    }

    /**
     * Adds a shader from sources in separate input streams. Assumes the shaders use GLSL ES version
     * 100.
     * @param vertexShader_stream
     *            GLSL source code for a vertex shader. Stream will be closed
     *            when method returns.
     * @param fragmentShader_stream
     *            GLSL source code for a fragment shader. Stream will be closed
     *            when method returns.
     */
    @Override
    public GVRShaderId newShader(InputStream vertexShader_stream,
            InputStream fragmentShader_stream) {
        return newShader(vertexShader_stream, fragmentShader_stream, GLSLESVersion.V100);
    }

    /**
     * Adds a shader from sources in separate input streams.
     * @param vertexShader_stream
     *            GLSL source code for a vertex shader. Stream will be closed
     *            when method returns.
     * @param fragmentShader_stream
     *            GLSL source code for a fragment shader. Stream will be closed
     *            when method returns.
     * @param glslesVersion GLSL ES version used by the shaders
     */
    public GVRShaderId newShader(InputStream vertexShader_stream,
                                 InputStream fragmentShader_stream, GLSLESVersion glslesVersion) {
        String vertexShader = TextFile.readTextFile(vertexShader_stream);
        String fragmentShader = TextFile.readTextFile(fragmentShader_stream);
        final VrAppSettings settings = getGVRContext().getActivity().getAppSettings();

        String defines;
        if (GLSLESVersion.V300 == glslesVersion) {
            defines = "#version " + glslesVersion.toInt() + " es\n";
            if (settings.isMultiviewSet())
            {
                defines = defines + "#define HAS_MULTIVIEW\n";
            }
        } else {
            defines = "";
        }

        vertexShader = defines + vertexShader;
        fragmentShader = defines + fragmentShader;

        return newShader(vertexShader, fragmentShader);
    }

    /**
     * Retrieves the shader template of the specified class.
     *
     * A shader template is capable of generating multiple variants
     * from a single shader source. The exact vertex and fragment
     * shaders are generated by GearVRF based on the lights
     * being used and the material attributes.
     *
     * Only one template of a given class is necessary because
     * shaders are global. The shader manager controls instantiation
     * of the shader instances but you may subclass GVRShaderTemplate
     * to create your own shader templates.
     *
     * @param templateClass class (subclass of GVRShaderTemplate)
     * @return instance of GVRShaderTemplate or null on error
     * {@link GVRShaderTemplate}
     */
    public GVRShaderTemplate retrieveShaderTemplate(Class<? extends GVRShaderTemplate> templateClass)
            throws IllegalArgumentException, UnsupportedOperationException {
        synchronized (mShaderTemplates)
        {
            GVRShaderTemplate template = mShaderTemplates.get(templateClass);
            if (template != null)
            {
                return template;
            }
            try
            {
                Constructor<? extends GVRShaderTemplate> constructor =
                        templateClass.getConstructor(GVRContext.class);
                template = (GVRShaderTemplate) constructor.newInstance(getGVRContext());
                mShaderTemplates.put(templateClass, template);
            }
            catch (NoSuchMethodException mex)
            {
                throw new UnsupportedOperationException("shader template of class "
                                                        + templateClass.getSimpleName()
                                                        +
                                                        " does not have a constructor which takes GVRContext "
                                                        + mex.getMessage());
            }
            catch (InstantiationException iex)
            {
                throw new UnsupportedOperationException("error creating shader template of class "
                                                        + templateClass.getSimpleName()
                                                        + iex.getMessage());
            }
            catch (IllegalAccessException aex)
            {
                throw new UnsupportedOperationException("error creating shader template of class "
                                                        + templateClass.getSimpleName()
                                                        + aex.getMessage());
            }
            catch (InvocationTargetException tex)
            {
                throw new UnsupportedOperationException("error creating shader template of class "
                                                        + templateClass.getSimpleName()
                                                        + tex.getMessage());
            }
            // TODO: generate event to force rebinding of shaders
            return template;
        }
    }

    protected Resources getResources() {
        return getGVRContext().getContext().getResources();
    }

    protected static InputStream open(Resources resources, String pathPrefix,
            String assetRelativeFilename) throws IOException {
        AssetManager assets = resources.getAssets();
        if (pathPrefix != null) {
            assetRelativeFilename = pathPrefix + File.separator
                    + assetRelativeFilename;
        }
        return assets.open(assetRelativeFilename);
    }

    protected static InputStream open(Resources resources, int resRawId) {
        return resources.openRawResource(resRawId);
    }

    /**
     * Maps the shader template class to the instance of the template.
     * Only one shader template of each class is necessary since
     * shaders are global.
     */
    protected Map<Class<? extends GVRShaderTemplate>, GVRShaderTemplate>
            mShaderTemplates = new HashMap<Class<? extends GVRShaderTemplate>, GVRShaderTemplate>();

    public enum GLSLESVersion {
        V100(100),
        V300(300);

        private GLSLESVersion(int v) {
            this.v = v;
        }
        private final int v;

        private int toInt() {
            return v;
        }
    }
}
