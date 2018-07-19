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

package org.gearvrf.script;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRResourceVolume;

import java.io.IOException;

import javax.script.ScriptEngine;

public interface IScriptManager {

    public static final String LANG_JAVASCRIPT = "js";

    ScriptEngine getEngine(String language);

    IScriptFile getScriptFile(IScriptable target);

    void destroy();

    IScriptFile loadScript(GVRAndroidResource resource, String language) throws IOException, GVRScriptException;

    void attachScriptFile(IScriptable target, IScriptFile scriptFile);

    void detachScriptFile(IScriptable target);

    void addVariable(String varName, Object value);

    void addGlobalBindings(ScriptEngine engine);

    IScriptBundle loadScriptBundle(String filePath, GVRResourceVolume volume) throws IOException;

    void bindScriptBundle(IScriptBundle scriptBundle, GVRMain gvrMain, boolean bindToMainScene)
            throws IOException, GVRScriptException;
}
