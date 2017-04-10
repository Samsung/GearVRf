/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor3d;

class AssetType {
    static final String TYPE_2D = "2D";
    static final String TYPE_3D = "3D";

    private final String type;

    private AssetType(String type) {
        this.type = type;
    }

    String getValue() {
        return type;
    }

    static AssetType fromString(String value) {
        AssetType assetType = new AssetType(value);
        if (TYPE_2D.equals(value) || TYPE_3D.equals(value)) {
            return assetType;
        } else {
            throw new IllegalArgumentException("Invalid Asset Type value");
        }
    }
}
