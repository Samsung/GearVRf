package org.gearvrf.widgetlib.widget;

import java.io.IOException;
import java.util.Iterator;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRShaderId;
import org.gearvrf.utility.Exceptions;
import static org.gearvrf.utility.Log.tag;

import org.json.JSONException;
import org.json.JSONObject;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.widgetlib.widget.Widget.Visibility;
import org.gearvrf.widgetlib.widget.animation.Animation;
import org.gearvrf.widgetlib.widget.animation.AnimationFactory;

import org.gearvrf.widgetlib.widget.properties.JSONHelpers;
import org.gearvrf.widgetlib.widget.properties.TextureFactory;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getJSONColorGl;

/**
 * Helper class to work with the widget states
 */
class WidgetStateInfo {
    public enum Properties {
        scene_object, material, animation, id
    }

    WidgetStateInfo(Widget parent, JSONObject info)
            throws JSONException, NoSuchMethodException, IOException {
        Widget levelWidget = null;
        GVRMaterial material = null;
        AnimationFactory.Factory factory = null;

        Iterator<String> iter = info.keys();
        while (iter.hasNext()) {
            final String type = iter.next();
            Log.d(TAG, "WidgetStateInfo(%s): type: %s", parent.getName(), type);

            final JSONObject typeInfo = info.optJSONObject(type);
            try {
            switch (Properties.valueOf(type)) {
                case scene_object:
                    levelWidget = getWidget(parent, typeInfo);
                    break;
                case material:
                    material = getMaterial(parent, typeInfo);
                    break;
                case animation:
                    factory = getAnimationFactory(typeInfo);
                    break;
                default:
                    Log.w(TAG, "unknown property: %s", Properties.valueOf(type));
            }
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "unknown property!");
            }
        }

        mLevelWidget = levelWidget;
        mAnimationFactory = factory;
        mMaterial = material;
    }

    void set(Widget widget, boolean set) {
        if (set) {
            Log.d(TAG, "set(%s): setting state ...", widget.getName());
            if (mLevelWidget != null) {
                Log.d(TAG, "set(%s): setting level widget %s", widget.getName(), mLevelWidget);
                mLevelWidget.setVisibility(Visibility.VISIBLE);
            }
            if (mMaterial != null) {
                Log.d(TAG, "set(%s): setting material ...", widget.getName(), mMaterial);
                widget.setMaterial(mMaterial);
            }
            if (mAnimation != null) {
                mAnimation.finish();
            }
            if (mAnimationFactory != null) {
                try {
                    Log.d(TAG, "set(%s): setting animation ...", widget.getName());
                    mAnimation = mAnimationFactory.create(widget);
                    mAnimation.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e, "set()");
                }
            }
        } else {
            if (mAnimation != null) {
                mAnimation.finish();
            }
            if (mLevelWidget != null) {
                mLevelWidget.setVisibility(Visibility.HIDDEN);
            }
        }
    }

    // TODO: Handle indexing into animations already on the object from the model
    private AnimationFactory.Factory getAnimationFactory(
            final JSONObject animationSpec) throws JSONException,
            NoSuchMethodException {
        if (animationSpec.has("duration")) {
            Log.d(TAG, "getAnimationFactory(): making factory for spec: %s", animationSpec);
            return AnimationFactory.makeFactory(animationSpec);
        } else if (animationSpec.has("id")) {
            final String id = animationSpec.getString("id");
            Log.d(TAG, "getAnimationFactory(): getting factory for '%s'", id);
            return AnimationFactory.getFactory(id);
        } else {
            throw Exceptions.RuntimeAssertion("Invalid animation spec: %s",
                                              animationSpec);
        }
    }

    private enum MaterialProperties {
        shader_type, main_texture, textures, color, ambient_color, diffuse_color, specular_color, specular_exponent, opacity
    }

    private GVRMaterial getMaterial(Widget widget, JSONObject materialSpec)
            throws JSONException, IOException {
        final GVRContext gvrContext = widget.getGVRContext();
        return getMaterial(gvrContext, materialSpec);
    }

    static private GVRShaderId getShaderId(JSONObject materialSpec) throws JSONException, IOException {
        final Iterator<String> iter = materialSpec.keys();
        while (iter.hasNext()) {
            final String key = iter.next();
            switch (MaterialProperties.valueOf(key)) {
                case shader_type:
                    final String shaderType = materialSpec.getString(key);
                    if (shaderType.equalsIgnoreCase("texture")) {
                        return GVRShaderType.Texture.ID;
                    } else if (shaderType.equalsIgnoreCase("cubemap")) {
                        return GVRShaderType.Cubemap.ID;
                    } else {
                        throw Exceptions
                                .RuntimeAssertion("Unsupported shader type '%s' specified for state",
                                        shaderType);
                    }
            }
        }
        return GVRShaderType.Texture.ID;
    }

    // TODO: MaterialFactory
    static private GVRMaterial getMaterial(final GVRContext gvrContext,
            JSONObject materialSpec) throws JSONException, IOException {

        GVRShaderId shaderId = getShaderId(materialSpec);
        GVRMaterial material = new GVRMaterial(gvrContext, shaderId);

        final Iterator<String> iter = materialSpec.keys();
        while (iter.hasNext()) {
            final String key = iter.next();
            switch (MaterialProperties.valueOf(key)) {
                case color:
                    final float[] color = getJSONColorGl(materialSpec,
                                                               key);
                    material.setColor(color[0], color[1], color[2]);
                    break;
                case ambient_color:
                    final float[] ambientColor = getJSONColorGl(materialSpec, key);
                    material.setAmbientColor(ambientColor[0], ambientColor[1],
                                             ambientColor[2], ambientColor[3]);
                    break;
                case diffuse_color:
                    final float[] diffuseColor = getJSONColorGl(materialSpec, key);
                    material.setDiffuseColor(diffuseColor[0], diffuseColor[1],
                                             diffuseColor[2], diffuseColor[3]);
                    break;
                case specular_color:
                    final float[] specularColor = getJSONColorGl(materialSpec, key);
                    material.setSpecularColor(specularColor[0],
                                              specularColor[1],
                                              specularColor[2],
                                              specularColor[3]);
                    break;
                case specular_exponent:
                    material.setSpecularExponent((float) materialSpec
                            .getDouble(key));
                    break;
                case opacity:
                    material.setOpacity((float) materialSpec.getDouble(key));
                    break;
            }
        }

        TextureFactory.loadMaterialTextures(material, materialSpec);

        return material;
    }

    private Widget getWidget(Widget parent, JSONObject stateSpec)
            throws JSONException {
        Widget levelWidget;
        String id = JSONHelpers.getString(stateSpec, Properties.id);
        levelWidget = parent.findChildByName(id);
        if (levelWidget == null) {
            throw Exceptions
                    .RuntimeAssertion("State widget '%s' not found", id);
        }
        Log.d(TAG, "getWidget(): got state widget '%s'", id);
        levelWidget.setVisibility(Visibility.HIDDEN);
        levelWidget.setFollowParentFocus(true);
        levelWidget.setFollowParentInput(true);
        levelWidget.setChildrenFollowFocus(true);
        levelWidget.setChildrenFollowInput(true);
        return levelWidget;
    }

    final private Widget mLevelWidget;
    final private AnimationFactory.Factory mAnimationFactory;
    final private GVRMaterial mMaterial;
    private Animation mAnimation;

    private static final String TAG = tag(WidgetStateInfo.class);
}
