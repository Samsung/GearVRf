package org.gearvrf.widgetlib.widget.properties;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImage;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRCompressedImage;
import static org.gearvrf.utility.Exceptions.RuntimeAssertion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Future;

import static org.gearvrf.widgetlib.main.Utility.getId;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.GVRBitmapTexture;

public class TextureFactory {
    static final private String TAG = TextureFactory.class.getSimpleName();
    static final String MAIN_TEXTURE = "main_texture";

    static public GVRTexture loadTexture(GVRContext context, JSONObject textureSpec) {
        ImmediateLoader loader = new ImmediateLoader(context);
        try {
            loadOneTextureFromJSON(context, textureSpec, loader);
            return loader.getTexture();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e, "loadTexture()");
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    static public GVRTexture loadFutureTexture(GVRContext context, JSONObject textureSpec) {
        ImmediateLoader loader = new ImmediateLoader(context);
        try {
            loadOneTextureFromJSON(context, textureSpec, loader);
            return loader.getTexture();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e, "loadFutureTexture()");
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    static public void loadMaterialTextures(GVRMaterial material, JSONObject textureSpec) {
        try {
            loadTexturesFromJSON(material, textureSpec);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e, "loadMaterialTextures()");
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    static private void loadTexturesFromJSON(final GVRMaterial material,
                                             final JSONObject materialSpec) throws JSONException, IOException {
        final JSONObject mainTextureSpec = JSONHelpers
                .optJSONObject(materialSpec, MaterialTextureProperties.main_texture);
        if (mainTextureSpec != null) {
            loadOneTextureFromJSON(material.getGVRContext(), mainTextureSpec,
                    new MaterialLoader(material, mainTextureSpec, "diffuseTexture"));
        }

        final JSONObject texturesSpec = JSONHelpers
                .optJSONObject(materialSpec, MaterialTextureProperties.textures);
        if (texturesSpec != null) {
            Iterator<String> iter = texturesSpec.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                final JSONObject textureSpec = texturesSpec.optJSONObject(key);
                if (textureSpec != null) {
                    loadOneTextureFromJSON(material.getGVRContext(), textureSpec,
                            new MaterialLoader(material, textureSpec, key));
                }
            }
        }
    }

    static private void loadOneTextureFromJSON(GVRContext context, final JSONObject textureSpec,
                                               Loader loader) throws JSONException,
            IOException {
        TextureType textureType = JSONHelpers.getEnum(textureSpec,
                BitmapProperties.type,
                TextureType.class);
        switch (textureType) {
            case bitmap:
                loadBitmapTextureFromJSON(context, textureSpec, loader);
                break;
            default:
                throw RuntimeAssertion("Invalid texture type: %s",
                        textureType);
        }
    }

    static private void loadBitmapTextureFromJSON(GVRContext context,
                                                  final JSONObject textureSpec,
                                                  Loader loader)
            throws JSONException, IOException {
        JSONObject bitmapSpec = JSONHelpers.getJSONObject(textureSpec, TextureType.bitmap);
        String resourceType = bitmapSpec
                .getString(BitmapProperties.resource_type.name());
        String id = bitmapSpec.getString(BitmapProperties.id.name());
        String type = bitmapSpec.getString(BitmapProperties.type.name());
        final GVRAndroidResource resource;

        switch (BitmapResourceType.valueOf(resourceType)) {
            case asset:
                resource = new GVRAndroidResource(context, id);
                break;
            case file:
                resource = new GVRAndroidResource(id);
                break;
            case resource:
                int resId = -1;
                switch (BitmapType.valueOf(type)) {
                    case uncompressed:
                        resId = getId(context.getContext(), id);
                        Log.d(TAG, "loadBitmapTextureFromJSON uncompressed id = %s resId = %d", id, resId);
                        break;
                    case compressed:
                        resId = getId(context.getContext(), id);
                        Log.d(TAG, "loadBitmapTextureFromJSON compressed id = %s resId = %d", id, resId);
                        break;
                    default:
                        break;
                }
                resource = new GVRAndroidResource(context, resId);
                break;
            case user:
                File docDir = JSONHelpers.getExternalJSONDocumentDirectory(context.getContext());
                File texturePath = new File(docDir, id);
                resource = new GVRAndroidResource(texturePath);
                break;
            default:
                throw RuntimeAssertion("Invalid bitmap texture resource type: %s",
                                resourceType);
        }

        final GVRTextureParameters textureParams;
        final JSONObject textureParamsSpec = JSONHelpers
                .optJSONObject(textureSpec,
                        TextureParametersProperties.texture_parameters);
        if (textureParamsSpec != null) {
            textureParams = textureParametersFromJSON(context,
                    textureParamsSpec);
        } else {
            textureParams = null;
        }

        loader.loadTexture(resource, textureParams);
    }

    static private GVRTextureParameters textureParametersFromJSON(GVRContext context,
                                                                  final JSONObject textureParamsSpec) throws JSONException {
        if (textureParamsSpec == null || textureParamsSpec.length() == 0) {
            return null;
        }

        final GVRTextureParameters textureParameters = new GVRTextureParameters(
                context);
        final Iterator<String> iter = textureParamsSpec.keys();
        while (iter.hasNext()) {
            final String key = iter.next();
            switch (TextureParametersProperties.valueOf(key)) {
                case min_filter_type:
                    textureParameters.setMinFilterType(GVRTextureParameters.TextureFilterType
                            .valueOf(textureParamsSpec.getString(key)));
                    break;
                case mag_filter_type:
                    textureParameters.setMagFilterType(GVRTextureParameters.TextureFilterType
                            .valueOf(textureParamsSpec.getString(key)));
                    break;
                case wrap_s_type:
                    textureParameters.setWrapSType(GVRTextureParameters.TextureWrapType
                            .valueOf(textureParamsSpec.getString(key)));
                    break;
                case wrap_t_type:
                    textureParameters.setWrapTType(GVRTextureParameters.TextureWrapType
                            .valueOf(textureParamsSpec.getString(key)));
                    break;
                case anisotropic_value:
                    textureParameters.setAnisotropicValue(textureParamsSpec
                            .getInt(key));
                    break;
            }
        }
        return textureParameters;
    }

    private enum BitmapResourceType {
        asset, file, resource, user
    }

    private enum BitmapProperties {
        resource_type, type, id
    }

    private enum BitmapType {
        compressed, uncompressed
    }

    private enum TextureType {
        bitmap
    }

    private enum TextureParametersProperties {
        texture_parameters, min_filter_type, mag_filter_type, wrap_s_type, wrap_t_type, anisotropic_value
    }

    private enum MaterialTextureProperties {
        textures, main_texture

    }

    private interface Loader {
        void loadTexture(GVRAndroidResource resource, GVRTextureParameters parameters);
    }

    private static class ImmediateLoader implements Loader {
        ImmediateLoader(GVRContext context) {
            mContext = context;
        }

        @Override
        public void loadTexture(GVRAndroidResource resource, GVRTextureParameters parameters) {
            GVRAssetLoader assetLoader = mContext.getAssetLoader();
            mTexture = assetLoader.loadTexture(resource, parameters);
        }

        public GVRTexture getTexture() {
            return mTexture;
        }

        private final GVRContext mContext;
        private GVRTexture mTexture;
    }

    private static class MaterialLoader implements Loader {
        MaterialLoader(GVRMaterial material, JSONObject textureSpec, String key) {
            mMaterial = material;
            mKey = key;
            mSpec = textureSpec;
        }

        @Override
        public void loadTexture(GVRAndroidResource resource, GVRTextureParameters parameters) {
            final GVRContext context = mMaterial.getGVRContext();
            context.getAssetLoader().loadTexture(
                    resource, new GVRAndroidResource.TextureCallback() {
                        @Override
                        public void loaded(GVRImage resource11,
                                           GVRAndroidResource androidResource) {
                            mMaterial.setTexture(mKey, new GVRBitmapTexture(context, (GVRBitmapImage)resource11));
                        }

                        @Override
                        public void failed(Throwable t, GVRAndroidResource androidResource) {
                            t.printStackTrace();
                            Log.e(TAG, t, "Failed to load texture '%s' from spec: %s", mKey,
                                    mSpec);
                        }

                        @Override
                        public boolean stillWanted(GVRAndroidResource androidResource) {
                            return true;
                        }
                    },
                    parameters, GVRAssetLoader.DEFAULT_PRIORITY, GVRCompressedImage.BALANCED);
        }

        private final GVRMaterial mMaterial;
        private final String mKey;
        private final JSONObject mSpec;
    }
}
