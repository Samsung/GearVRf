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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.utility.TextFile;

import android.content.res.AssetManager;
import android.content.res.Resources;

public abstract class GVRBaseShaderManager<MAP, ID> extends GVRHybridObject
        implements GVRShaderManagers<MAP, ID> {

    protected GVRBaseShaderManager(GVRContext gvrContext, long pointer) {
        super(gvrContext, pointer);
    }

    @Override
    public ID addShader(String pathPrefix, String vertexShader_asset,
            String fragmentShader_asset) {
        Resources resources = getResources();
        try {
            InputStream vertexShader = open(resources, pathPrefix,
                    vertexShader_asset);
            InputStream fragmentShader = open(resources, pathPrefix,
                    fragmentShader_asset);
            return addShader(vertexShader, fragmentShader);
        } catch (IOException e) {
            e.printStackTrace(); // give user a clue
            return null;
        }
    }

    @Override
    public ID addShader(int vertexShader_resRaw, int fragmentShader_resRaw) {
        Resources resources = getResources();
        InputStream vertexShader = open(resources, vertexShader_resRaw);
        InputStream fragmentShader = open(resources, fragmentShader_resRaw);
        return addShader(vertexShader, fragmentShader);
    }

    @Override
    public ID addShader(InputStream vertexShader_stream,
            InputStream fragmentShader_stream) {
        String vertexShader = TextFile.readTextFile(vertexShader_stream);
        String fragmentShader = TextFile.readTextFile(fragmentShader_stream);
        return addShader(vertexShader, fragmentShader);
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
}
