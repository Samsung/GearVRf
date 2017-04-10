/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This is a small convenience class that makes it easy to unzip a file and load entries as
 * {@link GVRAndroidResource}s into the GVRf framework.
 *
 * For cases where the number of files in an applications gets too large to handle, consider
 * zipping them and using the {@link ZipLoader} to process them one at a time.
 *
 * The class is flexible enough to allow all types of {@link GVRAndroidResource}s to be loaded.
 *
 * Note that the
 * {@link ZipLoader} makes use of a {@link ZipEntryProcessor}. The {@link ZipEntryProcessor} is a
 * user defined method that is applied to all entries in a zip file.
 */
public abstract class ZipLoader {
    private static final String TAG = ZipLoader.class.getSimpleName();

    /**
     * Make use of the {@link ZipEntryProcessor} to process the {@link GVRAndroidResource}s
     * obtained from the zip file.
     */
    public interface ZipEntryProcessor<T> {
        /**
         * This call is made for each entry in the zip file. Use this call to provide the
         * convenience function to process the {@link GVRAndroidResource}s obtained from the zip
         * file. For eg. use
         * {@link GVRContext#loadFutureTexture(GVRAndroidResource, int)} to return
         * a {@link Future<GVRTexture>} to the {@link ZipLoader}.
         *
         * @param context  the GVRf context
         * @param resource a resource entry obtained from the zip file
         * @return a processed zip resource entry
         */
        T getItem(GVRContext context, GVRAndroidResource resource);
    }

    /**
     * Use this call to load a zip file using the
     * {@link ZipLoader} and apply the {@link ZipEntryProcessor} to each entry. The result is a
     * list of all processed entries obtained from the zip file.
     *
     * @param gvrContext  the GVRf context
     * @param zipFileName the name of the zip file. This must be a file in the assets folder.
     * @param processor   the {@link ZipEntryProcessor} to be applied to each zip entry in the file.
     * @return a list of processed zip file entries.
     * @throws IOException this function returns an {@link IOException} if there are issues
     *                     processing the provided zip file.
     */
    public static <T> List<T> load(GVRContext gvrContext, String zipFileName, ZipEntryProcessor<T>
            processor) throws IOException {
        Context context = gvrContext.getContext();
        InputStream inputStream = context.getAssets().open(zipFileName);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        List<T> result = new ArrayList<T>();

        try {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zipInputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }

                byte[] bytes = baos.toByteArray();

                InputStream resourceInputStream = new ByteArrayInputStream(bytes);

                GVRAndroidResource androidResource = new GVRAndroidResource(zipEntry.getName(),
                        resourceInputStream);
                T item = processor.getItem(gvrContext, androidResource);
                result.add(item);
            }
        } finally {
            zipInputStream.close();
        }
        return result;
    }
}
