package org.gearvrf.widgetlib.widget;

import org.gearvrf.GVRSceneObject;

public final class Helpers {
    public static String getFullName(GVRSceneObject sceneObject) {
        if (sceneObject != null) {
            StringBuilder builder = new StringBuilder();
            getFullNameHelper(builder, sceneObject);
            return builder.toString();
        }
        return "<null>";
    }

    static private void getFullNameHelper(StringBuilder builder, GVRSceneObject sceneObject) {
        if (sceneObject != null) {
            GVRSceneObject parent = sceneObject.getParent();
            if (parent != null) {
                getFullNameHelper(builder, parent);
            }
            if (builder.length() > 0) {
                builder.append('.');
            }
            String name = sceneObject.getName();
            if (name == null || name.isEmpty()) {
                name = "<null>";
            }
            builder.append(name);
        }
    }

    private Helpers() {

    }
}
