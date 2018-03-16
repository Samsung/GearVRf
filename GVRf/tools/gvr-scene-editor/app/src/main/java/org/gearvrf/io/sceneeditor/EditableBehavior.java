package org.gearvrf.io.sceneeditor;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.BoundingVolume;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.sceneeditor.EditObjectView.EditViewChangeListener;
import org.gearvrf.utility.Log;

import java.io.IOException;

public class EditableBehavior extends GVRBehavior implements EditViewChangeListener {
    public static final String TAG = EditableBehavior.class.getSimpleName();
    private static final String ARROW_MODEL = "arrow.fbx";
    private static long TYPE_EDITABLE = ((long) EditableBehavior.class.hashCode() << 32) & (System
            .currentTimeMillis() & 0xffffffff);
    private final DetachListener detachListener;

    private CursorManager cursorManager;
    private GVRScene scene;
    private EditObjectView editableView;
    private GVRSceneObject arrow;
    private GVRAnimationEngine animationEngine;
    private GVRAnimation rotationAnimation;
    private Cursor cursor;

    public interface DetachListener {
        void onDetach();

        void onRemoveFromScene(GVRSceneObject gvrSceneObject);
    }

    protected EditableBehavior(CursorManager cursorManager, GVRScene scene, DetachListener
            listener) {
        super(cursorManager.getGVRContext());
        this.detachListener = listener;
        mType = getComponentType();
        this.scene = scene;
        this.cursorManager = cursorManager;
        animationEngine = cursorManager.getGVRContext().getAnimationEngine();
        try {
            arrow = getGVRContext().getAssetLoader().loadModel(ARROW_MODEL);
            rotationAnimation = new GVRRotationByAxisAnimation(arrow, 1.0f, 360.0f, 0.0f, 1.0f,
                    0.0f).setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        } catch (IOException e) {
            Log.e(TAG, "Could not load arrow model:", e.getMessage());
        }
    }

    @Override
    public void onAttach(final GVRSceneObject newOwner) {
        cursorManager.enableSettingsCursor(cursor);
        if (editableView == null) {
            editableView = new EditObjectView(scene, EditableBehavior
                    .this);
        }
        editableView.setSceneObject(newOwner);
        editableView.render();
        if (arrow != null) {
            scene.addSceneObject(arrow);
            rotationAnimation.start(animationEngine);
            adjustArrowPosition(newOwner);
        }
    }

    private void adjustArrowPosition(GVRSceneObject ownerObject) {
        if (ownerObject != null) {
            BoundingVolume volume = ownerObject.getBoundingVolume();
            arrow.getTransform().setPosition(volume.center.x, volume.center.y + volume.radius,
                    volume.center.z);
        }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        scene.removeSceneObject(arrow);
        animationEngine.stop(rotationAnimation);
        detachListener.onDetach();
    }

    @Override
    public void onClose() {
        cursorManager.disableSettingsCursor();
        if (getOwnerObject() != null) {
            getOwnerObject().detachComponent(getComponentType());
        }
    }

    @Override
    public void onScaleChange() {
        adjustArrowPosition(getOwnerObject());
    }

    @Override
    public void removeFromScene() {
        GVRSceneObject ownerObject = getOwnerObject();
        onClose();
        detachListener.onRemoveFromScene(ownerObject);
    }

    public static long getComponentType() {
        return TYPE_EDITABLE;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
