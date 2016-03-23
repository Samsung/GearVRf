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
 * You need to understand key points of this package to create a GVRF application.
 * 
 * The {@link Activity} that your {@code AndroidManifest.xml} file declares 
 * handles the {@code "android.intent.action.MAIN"} ({@link 
 * android.content.Intent#ACTION_MAIN}) <em>must</em> descend from {@link 
 * org.gearvrf.GVRActivity}, not directly from {@code Activity}.
 * 
 * <p>Your {@code GVRActivity} descendant <em>must</em> create an instance of a class 
 * that descends from {@link org.gearvrf.GVRScript} and pass
 * that instance to {@link 
 * org.gearvrf.GVRActivity#setScript(GVRScript, String)} 
 * (or to {@link 
 * org.gearvrf.GVRActivity#setScript(GVRScript, String, GVRSurfaceViewRenderer)}.
 * 
 * A {@linkplain org.gearvrf.GVRScene scene graph} contains any 
 * number of {@linkplain org.gearvrf.GVRSceneObject scene 
 * objects.} Scene objects have an {@linkplain org.gearvrf.GVRTransform 
 * 4x4 matrix} which positions them in the scene. Each scene object can have child
 * scene objects: moving the parent moves the children, maintaining the children's
 * positions with relation to each other.
 * 
 * <p>
 * To be visible, a scene object must have {@linkplain org.gearvrf.GVRRenderData
 * render data,} including a {@linkplain org.gearvrf.GVRMesh GL 
 * mesh} which defines the surface geometry and a {@linkplain org.gearvrf.GVRMaterial
 * material} which defines the surface appearance by specifying a GL shader and
 * its parameters.
 * 
 * <a name="assets"><h3>Assets</h3></a>
 * 
 * <p>
 * Several classes in this package can load resources from either {@code res/raw}, 
 * using standard Android {@code R.raw.} resource ids, or from the {@code assets}
 * tree, using assets-relative filenames. There are pluses and minuses to each 
 * approach.
 * 
 * <p>
 * Using {@code assets} has two benefits and one drawback. The first benefit
 * is that you can examine the tree at runtime. This lets you write code
 * that will load all the assets in a particular subtree, even if those
 * assets weren't part of your project when the loading code was written.
 * The second benefit is that you can build a sort of matrix of names, like
 * <table border="1">
 * <tr><td></td>
 *     <td>{@code thisPiece}</td>
 *     <td>{@code thatPiece}</td></tr>
 * <tr><td>{@code thisModel}</td>
 *     <td>{@code models/thisModel/thisPiece.whatever}</td>
 *     <td>{@code models/thisModel/thatPiece.whatever}</td></tr>
 * <tr><td>{@code thatModel}</td>
 *     <td>{@code models/thatModel/thisPiece.whatever}</td>
 *     <td>{@code models/thatModel/thatPiece.whatever}</td></tr>
 * </table>
 * The drawback is that you lose compile-time checks: a typo in an obscure 
 * branch of your code, or an inadvertently deleted (or renamed!) file may go 
 * undetected if you don't manage to test each and every branch of your code. 
 * All the methods that take an assets-relative filename will return {@code 
 * null} if the file does not exist or is not legible.
 * 
 * <p>
 * Using {@code res/raw} files is pretty much the mirror image: you
 * <em>know</em> the file will exist, but you've given up on discovery and
 * you've flattened your names a lot, even throwing away file extensions.
 * Thus, the resource-id overloads are probably best when you only have a few of
 * any particular type of resource; the assets-relative overloads start to make
 * more sense when you have lots of any one type of resource and/or need to 
 * do runtime resource discovery.
 * 
 * <a name="async"><h3>Asynchronous Resource Loading</h3></a>
 * 
 * Loading an Android {@link android.graphics.Bitmap} to create a {@link 
 * org.gearvrf.GVRBitmapTexture} can take hundreds of milliseconds;
 * loading a {@link org.gearvrf.GVRMesh} can take even longer. These
 * are not operations that you want to do on the GL thread!
 * 
 * <p>
 * GVRF includes an asynchronous loading facility, which improves throughput in 
 * three ways. First, by doing all the work on a background thread, then 
 * delivering the loaded resource to the GL thread on a {@link 
 * org.gearvrf.GVRContext#runOnGlThread(Runnable)
 * runOnGlThread()} callback. Second, they use a throttler to avoid
 * overloading the system and/or running out of memory. Third, they do
 * 'request consolidation' - if you issue any requests for a particular file
 * while there is still a pending request, the file will only be read once,
 * and each callback will get the same resource. 
 * 
 * <p>
 * The asynchronous loader includes a priority system, so that you can load
 * resources that color lots of pixels before resources that only color a few 
 * pixels before resources that aren't currently visible. There are four main
 * things you should keep in mind about priorities:<ul>
 * 
 * <li>Priorities are 32-bit Java {@code int}s, but not every {@code int} is a 
 * valid priority. Priorities run from {@link 
 * org.gearvrf.GVRContext#LOWEST_PRIORITY} to {@link 
 * org.gearvrf.GVRContext#HIGHEST_PRIORITY}. 
 * 
 * <li>Priorities use numerical order: a 2 is higher than a 1, and will run first.
 * 
 * <li>All async loading uses the same throttler and the same priority system: 
 * a priority 1 mesh will load after a priority 2 texture, even if there are no 
 * higher priority meshes in the queue.
 * 
 * <li>Priorities only apply to enqueued requests! If the async loader is not
 * currently servicing "too many" requests, any request will be executed 
 * immediately; if the async loader <em>is</em> currently servicing "too many" 
 * requests, all requests will be queued, and then serviced in priority order.
 * ("Too many" is deliberately vague: the current implementation is based strictly
 * on the device's number of CPU cores, but this may change in future releases.)   
 * 
 * </ul>
 */
package org.gearvrf;