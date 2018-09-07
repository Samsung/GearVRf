package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMeshMorph;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.keyframe.GVRFloatAnimation;
import org.gearvrf.utility.Log;

/** 
 * Animates a set of blend shape weights over time.
 * <p>
 * The <i>target</i> of a morph animation is a {@link GVRMeshMorph}
 * component controlling a set of blend shapes used to morph a mesh
 * dynamically. Each blend shape has a weight associated with it
 * indicating what proportion of that blend shape is used to
 * morph the base mesh. This class provides an animation channel
 * for the blend weights of a mesh morph.
 * <p>
 * Each key frame for a morph animation has a time and N floating
 * point values for the N blend weights. Each frame the blend
 * weights given to the {@link GVRMeshMorph} are calculated
 * by interpolating between the key frames.
 * @see GVRMeshMorph
 * @see GVRAnimation
 */
public final class GVRMorphAnimation extends GVRAnimation implements PrettyPrint
{
    private static final String TAG = GVRMorphAnimation.class.getSimpleName();
    protected float[] mKeys;
    protected GVRFloatAnimation mKeyInterpolator;
    protected float[] mCurrentValues;

    /**
     * Creates a morph animation for the given mesh morph.
     * <p>
     * The key frames are all stored in a single contiguous
     * floating point array. Each key frame has a time followed
     * by N blend weights (where N is the key size - 1).
     * @param keyData blend weight key data.
     * @param keySize number of floats per key.
     */
    public GVRMorphAnimation(GVRMeshMorph target, float[] keyData, int keySize)
    {
        super(target, keyData[keyData.length - keySize] - keyData[0]);
        mKeys = keyData;
        mKeyInterpolator = new GVRFloatAnimation(keyData, keySize);
        mCurrentValues = new float[keySize - 1];
    }

    /**
     * Computes the blend weights for the given time and
     * updates them in the target.
     */
    public void animate(GVRHybridObject object, float animationTime)
    {
        GVRMeshMorph morph  = (GVRMeshMorph) mTarget;

        mKeyInterpolator.animate(animationTime * mDuration, mCurrentValues);
        morph.setWeights(mCurrentValues);
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRMorphAnimation.class.getSimpleName());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }
}

