package org.gearvrf.animation.keyframe;

import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 
 * Describes the animation of a single node.<p>
 * 
 * The node name ({@link #getNodeName()} specifies the bone/node which is 
 * affected by this animation channel. The keyframes are given in three 
 * separate series of values, one each for position, rotation and scaling. 
 * The transformation matrix computed from these values replaces the node's 
 * original transformation matrix at a specific time.<p>
 * 
 * This means all keys are absolute and not relative to the bone default pose.
 * The order in which the transformations are applied is - as usual - 
 * scaling, rotation, translation.<p>
 */
public final class GVRAnimationChannel implements PrettyPrint {
    protected static interface Interpolator<T> {
        T interpolate(Object begin, Object end, float factor);
    }

    protected static Interpolator<Vector3f> sInterpolatorVector3f = new Interpolator<Vector3f>() {
        public Vector3f interpolate(Object begin_, Object end_, float factor) {
            Vector3f begin = (Vector3f) begin_;
            Vector3f end = (Vector3f) end_;

            return new Vector3f().set(end).sub(begin).mul(factor).add(begin);
        }
    };

    protected static Interpolator<Quaternionf> sInterpolatorQuaternion = new Interpolator<Quaternionf>() {
        public Quaternionf interpolate(Object begin_, Object end_, float factor) {
            Quaternionf begin = (Quaternionf) begin_;
            Quaternionf end = (Quaternionf) end_;

            return new Quaternionf().set(begin).slerp(end, factor);
        }
    };

    /**
     * Constructor.
     * 
     * @param nodeName name of corresponding scene graph node
     * @param numPosKeys number of position keys
     * @param numRotKeys number of rotation keys
     * @param numScaleKeys number of scaling keys
     * @param preBehavior behavior before animation start
     * @param postBehavior behavior after animation end
     */
    public GVRAnimationChannel(String nodeName, int numPosKeys, int numRotKeys, 
            int numScaleKeys, GVRAnimationBehavior preBehavior, GVRAnimationBehavior postBehavior) {

        m_nodeName = nodeName;
        mPositionKeys = new GVRPositionKey[numPosKeys];
        mRotationKeys = new GVRRotationKey[numRotKeys];
        mScaleKeys = new GVRScaleKey[numScaleKeys];
        mPreState = preBehavior;
        mPostState = postBehavior;

        mCurrentTransform = new Matrix4f();
    }


    /** 
     * Returns the name of the scene graph node affected by this animation.<p>
     * 
     * The node must exist and it must be unique.
     * 
     * @return the name of the affected node
     */
    public String getNodeName() {
        return m_nodeName;
    }


    /** 
     * Returns the number of position keys.
     * 
     * @return the number of position keys
     */
    public int getNumPosKeys() {
        return mPositionKeys.length;
    }
    
    /**
     * Returns the time component of the specified position key. 
     * 
     * @param keyIndex the index of the position key
     * @return the time component
     */
    public double getPosKeyTime(int keyIndex) {
        return mPositionKeys[keyIndex].getTime();
    }

    /**
     * Returns the position as vector.<p>
     * 
     * @param keyIndex the index of the position key
     * 
     * @return the position as vector
     */
    public Vector3f getPosKeyVector(int keyIndex) {
        return mPositionKeys[keyIndex].getValue();
    }

    public void setPosKeyVector(int keyIndex, float time, Vector3f pos) {
        mPositionKeys[keyIndex] = new GVRPositionKey(time, pos.x, pos.y, pos.z);
    }

    /** 
     * Returns the number of rotation keys.
     * 
     * @return the number of rotation keys
     */
    public int getNumRotKeys() {
       return mRotationKeys.length;
    }


    /**
     * Returns the time component of the specified rotation key. 
     * 
     * @param keyIndex the index of the position key
     * @return the time component
     */
    public double getRotKeyTime(int keyIndex) {
        return mRotationKeys[keyIndex].getTime();
    }


    /**
     * Returns the rotation as quaternion.<p>
     * 
     * 
     * @param keyIndex the index of the rotation key
     * 
     * @return the rotation as quaternion
     */
    public Quaternionf getRotKeyQuaternion(int keyIndex) {
        return mRotationKeys[keyIndex].getValue();
    } 

    public void setRotKeyQuaternion(int keyIndex, float time, Quaternionf rot) {
        mRotationKeys[keyIndex] = new GVRRotationKey(time, rot.x, rot.y, rot.z, rot.w);
    }

    /** 
     * Returns the number of scaling keys.
     * 
     * @return the number of scaling keys
     */
    public int getNumScaleKeys() {
        return mScaleKeys.length;
    }


    /**
     * Returns the time component of the specified scaling key. 
     * 
     * @param keyIndex the index of the position key
     * @return the time component
     */
    public double getScaleKeyTime(int keyIndex) {
        return mRotationKeys[keyIndex].getTime();
    }


    /**
     * Returns the scaling factor as vector.<p>
     * 
     * @param keyIndex the index of the scale key
     * 
     * @return the scaling factor as vector
     */
    public Vector3f getScaleKeyVector(int keyIndex) {
        return mScaleKeys[keyIndex].getValue();
    }

    public void setScaleKeyVector(int keyIndex, float time, Vector3f scale) {
        mScaleKeys[keyIndex] = new GVRScaleKey(time, scale.x, scale.y, scale.z);
    }

    /** 
     * Defines how the animation behaves before the first key is encountered.
     * <p>
     *
     * The default value is {@link AiAnimBehavior#DEFAULT} (the original 
     * transformation matrix of the affected node is used).
     * 
     * @return the animation behavior before the first key
     */
    public GVRAnimationBehavior getPreState() {
        return mPreState;
    }
    

    /** 
     * Defines how the animation behaves after the last key was processed.<p>
     *
     * The default value is {@link AiAnimBehavior#DEFAULT} (the original
     * transformation matrix of the affected node is taken).
     * 
     * @return the animation behavior before after the last key
     */
    public GVRAnimationBehavior getPostState() {
        return mPostState;
    }

    /**
     * Obtains the transform for a specific time in animation.
     * 
     * @param animationTime The time in animation.
     * 
     * @return The transform.
     */
    public Matrix4f animate(float animationTime) {
        Vector3f scale = getScale(animationTime);
        Vector3f pos = getPosition(animationTime);
        Quaternionf rot = getRotation(animationTime);

        // Allocation-free
        Matrix4f mat = mCurrentTransform.set(rot);

        mat.m00 *= scale.x;
        mat.m01 *= scale.x;
        mat.m02 *= scale.x;
        mat.m10 *= scale.y;
        mat.m11 *= scale.y;
        mat.m12 *= scale.y;
        mat.m20 *= scale.z;
        mat.m21 *= scale.z;
        mat.m22 *= scale.z;
        mat.m30 = pos.x;
        mat.m31 = pos.y;
        mat.m32 = pos.z;

        return mat;
    }

    protected Vector3f getPosition(float time) {
        Vector3f defaultPosition = new Vector3f();
        
        if (mPositionKeys.length == 0) {
            return defaultPosition;
        } else if (mPositionKeys.length == 1) {
            return mPositionKeys[0].getValue();
        }

        return (Vector3f) interpolate(time, mPositionKeys, sInterpolatorVector3f);
    }

    protected Vector3f getScale(float time) {
        Vector3f defaultScale = new Vector3f(1f, 1f, 1f);
        
        if (mScaleKeys.length == 0) {
            return defaultScale;
        } else if (mScaleKeys.length == 1) {
            return mScaleKeys[0].getValue();
        }

        return (Vector3f) interpolate(time, mScaleKeys, sInterpolatorVector3f);
    }

    protected Quaternionf getRotation(float time) {
        Quaternionf defaultRotation = new Quaternionf();

        if (mRotationKeys.length == 0) {
            return defaultRotation;
        } else if (mRotationKeys.length == 1) {
            return mRotationKeys[0].getValue();
        }

        return (Quaternionf) interpolate(time, mRotationKeys, sInterpolatorQuaternion);
    }

    protected Object interpolate(float time, GVRKeyFrame<?>[] keys, Interpolator<?> interpolator) {
        int index = getKeyIndex(time, keys);
        int nextIndex = index + 1;

        if (keys[index].getTime() <= time && time <= keys[nextIndex].getTime()) {
            // interpolate
            float deltaTime = (float)(keys[nextIndex].getTime() - keys[index].getTime());
            float factor = (float)((time - keys[index].getTime()) / deltaTime);

            Object start = keys[index].getValue();
            Object end = keys[nextIndex].getValue();

            return interpolator.interpolate(start, end, factor);
        } else {
            // time is out of range of animation time frame
            float firstFrameTime = keys[0].getTime();
            float lastFrameTime = keys[keys.length - 1].getTime();
            Object firstFrameValue = keys[0].getValue();
            Object lastFrameValue = keys[keys.length - 1].getValue();

            if (time <= firstFrameTime) {
                return firstFrameValue;
            } else if (time >= lastFrameTime) {
                return lastFrameValue;
            } else {
                // Shouldn't happen
                throw new RuntimeException("Interpolation failed");
            }
        }
    }

    protected int getKeyIndex(float time, GVRKeyFrame<?>[] keys) {
        for (int i = 0; i < (keys.length - 1); i++) {
            if (time < (float) keys[i + 1].getTime()) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRAnimationChannel.class.getSimpleName());
        sb.append(" [nodeName=" + m_nodeName + ", positionKeys="
                + mPositionKeys.length + ", rotationKeys="
                + mRotationKeys.length + ", scaleKeys="
                + mScaleKeys.length + ", m_preState=" + mPreState
                + ", m_postState=" + mPostState + "]");
        sb.append(System.lineSeparator());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    /**
     * Node name.
     */
    private final String m_nodeName;

    private final GVRPositionKey[] mPositionKeys;
    private final GVRRotationKey[] mRotationKeys;
    private final GVRScaleKey[] mScaleKeys;

    protected Matrix4f mCurrentTransform;

    /**
     * Pre-animation behavior.
     */
    private final GVRAnimationBehavior mPreState;

    /**
     * Post-animation behavior.
     */
    private final GVRAnimationBehavior mPostState;
}

