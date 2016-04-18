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

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRCompressedCubemapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.asynchronous.Throttler.GlConverter;
import org.gearvrf.utility.FileNameUtils;

/**
 * Async resource loading: compressed cube map textures.
 *
 * We directly use CompressedTexture.load() in loadResource() to detect
 * the format of and load compressed textures.
 *
 * @since 1.6.9
 */
class AsyncCompressedCubemapTexture {

    /*
     * The API
     */

    static void loadTexture(GVRContext gvrContext,
            CancelableCallback<GVRCompressedCubemapTexture> callback,
            GVRAndroidResource resource, int priority, Map<String, Integer> map) {
        faceIndexMap = map;
        AsyncManager.get().getScheduler().registerCallback(gvrContext, TEXTURE_CLASS, callback,
                resource, priority);
    }

    private static Map<String, Integer> faceIndexMap;
    
    private static final Class<GVRCompressedCubemapTexture> TEXTURE_CLASS = GVRCompressedCubemapTexture.class;

    /*
     * Singleton
     */
    private static AsyncCompressedCubemapTexture sInstance = new AsyncCompressedCubemapTexture();

    /**
     * Gets the {@link AsyncCompressedCubemapTexture} singleton for loading compressed cubmap textures.
     * @return The {@link AsyncCompressedCubemapTexture} singleton.
     */
    public static AsyncCompressedCubemapTexture get() {
        return sInstance;
    }

    private AsyncCompressedCubemapTexture() {
        AsyncManager.get().registerDatatype(TEXTURE_CLASS,
                new AsyncLoaderFactory<GVRCompressedCubemapTexture, CompressedTexture[]>() {

                    @Override
                    AsyncLoader<GVRCompressedCubemapTexture, CompressedTexture[]> threadProc(
                            GVRContext gvrContext,
                            GVRAndroidResource request,
                            CancelableCallback<GVRCompressedCubemapTexture> cancelableCallback,
                            int priority) {
                        return new AsyncLoadCompressedCubemapTextureResource(gvrContext,
                                request, cancelableCallback, priority);
                    }
                });
    }

    /*
     * Static constants
     */

    /*
     * Asynchronous loader for compressed cubemap texture
     */

    private static class AsyncLoadCompressedCubemapTextureResource extends
    AsyncLoader<GVRCompressedCubemapTexture, CompressedTexture[]> {

      private static final GlConverter<GVRCompressedCubemapTexture, CompressedTexture[]> sConverter =
          new GlConverter<GVRCompressedCubemapTexture, CompressedTexture[]>() {

        @Override
        public GVRCompressedCubemapTexture convert(GVRContext gvrContext,
            CompressedTexture[] textureArray) {
          CompressedTexture texture = textureArray[0];
          byte[][] data = new byte[6][];
          int[] dataOffset = new int[6];
          for (int i = 0; i < 6; ++i) {
            data[i] = textureArray[i].getArray();
            dataOffset[i] = textureArray[i].getArrayOffset();
          }
          return new GVRCompressedCubemapTexture(gvrContext, texture.internalformat,
                  texture.width, texture.height,
                  texture.imageSize, data, dataOffset);
        }
      };

      protected AsyncLoadCompressedCubemapTextureResource(GVRContext gvrContext,
          GVRAndroidResource request,
          CancelableCallback<GVRCompressedCubemapTexture> callback, int priority) {
        super(gvrContext, sConverter, request, callback);
      }

      @Override
      protected CompressedTexture[] loadResource() {
        CompressedTexture[] textureArray = new CompressedTexture[6];
        ZipInputStream zipInputStream = new ZipInputStream(resource.getStream());

        try {
          ZipEntry zipEntry = null;
          while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String imageName = zipEntry.getName();
            String imageBaseName = FileNameUtils.getBaseName(imageName);
            Integer imageIndex = faceIndexMap.get(imageBaseName);
            if (imageIndex == null) {
              throw new IllegalArgumentException("Name of image ("
                  + imageName + ") is not set!");
            }
            textureArray[imageIndex] =
                CompressedTexture.load(zipInputStream, (int)zipEntry.getSize(), false);
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            zipInputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        resource.closeStream();
        return textureArray;
      }
    }
}
