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

package org.gearvrf.urlmodelloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.utility.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;

public class URLModelLoaderViewManager extends GVRScript {

    JSONObject jsonobject;
    JSONArray jsonarray;
    ProgressDialog mProgressDialog;
    RemoteFileAttributes modelUtils = new RemoteFileAttributes();
    private GVRScene scene;

    @Override
    public void onInit(GVRContext gvrContext) throws InterruptedException, ExecutionException, IOException {

        scene = gvrContext.getNextMainScene();
        scene.setFrustumCulling(true);

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        String jsonFileURL = "https://raw.githubusercontent.com/lokeshguddu/lokeshguddu.github.io/master/remote_model_urls.json";

        // Populate the RemoteFileAttributes Class
        new ParseJsonURL().execute(jsonFileURL).get();

        // Model File Name
        String modelFileName = modelUtils.getModelFileName();

        // Download model file
        new DownloadFile().execute(modelUtils.getModelFileURL()).get();

        // Download textures
        String[] textureFileURLArray = modelUtils.getTextureFileURLArray();
        new DownloadFile().execute(textureFileURLArray).get();

        // Model with texture
        GVRSceneObject astroBoyModel = gvrContext.getAssimpModelFromSDCard(modelFileName);

        ModelPosition astroBoyModelPosition = new ModelPosition();

        astroBoyModelPosition.setPosition(0.0f, -0.4f, -0.5f);

        astroBoyModel.getTransform().setScale(5, 5, 5);

        astroBoyModel.getTransform().setPosition(astroBoyModelPosition.x, astroBoyModelPosition.y,
                astroBoyModelPosition.z);

        // add the scene object to the scene graph
        scene.addSceneObject(astroBoyModel);

    }

    @Override
    public void onStep() {
    }

    void onTap() {
        // toggle whether stats are displayed.
        boolean statsEnabled = scene.getStatsEnabled();
        scene.setStatsEnabled(!statsEnabled);
    }

    // Here we are diverting the JSON related task to a different thread
    private class ParseJsonURL extends AsyncTask<String, Void, RemoteFileAttributes> {

        @Override
        protected RemoteFileAttributes doInBackground(String... jsonFileURL) {
            jsonobject = JSONDownloader.getJSONfromURL(jsonFileURL[0]);

            String modelFileURL = jsonobject.optString("model");

            try {
                // Textures
                jsonarray = jsonobject.getJSONArray("textures");

                modelUtils.setTextureFileURLArraySize(jsonarray.length());

                for (int i = 0; i < jsonarray.length(); i++) {

                    jsonobject = jsonarray.getJSONObject(i);

                    modelUtils.setTextureFileURLArrayAtIndex(i, jsonobject.optString("texture"));
                }

                // Model

                System.out.println("Model Link: " + modelFileURL);

                modelUtils.setModelFileURL(modelFileURL);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return modelUtils;
        }
    }

    private class DownloadFile extends AsyncTask<String, String, String> {

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... fileURLs) {
            int count;
            try {
                for (String fileURL : fileURLs) {
                    System.out.println("FILE_URL: " + fileURL);
                    URL url = new URL(fileURL);
                    URLConnection conection = url.openConnection();
                    conection.connect();

                    // System.out.println("---HERE---");

                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    int indexOfLastSlash = fileURL.lastIndexOf('/');
                    String directoryPath = Environment.getExternalStorageDirectory().getPath();
                    System.out.println("---HERE--- : " + indexOfLastSlash);
                    String outputFileName = fileURL.substring(indexOfLastSlash + 1);

                    System.out.println("OUT_FILE_NAME: " + outputFileName);

                    // Output stream to write file
                    OutputStream output = new FileOutputStream(directoryPath + "/" + outputFileName);

                    byte data[] = new byte[1024];

                    while ((count = input.read(data)) != -1) {
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }
    }

}

class ModelPosition {
    float x;
    float y;
    float z;

    void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}