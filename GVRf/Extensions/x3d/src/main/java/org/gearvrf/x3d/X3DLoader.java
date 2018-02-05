/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.x3d;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

final class X3DLoader {
    public static GVRSceneObject load(final GVRContext context, final GVRAssetLoader.AssetRequest assetRequest, final GVRSceneObject root) throws IOException {
        final GVRResourceVolume volume = assetRequest.getVolume();
        final String fileName = assetRequest.getBaseName();
        final GVRAndroidResource resource = volume.openResource(fileName);
        root.setName(fileName);

        X3Dobject x3dObject = new org.gearvrf.x3d.X3Dobject(assetRequest, root);
        try
        {
            InputStream inputStream;
            ShaderSettings shaderSettings = new ShaderSettings(new GVRMaterial(context));
            if (!X3Dobject.UNIVERSAL_LIGHTS)
            {
                X3DparseLights x3dParseLights = new X3DparseLights(context, root);
                inputStream = resource.getStream();
                if (inputStream == null)
                {
                    throw new FileNotFoundException(fileName + " not found");
                }
                Log.d("X3DLoader", "Parse: " + fileName);
                x3dParseLights.Parse(inputStream, shaderSettings);
                inputStream.close();
            }
            inputStream = resource.getStream();
            if (inputStream == null)
            {
                throw new FileNotFoundException(fileName + " not found");
            }

            try {
                x3dObject.Parse(inputStream, shaderSettings);
                assetRequest.onModelLoaded(context, root, fileName);
            } finally {
                inputStream.close();
            }
        }
        catch (Exception ex)
        {
            assetRequest.onModelError(context, ex.getMessage(), fileName);
            throw ex;
        }
        return root;
    }
}
