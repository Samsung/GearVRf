package org.gearvrf.widgetlib.widget.basic;

import org.gearvrf.widgetlib.widget.NodeEntry;
import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

/**
 * A radio button is a two-state button that can be either checked or unchecked. When the radio
 * button is unchecked, the user can press or click it to check it. However, contrary to a
 * CheckBox, a radio button cannot be unchecked by the user once checked.
 * Radio buttons are normally used together in a RadioGroup.
 */
public class RadioButton extends CheckableButton {
    /**
     * Create new instance of RadioButton with specified size
     * @param context
     * @param width button width
     * @param height button height
     */
    public RadioButton(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    /**
     * Create new instance of RadioButton with specified size
     * @param context
     */
    public RadioButton(GVRContext context) {
        super(context);
    }

    /**
     * Create new instance of RadioButton with specified size
     * @param context
     */
    public RadioButton(GVRContext context, JSONObject properties) {
        super(context, properties);
    }

    /**
     * Create new instance of RadioButton wrapping around GVRF sceneObject; parsed from the model
     *
     * @param context
     * @param sceneObject
     * @param attributes
     * @throws InstantiationException
     */
    @Deprecated
    public RadioButton(GVRContext context, GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    /**
     * Create new instance of RadioButton wrapping around GVRF sceneObject
     *
     * @param context
     * @param sceneObject
     * @throws InstantiationException
     */
    public RadioButton(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    protected RadioButton(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    /**
     * Change the checked state of the button to the inverse of its current state.
     * If the radio button is already checked, this method will not toggle the radio button.
     */
    @Override
    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }

    @Override
    protected Widget createGraphicWidget() {
        return new Graphic(getGVRContext(), getHeight());
    }

    static private class Graphic extends Widget {
        Graphic(GVRContext context, float size) {
            super(context, size, size);
            setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        }
    }
}
