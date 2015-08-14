
package org.gearvrf.controls.focus;

import org.gearvrf.GVRSceneObject;


public interface FocusListener {

    public void gainedFocus(GVRSceneObject object);

    public void lostFocus(GVRSceneObject object);

    public void inFocus(GVRSceneObject object);

}
