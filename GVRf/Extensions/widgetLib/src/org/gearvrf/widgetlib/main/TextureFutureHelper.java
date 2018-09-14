package org.gearvrf.widgetlib.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.utility.RuntimeAssertion;
import static org.gearvrf.utility.Log.tag;

import java.util.List;

import org.gearvrf.widgetlib.log.Log;

/**
 * Utilities for TextureFutures
 */
public final class TextureFutureHelper {
    private static final String TAG = tag(TextureFutureHelper.class);
    TextureFutureHelper(GVRContext gvrContext) {
        mContext = gvrContext;
    }

    private final SparseArray<GVRBitmapTexture> mColorTextureCache = new SparseArray<>();
    private final GVRContext mContext;

    private static class ImmutableBitmapTexture extends GVRBitmapTexture {
        ImmutableBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
            super(gvrContext, bitmap);
        }

        @Override
        public void setAtlasInformation(List<GVRAtlasInformation> atlasInformation) {
            onMutatingCall("setAtlasInformation");
        }

        @Override
        public void updateTextureParameters(GVRTextureParameters textureParameters) {
            onMutatingCall("updateTextureParameters");
        }

        private void onMutatingCall(final String method) {
            final String msg = "%s(): mutating call on ImmutableBitmapTexture!";
            Log.e(TAG, msg, method);
            throw new RuntimeAssertion(msg, method);
        }
    }

    public GVRBitmapTexture getBitmapTexture(int resId) {
        final Resources resources = mContext.getActivity().getResources();
        final Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        return new GVRBitmapTexture(mContext, bitmap);
    }

    /**
     * Gets an immutable {@linkplain GVRBitmapTexture texture} with the specified color,
     * returning a cached instance if possible.
     *
     * @param color An Android {@link Color}.
     * @return And immutable instance of {@link GVRBitmapTexture}.
     */
    public GVRBitmapTexture getSolidColorTexture(int color) {
        GVRBitmapTexture texture;
        synchronized (mColorTextureCache) {
            texture = mColorTextureCache.get(color);
            Log.d(TAG, "getSolidColorTexture(): have cached texture for 0x%08X: %b", color, texture != null);
            if (texture == null) {
                texture = new ImmutableBitmapTexture(mContext, makeSolidColorBitmap(color));
                Log.d(TAG, "getSolidColorTexture(): caching texture for 0x%08X", color);
                mColorTextureCache.put(color, texture);
                Log.d(TAG, "getSolidColorTexture(): succeeded caching for 0x%08X: %b",
                        color, mColorTextureCache.indexOfKey(color) >= 0);
            }
        }

        return texture;
    }

    @NonNull
    private static Bitmap makeSolidColorBitmap(int color) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        if (color != -1) {
            bitmap.eraseColor(color);
        }
        return bitmap;
    }
}