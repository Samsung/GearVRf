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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.script.javascript.RhinoScriptEngineFactory;
import org.gearvrf.utility.Log;

import com.naef.jnlua.script.LuaScriptEngineFactory;

/**
 * The script manager class handles script engines, script attachment/
 * detachment with scriptable objects, and other operation related to
 * scripting.
 */
public class GVRScriptManager {
    private static final String TAG = GVRScriptManager.class.getSimpleName();
    public static final String LANG_LUA = "lua";
    public static final String LANG_JAVASCRIPT = "js";
    public static final String VAR_NAME_GVRF = "gvrf";

    protected GVRContext mGvrContext;
    protected Map<String, ScriptEngine> mEngines;

    protected Map<String, Object> mGlobalVariables;

    protected Map<IScriptable, GVRScriptFile> mScriptMap;

    /**
     * Constructor.
     *
     * @param gvrContext
     *     The GVR Context.
     */
    public GVRScriptManager(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        mGlobalVariables = new TreeMap<String, Object>();
        mScriptMap = Collections.synchronizedMap(new HashMap<IScriptable, GVRScriptFile>());

        Thread.currentThread().setContextClassLoader(
                gvrContext.getActivity().getClassLoader());

        initializeGlobalVariables();
        initializeEngines();
    }

    private void initializeGlobalVariables() {
        mGlobalVariables.put(VAR_NAME_GVRF, mGvrContext);
    }

    private void initializeEngines() {
        mEngines = new TreeMap<String, ScriptEngine>();

        // Add languages
        mEngines.put(LANG_LUA, new LuaScriptEngineFactory().getScriptEngine());
        mEngines.put(LANG_JAVASCRIPT, new RhinoScriptEngineFactory().getScriptEngine());

        // Add variables to engines
        refreshGlobalBindings();
    }

    private void refreshGlobalBindings() {
        for (ScriptEngine se : mEngines.values()) {
            addGlobalBindings(se);
        }
    }

    protected void addGlobalBindings(ScriptEngine engine) {
        Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        if (bindings == null) {
            bindings = engine.createBindings();
            engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        }

        synchronized (mGlobalVariables) {
            for (Map.Entry<String, Object> ent : mGlobalVariables.entrySet()) {
                bindings.put(ent.getKey(), ent.getValue());
            }
        }
    }

    /**
     * Returns an engine based on language.
     *
     * @param language The name of the language. Please use constants
     * defined in {@code ScriptManager}, such as LANG_LUA and LANG_JAVASCRIPT.
     *
     * @return The engine object. {@code null} if the specified engine is
     * not found.
     */
    public ScriptEngine getEngine(String language) {
        return mEngines.get(language);
    }

    /**
     * Add a variable to the scripting context.
     * 
     * @param varName The variable name.
     * @param value The variable value.
     */
    public void addVariable(String varName, Object value) {
        synchronized (mGlobalVariables) {
            mGlobalVariables.put(varName, value);
        }
        refreshGlobalBindings();
    }

    /**
     * Attach a script file to a scriptable target.
     *
     * @param target The scriptable target.
     * @param scriptFile The script file object.
     */
    public void attachScriptFile(IScriptable target, GVRScriptFile scriptFile) {
        mScriptMap.put(target, scriptFile);
    }

    /**
     * Detach any script file from a scriptable target.
     *
     * @param target The scriptable target.
     */
    public void detachScriptFile(IScriptable target) {
        mScriptMap.remove(target);
    }

    /**
     * Gets a script file from a scriptable target.
     * @param target The scriptable target.
     * @return The script file or {@code null}.
     */
    public GVRScriptFile getScriptFile(IScriptable target) {
        return mScriptMap.get(target);
    }

    /**
     * Loads a script file using {@GVRAndroidResource}.
     * @param resource The resource object.
     * @param language The language string.
     * @return A script file object or {@code null} if not found.
     * @throws IOException
     */
    public GVRScriptFile loadScript(GVRAndroidResource resource, String language) throws IOException {
        if (getEngine(language) == null) {
            Log.e(TAG, "The language is unknown: %s", language);
            return null;
        }

        GVRScriptFile script = null;
        if (language.equals(LANG_LUA)) {
            script = new GVRLuaScriptFile(mGvrContext, resource.getStream());
        } else if (language.equals(LANG_JAVASCRIPT)) {
            script = new GVRJavascriptScriptFile(mGvrContext, resource.getStream());
        }

        return script;
    }
}
