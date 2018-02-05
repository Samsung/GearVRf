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

package org.gearvrf;

interface IActivityNative {
    void onDestroy();

    void setCameraRig(GVRCameraRig cameraRig);

    void onUndock();

    void onDock();

    long getNative();

    void onPause();

    void onResume();
}

class ActivityNativeStub implements IActivityNative {

    @Override
    public void onDestroy() {

    }

    @Override
    public void setCameraRig(GVRCameraRig cameraRig) {

    }

    @Override
    public void onUndock() {

    }

    @Override
    public void onDock() {

    }

    @Override
    public long getNative() {
        return 0;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}