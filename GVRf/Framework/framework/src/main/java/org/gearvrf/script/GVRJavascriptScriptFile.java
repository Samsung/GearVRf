package org.gearvrf.script;

import android.util.Log;

import org.gearvrf.GVRContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Represents a Javascript file. The script text can be loaded in one
 * of the following ways.
 * <ul>
 * <li>
 *   Loaded from a {@link org.gearvrf.GVRAndroidResource} using {@link GVRScriptManager#loadScript(org.gearvrf.GVRAndroidResource, String)}.
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
 * script using {@link GVRScriptFile#invokeFunction(String, Object[])},
 * or attach it to a scriptable object using {@link GVRScriptManager#attachScriptFile(IScriptable, GVRScriptFile)}
 * to handle events delivered to it.
 */


public class GVRJavascriptScriptFile  extends GVRScriptFile {

    /**
     * Loads a Javascript file from {@code inputStream}.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param inputStream
     *     The input stream from which the script is loaded.
     * @throws IOException if the script cannot be read.
     */

    public GVRJavascriptScriptFile(GVRContext gvrContext, InputStream inputStream) throws IOException {
        super(gvrContext, GVRScriptManager.LANG_JAVASCRIPT);
        load(inputStream);
    }


    /**
     * Loads a Javascript file from a text string.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param scriptText
     *     String containing a Javascript program.
     */
    private static final String TAG = GVRJavascriptScriptFile.class.getSimpleName();

    //protected String mScriptText;
    //protected GVRContext mGvrContext;

    public GVRJavascriptScriptFile(GVRContext gvrContext, String scriptText) {
        super(gvrContext, GVRScriptManager.LANG_JAVASCRIPT);
        setScriptText(scriptText);
    }

    protected String getInvokeStatement(String eventName, Object[] params) {

        StringBuilder sb = new StringBuilder();

        // function name
        sb.append(eventName);
        sb.append("(");

        // params
        for (int i = 0; i < params.length; ++i) {
            if (i != 0) {
                sb.append(", ");
            }
            // commented out just to get this to build
            sb.append(getDefaultParamName(i));
        }

        sb.append(");");
        return sb.toString();
    }
}
