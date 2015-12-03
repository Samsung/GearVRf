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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;

import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;

import static android.opengl.GLES30.*;

import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;

/**
 * One of the key GVRF classes: Encapsulates the data associated with rendering
 * a mesh.
 * 
 * This includes the {@link GVRMesh mesh} itself, the mesh's {@link GVRMaterial
 * material}, camera association, rendering order, and various other parameters.
 */
public class GVRRenderData extends GVRComponent implements PrettyPrint {

    private GVRMesh mMesh;
    private ArrayList<GVRRenderPass> mRenderPassList;
    private static final String TAG = "GearVRf";

    private GVRLight mLight;
    
    /** Just for {@link #getMeshEyePointee()} */
    private Future<GVRMesh> mFutureMesh;

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
        
        GVRRenderPass basePass = new GVRRenderPass(gvrContext);
        mRenderPassList = new ArrayList<GVRRenderPass>();
        addPass(basePass);
        isLightEnabled = false;
    }

    private GVRRenderData(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
        
        GVRRenderPass basePass = new GVRRenderPass(gvrContext);
        mRenderPassList = new ArrayList<GVRRenderPass>();
        addPass(basePass);
    }

    /**
     * @return The {@link GVRMesh mesh} being rendered.
     */
    public GVRMesh getMesh() {
        return mMesh;
    }

    /**
     * Set the {@link GVRMesh mesh} to be rendered.
     * 
     * @param mesh
     *            The mesh to be rendered.
     */
    public void setMesh(GVRMesh mesh) {
        synchronized (this) {
            mMesh = mesh;
            mFutureMesh = null;
        }
        NativeRenderData.setMesh(getNative(), mesh.getNative());
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
        synchronized (this) {
            mFutureMesh = mesh;
        }
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
     * Return a {@code Future<GVREyePointee>} or {@code null}.
     * 
     * If you use {@link #setMesh(Future)}, trying to create a
     * {@link GVRMeshEyePointee} in the 'normal' (synchronous) way will fail,
     * because this {@link GVRRenderData} won't have a mesh yet. This method
     * prevents that problem by returning an {@code Future} tied to the current
     * mesh status:
     * <ul>
     * <li>If you have already set a mesh, you will get a {@code Future} with
     * {@code get()} methods that return immediately.
     * <li>If you are currently waiting on a {@code Future<GVRMesh>}, you will
     * get a 'true' {@code Future} that waits for the {@code Future<GVRMesh>}.
     * <li>If you have neither, you will get {@code null}.
     * </ul>
     * 
     * <p>
     * This overload will return a {@code Future<GVREyePointee>} that uses the
     * mesh's bounding box; use the {@link #getMeshEyePointee(boolean)} overload
     * if you would prefer to use the actual mesh. With complicated meshes, it's
     * cheaper - though less accurate - to use the bounding box.
     * 
     * @return Either a {@code Future<GVREyePointee>} or {@code null}.
     */
    public Future<GVREyePointee> getMeshEyePointee() {
        return getMeshEyePointee(true);
    }

    /**
     * Return a {@code Future<GVREyePointee>} or {@code null}.
     * 
     * If you use {@link #setMesh(Future)}, trying to create a
     * {@link GVRMeshEyePointee} in the 'normal' (synchronous) way will fail,
     * because this {@link GVRRenderData} won't have a mesh yet. This method
     * prevents that problem by returning an {@code Future} tied to the current
     * mesh status:
     * <ul>
     * <li>If you have already set a mesh, you will get a {@code Future} with
     * {@code get()} methods that return immediately.
     * <li>If you are currently waiting on a {@code Future<GVRMesh>}, you will
     * get a 'true' {@code Future} that waits for the {@code Future<GVRMesh>}.
     * <li>If you have neither, you will get {@code null}.
     * </ul>
     * 
     * @param useBoundingBox
     *            When {@code true}, will use {@link GVRMesh#getBoundingBox()};
     *            when {@code false} will use {@code mesh} directly. With
     *            complicated meshes, it's cheaper - though less accurate - to
     *            use the bounding box.
     * 
     * @return Either a {@code Future<GVREyePointee>} or {@code null}.
     */
    public Future<GVREyePointee> getMeshEyePointee(boolean useBoundingBox) {
        synchronized (this) {
            if (mMesh != null) {
                // Wrap an eye pointee around the mesh,
                // return a non-blocking wrapper
                GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mMesh,
                        useBoundingBox);
                return new FutureWrapper<GVREyePointee>(eyePointee);
            } else if (mFutureMesh != null) {
                // Return a true (blocking) Future, tied to the Future<GVRMesh>
                return new FutureMeshEyePointee(mFutureMesh, useBoundingBox);
            } else {
                // No mesh
                return null;
            }
        }
    }

    private static class FutureMeshEyePointee implements Future<GVREyePointee> {

        private final Future<GVRMesh> mFutureMesh;
        private final boolean mUseBoundingBox;

        private FutureMeshEyePointee(Future<GVRMesh> futureMesh,
                boolean useBoundingBox) {
            mFutureMesh = futureMesh;
            mUseBoundingBox = useBoundingBox;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return isDone() ? false : mFutureMesh.cancel(mayInterruptIfRunning);
        }

        @Override
        public GVRMeshEyePointee get() throws InterruptedException,
                ExecutionException {
            GVRMesh mesh = mFutureMesh.get();
            return new GVRMeshEyePointee(mesh, mUseBoundingBox);
        }

        @Override
        public GVRMeshEyePointee get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException {
            GVRMesh mesh = mFutureMesh.get(timeout, unit);
            return new GVRMeshEyePointee(mesh, mUseBoundingBox);
        }

        @Override
        public boolean isCancelled() {
            return mFutureMesh.isCancelled();
        }

        @Override
        public boolean isDone() {
            return mFutureMesh.isDone();
        }
    }

    /**
     * Add a render {@link GVRRenderPass pass} to this RenderData.
     * @param pass
     */
    public void addPass(GVRRenderPass pass) {
        mRenderPassList.add(pass);
        NativeRenderData.addPass(getNative(), pass.getNative());
    }
    
    /**
     * Get a Rendering {@link GVRRenderPass Pass} for this Mesh
     * @param passIndex The index of the RenderPass to get.
     * @return
     */
    public GVRRenderPass getPass(int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex);
        } else {
            Log.e(TAG, "Trying to get invalid pass. Pass " + passIndex + " was not created.");
            return null;
        }
    }
    
    /**
     * @return The {@link GVRMaterial material} the {@link GVRMesh mesh} is
     *         being rendered with.
     */
    public GVRMaterial getMaterial() {
        return getMaterial(0);
    }
    
    /**
     * @param The {@link GVRRenderPass pass} index to retrieve material from.
     * @return The {@link GVRMaterial material} the {@link GVRMesh mesh} is
     *         being rendered with.
     */
    public GVRMaterial getMaterial(int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex).getMaterial();
        } else {
            Log.e(TAG, "Trying to get material from invalid pass. Pass " + passIndex + " was not created.");
            return null;
        }
    }

    /**
     * Set the {@link GVRMaterial material} the mesh will be rendered with.
     * 
     * @param material
     *            The {@link GVRMaterial material} for rendering.
     */
    public void setMaterial(GVRMaterial material) {
        setMaterial(material, 0);
    }

    /**
     * Set the {@link GVRMaterial material} this pass will be rendered with.
     * 
     * @param material
     *            The {@link GVRMaterial material} for rendering.
     * @param passIndex
     *            The rendering pass this material will be assigned to.
     * 
     */
    public void setMaterial(GVRMaterial material, int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            mRenderPassList.get(passIndex).setMaterial(material);
        } else {
            Log.e(TAG, "Trying to set material from invalid pass. Pass " + passIndex + " was not created.");
        }
        
    }

    /**
     * @return The {@link GVRLight light} the {@link GVRMesh mesh} is being lit
     *         by.
     */
    public GVRLight getLight() {
        return mLight;
    }

    /**
     * Set the {@link GVRLight light} the mesh will be lit by.
     * 
     * @param light
     *            The {@link GVRLight light} for rendering.
     */
    public void setLight(GVRLight light) {
        boolean supportsLight = false;
        
        for (int pass = 0; pass < mRenderPassList.size(); ++pass) {
            if (mRenderPassList.get(pass).getMaterial().getShaderType() == GVRShaderType.Texture.ID) {
                supportsLight = true;
                break;
            }
        }
        
        if (!supportsLight) {
            throw new UnsupportedOperationException(
                    "Only Texture shader can has light.");
        }
        
        mLight = light;
        NativeRenderData.setLight(getNative(), light.getNative());
        isLightEnabled = true;
    }

    /**
     * Enable lighting effect for the render_data. Note that it is different to
     * GVRLight.enable(). GVRLight.enable turn on a light, while this method
     * enable the lighting effect for the render_data. The lighting effect is
     * applied if and only if {@code mLight} is enabled (i.e. on) AND the
     * lighting effect is enabled for the render_data.
     */
    public void enableLight() {
        if (mLight == null) {
            throw new UnsupportedOperationException("No light is added yet.");
        }
        NativeRenderData.enableLight(getNative());
        isLightEnabled = true;
    }

    /**
     * Disable lighting effect for the render_data. Note that it is different to
     * GVRLight.disable(). GVRLight.disable turn off a light, while this method
     * disable the lighting effect for the render_data. The lighting effect is
     * applied if and only if {@code mLight} is enabled (i.e. on) AND the
     * lighting effect is enabled for the render_data.
     */
    public void disableLight() {
        if (mLight == null) {
            throw new UnsupportedOperationException("No light is added yet.");
        }
        NativeRenderData.disableLight(getNative());
        isLightEnabled = false;
    }

    /**
     * Get the enable/disable status for the lighting effect. Note that it is
     * different to enable/disable status of the light. The lighting effect is
     * applied if and only if {@code mLight} is enabled (i.e. on) AND the
     * lighting effect is enabled for the render_data.
     * 
     * @return true if lighting effect is enabled, false if lighting effect is
     *         disabled.
     */
    public boolean isLightEnabled() {
        return isLightEnabled;
    }

    /**
     * Get the rendering options bit mask.
     * 
     * @return The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public int getRenderMask() {
        return NativeRenderData.getRenderMask(getNative());
    }

    /**
     * Set the rendering options bit mask.
     * 
     * @param renderMask
     *            The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public void setRenderMask(int renderMask) {
        NativeRenderData.setRenderMask(getNative(), renderMask);
    }

    /**
     * @return The order in which this mesh will be rendered.
     * @see GVRRenderingOrder
     */
    public int getRenderingOrder() {
        return NativeRenderData.getRenderingOrder(getNative());
    }

    /**
     * Set the order in which this mesh will be rendered.
     * 
     * @param renderingOrder
     *            See {@link GVRRenderingOrder}
     */
    public void setRenderingOrder(int renderingOrder) {
        NativeRenderData.setRenderingOrder(getNative(), renderingOrder);
    }

    /**
     * @deprecated Use {@code getCullFace() } instead.
     * @see #getCullFace() 
     * @return {@code true} if {@code GL_CULL_FACE} is enabled, {@code false} if
     *         not.
     */
    public boolean getCullTest() {
        return getCullFace(0) != GVRCullFaceEnum.None;
    }

    /**
     * @return current face to be culled See {@link GVRCullFaceEnum}.
     */
    public GVRCullFaceEnum getCullFace() {
        return getCullFace(0);
    }

    /**
     * @param passIndex
     *            The rendering pass index to query cull face state.
     * @return current face to be culled See {@link GVRCullFaceEnum}.
     */
    public GVRCullFaceEnum getCullFace(int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex).getCullFace();
        } else {
            Log.e(TAG, "Trying to get cull face from invalid pass. Pass " + passIndex + " was not created.");
            return GVRCullFaceEnum.Back;
        }
    }

    /**
     * @deprecated Use {@code setCullFace(GVRCullFaceEnum cullFace)} instead.
     * @see #setCullFace(int cullFace)
     * Set the {@code GL_CULL_FACE} option
     * 
     * @param cullTest
     *            {@code true} if {@code GL_CULL_FACE} should be enabled,
     *            {@code false} if not.
     * @param pass
     */
    public void setCullTest(boolean cullTest) {
        if (cullTest) {
            setCullFace(GVRCullFaceEnum.Back);
        } else {
            setCullFace(GVRCullFaceEnum.None);
        }

    }

    /**
     * Set the face to be culled
     * 
     * @param cullFace
     *            {@code GVRCullFaceEnum.Back} Tells Graphics API to discard
     *            back faces, {@code GVRCullFaceEnum.Front} Tells Graphics API
     *            to discard front faces, {@code GVRCullFaceEnum.None} Tells
     *            Graphics API to not discard any face
     */
    public void setCullFace(GVRCullFaceEnum cullFace) {
        setCullFace(cullFace, 0);
    }

    /**
     * Set the face to be culled
     * 
     * @param cullFace
     *            {@code GVRCullFaceEnum.Back} Tells Graphics API to discard
     *            back faces, {@code GVRCullFaceEnum.Front} Tells Graphics API
     *            to discard front faces, {@code GVRCullFaceEnum.None} Tells
     *            Graphics API to not discard any face
     * @param passIndex
     *            The rendering pass to set cull face state
     */
    public void setCullFace(GVRCullFaceEnum cullFace, int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            mRenderPassList.get(passIndex).setCullFace(cullFace);
        } else {
            Log.e(TAG, "Trying to set cull face to a invalid pass. Pass " + passIndex + " was not created.");
        }
    }

    /**
     * @return {@code true} if {@code GL_POLYGON_OFFSET_FILL} is enabled,
     *         {@code false} if not.
     */
    public boolean getOffset() {
        return NativeRenderData.getOffset(getNative());
    }

    /**
     * Set the {@code GL_POLYGON_OFFSET_FILL} option
     * 
     * @param offset
     *            {@code true} if {@code GL_POLYGON_OFFSET_FILL} should be
     *            enabled, {@code false} if not.
     */
    public void setOffset(boolean offset) {
        NativeRenderData.setOffset(getNative(), offset);
    }

    /**
     * @return The {@code factor} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetFactor() {
        return NativeRenderData.getOffsetFactor(getNative());
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
        NativeRenderData.setOffsetFactor(getNative(), offsetFactor);
    }

    /**
     * @return The {@code units} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetUnits() {
        return NativeRenderData.getOffsetUnits(getNative());
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
        NativeRenderData.setOffsetUnits(getNative(), offsetUnits);
    }

    /**
     * @return {@code true} if {@code GL_DEPTH_TEST} is enabled, {@code false}
     *         if not.
     */
    public boolean getDepthTest() {
        return NativeRenderData.getDepthTest(getNative());
    }

    /**
     * Set the {@code GL_DEPTH_TEST} option
     * 
     * @param depthTest
     *            {@code true} if {@code GL_DEPTH_TEST} should be enabled,
     *            {@code false} if not.
     */
    public void setDepthTest(boolean depthTest) {
        NativeRenderData.setDepthTest(getNative(), depthTest);
    }

    /**
     * @return {@code true} if {@code GL_BLEND} is enabled, {@code false} if
     *         not.
     */
    public boolean getAlphaBlend() {
        return NativeRenderData.getAlphaBlend(getNative());
    }

    /**
     * Set the {@code GL_BLEND} option
     * 
     * @param alphaBlend
     *            {@code true} if {@code GL_BLEND} should be enabled,
     *            {@code false} if not.
     */
    public void setAlphaBlend(boolean alphaBlend) {
        NativeRenderData.setAlphaBlend(getNative(), alphaBlend);
    }

    /**
     * @return The OpenGL draw mode (e.g. GL_TRIANGLES).
     */
    public int getDrawMode() {
        return NativeRenderData.getDrawMode(getNative());
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
                    "drawMode must be one of GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES, GL_TRIANGLE_FAN, GL_TRIANGLE_STRIP.");
        }
        NativeRenderData.setDrawMode(getNative(), drawMode);
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        GVRMesh mesh = null;
        if (mMesh != null) {
            mesh = mMesh;
        } else if (this.mFutureMesh != null) {
            try {
                mesh = mFutureMesh.get();
            } catch (Exception e) {
            }
        }
        if (mesh != null) {
            sb.append(Log.getSpaces(indent));
            sb.append("Mesh: ");
            mesh.prettyPrint(sb, indent);
        }

        sb.append(Log.getSpaces(indent));
        sb.append("Light: " + mLight);
        sb.append(System.lineSeparator());

        sb.append(Log.getSpaces(indent));
        sb.append("Light enabled: " + isLightEnabled);
        sb.append(System.lineSeparator());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    private boolean isLightEnabled;
}

class NativeRenderData {
    static native long ctor();

    static native void setMesh(long renderData, long mesh);

    static native void addPass(long renderData, long renderPass);

    static native void setLight(long renderData, long light);

    static native void enableLight(long renderData);

    static native void disableLight(long renderData);

    static native int getRenderMask(long renderData);

    static native void setRenderMask(long renderData, int renderMask);

    static native int getRenderingOrder(long renderData);

    static native void setRenderingOrder(long renderData, int renderingOrder);
    
    static native boolean getOffset(long renderData);

    static native void setOffset(long renderData, boolean offset);

    static native float getOffsetFactor(long renderData);

    static native void setOffsetFactor(long renderData, float offsetFactor);

    static native float getOffsetUnits(long renderData);

    static native void setOffsetUnits(long renderData, float offsetUnits);

    static native boolean getDepthTest(long renderData);

    static native void setDepthTest(long renderData, boolean depthTest);

    static native boolean getAlphaBlend(long renderData);

    public static native void setAlphaBlend(long renderData, boolean alphaBlend);

    public static native int getDrawMode(long renderData);

    public static native void setDrawMode(long renderData, int draw_mode);

}
