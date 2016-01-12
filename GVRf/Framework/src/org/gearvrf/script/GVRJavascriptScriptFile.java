package org.gearvrf.script;

import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRContext;

public class GVRJavascriptScriptFile extends GVRScriptFile {
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
