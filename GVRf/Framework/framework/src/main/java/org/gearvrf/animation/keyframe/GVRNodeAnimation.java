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
package org.gearvrf.animation.keyframe;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.animation.GVRTransformAnimation;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

/**
 * Represents animation based on a sequence of key frames.
 */
public class GVRNodeAnimation extends GVRTransformAnimation implements PrettyPrint
{
    protected final String mName;
    protected final GVRAnimationChannel mChannel;

    /**
     * Constructor.
     *
     * @param name The name of the animation.
     * @param target The target object it influences.
     * @param duration Duration of the animation in seconds.
     */
    public GVRNodeAnimation(String name, GVRSceneObject target, float duration, GVRAnimationChannel channel)
    {
    	super(target.getTransform(), duration);
        mName = name;
        mChannel = channel;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRNodeAnimation.class.getSimpleName());
        sb.append("[name=" + mName + ", duration=" + getDuration());
        sb.append(System.lineSeparator());
        mChannel.prettyPrint(sb, indent + 2);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }


    public void animate(float timeInSec)
    {
        if (mChannel != null)
        {
            mChannel.animate(timeInSec, mTempMtx);
            mTransform.setModelMatrix(mTempMtx);
        }
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio)
    {
        animate(getDuration() * ratio);
    }

}
