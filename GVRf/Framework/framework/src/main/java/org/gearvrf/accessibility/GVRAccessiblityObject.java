/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.gearvrf.accessibility;

import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class GVRAccessiblityObject extends GVRSceneObject {

    private GVRAccessibilityTalkBack mTalkBack;

    public GVRAccessiblityObject(GVRContext gvrContext, float width, float height, GVRTexture texture, GVRMaterialShaderId shaderId) {
        super(gvrContext, width, height, texture, shaderId);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, float width, float height) {
        super(gvrContext, width, height);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, Future<GVRMesh> futureMesh, Future<GVRTexture> futureTexture) {
        super(gvrContext, futureMesh, futureTexture);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, GVRAndroidResource mesh, GVRAndroidResource texture) {
        super(gvrContext, mesh, texture);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture, GVRMaterialShaderId shaderId) {
        super(gvrContext, mesh, texture, shaderId);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);

    }

    public GVRAccessiblityObject(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, mesh);

    }

    public GVRAccessiblityObject(GVRContext gvrContext) {
        super(gvrContext);

    }

    public GVRAccessibilityTalkBack getTalkBack() {
        return mTalkBack;
    }

    public void setTalkBack(GVRAccessibilityTalkBack mTalkBack) {
        this.mTalkBack = mTalkBack;
    }

}
