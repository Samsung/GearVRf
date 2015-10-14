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

/**
 * Implementation of asynch resource loading.
 * 
 * Most classes are hidden, implementation details. You need to implement 
 * {@link org.gearvrf.asynchronous.GVRCompressedTextureLoader}
 * to use compressed texture formats that GVRF does not support; you may find 
 * {@link org.gearvrf.asynchronous.GVRCompressedTexture}
 * useful for debugging. 
 * 
 * {@link org.gearvrf.asynchronous.GVRAsynchronousResourceLoader} 
 * is public only so that {@link org.gearvrf.GVRContext} can make cross-package
 * calls: It <em>is</em> an official part of the API, but we suggest you avoid 
 * using it directly.
 * 
 * @since 1.6.1
 */
package org.gearvrf.asynchronous;