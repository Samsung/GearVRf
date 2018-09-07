package org.gearvrf.widgetlib.widget;

import org.gearvrf.widgetlib.main.CommandBuffer;
import org.gearvrf.widgetlib.main.CommandBuffer.Command;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

// TODO: Replace mExternalData references with posting opcodes to command buffer
// TODO: Extend GVRRenderData for a static "identity" instance for "no render data" scenarios
// IDEA: With above, once we're buffering operations, have a "postOp()" method that does a
// centralized check for whether our render data is valid.  Overhead of fetching opcode instances
// from pool and configuring them should be low, and it would be a much tidier approach than doing
// "if (mRenderDataCache != null)" everywhere.
class RenderDataCache {
    RenderDataCache(GVRSceneObject sceneObject) {
        mExternalRenderData = sceneObject.getRenderData();
        if (mExternalRenderData != null) {
            mRenderData = new GVRRenderData(sceneObject.getGVRContext());
            mRenderData.setDepthTest(mExternalRenderData.getDepthTest());
            mRenderData.setMesh(mExternalRenderData.getMesh());
            mRenderData.setOffset(mExternalRenderData.getOffset());
            mRenderData.setOffsetFactor(mExternalRenderData.getOffsetFactor());
            mRenderData.setOffsetUnits(mExternalRenderData.getOffsetUnits());
            mRenderData.setRenderingOrder(mExternalRenderData.getRenderingOrder());

            // No getters available!!!
//            mRenderData.setStencilFunc(...);
//            mRenderData.setStencilMask(renderData.getStencilMask());
//            mRenderData.setStencilTest(renderData.getStencilTest());
            final GVRMaterial material = mExternalRenderData.getMaterial();
            mRenderData.setMaterial(material);
            mMaterialCache = new MaterialCache(material);
        } else {
            mRenderData = null;
            mMaterialCache = new MaterialCache();
        }
    }

    boolean hasRenderData() {
        return mRenderData != null;
    }

    void setMesh(GVRMesh mesh) {
        if (mRenderData != null) {
            SET_MESH.buffer(mExternalRenderData, mesh);
            mRenderData.setMesh(mesh);
        }
    }

    void setOffset(boolean offset) {
        if (mRenderData != null) {
            SET_OFFSET.buffer(mExternalRenderData, offset);
            mRenderData.setOffset(offset);
        }
    }

    float getOffsetFactor() {
        if (mRenderData != null) {
            return mRenderData.getOffsetFactor();
        }
        return 0;
    }

    boolean getOffset() {
        return mRenderData != null && mRenderData.getOffset();
    }

    float getOffsetUnits() {
        if (mRenderData != null) {
            return mRenderData.getOffsetUnits();
        }
        return 0;
    }

    int getRenderingOrder() {
        if (mRenderData != null) {
            return mRenderData.getRenderingOrder();
        }
        return -1;
    }

    GVRMesh getMesh() {
        if (mRenderData != null) {
            return mRenderData.getMesh();
        }
        return null;
    }

    void setOffsetFactor(float offsetFactor) {
        if (mRenderData != null) {
            SET_OFFSET_FACTOR.buffer(mExternalRenderData, offsetFactor);
            mRenderData.setOffsetFactor(offsetFactor);
        }
    }

    void setRenderingOrder(int renderingOrder) {
        if (mRenderData != null) {
            SET_RENDERING_ORDER.buffer(mExternalRenderData, renderingOrder);
            mRenderData.setRenderingOrder(renderingOrder);
        }
    }

    void setCullFace(GVRRenderPass.GVRCullFaceEnum cullFace) {
        if (mRenderData != null) {
            SET_CULL_FACE.buffer(mExternalRenderData, cullFace);
            mRenderData.setCullFace(cullFace);
        }
    }

    void setOffsetUnits(float offsetUnits) {
        if (mRenderData != null) {
            SET_OFFSET_UNITS.buffer(mExternalRenderData, offsetUnits);
            mRenderData.setOffsetUnits(offsetUnits);
        }
    }

    boolean getDepthTest() {
        return mRenderData != null && mRenderData.getDepthTest();
    }

    void setDepthTest(boolean depthTest) {
        if (mRenderData != null) {
            SET_DEPTH_TEST.buffer(mExternalRenderData, depthTest);
            mRenderData.setDepthTest(depthTest);
        }
    }

    void setStencilTest() {
        if (mRenderData != null) {
            SET_STENCIL_TEST.buffer(mExternalRenderData);
            mRenderData.setStencilTest(true);
        }
    }

    void setStencilFunc(int func) {
        if (mRenderData != null) {
            SET_STENCIL_FUNC.buffer(mExternalRenderData, func);
            mRenderData.setStencilFunc(func, 1, 0xFF);
        }
    }

    void setStencilMask() {
        if (mRenderData != null) {
            SET_STENCIL_MASK.buffer(mExternalRenderData);
            mRenderData.setStencilMask(0x00);
        }
    }

    MaterialCache getMaterial() {
        return mMaterialCache;
    }

    void setMaterial(GVRMaterial material) {
        if (mRenderData != null) {
            SET_MATERIAL.buffer(mExternalRenderData, material);
            mRenderData.setMaterial(material);
        }
        mMaterialCache.set(material);
    }

    private static final class SET_MESH {
        static void buffer(GVRRenderData renderData, GVRMesh mesh) {
            CommandBuffer.Command.buffer(sExecutor, renderData, mesh);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final GVRMesh mesh = (GVRMesh) params[1];
                renderData.setMesh(mesh);
            }
        };
    }

    private static final class SET_OFFSET {
        static void buffer(GVRRenderData renderData, boolean offset) {
            CommandBuffer.Command.buffer(sExecutor, renderData, offset);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final boolean offset = (boolean) params[1];
                renderData.setOffset(offset);
            }
        };
    }

    private static final class SET_OFFSET_FACTOR {
        static void buffer(GVRRenderData renderData, float offsetFactor) {
            CommandBuffer.Command.buffer(sExecutor, renderData, offsetFactor);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final float offsetFactor = (float) params[1];
                renderData.setOffsetFactor(offsetFactor);
            }
        };
    }

    private static final class SET_RENDERING_ORDER {
        static void buffer(GVRRenderData renderData, int renderingOrder) {
            CommandBuffer.Command.buffer(sExecutor, renderData, renderingOrder);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final int renderingOrder = (int) params[1];
                renderData.setRenderingOrder(renderingOrder);
            }
        };
    }

    private static final class SET_CULL_FACE {
        static void buffer(GVRRenderData renderData, GVRRenderPass.GVRCullFaceEnum cullFace) {
            CommandBuffer.Command.buffer(sExecutor, renderData, cullFace);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final GVRRenderPass.GVRCullFaceEnum cullFace = (GVRRenderPass.GVRCullFaceEnum)params[1];
                renderData.setCullFace(cullFace);
            }
        };
    }

    private static final class SET_OFFSET_UNITS {
        static void buffer(GVRRenderData renderData, float offsetUnits) {
            CommandBuffer.Command.buffer(sExecutor, renderData, offsetUnits);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final float offsetUnits = (float) params[1];
                renderData.setOffsetUnits(offsetUnits);
            }
        };
    }

    private static final class SET_DEPTH_TEST {
        static void buffer(GVRRenderData renderData, boolean depthTest) {
            CommandBuffer.Command.buffer(sExecutor, renderData, depthTest);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final boolean depthTest = (boolean) params[1];
                renderData.setDepthTest(depthTest);
            }
        };
    }

    private static final class SET_STENCIL_TEST {
        static void buffer(GVRRenderData renderData) {
            CommandBuffer.Command.buffer(sExecutor, renderData, true);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final boolean flag = (boolean) params[1];
                renderData.setStencilTest(flag);
            }
        };
    }

    private static final class SET_STENCIL_FUNC {
        static void buffer(GVRRenderData renderData, int func) {
            CommandBuffer.Command.buffer(sExecutor, renderData, func, 1, 0xFF);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final int func = (int) params[1];
                final int ref = (int) params[2];
                final int mask = (int) params[3];
                renderData.setStencilFunc(func, ref, mask);
            }
        };
    }

    private static final class SET_STENCIL_MASK {
        static void buffer(GVRRenderData renderData) {
            CommandBuffer.Command.buffer(sExecutor, renderData, 0x00);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                final GVRRenderData renderData = (GVRRenderData) params[0];
                final int mask = (int) params[1];
                renderData.setStencilMask(mask);
            }
        };
    }

    private static final class SET_MATERIAL {
        public static void buffer(GVRRenderData renderData, GVRMaterial material) {
            CommandBuffer.Command.buffer(sExecutor, renderData, material);
        }

        private static final Command.Executor sExecutor = new Command.Executor() {
            @Override
            public void exec(Object... params) {
                GVRRenderData renderData = (GVRRenderData) params[0];
                GVRMaterial material = (GVRMaterial) params[1];
                renderData.setMaterial(material);
            }
        };
    }

    static class MaterialCache {

        static final String MATERIAL_DIFFUSE_TEXTURE = "diffuseTexture";

        int getRgbColor() {
            if (mMaterial != null) {
                return mMaterial.getRgbColor();
            }
            return 0;
        }

        public float[] getColor() {
            if (mMaterial != null) {
                return mMaterial.getColor();
            }
            return null;
        }

        public void setColor(int color) {
            if (mMaterial != null) {
                SET_COLOR.buffer(mExternalMaterial, color);
                mMaterial.setColor(color);
            }
        }

        void setColor(float r, float g, float b) {
            if (mMaterial != null) {
                SET_COLOR_RGB.buffer(mExternalMaterial, r, g, b);
                mMaterial.setColor(r, g, b);
            }
        }

        public void setTexture(GVRTexture texture) {
            if (mMaterial != null) {
                SET_TEXTURE.buffer(mExternalMaterial, texture);
                mMaterial.setMainTexture(texture);
                // Models use the new shader framework which has no single main texture
                mMaterial.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
            }
        }

        public void setTexture(String name, GVRTexture texture) {
            if (mMaterial != null) {
                SET_NAMED_TEXTURE.buffer(mExternalMaterial, name, texture);
                mMaterial.setTexture(name, texture);
            }
        }

        public float getOpacity() {
            if (mMaterial != null) {
                return mMaterial.getOpacity();
            }
            return 0;
        }

        public void setOpacity(float opacity) {
            if (mMaterial != null) {
                SET_OPACITY.buffer(mExternalMaterial, opacity);
                mMaterial.setOpacity(opacity);
            }
        }

        private MaterialCache() {

        }

        private MaterialCache(GVRMaterial material) {
            set(material);
        }

        private void set(GVRMaterial material) {
            if (material != null) {
                mMaterial = new GVRMaterial(material.getGVRContext());
                // TODO: Add named texture entry for main texture
                mMaterial.setMainTexture(material.getMainTexture());
                mMaterial.setOpacity(material.getOpacity());
                //mMaterial.setColor(material.getRgbColor());
                for (String textureName : material.getTextureNames()) {
                    mMaterial.setTexture(textureName, material.getTexture(textureName));
                }
            } else {
                mMaterial = null;
            }
            mExternalMaterial = material;
        }

        private static final class SET_COLOR {
            public static void buffer(GVRMaterial material, int color) {
                CommandBuffer.Command.buffer(sExecutor, material, color);
            }

            private static final Command.Executor sExecutor = new Command.Executor() {
                @Override
                public void exec(Object... params) {
                    final GVRMaterial material = (GVRMaterial) params[0];
                    final int color = (int) params[1];
                    material.setColor(color);
                }
            };
        }

        private static final class SET_COLOR_RGB {
            public static void buffer(GVRMaterial material, float r, float g, float b) {
                CommandBuffer.Command.buffer(sExecutor, material, r, g, b);
            }

            private static final Command.Executor sExecutor = new Command.Executor() {
                @Override
                public void exec(Object... params) {
                    final GVRMaterial material = (GVRMaterial) params[0];
                    final float r = (float) params[1];
                    final float g = (float) params[2];
                    final float b = (float) params[3];
                    material.setColor(r, g, b);
                }
            };
        }

        private static final class SET_OPACITY {
            public static void buffer(GVRMaterial material, float opacity) {
                CommandBuffer.Command.buffer(sExecutor, material, opacity);
            }

            private static final Command.Executor sExecutor = new Command.Executor() {
                @Override
                public void exec(Object... params) {
                    final GVRMaterial material = (GVRMaterial) params[0];
                    final float opacity = (float) params[1];
                    material.setOpacity(opacity);
                }
            };
        }

        private static final class SET_TEXTURE {
            public static void buffer(GVRMaterial material, GVRTexture texture) {
                CommandBuffer.Command.buffer(sExecutor, material, texture);
            }

            private static Command.Executor sExecutor = new Command.Executor() {
                @Override
                public void exec(Object... params) {
                    final GVRMaterial material = (GVRMaterial) params[0];
                    final GVRTexture texture = (GVRTexture) params[1];
                    material.setMainTexture(texture);
                    material.setTexture(MATERIAL_DIFFUSE_TEXTURE, texture);
                }
            };
        }

        private static final class SET_NAMED_TEXTURE {
            public static void buffer(GVRMaterial material, String key, GVRTexture texture) {
                CommandBuffer.Command.buffer(sExecutor, material, key, texture);
            }

            private static final Command.Executor sExecutor = new Command.Executor() {
                @Override
                public void exec(Object... params) {
                    final GVRMaterial material = (GVRMaterial) params[0];
                    final String name = (String) params[1];
                    final GVRTexture texture = (GVRTexture) params[2];
                    material.setTexture(name, texture);
                }
            };
        }

        private GVRMaterial mExternalMaterial;
        private GVRMaterial mMaterial;

    }

    private final GVRRenderData mExternalRenderData;
    private final GVRRenderData mRenderData;
    private final MaterialCache mMaterialCache;
}
