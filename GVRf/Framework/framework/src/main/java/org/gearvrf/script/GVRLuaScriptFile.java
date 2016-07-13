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

import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRContext;

/**
 * Represents a Lua script file. The script text can be loaded in one
 * of the following ways.
 * <ul>
 * <li>
 *   Loaded from a {@link GVRAndroidResource} using {@link GVRScriptManager#loadScript(org.gearvrf.GVRAndroidResource, String)}.
 * </li>
 * <li>
 *   Constructed locally and then set the text using {@link #setScriptText(String)}.
 * </li>
 * <li>
 *   Constructed locally and then load the text using {@link #load(InputStream)}.
 * </li>
 * </ul>
 *
 * Once a script text is set or loaded, you can invoke functions in the
 * script using {@link GVRScriptFile#invokeFunction(String, Object[]),
 * or attach it to a scriptable object using {@link GVRScriptManager#attachScriptFile(IScriptable, GVRScriptFile)}
 * to handle events delivered to it.
 */
public class GVRLuaScriptFile extends GVRScriptFile {
    /**
     * Loads a Lua script from {@code inputStream}.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param inputStream
     *     The input stream from which the script is loaded.
     * @throws IOException
     */
    public GVRLuaScriptFile(GVRContext gvrContext, InputStream inputStream) throws IOException {
        super(gvrContext, GVRScriptManager.LANG_LUA);
        load(inputStream);
    }

    /**
     * Loads a Lua script from a text string.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param scriptText
     *     String containing the text of a LUA program.
     */
    public GVRLuaScriptFile(GVRContext gvrContext, String scriptText) {
        super(gvrContext, GVRScriptManager.LANG_LUA);
        setScriptText(scriptText);
    }
    
    protected String getInvokeStatement(String eventName, Object[] params) {
        StringBuilder sb = new StringBuilder();
        sb.append("return ");

        // function name
        sb.append(eventName);
        sb.append("(");

        // params
        for (int i = 0; i < params.length; ++i) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(getDefaultParamName(i));
        }

        sb.append(")");
        return sb.toString();
    }
}
