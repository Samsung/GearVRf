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

public class FileExtension {
  public static String getBaseName(String fileName) {
    if (fileName == null)
      return null;

    String[] tokens = split(fileName);
    return tokens[0];
  }

  public static String getExtension(String fileName) {
    if (fileName == null)
      return null;

    String[] tokens = split(fileName);
    if (tokens.length >= 2)
      return tokens[tokens.length - 1];

    return null;
  }

  // http://stackoverflow.com/questions/4545937/java-splitting-the-filename-into-a-base-and-extension
  private static String[] split(String fileName) {
    String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
    return tokens;
  }
}
