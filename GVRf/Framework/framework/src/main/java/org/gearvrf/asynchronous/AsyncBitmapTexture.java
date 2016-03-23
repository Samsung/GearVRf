/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.asynchronous;

import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetIntegerv;
import static org.gearvrf.utility.Threads.threadId;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.asynchronous.Throttler.GlConverter;
import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.RecycleBin;
import org.gearvrf.utility.Threads;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Display;
import android.view.WindowManager;

/**
 * Async resource loading: bitmap textures.
 * 
 * <p>
 * Should always call either {@link #setup(Context)} or
 * {@link #setup(Context, ImageSizePolicy)} before first use; the default
 * settings are very conservative, and will usually give much smaller textures
 * than necessary.
 * 
 * @since 1.6.1
 */
class AsyncBitmapTexture {

    /*
     * The API
     */

    static void loadTexture(GVRContext gvrContext,
            CancelableCallback<GVRBitmapTexture> callback,
            GVRAndroidResource resource, int priority) {
        AsyncManager.get().getScheduler().registerCallback(gvrContext, TEXTURE_CLASS, callback,
                resource, priority);
    }

    private static final Class<GVRBitmapTexture> TEXTURE_CLASS = GVRBitmapTexture.class;
    /*
     * Singleton
     */
    private static AsyncBitmapTexture sInstance = new AsyncBitmapTexture();

    /**
     * Gets the {@link AsyncBitmapTexture} singleton for loading bitmap textures.
     * @return The {@link AsyncBitmapTexture} singleton.
     */
    public static AsyncBitmapTexture get() {
        return sInstance;
    }

    private AsyncBitmapTexture() {
        AsyncManager.get().registerDatatype(TEXTURE_CLASS,
                new AsyncLoaderFactory<GVRBitmapTexture, Bitmap>() {
            @Override
            AsyncLoader<GVRBitmapTexture, Bitmap> threadProc(GVRContext gvrContext,
                    GVRAndroidResource request,
                    CancelableCallback<GVRBitmapTexture> callback,
                    int priority) {
                return new AsyncLoadTextureResource(gvrContext,
                        request, callback, priority);
            }
        });
    }

    /*
     * Static constants
     */

    private static final String TAG = Log.tag(AsyncBitmapTexture.class);

    

    /** Ridiculous amounts of detail about decodeFile() */
    protected static final boolean VERBOSE_DECODE = false;

    private static final int DECODE_BUFFER_SIZE = 1024 * 16;

    protected static final boolean RUNTIME_ASSERTIONS = Threads.RUNTIME_ASSERTIONS;
    protected static final boolean CHECK_ARGUMENTS = Threads.RUNTIME_ASSERTIONS;

    /** A very low value, for testing */
    private static final int DEFAULT_GL_MAX_TEXTURE_SIZE = 1024;

    /**
     * Can only load textures whose width and height are both less than
     * {@link #glMaxTextureSize}. The default value is almost certainly too
     * small; the actual value is set by {@link #onGlInitialization()}
     */
    static int glMaxTextureSize = DEFAULT_GL_MAX_TEXTURE_SIZE;

    /**
     * Largest image that we will load into memory.
     * 
     * A multiple of the "memory class" - <i>e.g.</i> 7.5% of a 32M heap is a
     * 2.4M image; 6% is 1.92M
     */
    private static final float MAXIMUM_IMAGE_FACTOR = 0.125f;

    /**
     * When {@link #fractionalDecode(FractionalDecodeShim, Options, int, int)}
     * reads large bitmaps one 'stripe' at a time, this is the maximum number of
     * pixels in a stripe.
     */
    private static final int SLICE_SIZE = 1024 * 1024;

    /*
     * static field(s) and setup
     */

    /**
     * After {@link #setup(Context)} has been called, the computed maximum image
     * size, in bytes. Compiled in value allows image loading to work (albeit
     * with very conservative default settings) if setup() is not called.
     */
    protected static int maxImageSize = 1024 * 1024; // bytes

    /**
     * Either {@link #setup(Context)} or
     * {@link #setup(Context, ImageSizePolicy)} <em>should</em> be called before
     * first use; the default settings are very conservative, and will usually
     * give much smaller textures than necessary.
     */
    static Context setup(GVRContext context) {
        return setup(context, null);
    }

    /**
     * Either {@link #setup(Context)} or
     * {@link #setup(Context, ImageSizePolicy)} <em>should</em> be called before
     * first use; the default settings are very conservative, and will usually
     * give much smaller textures than necessary.
     */
    static Context setup(GVRContext gvrContext, ImageSizePolicy sizePolicy) {
        Context androidContext = gvrContext.getContext();
        Memory.setup(androidContext);

        if (sizePolicy == null) {
            sizePolicy = new DefaultImageSizePolicy();
        }

        Context applicationContext = androidContext.getApplicationContext();
        // Should only be null in a unit test
        applicationContext = applicationContext == null ? androidContext
                : applicationContext;

        getScreenSize(applicationContext);

        int heapSize = Memory.getMemoryClass();
        maxImageSize = (int) (heapSize * sizePolicy.getMaximumImageFactor());
        Log.d(TAG, "Set maxImageSize == %, d", maxImageSize);

        gvrContext.runOnGlThread(new Runnable() {

            @Override
            public void run() {
                onGlInitialization();
            }
        });

        return applicationContext;
    }

    /**
     * Returns screen height and width
     * 
     * @param context
     *            Any non-null Android Context
     * @return .x is screen width; .y is screen height.
     */
    private static Point getScreenSize(Context context) {
        return getScreenSize(context, null);
    }

    /**
     * Returns screen height and width
     * 
     * @param context
     *            Any non-null Android Context
     * @param p
     *            Optional Point to reuse. If null, a new Point will be created.
     * @return .x is screen width; .y is screen height.
     */
    private static Point getScreenSize(Context context, Point p) {
        if (p == null) {
            p = new Point();
        }
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(p);
        return p;
    }

    /**
     * This method must be called, on the GL thread, at least once after
     * initialization: it is safe to call it more than once.
     * 
     * Not calling this method leaves {@link #glMaxTextureSize} set to a default
     * value, which may be smaller than necessary.
     */
    private static void onGlInitialization() {
        if (glUninitialized) {
            int[] size = new int[] { -1 };
            glGetIntegerv(GL_MAX_TEXTURE_SIZE, size, 0);

            int errorCode = glGetError();
            if (errorCode != GL_NO_ERROR) {
                throw Exceptions.RuntimeAssertion(
                        "Error %d getting max texture size", errorCode);
            }

            int maxTextureSize = size[0];
            if (maxTextureSize <= 0) {
                throw Exceptions.RuntimeAssertion(
                        "Invalid max texture size %d", maxTextureSize);
            }

            glMaxTextureSize = maxTextureSize;
            Log.d(TAG, "Actual GL_MAX_TEXTURE_SIZE = %d", glMaxTextureSize);

            glUninitialized = false;
        }
    }

    private static boolean glUninitialized = true;

    private static class DefaultImageSizePolicy implements ImageSizePolicy {

        @Override
        public float getMaximumImageFactor() {
            return MAXIMUM_IMAGE_FACTOR;
        }
    }

    private static class Memory {
        private static final String TAG = Log.tag(Memory.class);

        private static int memoryClass = 0;

        /**
         * Unit tests may create mainActivity several times in the same process.
         * We don't want to run setup more than once.
         */
        private static boolean initialized = false;

        /**
         * {@link #getMemoryClass()} will not be accurate until you call
         * {@link #setup(Context)}
         */
        static synchronized void setup(Context context) {
            if (initialized) {
                return;
            }

            getMemoryClass(context);

            initialized = true;
        }

        private static void getMemoryClass(Context context) {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Activity.ACTIVITY_SERVICE);
            memoryClass = activityManager.getMemoryClass() * 1024 * 1024;
            Log.d(TAG, "MemoryClass = %dM", memoryClass / (1024 * 1024));
        }

        /** Heap size, in bytes */
        static int getMemoryClass() {
            return memoryClass;
        }
    }

    /*
     * Asynchronous loader
     */

    private static class AsyncLoadTextureResource extends
            AsyncLoader<GVRBitmapTexture, Bitmap> {

        private static final GlConverter<GVRBitmapTexture, Bitmap> sConverter = new GlConverter<GVRBitmapTexture, Bitmap>() {

            @Override
            public GVRBitmapTexture convert(GVRContext gvrContext, Bitmap bitmap) {
                return new GVRBitmapTexture(gvrContext, bitmap);
            }
        };

        protected AsyncLoadTextureResource(GVRContext gvrContext,
                GVRAndroidResource request,
                CancelableCallback<GVRBitmapTexture> callback, int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected Bitmap loadResource() {
            Bitmap bitmap = decodeStream(resource.getStream(),
                    glMaxTextureSize, glMaxTextureSize, true, null, false);
            resource.closeStream();
            return bitmap;
        }
    }

    /*
     * decodeStream
     */

    /**
     * A wrapper around
     * {@link android.graphics.BitmapFactory#decodeStream(InputStream)
     * BitmapFactory.decodeStream} that uses an
     * {@link android.graphics.BitmapFactory.Options} <code>inTempStorage</code>
     * decode buffer. On low memory, returns half (quarter, eighth, ...) size
     * images.
     * <p>
     * If {@code stream} is a {@link FileInputStream} and is at offset 0 (zero),
     * uses
     * {@link android.graphics.BitmapFactory#decodeFileDescriptor(FileDescriptor)
     * BitmapFactory.decodeFileDescriptor()} instead of
     * {@link android.graphics.BitmapFactory#decodeStream(InputStream)
     * BitmapFactory.decodeStream()}.
     * 
     * @param stream
     *            Bitmap stream
     * @param requestedWidth
     *            If >= 0, specifies a target width; returned Bitmap will be at
     *            least this wide (unless we run out of memory) while preserving
     *            the original aspect ratio. Image may be a good deal wider than
     *            requestedWidth, as we only shrink by powers of two.
     * @param requestedHeight
     *            If >= 0, specifies a target height; returned Bitmap will be at
     *            least this high (unless we run out of memory) while preserving
     *            the original aspect ratio. Image may be a good deal taller
     *            than requestedHeight, as we only shrink by powers of two.
     * @param canShrink
     *            On low memory, can we return half (quarter, eighth, ...) size
     *            image? If false, return null on any OutOfMemoryError
     * @param possibleAlternative
     *            We may have a cached copy that's at least as big as the
     *            largest possible decode: Passing in the cached bitmap (if any)
     *            allows us to detect this before doing the second decode.
     * @param closeStream
     *            If {@code true}, closes {@code stream}
     * @return Bitmap, or null if cannot be decoded into a bitmap
     */
    static Bitmap decodeStream(InputStream stream, int requestedWidth,
            int requestedHeight, final boolean canShrink,
            Bitmap possibleAlternative, boolean closeStream) {
        BitmapFactory.Options options = standardBitmapFactoryOptions();

        try {
            DecodeHelper helper;
            if (stream instanceof FileInputStream) {
                helper = new DecodeFileStreamHelper((FileInputStream) stream);
            } else {
                helper = new DecodeStreamHelper(stream);
            }
            helper.setInSampleSize(options, requestedWidth, requestedHeight);

            if (useAlternativeBitmap(possibleAlternative, options)) {
                return possibleAlternative;
            }

            do {
                try {
                    return helper.decode(options, requestedWidth,
                            requestedHeight);
                } catch (OutOfMemoryError m) {
                    // Rewind stream to read again
                    helper.rewind();
                    options.inSampleSize *= 2; // try again, at half-size
                }
            } while (canShrink);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (options != null && options.inTempStorage != null) {
                bufferBin.put(options.inTempStorage);
            }

            if (stream != null && closeStream) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null; // OutOfMemoryError, canShrink == false
    }

    private static boolean useAlternativeBitmap(Bitmap possibleAlternative,
            BitmapFactory.Options options) {
        boolean result = possibleAlternative != null
                && possibleAlternative.getWidth() >= options.outWidth
                        / options.inSampleSize
                && possibleAlternative.getHeight() >= options.outHeight
                        / options.inSampleSize;
        if (VERBOSE_DECODE) { // Condition split to silence warning
            if (result) {
                Log.d(TAG,
                        "Thread %d: Skipping decode for %dx%d bitmap - using cached alternative",
                        threadId(), options.outWidth / options.inSampleSize,
                        options.outHeight / options.inSampleSize);
            }
        }
        return result;
    }

    private static Options standardBitmapFactoryOptions() {
        Options options = new Options();
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = bufferBin.get();
        if (options.inTempStorage == null) {
            options.inTempStorage = new byte[DECODE_BUFFER_SIZE];
        }
        return options;
    }

    private interface GetBounds {
        /**
         * Should just do the decodeX - may assume that
         * <code>options.inJustDecodeBounds == true;</code>
         */
        void getBounds(Options options);
    }

    private static class GetStreamBounds implements GetBounds {

        private final InputStream stream;

        private GetStreamBounds(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public void getBounds(Options options) {
            BitmapFactory.decodeStream(stream, null, options);
        }
    }

    private static void setInSampleSize(Options options, int requestedWidth,
            int requestedHeight, GetBounds get) {
        try {
            // Get the dimensions
            options.inJustDecodeBounds = true;
            get.getBounds(options);

            // Do we have a target size?
            if (requestedWidth == 0 && requestedHeight == 0) {
                options.inSampleSize = 1; // No: try for a full-size image
            } else {
                // Calculate the scale
                int scaleWidth = scale(requestedWidth, options.outWidth);
                int scaleHeight = scale(requestedHeight, options.outHeight);
                int scale = Math.max(Math.min(scaleWidth, scaleHeight), 1);

                options.inSampleSize = Integer.highestOneBit(scale);
            }

            if (VERBOSE_DECODE) {
                Log.d(TAG,
                        "Thread %d: File is %dx%d. Request size is %dx%d; calculated size is %dx%d",
                        threadId(), options.outWidth, options.outHeight,
                        requestedWidth, requestedHeight, options.outWidth
                                / options.inSampleSize, options.outHeight
                                / options.inSampleSize);
            }

            while ((options.outWidth / options.inSampleSize)
                    * (options.outHeight / options.inSampleSize) * 4 > maxImageSize) {

                if (VERBOSE_DECODE) {
                    Log.d(TAG, "Thread %d: %dx%d is too big: reducing",
                            threadId(),
                            options.outWidth / options.inSampleSize,
                            options.outHeight / options.inSampleSize);
                }

                options.inSampleSize *= 2;
            }

            if (VERBOSE_DECODE) {
                Log.d(TAG, "Thread %d: Requesting %dx%d copy of %dx%d image",
                        threadId(), options.outWidth / options.inSampleSize,
                        options.outHeight / options.inSampleSize,
                        options.outWidth, options.outHeight);
            }

        } finally {
            options.inJustDecodeBounds = false;
        }
    }

    private static int scale(int requestedSize, int outSize) {
        if (requestedSize == 0) {
            // No request
            return 1;
        } else if (requestedSize < 0) {
            // Minimum size
            return outSize / -requestedSize;
        } else {
            // Maximum size
            int scale = Math.max(1,
                    Integer.highestOneBit(outSize / requestedSize));

            if (outSize / scale > requestedSize) {
                scale <<= 1; // double it
            }
            return scale;
        }
    }

    private static int scale(int requestedSize, float outSize) {
        return scale(requestedSize, (int) (outSize + 0.5f));
    }

    private interface DecodeHelper {
        public void setInSampleSize(BitmapFactory.Options options,
                int requestedWidth, int requestedHeight) throws IOException;

        public Bitmap decode(BitmapFactory.Options options, int requestedWidth,
                int requestedHeight) throws IOException;

        public void rewind() throws IOException;
    }

    /**
     * Shim that lets
     * {@link AsyncBitmapTexture#fractionalDecode(FractionalDecodeShim, Options, int, int)}
     * work with either InputStream or FileDescriptor.
     */
    private interface FractionalDecodeShim {
        /**
         * We don't need to (or can't) use a {@link BitmapRegionDecoder}: just
         * call a BitmapFactory decode method directly
         * 
         */
        Bitmap decode(Options options);

        /**
         * Call the appropriate {@link BitmapRegionDecoder} newInstance()
         * overload
         * 
         * @return A new {@code BitmapRegionDecoder}, or {@code null}
         *         "if the image format is not supported or can not be decoded."
         */
        BitmapRegionDecoder newRegionDecoder();
    }

    /**
     * Use {@link BitmapRegionDecoder} to read the bitmap in smallish slices,
     * and resize each slice so that the target bitmap matches requestedWidth or
     * requestedHeight in at least one dimension.
     */
    private static Bitmap fractionalDecode(FractionalDecodeShim shim,
            Options options, int requestedWidth, int requestedHeight) {
        final int rawWidth = options.outWidth;
        final int rawHeight = options.outHeight;
        final float sampledWidth = (float) rawWidth / options.inSampleSize;
        final float sampledHeight = (float) rawHeight / options.inSampleSize;

        // Use the simple path, if we can/must
        if (requestedWidth == 0
                || requestedHeight == 0 //
                || sampledWidth <= Math.abs(requestedWidth)
                || sampledHeight <= Math.abs(requestedHeight)) {
            if (VERBOSE_DECODE) {
                Log.d(TAG,
                        "Can't use slice decoder: sampledWidth = %.0f, requestedWidth = %d; sampledHeight = %.0f, requestedHeight = %d",
                        sampledWidth, requestedWidth, sampledHeight,
                        requestedHeight);
            }
            return shim.decode(options);
        }

        // We must use a BitmapRegionDecoder to read and resize 'slices' of the
        // input
        BitmapRegionDecoder decoder = shim.newRegionDecoder();
        try {

            float scale = Math.max(scale(requestedWidth, sampledWidth),
                    scale(requestedHeight, sampledHeight));

            int scaledSliceWidth = roundUp(sampledWidth * scale);
            int slices = (decoder == null) ? 1 : roundUp(sampledHeight
                    / (SLICE_SIZE / sampledWidth));
            int sliceRows = (int) (rawHeight / slices);
            float scaledSliceRows = sliceRows / options.inSampleSize * scale;

            Bitmap result = Bitmap.createBitmap((int) (sampledWidth * scale),
                    (int) (sampledHeight * scale), Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

            // Rect decode, uses 'raw' coordinates
            Rect decode = new Rect(0, 0, rawWidth, sliceRows);
            // RectF target, uses scaled coordinates
            RectF target = new RectF(0, 0, scaledSliceWidth, scaledSliceRows);
            Bitmap slice = options.inBitmap = null;
            boolean hasAlpha = false;
            for (int index = 0; index < slices; ++index) {
                slice = options.inBitmap = //
                (decoder == null) ? shim.decode(options) : //
                        decoder.decodeRegion(decode, options);

                hasAlpha |= slice.hasAlpha();

                canvas.drawBitmap(slice, null, target, paint);

                decode.offset(0, sliceRows);
                target.offset(0, scaledSliceRows);
            }
            slice.recycle();

            // log(TAG,
            // "fractionalDecode: result.hasAlpha() == %b, slices' hasAlpha == %b",
            // result.hasAlpha(), hasAlpha);
            result.setHasAlpha(hasAlpha);
            return result;

        } finally {
            if (decoder != null) {
                decoder.recycle();
            }
        }
    }

    private static int roundUp(float f) {
        // The (int) cast rounds towards 0
        // http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.3
        return (int) (f + 1.0 - Float.MIN_VALUE);
    }

    private static class FractionalDecodeStreamShim implements
            FractionalDecodeShim {
        private final InputStream mStream;

        public FractionalDecodeStreamShim(InputStream stream) {
            mStream = stream;
        }

        @Override
        public Bitmap decode(Options options) {
            return BitmapFactory.decodeStream(mStream, null, options);
        }

        @Override
        public BitmapRegionDecoder newRegionDecoder() {
            try {
                return BitmapRegionDecoder.newInstance(mStream, false);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static class FractionalDecodeDescriptorShim implements
            FractionalDecodeShim {
        private final FileDescriptor mDescriptor;

        public FractionalDecodeDescriptorShim(FileDescriptor descriptor) {
            mDescriptor = descriptor;
        }

        @Override
        public Bitmap decode(Options options) {
            return BitmapFactory.decodeFileDescriptor(mDescriptor, null,
                    options);
        }

        @Override
        public BitmapRegionDecoder newRegionDecoder() {
            try {
                return BitmapRegionDecoder.newInstance(mDescriptor, false);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static class DecodeStreamHelper implements DecodeHelper {

        DecodeStreamHelper(InputStream stream) {
            mStream = stream;
            if (mStream.markSupported()) {
                mStream.mark(Integer.MAX_VALUE);
            }
        }

        @Override
        public void setInSampleSize(Options options, int requestedWidth,
                int requestedHeight) {
            AsyncBitmapTexture.setInSampleSize(options, requestedWidth,
                    requestedHeight, new GetStreamBounds(mStream));
            rewind();
        }

        @Override
        public Bitmap decode(Options options, int requestedWidth,
                int requestedHeight) {
            return fractionalDecode(new FractionalDecodeStreamShim(mStream),
                    options, requestedWidth, requestedHeight);
        }

        @Override
        public void rewind() {
            if (mStream.markSupported()) {
                try {
                    mStream.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private final InputStream mStream;
    }

    private static class DecodeFileStreamHelper implements DecodeHelper {

        DecodeFileStreamHelper(FileInputStream stream) throws IOException {
            mStream = stream;
            mOffset = stream.getChannel().position();
        }

        @Override
        public void setInSampleSize(Options options, int requestedWidth,
                int requestedHeight) throws IOException {
            // We'll need our original offset into the stream in order to rewind
            // for subsequent decode attempts. Also, getting the bitmap bounds
            // from the stream involves reading the stream, which moves our
            // offset into the stream.
            AsyncBitmapTexture.setInSampleSize(options, requestedWidth,
                    requestedHeight, new GetStreamBounds(mStream));
            mStream.getChannel().position(mOffset);
        }

        @Override
        public Bitmap decode(Options options, int requestedWidth,
                int requestedHeight) throws IOException {

            FractionalDecodeShim shim = (mOffset > 0)
            // Apparently if the file descriptor has been offset at
            // all, decodeFileDescriptor() doesn't work.
            ? new FractionalDecodeStreamShim(mStream)
                    // When the stream is at position zero, we can use a
                    // more optimal decode()
                    : new FractionalDecodeDescriptorShim(mStream.getFD());
            // }
            return fractionalDecode(shim, options, requestedWidth,
                    requestedHeight);
        }

        @Override
        public void rewind() throws IOException {
            // Rewind stream to read again
            mStream.getChannel().position(mOffset);
        }

        private final long mOffset;
        private final FileInputStream mStream;
    }

    /**
     * A soft referenced set of <code>byte[DECODE_BUFFER_SIZE]</code> arrays.)
     */
    private static RecycleBin<byte[]> bufferBin = RecycleBin.<byte[]> soft()
            .synchronize();

    /*
     * Image size policy
     */

    /**
     * {@link AsyncBitmapTexture#setup(Context)} creates a default version of
     * this to set the {@link AsyncBitmapTexture#maxImageSize}; call
     * {@link AsyncBitmapTexture#setup(Context, ImageSizePolicy)} with your own
     * {@link ImageSizePolicy} to specify a different ratio of heap size to
     * maximum image size.
     */
    interface ImageSizePolicy {

        /**
         * Largest image that we will load into memory.
         * 
         * A multiple of the "memory class" - <i>e.g.</i> 7.5% of a 32M heap is
         * a 2.4M image; 6% is 1.92M
         */
        float getMaximumImageFactor();
    }

}
