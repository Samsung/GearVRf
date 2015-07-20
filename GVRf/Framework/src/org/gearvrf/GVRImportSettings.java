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

/**
 * Encapsulates Import import settings to be passed in to Importer.
 * TODO: Couldn't find a way to use bitwise | operators with enum. Think of a better way to 
 *       guarantee client code won't call invalid settings.
 */
public class GVRImportSettings {
    /**
     * Use Default Importer Settings with no additional options.
     */
    public static final int DEFAULT = 0x0;
    
    /**
     * Tell Importer to calculate hard normals in case they are not present in imported model.
     */
    public static final int CALCULATE_SMOOTH_NORMALS = 0x1;
    
    /**
     * Tell Importer to calculate tangents in case they are not present in imported model.
     * This is required for bump map as most models won't have tangents exported.
     */
    public static final int CALCULATE_NORMALS = 0x2;
    
    /**
     * Tell Importer to calculate smooth normals in case they are not present in imported model. 
     */
    public static final int CALCULATE_TANGENTS = 0x4;
    
    /**
     * Tell Importer to optimize mesh to reduce drawcalls and improve cache locality. This might
     * increase loading time.
     */
    public static final int OPTIMIZE_MESH_DATA = 0x8;
    
    /**
     * Stores current settings value
     */
    private int mValue;
    
    public GVRImportSettings(int settings) {
        mValue = settings;
    }
    
    public int getValue() {
        return mValue;
    }
}