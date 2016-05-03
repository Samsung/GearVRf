package org.gearvrf;

public final class GVRVertexBoneData implements PrettyPrint {
    @SuppressWarnings("unused")
    private GVRMesh mMesh;
    private long mNative;

    /**
     * Constructor.
     */
    public GVRVertexBoneData(GVRContext gvrContext, GVRMesh mesh) {
        mMesh = mesh; // holds a reference to mesh to avoid GC
        mNative = NativeVertexBoneData.get(mesh.getNative());
    }

    public int getFreeBoneSlot(int vertexId) {
        return NativeVertexBoneData.getFreeBoneSlot(getNative(), vertexId);
    }

    public void setVertexBoneWeight(int vertexId, int boneSlot, int boneId, float boneWeight) {
        NativeVertexBoneData.setVertexBoneWeight(getNative(), vertexId, boneSlot, boneId, boneWeight);
    }

    public void normalizeWeights() {
        NativeVertexBoneData.normalizeWeights(getNative());
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {        
    }

    private long getNative() {
        return mNative;
    }
}

class NativeVertexBoneData {
    static native long get(long nativeMesh);
    static native int getFreeBoneSlot(long nativePtr, int vertexId);
    static native void setVertexBoneWeight(long nativePtr, int vertexId, int boneSlot, int boneId, float boneWeight);
    static native void normalizeWeights(long nativePtr);
}