package org.gearvrf.animation.keyframe;

import org.gearvrf.GVRMeshMorph;
import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;

/** 
 * Describes the animation of a set of floating point values.
 */
public final class GVRMorphController implements PrettyPrint
{
    private static final String TAG = GVRMorphController.class.getSimpleName();
    protected float[] mKeys;
    protected GVRFloatAnimation mKeyInterpolator;
    protected float[] mCurrentValues;

    /**
     * Constructor.
     *
     * @param keyData blend weight keys
     * @param keySize number of floats per key
     */
    public GVRMorphController(GVRMeshMorph target, float[] keyData, int keySize)
    {
        mKeys = keyData;
        mKeyInterpolator = new GVRFloatAnimation(keyData, keySize);
        mCurrentValues = new float[keySize - 1];
    }

    public void animate(float animationTime)
    {
        mKeyInterpolator.animate(animationTime, mCurrentValues);
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRMorphController.class.getSimpleName());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }
}

