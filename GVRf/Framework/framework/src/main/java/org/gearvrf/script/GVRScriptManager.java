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
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.IScriptEvents;
import org.gearvrf.script.javascript.RhinoScriptEngineFactory;

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

    // For script bundles. All special targets start with @.
    public static final String TARGET_PREFIX = "@";
    public static final String TARGET_GVRSCRIPT = "@GVRScript";
    public static final String TARGET_GVRACTIVITY = "@GVRActivity";

    interface TargetResolver {
        IScriptable getTarget(GVRContext gvrContext, String name);
    }

    static Map<String, TargetResolver> sBuiltinTargetMap;

    // Provide getters for non-scene-object targets.
    static {
        sBuiltinTargetMap = new TreeMap<String, TargetResolver>();

        // Target resolver for "@GVRScript"
        sBuiltinTargetMap.put(TARGET_GVRSCRIPT, new TargetResolver() {
            @Override
            public IScriptable getTarget(GVRContext gvrContext,
                    String name) {
                return gvrContext.getActivity().getScript();
            }
        });

        // Target resolver for "@GVRActivity"
        sBuiltinTargetMap.put(TARGET_GVRACTIVITY, new TargetResolver() {
            @Override
            public IScriptable getTarget(GVRContext gvrContext,
                    String name) {
                return gvrContext.getActivity();
            }
        });
    }

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
        scriptFile.invokeFunction("onAttach", new Object[] { target });
    }

    /**
     * Detach any script file from a scriptable target.
     *
     * @param target The scriptable target.
     */
    public void detachScriptFile(IScriptable target) {
        GVRScriptFile scriptFile = mScriptMap.remove(target);
        if (scriptFile != null) {
            scriptFile.invokeFunction("onDetach", new Object[] { target });
        }
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
    public GVRScriptFile loadScript(GVRAndroidResource resource, String language) throws IOException, GVRScriptException {
        if (getEngine(language) == null) {
            mGvrContext.logError("Script language " + language + " unsupported", this);
            throw new GVRScriptException(String.format("The language is unknown: %s", language));
        }

        GVRScriptFile script = null;
        if (language.equals(LANG_LUA)) {
            script = new GVRLuaScriptFile(mGvrContext, resource.getStream());
        } else if (language.equals(LANG_JAVASCRIPT)) {
            script = new GVRJavascriptScriptFile(mGvrContext, resource.getStream());
        }

        resource.closeStream();
        return script;
    }

    /**
     * Load a script bundle file. It defines bindings between scripts and GVRf objects
     * (e.g., scene objects and the {@link GVRScript} object).
     *
     * If {@linkplain GVRScriptEntry script entry} contains a {@code volume} attribute, the
     * script is loaded from the specified volume. Otherwise, it is loaded from the volume
     * specified by the {@code volume} parameter.
     *
     * @param filePath
     *        The path and filename of the script bundle.
     * @param volume
     *        The {@link GVRResourceVolume} from which to load the bundle file and scripts.
     * @return
     *         The loaded {@linkplain GVRScriptBundle script bundle}.
     *
     * @throws IOException
     */
    public GVRScriptBundle loadScriptBundle(String filePath, GVRResourceVolume volume) throws IOException {
        GVRScriptBundle bundle = GVRScriptBundle.loadFromFile(mGvrContext, filePath, volume);
        return bundle;
    }

    /**
     * Binds a script bundle to a {@link GVRScene} object.
     *
     * @param scriptBundle
     *     The script bundle.
     * @param gvrScript
     *     The {@link GVRScript} to bind to.
     * @param bindToMainScene
     *     If {@code true}, also bind it to the main scene on the event {@link GVRScript#onAfterInit}.
     * @throws GVRScriptException
     * @throws IOException
     */
    public void bindScriptBundle(final GVRScriptBundle scriptBundle, final GVRScript gvrScript, boolean bindToMainScene)
            throws IOException, GVRScriptException {
        // Here, bind to all targets except SCENE_OBJECTS. Scene objects are bound when scene is set.
        bindHelper(scriptBundle, null, BIND_MASK_GVRSCRIPT | BIND_MASK_GVRACTIVITY);

        if (bindToMainScene) {
            final IScriptEvents bindToSceneListener = new GVREventListeners.ScriptEvents() {
                GVRScene mainScene = null;

                @Override
                public void onInit(GVRContext gvrContext) throws Throwable {
                    mainScene = gvrContext.getNextMainScene();
                }

                @Override
                public void onAfterInit() {
                    try {
                        bindScriptBundleToScene(scriptBundle, mainScene);
                    } catch (IOException e) {
                        mGvrContext.logError(e.getMessage(), this);
                    } catch (GVRScriptException e) {
                        mGvrContext.logError(e.getMessage(), this);
                    } finally {
                        // Remove the listener itself
                        gvrScript.getEventReceiver().removeListener(this);
                    }
                }
            };

            // Add listener to bind to main scene when event "onAfterInit" is received
            gvrScript.getEventReceiver().addListener(bindToSceneListener);
        }
    }

    /**
     * Binds a script bundle to a scene.
     * @param scriptBundle
     *         The {@code GVRScriptBundle} object containing script binding information.
     * @param GVRScene
     *         The scene to bind to.
     * @throws GVRScriptException
     * @throws IOException
     */
    public void bindScriptBundleToScene(GVRScriptBundle scriptBundle, GVRScene scene) throws IOException, GVRScriptException {
        for (GVRSceneObject sceneObject : scene.getSceneObjects()) {
            bindBundleToSceneObject(scriptBundle, sceneObject);
        }
    }

    /**
     * Binds a script bundle to scene graph rooted at a scene object.
     * @param scriptBundle
     *         The {@code GVRScriptBundle} object containing script binding information.
     * @param rootSceneObject
     *         The root of the scene object tree to which the scripts are bound.
     * @throws IOException
     */
    public void bindBundleToSceneObject(GVRScriptBundle scriptBundle, GVRSceneObject rootSceneObject)
            throws IOException, GVRScriptException
    {
        bindHelper(scriptBundle, rootSceneObject, BIND_MASK_SCENE_OBJECTS);
    }

    protected int BIND_MASK_SCENE_OBJECTS = 0x0001;
    protected int BIND_MASK_GVRSCRIPT     = 0x0002;
    protected int BIND_MASK_GVRACTIVITY   = 0x0004;

    // Helper function to bind script bundler to various targets
    protected void bindHelper(GVRScriptBundle scriptBundle, GVRSceneObject rootSceneObject, int bindMask)
            throws IOException, GVRScriptException
    {
        for (GVRScriptBindingEntry entry : scriptBundle.file.binding) {
            GVRAndroidResource rc;
            if (entry.volumeType == null || entry.volumeType.isEmpty()) {
                rc = scriptBundle.volume.openResource(entry.script);
            } else {
                GVRResourceVolume.VolumeType volumeType = GVRResourceVolume.VolumeType.fromString(entry.volumeType);
                if (volumeType == null) {
                    throw new GVRScriptException(String.format("Volume type %s is not recognized, script=%s",
                            entry.volumeType, entry.script));
                }
                rc = new GVRResourceVolume(mGvrContext, volumeType).openResource(entry.script);
            }

            GVRScriptFile scriptFile = loadScript(rc, entry.language);

            String targetName = entry.target;
            if (targetName.startsWith(TARGET_PREFIX)) {
                TargetResolver resolver = sBuiltinTargetMap.get(targetName);
                IScriptable target = resolver.getTarget(mGvrContext, targetName);

                // Apply mask
                boolean toBind = false;
                if ((bindMask & BIND_MASK_GVRSCRIPT) != 0 && targetName.equalsIgnoreCase(TARGET_GVRSCRIPT)) {
                    toBind = true;
                }

                if ((bindMask & BIND_MASK_GVRACTIVITY) != 0 && targetName.equalsIgnoreCase(TARGET_GVRACTIVITY)) {
                    toBind = true;
                }

                if (toBind) {
                    attachScriptFile(target, scriptFile);
                }
            } else {
                if ((bindMask & BIND_MASK_SCENE_OBJECTS) != 0) {
                    if (targetName.equals(rootSceneObject.getName())) {
                        attachScriptFile(rootSceneObject, scriptFile);
                    }

                    // Search in children
                    GVRSceneObject[] sceneObjects = rootSceneObject.getSceneObjectsByName(targetName);
                    if (sceneObjects != null) {
                        for (GVRSceneObject sceneObject : sceneObjects) {
                            GVRScriptBehavior b = new GVRScriptBehavior(sceneObject.getGVRContext());
                            b.setScriptFile(scriptFile);
                            sceneObject.attachComponent(b);
                        }
                    }
                }
            }
        }
    }
}