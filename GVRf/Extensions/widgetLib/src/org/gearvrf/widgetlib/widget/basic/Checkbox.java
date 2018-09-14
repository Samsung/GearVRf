package org.gearvrf.widgetlib.widget.basic;

import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.NodeEntry;
import org.gearvrf.widgetlib.widget.layout.Layout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

/**
 * A checkbox is a specific type of two-state button that can be either checked or unchecked.
 */
public class Checkbox extends CheckableButton {

    /**
     * Create new instance of Checkbox with specific size
     * @param context
     * @param width
     * @param height
     */
    public Checkbox(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    /**
     * Create new instance of Checkbox wrapping around GVRF sceneObject parsed from the model
     * @param context
     * @param sceneObject
     * @param attributes
     * @throws InstantiationException
     */
    @Deprecated
    public Checkbox(GVRContext context, GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    /**
     * Create new instance of Checkbox wrapping around GVRF sceneObject
     *
     * @param context
     * @param sceneObject
     */
    public Checkbox(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    protected Checkbox(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
    }

    protected LightTextWidget createTextWidget() {
        LightTextWidget textWidget = super.createTextWidget();
        textWidget.setPositionZ(PADDING_Z);
        return textWidget;
    }

    @Override
    protected float getTextWidgetWidth() {
        return getWidth() - getHeight() - getDefaultLayout().getDividerPadding(Layout.Axis.X);
    }

    @Override
    protected Widget createGraphicWidget() {
        Widget graphic = new Graphic(getGVRContext(), getHeight());
        graphic.setPositionZ(PADDING_Z);
        graphic.setRenderingOrder(getRenderingOrder() + 1);
        return graphic;
    }

    static private class Graphic extends Widget {
        Graphic(GVRContext context, float size) {
            super(context, size, size);
        }
    }

    @SuppressWarnings("unused")
    private static final String TAG = Checkbox.class.getSimpleName();
    private static final float PADDING_Z = 0.025f;
}
