package org.gearvrf.widgetlib.main;

import android.graphics.Bitmap;

import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImage;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;

import java.util.concurrent.Future;

public class GVRBitmapTexture extends GVRTexture {

    public GVRBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
        this(gvrContext, new GVRBitmapImage(gvrContext, bitmap));
    }

    public GVRBitmapTexture(GVRContext gvrContext, GVRBitmapImage image) {
        super(gvrContext);
        setImage(image);
    }

    public GVRBitmapTexture(GVRContext gvrContext, int width, int height,
                            byte[] grayscaleData) throws IllegalArgumentException {
        this(gvrContext, width, height, grayscaleData,
                gvrContext.DEFAULT_TEXTURE_PARAMETERS);
    }

    public GVRBitmapTexture(GVRContext gvrContext, int width, int height,
                            byte[] grayscaleData, GVRTextureParameters textureParameters)
            throws IllegalArgumentException {
        super(gvrContext);
        setImage(new GVRBitmapImage(gvrContext, width, height, grayscaleData));
    }

    public void update(int width, int height, byte[] grayscaleData) {
        GVRImage image = getImage();
        if (image instanceof GVRBitmapImage) {
            ((GVRBitmapImage)image).update(width, height, grayscaleData);
        } else {
            throw new RuntimeException("internal error");
        }
    }
}
