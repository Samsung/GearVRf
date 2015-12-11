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

public class RemoteFileAttributes {

    private String modelFileURL;
    private String[] textureFileURLArray;

    public String getModelFileURL() {
        return modelFileURL;
    }

    public void setModelFileURL(String modelFileURL) {
        this.modelFileURL = modelFileURL;
    }

    public String getModelFileName() {
        if (modelFileURL.length() > 0) {
            int indexOfLastSlash = modelFileURL.lastIndexOf('/');
            String modelFileName = modelFileURL.substring(indexOfLastSlash + 1);
            return modelFileName;
        } else {
            return null;
        }
    }

    public void setTextureFileURLArraySize(int arraySize) {
        this.textureFileURLArray = new String[arraySize];
    }

    public String getTextureFileURLAtIndex(int index) {
        return textureFileURLArray[index];
    }

    public void setTextureFileURLArrayAtIndex(int index, String textureFileURL) {
        textureFileURLArray[index] = textureFileURL;
    }

    public String[] getTextureFileURLArray() {
        return textureFileURLArray;
    }
}
