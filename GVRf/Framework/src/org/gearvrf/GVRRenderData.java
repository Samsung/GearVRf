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

package org.gearvrf;

import java.util.concurrent.Future;
import static android.opengl.GLES30.*;
import org.gearvrf.utility.Threads;

/**
 * One of the key GVRF classes: Encapsulates the data associated with rendering
 * a mesh.
 * 
 * This includes the {@link GVRMesh mesh} itself, the mesh's {@link GVRMaterial
 * material}, camera association, rendering order, and various other parameters.
 */
public class GVRRenderData extends GVRComponent {

    /**
     * Rendering hints.
     * 
     * You might expect the rendering process to sort the scene graph, from back
     * to front, so it can then draw translucent objects over the objects behind
     * them. But that's not how GVRF works. Instead, it sorts the scene graph by
     * render order, then draws the sorted graph in traversal order. (Please
     * don't waste your time getting angry or trying to make sense of this;
     * please just take it as a bald statement of How GVRF Currently Works.)
     * 
     * <p>
     * The point is, to get transparency to work as you expect, you do need to
     * explicitly call {@link GVRRenderData#setRenderingOrder(int)
     * setRenderingOrder():} objects are sorted from low render order to high
     * render order, so that a {@link #GEOMETRY} object will show through a
     * {@link #TRANSPARENT} object.
     */
    public abstract static class GVRRenderingOrder {
        /**
         * Rendered first, below any other objects at the same distance from the
         * camera
         */
        public static final int BACKGROUND = 1000;
        /**
         * The default render order, if you don't explicitly call
         * {@link GVRRenderData#setRenderingOrder(int)}
         */
        public static final int GEOMETRY = 2000;
        /** The rendering order for see-through objects */
        public static final int TRANSPARENT = 3000;
        /** The rendering order for sprites {@literal &c.} */
        public static final int OVERLAY = 4000;
    };

    /** Items for the rendering options bit mask. */
    public abstract static class GVRRenderMaskBit {
        /**
         * Render the mesh in the left {@link GVRCamera camera}.
         */
        public static final int Left = 0x1;
        /**
         * Render the mesh in the right {@link GVRCamera camera}.
         */
        public static final int Right = 0x2;
    }

    /**
     * Constructor.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRRenderData(GVRContext gvrContext) {
        super(gvrContext, NativeRenderData.ctor());
    }

    private GVRRenderData(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    static GVRRenderData factory(GVRContext gvrContext, long ptr) {
        GVRHybridObject wrapper = wrapper(ptr);
        return wrapper == null ? new GVRRenderData(gvrContext, ptr)
                : (GVRRenderData) wrapper;
    }

    @Override
    protected final boolean registerWrapper() {
        return true;
    }

    /**
     * @return The {@link GVRMesh mesh} being rendered.
     */
    public GVRMesh getMesh() {
        long ptr = NativeRenderData.getMesh(getPtr());
        return ptr == 0 ? null : GVRMesh.factory(getGVRContext(), ptr);
    }

    /**
     * Set the {@link GVRMesh mesh} to be rendered.
     * 
     * @param mesh
     *            The mesh to be rendered.
     */
    public void setMesh(GVRMesh mesh) {
        NativeRenderData.setMesh(getPtr(), mesh.getPtr());
    }

    /**
     * Asynchronously set the {@link GVRMesh mesh} to be rendered.
     * 
     * Uses a background thread from the thread pool to wait for the
     * {@code Future.get()} method; unless you are loading dozens of meshes
     * asynchronously, the extra overhead should be modest compared to the cost
     * of loading a mesh.
     * 
     * @param mesh
     *            The mesh to be rendered.
     * 
     * @since 1.6.7
     */
    public void setMesh(final Future<GVRMesh> mesh) {
        Threads.spawn(new Runnable() {

            @Override
            public void run() {
                try {
                    setMesh(mesh.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return The {@link GVRMaterial material} the {@link GVRMesh mesh} is
     *         being rendered with.
     */
    public GVRMaterial getMaterial() {
        long ptr = NativeRenderData.getMaterial(getPtr());
        return ptr == 0 ? null : new GVRMaterial(getGVRContext(), ptr);
    }

    /**
     * Set the {@link GVRMaterial material} the mesh will be rendered with.
     * 
     * @param material
     *            The {@link GVRMaterial material} for rendering.
     */
    public void setMaterial(GVRMaterial material) {
        NativeRenderData.setMaterial(getPtr(), material.getPtr());
    }

    /**
     * Get the rendering options bit mask.
     * 
     * @return The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public int getRenderMask() {
        return NativeRenderData.getRenderMask(getPtr());
    }

    /**
     * Set the rendering options bit mask.
     * 
     * @param renderMask
     *            The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public void setRenderMask(int renderMask) {
        NativeRenderData.setRenderMask(getPtr(), renderMask);
    }

    /**
     * @return The order in which this mesh will be rendered.
     * @see GVRRenderingOrder
     */
    public int getRenderingOrder() {
        return NativeRenderData.getRenderingOrder(getPtr());
    }

    /**
     * Set the order in which this mesh will be rendered.
     * 
     * @param renderingOrder
     *            See {@link GVRRenderingOrder}
     */
    public void setRenderingOrder(int renderingOrder) {
        NativeRenderData.setRenderingOrder(getPtr(), renderingOrder);
    }

    /**
     * @return {@code true} if {@code GL_CULL_FACE} is enabled, {@code false} if
     *         not.
     */
    public boolean getCullTest() {
        return NativeRenderData.getCullTest(getPtr());
    }

    /**
     * Set the {@code GL_CULL_FACE} option
     * 
     * @param cullTest
     *            {@code true} if {@code GL_CULL_FACE} should be enabled,
     *            {@code false} if not.
     */
    public void setCullTest(boolean cullTest) {
        NativeRenderData.setCullTest(getPtr(), cullTest);
    }

    /**
     * @return {@code true} if {@code GL_POLYGON_OFFSET_FILL} is enabled,
     *         {@code false} if not.
     */
    public boolean getOffset() {
        return NativeRenderData.getOffset(getPtr());
    }

    /**
     * Set the {@code GL_POLYGON_OFFSET_FILL} option
     * 
     * @param offset
     *            {@code true} if {@code GL_POLYGON_OFFSET_FILL} should be
     *            enabled, {@code false} if not.
     */
    public void setOffset(boolean offset) {
        NativeRenderData.setOffset(getPtr(), offset);
    }

    /**
     * @return The {@code factor} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetFactor() {
        return NativeRenderData.getOffsetFactor(getPtr());
    }

    /**
     * Set the {@code factor} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * 
     * @param offsetFactor
     *            Per OpenGL docs: Specifies a scale factor that is used to
     *            create a variable depth offset for each polygon. The initial
     *            value is 0.
     * @see #setOffset(boolean)
     */
    public void setOffsetFactor(float offsetFactor) {
        NativeRenderData.setOffsetFactor(getPtr(), offsetFactor);
    }

    /**
     * @return The {@code units} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetUnits() {
        return NativeRenderData.getOffsetUnits(getPtr());
    }

    /**
     * Set the {@code units} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * 
     * @param offsetUnits
     *            Per OpenGL docs: Is multiplied by an implementation-specific
     *            value to create a constant depth offset. The initial value is
     *            0.
     * @see #setOffset(boolean)
     */
    public void setOffsetUnits(float offsetUnits) {
        NativeRenderData.setOffsetUnits(getPtr(), offsetUnits);
    }

    /**
     * @return {@code true} if {@code GL_DEPTH_TEST} is enabled, {@code false}
     *         if not.
     */
    public boolean getDepthTest() {
        return NativeRenderData.getDepthTest(getPtr());
    }

    /**
     * Set the {@code GL_DEPTH_TEST} option
     * 
     * @param depthTest
     *            {@code true} if {@code GL_DEPTH_TEST} should be enabled,
     *            {@code false} if not.
     */
    public void setDepthTest(boolean depthTest) {
        NativeRenderData.setDepthTest(getPtr(), depthTest);
    }

    /**
     * @return {@code true} if {@code GL_BLEND} is enabled, {@code false} if
     *         not.
     */
    public boolean getAlphaBlend() {
        return NativeRenderData.getAlphaBlend(getPtr());
    }

    /**
     * Set the {@code GL_BLEND} option
     * 
     * @param alphaBlend
     *            {@code true} if {@code GL_BLEND} should be enabled,
     *            {@code false} if not.
     */
    public void setAlphaBlend(boolean alphaBlend) {
        NativeRenderData.setAlphaBlend(getPtr(), alphaBlend);
    }

    /**
     * @return The OpenGL draw mode (e.g. GL_TRIANGLES).
     */
    public int getDrawMode() {
        return NativeRenderData.getDrawMode(getPtr());
    }

    /**
     * Set the draw mode for this mesh. Default is GL_TRIANGLES.
     * 
     * @param drawMode
     */
    public void setDrawMode(int drawMode) {
        if (drawMode != GL_POINTS && drawMode != GL_LINES
                && drawMode != GL_LINE_STRIP && drawMode != GL_LINE_LOOP
                && drawMode != GL_TRIANGLES && drawMode != GL_TRIANGLE_STRIP
                && drawMode != GL_TRIANGLE_FAN) {
            throw new IllegalArgumentException(
                    "drawMode must be one of GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES, GL_TRIANGLE_FAN, GL_TRIANGLE_STRIP.  Value passed was: "
                            + drawMode);
        }
        NativeRenderData.setDrawMode(getPtr(), drawMode);
    }

}

class NativeRenderData {
    public static native long ctor();

    public static native long getMesh(long renderData);

    public static native void setMesh(long renderData, long mesh);

    public static native long getMaterial(long renderData);

    public static native void setMaterial(long renderData, long material);

    public static native int getRenderMask(long renderData);

    public static native void setRenderMask(long renderData, int renderMask);

    public static native int getRenderingOrder(long renderData);

    public static native void setRenderingOrder(long renderData,
            int renderingOrder);

    public static native boolean getCullTest(long renderData);

    public static native void setCullTest(long renderData, boolean cullTest);

    public static native boolean getOffset(long renderData);

    public static native void setOffset(long renderData, boolean offset);

    public static native float getOffsetFactor(long renderData);

    public static native void setOffsetFactor(long renderData,
            float offsetFactor);

    public static native float getOffsetUnits(long renderData);

    public static native void setOffsetUnits(long renderData, float offsetUnits);

    public static native boolean getDepthTest(long renderData);

    public static native void setDepthTest(long renderData, boolean depthTest);

    public static native boolean getAlphaBlend(long renderData);

    public static native void setAlphaBlend(long renderData, boolean alphaBlend);

    public static native int getDrawMode(long renderData);

    public static native void setDrawMode(long renderData, int draw_mode);

}
