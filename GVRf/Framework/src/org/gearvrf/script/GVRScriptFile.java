package org.gearvrf.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.gearvrf.GVRContext;

public abstract class GVRScriptFile {
    private static final String TAG = GVRScriptFile.class.getSimpleName();
    GVRContext mGvrContext;
    protected String mLanguage;
    protected String mScriptText;
    protected ScriptEngine mLocalEngine; 

    public GVRScriptFile(GVRContext gvrContext, String language) {
        mGvrContext = gvrContext;
        mLanguage = language;
        ScriptEngine engine = mGvrContext.getScriptManager().getEngine(mLanguage);
        mLocalEngine = engine.getFactory().getScriptEngine();

        // Add globals
        mGvrContext.getScriptManager().addGlobalBindings(mLocalEngine);
    }

    public void load(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(System.lineSeparator());
        }

        setScriptText(out.toString());
    }

    public void setScriptText(String scriptText) {
        mScriptText = scriptText;
        try {
            mLocalEngine.eval(mScriptText);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public String getScriptText() {
        return mScriptText;
    }

    public boolean invokeFunction(String eventName, Object[] params) {
        String statement = getInvokeStatement(eventName, params);
        Bindings localBindings = mLocalEngine.createBindings();
        fillBindings(localBindings, params);
        mLocalEngine.setBindings(localBindings, ScriptContext.ENGINE_SCOPE);

        try {
            mLocalEngine.eval(statement);
        } catch (ScriptException e) {
            return false;
        }

        return true;
    }

    protected String getDefaultParamName(int i) {
        return "arg" + Integer.toString(i);
    }

    protected void fillBindings(Bindings localBindings, Object[] params) {
        for (int i = 0; i < params.length; ++i) {
            localBindings.put(getDefaultParamName(i), params[i]);
        }
    }

    protected abstract String getInvokeStatement(String eventName, Object[] params);
}
