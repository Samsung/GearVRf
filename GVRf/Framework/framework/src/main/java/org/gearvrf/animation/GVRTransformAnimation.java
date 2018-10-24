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


package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


/**
 * Represents animation based on a sequence of key frames.
 */
public class GVRTransformAnimation extends GVRAnimation implements PrettyPrint
{
    protected final Matrix4f mTempMtx;
    protected final Vector3f mPosition = new Vector3f();
    protected final Vector3f mScale = new Vector3f();
    protected final Quaternionf mRotation = new Quaternionf();
    protected final GVRTransform mTransform;


    /**
     * Constructor.
     *
     * @param target The target object it influences.
     * @param duration Duration of the animation in seconds.
     */
    public GVRTransformAnimation(GVRTransform target, float duration)
    {
        super(target, duration);
        mTempMtx = target.getLocalModelMatrix4f();
        mScale.set(target.getScaleX(), target.getScaleY(), target.getScaleZ());
        mPosition.set(target.getPositionX(), target.getPositionY(), target.getPositionZ());
        mRotation.set(target.getRotationX(), target.getRotationY(), target.getRotationZ(), target.getRotationW());
        mTransform = target;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(org.gearvrf.animation.keyframe.GVRNodeAnimation.class.getSimpleName());
        sb.append("[duration=" + getDuration());
        sb.append(System.lineSeparator());
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    Matrix4f getCurrentMatrix()
    {
        return mTempMtx;
    }

    public void setPosition(float x, float y, float z)
    {
        mPosition.set(x, y, z);
    }

    public void setScale(float x, float y, float z)
    {
        mScale.set(x, y, z);
    }

    public void setRotation(float x, float y, float z, float w)
    {
        mRotation.set(x, y, z, w);
    }

    public void animate(float timeInSec)
    {
        mTempMtx.translationRotateScale(mPosition, mRotation, mScale);
        mTransform.setModelMatrix(mTempMtx);
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio)
    {
        animate(getDuration() * ratio);
    }

}

