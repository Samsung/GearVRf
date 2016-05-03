package org.gearvrf.utility;

import java.io.FileOutputStream;
import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;

/**
 * Utilities for basic image I/O and manipulation.
 */
public class ImageUtils
{
    /**
     * Generates a {@code Bitmap} from a byte array containing {@code width} *
     * {@code height} pixels. The pixel format is RGBA in little endian. The
     * alpha value is not used in the result bitmap. The bitmap is also
     * vertically flipped.
     *
     * @param byteArray The input byte array.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return The generated {@code Bitmap} object.
     */
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

    /**
     * Generates a {@code Bitmap} from a int array containing {@code width} *
     * {@code height} pixels. The pixel format is in ARGB_8888 format for
     * Android.
     *
     * @param argbIntBuffer The int array containing pixels values.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return The generated {@code Bitmap} object.
     */
    public static Bitmap generateBitmap(final int[] argbIntBuffer,
            final int width, final int height) {
        return Bitmap.createBitmap(argbIntBuffer,
                                   width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     * Saves a {@code Bitmap} as a PNG file.
     *
     * @param filename The file path on the file system.
     * @param bitmap The input {@code Bitmap} object.
     * @return {@code true} if successful.
     */
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

    /**
     * Creates a {@code MediaPlayer} with a specified data source. The returned media player
     * is not started.
     *
     * @param filenameOrURL
     *         A string representing a path of the data file, or a URL.
     *
     * @return The {@code MediaPlayer} object if successful, or {@code null} if there is
     *         an error.
     */
    public static MediaPlayer createMediaPlayer(String filenameOrURL) {
        // create mediaplayer instance
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filenameOrURL);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return mediaPlayer;
    }
}
