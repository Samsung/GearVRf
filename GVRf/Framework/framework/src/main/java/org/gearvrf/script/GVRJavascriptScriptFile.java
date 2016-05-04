package org.gearvrf.script;

import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRContext;

/**
 * Represents a Javascript file. The script text can be loaded in one
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
public class GVRJavascriptScriptFile extends GVRScriptFile {
    /**
     * Loads a Javascript file from {@code inputStream}.
     *
     * @param gvrContext
     *     The GVR Context.
     * @param inputStream
     *     The input stream from which the script is loaded.
     * @throws IOException
     */
    public GVRJavascriptScriptFile(GVRContext gvrContext, InputStream inputStream) throws IOException {
        super(gvrContext, GVRScriptManager.LANG_JAVASCRIPT);
        load(inputStream);
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
            sb.append(getDefaultParamName(i));
        }

        sb.append(");");
        return sb.toString();
    }
}
