package org.gearvrf;

/**
 * A single influence of a bone on a vertex.
 */
public final class GVRBoneWeight extends GVRHybridObject {
    /**
     * Constructor.
     */
    public GVRBoneWeight(GVRContext gvrContext) {
        super(gvrContext, NativeBoneWeight.ctor());
    }

    /**
     * Sets the index of the vertex which is influenced by the bone.
     *
     * @param vertexId the vertex index
     */
    public void setVertexId(int vertexId) {
        NativeBoneWeight.setVertexId(getNative(), vertexId);
    }

    /**
     * Gets the index of the vertex which is influenced by the bone.
     *
     * @return the vertex index
     */
    public int getVertexId() {
        return NativeBoneWeight.getVertexId(getNative());
    }

    /**
     * Sets the strength of the influence in the range (0...1). <p>
     * The influence from all bones at one vertex amounts to 1.
     *
     * @param weight the influence
     */
    public void setWeight(float weight) {
        NativeBoneWeight.setWeight(getNative(), weight);
    }

    /**
     * The strength of the influence in the range (0...1).<p>
     *
     * The influence from all bones at one vertex amounts to 1
     *
     * @return the influence
     */
    public float getWeight() {
        return NativeBoneWeight.getWeight(getNative());
    }

    @Override
    public String toString() {
        return "[vid=" + getVertexId() + ", wt=" + getWeight() + "]";
    }
}

class NativeBoneWeight {
    static native long ctor();
    static native long getComponentType();
    static native void setVertexId(long ptr, int vertexId);
    static native int getVertexId(long ptr);
    static native void setWeight(long ptr, float weight);
    static native float getWeight(long ptr);
}