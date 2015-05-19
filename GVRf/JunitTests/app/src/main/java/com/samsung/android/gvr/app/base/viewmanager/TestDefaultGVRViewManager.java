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


package com.samsung.android.gvr.app.base.viewmanager;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.DefaultGVRTestActivity;

public class TestDefaultGVRViewManager extends GVRScript {

    public static GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        synchronized (DefaultGVRTestActivity.class) {
            DefaultGVRTestActivity.sContextLoaded = true;
            DefaultGVRTestActivity.class.notify();

        }
        mGVRContext = gvrContext;
        onInitTest();
    }

    @Override
    public void onStep() {
    }

    public void onInitTest() {
        GVRAndroidResource androidResource = new GVRAndroidResource(mGVRContext, R.mipmap.cylinder);
        GVRMesh mesh = mGVRContext.loadMesh(androidResource);

        GVRSceneObject leftScreen = new GVRSceneObject(mGVRContext, mesh,
                mGVRContext.loadTexture("sample_20140509_l.bmp"));
        GVRSceneObject rightScreen = new GVRSceneObject(mGVRContext, mesh,
                mGVRContext.loadTexture("sample_20140509_r.bmp"));

        mGVRContext.getMainScene().addSceneObject(leftScreen);
        mGVRContext.getMainScene().addSceneObject(rightScreen);
    }

}
