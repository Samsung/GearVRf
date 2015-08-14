package org.gearvrf.controls.gamepad;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;


public class GamepadObject extends GVRSceneObject {

    private GamepadVirtual gamepadVirtual;

    public GamepadObject(GVRContext gvrContext) {
        super(gvrContext);

        gamepadVirtual = new GamepadVirtual(gvrContext);
        
        GVRSceneObject mGVRSceneObject = new GVRSceneObject(gvrContext);
        mGVRSceneObject.addChildObject(gamepadVirtual);
        addChildObject(mGVRSceneObject);
    }

    public GamepadVirtual getGamepadVirtual() {
        return gamepadVirtual;
    }
}
