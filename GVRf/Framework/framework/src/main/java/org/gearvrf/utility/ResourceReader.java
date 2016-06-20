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

package org.gearvrf.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceReader {
    private static final int BUFFER_SIZE = 8192; /* bytes */

    public static byte[] readStream(InputStream stream) {
        ByteArrayOutputStream ostream = null;
        try {
            ostream = new ByteArrayOutputStream(stream.available());
            byte data[] = new byte[BUFFER_SIZE];
            int count;
            while ((count = stream.read(data)) != -1) {
                ostream.write(data, 0, count);
            }
            return ostream.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }

            if (ostream != null) {
                try {
                    ostream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
