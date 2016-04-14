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

package org.gearvrf.plugin;



import android.os.Bundle;

/**
 * Plugin interface to be implemented by GVRF plugins
 */
public interface GVRPlugin {

    void init(Object... args);

    void destroy();

    void onCreateCallback(Bundle savedInstanceState);

    void onResumeCallback();

    void onPauseCallback();

    void onDestroyCallback();

    boolean isInitialised();

    void syncNotify();

    void syncWait();

}
