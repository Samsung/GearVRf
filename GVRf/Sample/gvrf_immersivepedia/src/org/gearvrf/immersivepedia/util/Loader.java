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

package org.gearvrf.immersivepedia.util;

import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.R;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;

import java.io.File;
import java.io.FileNotFoundException;

public class Loader {

    private static final String MESH_FILE = "_mesh.fbx";
    private static final String TEXTURE_DIFFUSE_FILE = "_tex_diffuse.png";

    public static FocusableSceneObject loadFocusableSceneObjectFromFile(GVRContext context,
            String fileName, int meshId, int textureId) {

        GVRAndroidResource meshResource = null;

        try {
            File meshFile = getFile(context, fileName + MESH_FILE);

            if (meshFile.exists() == true) {
                meshResource = new GVRAndroidResource(meshFile);
            } else {
                meshResource = new GVRAndroidResource(context, meshId);
            }

            GVRMesh mesh = context.loadMesh(new GVRAndroidResource(meshFile));

            File textureFile = getFile(context, fileName + TEXTURE_DIFFUSE_FILE);
            GVRAndroidResource textureResource = null;

            if (textureFile.exists() == true) {
                textureResource = new GVRAndroidResource(textureFile);
            } else {
                textureResource = new GVRAndroidResource(context, textureId);
            }
            GVRTexture texture = context.loadTexture(textureResource);

            return new FocusableSceneObject(context, mesh, texture);
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.toString());
            e.printStackTrace();
        }

        return null;
    }

    public static GVRAndroidResource loadResourceFromFile(GVRContext context,
            String fileName, int resourceId) {
        try {
            File resource = getFile(context, fileName);

            if (resource.exists() == true) {
                return new GVRAndroidResource(resource);
            }
            return new GVRAndroidResource(context, resourceId);
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.toString());
            e.printStackTrace();
        }

        return null;
    }

    private static File getFile(GVRContext context, String fileName) {
        Log.i("vr-getfile", fileName);
        Log.i("vr-getfile", Environment.getExternalStorageDirectory() + File.separator
                + context.getContext().getString(R.string.app_name) + File.separator + fileName);

        return new File(Environment.getExternalStorageDirectory() + File.separator
                + context.getContext().getString(R.string.app_name) + File.separator + fileName);
    }

}
