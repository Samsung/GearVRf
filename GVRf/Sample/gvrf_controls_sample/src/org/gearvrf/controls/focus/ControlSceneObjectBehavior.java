
package org.gearvrf.controls.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;

public class ControlSceneObjectBehavior {

    public static void process(GVRContext context) {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(context.getMainScene());

        ArrayList<GVRSceneObject> needToDisableFocus = new ArrayList<GVRSceneObject>();

        for (GVRSceneObject obj : context.getMainScene().getWholeSceneObjects()) {
            needToDisableFocus.add(obj);
        }

        for (GVREyePointeeHolder holder : eyePointeeHolders) {

            if (ControlSceneObject.hasFocusMethods(holder.getOwnerObject())) {
                ControlSceneObject controlObject = (ControlSceneObject) holder.getOwnerObject();
                controlObject.setFocus(true);
                controlObject.dispatchInFocus();
                needToDisableFocus.remove(controlObject);
            }
        }

        for (GVRSceneObject obj : needToDisableFocus) {
            if (ControlSceneObject.hasFocusMethods(obj)) {
                ControlSceneObject control = (ControlSceneObject) obj;
                control.setFocus(false);
            }
        }

    }
}
