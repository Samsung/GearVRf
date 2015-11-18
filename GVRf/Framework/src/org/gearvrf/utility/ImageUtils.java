package org.gearvrf.utility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageUtils
{
    public static Bitmap generateBitmapFlipV(final byte[] byteArray, final int width,
            final int height) {
        int[] pixels = new int[width * height];
        for (int row = 0; row < height; row++) {
            int start_position = row * width;
            int reverse_start_position = (height - 1 - row) * width;
            for (int col = 0; col < width; col++) {
                int position = (start_position + col) * 4;
                int r = byteArray[position++] & 0xff;
                int g = byteArray[position++] & 0xff;
                int b = byteArray[position] & 0xff;
                // flip the image vertically
                pixels[reverse_start_position + col] = Color.rgb(r, g, b);
            }
        }
        return Bitmap.createBitmap(pixels, width, height,
                Bitmap.Config.ARGB_8888);
    }

    public static Bitmap generateBitmap(final int[] argbIntBuffer,
            final int width, final int height) {
        return Bitmap.createBitmap(argbIntBuffer,
                                   width, height, Bitmap.Config.ARGB_8888);
    }

    public static boolean saveBitmapAsPNG(String filename, Bitmap bitmap) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
