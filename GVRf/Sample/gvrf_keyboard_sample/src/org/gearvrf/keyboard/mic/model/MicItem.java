
package org.gearvrf.keyboard.mic.model;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class MicItem extends GVRSceneObject {

    public static final float WIDTH = 1.2f;
    public static float HIGHT = 1.2f;

    public MicItem(GVRContext gvrContext, int gVRAndroidResourceTexture) {

        super(gvrContext, HIGHT, WIDTH, gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                gVRAndroidResourceTexture)));
        setName(SceneObjectNames.MIC_ITEM);

    }

}
