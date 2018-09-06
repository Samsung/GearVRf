package org.gearvrf;

import org.gearvrf.jassimp.AiWrapperProvider;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A single bone of a mesh.<p>
 *
 * A bone has a name by which it can be found in the frame hierarchy and by
 * which it can be addressed by animations. In addition it has a number of
 * influences on vertices. <p>
 *
 * This class is designed to be mutable, i.e., the returned collections are
 * writable and may be modified. <p>
 *
 * Instantaneous pose of the bone during animation is not part of this class,
 * but in {@code GVRSkeleton}. This allows multiple instances of the same mesh
 * and bones.
 */
public final class GVRBone extends GVRComponent implements PrettyPrint {
    /**
     * Constructor.
     */
    public GVRBone(GVRContext gvrContext) {
        super(gvrContext, NativeBone.ctor());
    }

    static public long getComponentType() {
        return NativeBone.getComponentType();
    }


    public void onAttach(GVRSceneObject owner)
    {
        String name = getName();

        if (!"".equals(name))
        {
            NativeBone.setName(getNative(), name);
        }
    }
    public void setBoneId(int id)
    {
        mBoneId = id;
    }

    public int getBoneId() { return mBoneId; }

    /**
     * Returns the name of the bone.
     *
     * @return the name
     */
    public String getName()
    {
        GVRSceneObject owner = getOwnerObject();
        String name = "";

        if (owner != null)
        {
            name = owner.getName();
            if (name == null)
                return "";
        }
        return name;
    }

    public void setOffsetMatrix(float[] offsetMatrix)
    {
        NativeBone.setOffsetMatrix(getNative(), offsetMatrix);
    }

    /**
     * Sets the final transform of the bone during animation.
     *
     * @param finalTransform The transform matrix representing
     * the bone's pose after computing the skeleton.
     */
    public void setFinalTransformMatrix(float[] finalTransform)
    {
        NativeBone.setFinalTransformMatrix(getNative(), finalTransform);
    }

    /**
     * Sets the final transform of the bone during animation.
     *
     * @param finalTransform The transform matrix representing
     * the bone's pose after computing the skeleton.
     */
    public void setFinalTransformMatrix(Matrix4f finalTransform)
    {
        float[] mat = new float[16];
        finalTransform.get(mat);
        NativeBone.setFinalTransformMatrix(getNative(), mat);
    }

    /**
     * Gets the final transform of the bone.
     *
     * @return the 4x4 matrix representing the final transform of the
     * bone during animation, which comprises bind pose and skeletal
     * transform at the current time of the animation.
     */
    public Matrix4f getFinalTransformMatrix()
    {
        final FloatBuffer fb = ByteBuffer.allocateDirect(4*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        NativeBone.getFinalTransformMatrix(getNative(), fb);
        return new Matrix4f(fb);
    }

    /**
     * Returns the offset matrix.<p>
     *
     * The offset matrix is a 4x4 matrix that transforms from mesh space to
     * bone space in bind pose.<p>
     *
     * This method is part of the wrapped API (see {@link AiWrapperProvider}
     * for details on wrappers).
     *
     * @return the offset matrix
     */
    public Matrix4f getOffsetMatrix()
    {
        Matrix4f offsetMatrix = new Matrix4f();
        offsetMatrix.set(NativeBone.getOffsetMatrix(getNative()));
        return offsetMatrix;
    }

    /**
     * Return the offset matrix as a float array.
     *
     * @return the offset matrix as a float array.
     */
    public float[] getOffsetMatrixFloatArray() {
        return NativeBone.getOffsetMatrix(getNative());
    }


    /**
     * Pretty-print the object.
     */
    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRBone.class.getSimpleName());
        sb.append(" [name=" + getName() + ", boneId=" + getBoneId()
                + ", offsetMatrix=" + getOffsetMatrix()
                + ", finalTransformMatrix=" + getFinalTransformMatrix() // crashes debugger
                + "]");
        sb.append(System.lineSeparator());
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    private int mBoneId;    // ID of bone in skeleton

}

class NativeBone {
    static native long ctor();
    static native long getComponentType();
    static native void setName(long object, String mName);
    static native void setOffsetMatrix(long object, float[] offsetMatrix);
    static native float[] getOffsetMatrix(long object);
    static native void setFinalTransformMatrix(long object, float[] offsetMatrix);
    static native void getFinalTransformMatrix(long object, FloatBuffer output);
}