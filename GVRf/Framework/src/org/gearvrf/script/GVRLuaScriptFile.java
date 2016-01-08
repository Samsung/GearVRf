package org.gearvrf.script;

import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRContext;

public class GVRLuaScriptFile extends GVRScriptFile {
    public GVRLuaScriptFile(GVRContext gvrContext, InputStream inputStream) throws IOException {
        super(gvrContext, GVRScriptManager.LANG_LUA);
        load(inputStream);
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
