
package org.gearvrf.keyboard.mic;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.mic.model.MicItem;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class MicGroupIcons extends GVRSceneObject {

    private MicItem mIcon;
    int mOff = R.drawable.mic_icon_off;
    int mOn = R.drawable.mic_icon_on;

    public MicGroupIcons(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.MIC_GROUP_ICONS);

        mIcon = new MicItem(gvrContext, mOff);
        mIcon.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_ICON);
        this.addChildObject(mIcon);
    }

    public void show() {

        changeMicIcon(mOn);
    }

    private void changeMicIcon(final int res) {
        if (mIcon != null) {

            this.getGVRContext().runOnGlThread(new Runnable() {

                @Override
                public void run() {
                    mIcon.getRenderData()
                            .getMaterial()
                            .setMainTexture(
                                    MicGroupIcons.this.getGVRContext().loadTexture(
                                            new GVRAndroidResource(MicGroupIcons.this
                                                    .getGVRContext(), res)));
                }

            });

        }

    }

    public void hide() {
        changeMicIcon(mOff);
    }

    // public void animateOpacity(GVRContext context) {
    // if (!isVisibleByOpacity) {
    // isVisibleByOpacity = true;
    // opacityAnimation = new GVROpacityAnimation(this, 1, 1);
    // opacityAnimation.start(context.getAnimationEngine());
    // }
    // }

}
