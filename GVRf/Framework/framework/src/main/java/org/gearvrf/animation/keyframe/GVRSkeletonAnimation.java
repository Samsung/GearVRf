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

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRPose;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

/**
 * Animates a skeleton with separate animation channels for each bone.
 * <p>
 * Skeletal animation is performed by modify the current pose of
 * the {@link GVRSkeleton} associated with this animator.
 * Each bone of the skeleton can be driven be a separate
 * {@link GVRAnimationChannel} which contains a sequence
 * of translations, rotations and scaling to apply to the bone
 * over time.
 * <p>
 * Each frame the skeleton animation asks the animation channels
 * associated with each bone to compute the matrix for animating
 * that bone. This matrix is used to update the current pose
 * of the skeleton and the scene objects associated with the bone.
 * After all channels have been evaluated, the skinning pose is
 * computed to drive skinned meshes.
 * @see GVRSkeleton
 * @see org.gearvrf.animation.GVRSkin
 * @see GVRPose
 */
public class GVRSkeletonAnimation extends GVRAnimation implements PrettyPrint {
    protected String mName;
    private GVRSkeleton mSkeleton = null;

    /**
     * List of animation channels for each of the
     * animated bones.
     */
    protected GVRAnimationChannel[] mBoneChannels;

    /**
     * Create a skeleton animation with bones from the given hierarchy.
     *
     * @param name The name of the animation.
     * @param target The target hierachy containing scene objects for bones.
     * @param duration Duration of the animation in seconds.
     */
    public GVRSkeletonAnimation(String name, GVRSceneObject target, float duration)
    {
    	super(target, duration);
        mName = name;
    }

    /**
     * Create a skeleton animation with bones from the given hierarchy.
     *
     * @param name The name of the animation.
     * @param skel The skeleton being animated.
     * @param duration Duration of the animation in seconds.
     */
    public GVRSkeletonAnimation(String name, GVRSkeleton skel, float duration)
    {
        super(skel.getOwnerObject(), duration);
        mName = name;
        mSkeleton = skel;
        for (int boneId = 0; boneId < mSkeleton.getNumBones(); ++boneId)
        {
            mSkeleton.setBoneOptions(boneId, GVRSkeleton.BONE_ANIMATE);
        }
        mBoneChannels = new GVRAnimationChannel[mSkeleton.getNumBones()];
    }

    /**
     * Get the skeleton for this animation.
     * @return {@link GVRSkeleton} this animation drives.
     */
    public GVRSkeleton getSkeleton() { return mSkeleton; }

    /**
     * Add a channel to the animation to animate the named bone.
     * @param boneName  name of bone to animate.
     * @param channel   The animation channel.
     */
    public void addChannel(String boneName, GVRAnimationChannel channel)
    {
        int boneId = mSkeleton.getBoneIndex(boneName);
        if (boneId >= 0)
        {
            mBoneChannels[boneId] = channel;
            mSkeleton.setBoneOptions(boneId, GVRSkeleton.BONE_ANIMATE);
            Log.d("BONE", "Adding animation channel %d %s ", boneId, boneName);
        }
    }

    /**
     * Find the channel in the animation that animates the named bone.
     * @param boneName  name of bone to animate.
     */
    public GVRAnimationChannel findChannel(String boneName)
    {
        int boneId = mSkeleton.getBoneIndex(boneName);
        if (boneId >= 0)
        {
            return mBoneChannels[boneId];
        }
        return null;
    }

    private GVRSceneObject findParent(GVRSceneObject child, List<String> boneNames)
    {
        GVRSceneObject parent = child.getParent();

        if (parent == null)
        {
            return null;
        }
        String nodeName = parent.getName();
        int parBoneId = boneNames.indexOf(nodeName);

        if (parBoneId >= 0)
        {
            return parent;
        }
        return null;
    }

    /**
     * Create a skeleton from the target hierarchy which has the given bones.
     * <p>
     * The structure of the target hierarchy is used to determine bone parentage.
     * The skeleton will have only the bones designated in the list.
     * The hierarchy is expected to be connected with no gaps or unnamed nodes.
     * @param boneNames names of bones in the skeleton.
     * @return {@link GVRSkeleton} created from the target hierarchy.
     */
    public GVRSkeleton createSkeleton(List<String> boneNames)
    {
        int numBones = boneNames.size();
        GVRSceneObject root = (GVRSceneObject) mTarget;
        mSkeleton = new GVRSkeleton(root, boneNames);
        for (int boneId = 0; boneId < numBones; ++boneId)
        {
            mSkeleton.setBoneOptions(boneId, GVRSkeleton.BONE_ANIMATE);
        }
        mBoneChannels = new GVRAnimationChannel[numBones];
        return mSkeleton;
    }

    public void setSkeleton(GVRSkeleton skel, List<String> boneNames)
    {
        int numBones = skel.getNumBones();

        mSkeleton = skel;
        if (boneNames != null)
        {
            for (int boneId = 0; boneId < numBones; ++boneId)
            {
                mSkeleton.setBoneName(boneId, boneNames.get(boneId));
            }
        }
        if (mBoneChannels == null)
        {
            mBoneChannels = new GVRAnimationChannel[numBones];
        }
        else
        {
            for (int i = 0; i < mBoneChannels.length; ++i)
            {
                if (mBoneChannels[i] != null)
                {
                    skel.setBoneOptions(i, GVRSkeleton.BONE_ANIMATE);
                }
            }
        }
    }

    public void setTarget(GVRSceneObject target)
    {
        mTarget = target;
        if ((mSkeleton != null) &&
            target.getComponent(GVRSkeleton.getComponentType()) == null)
        {
            target.attachComponent(mSkeleton);
        }
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio)
    {
        animate(getDuration() * ratio);
    }

    /**
     * Compute pose of skeleton at the given time from the animation channels.
     * @param timeInSec animation time in seconds.
     */
    public void animate(float timeInSec)
    {
        GVRSkeleton skel = getSkeleton();
        GVRPose pose = skel.getPose();
        computePose(timeInSec,pose);
        skel.poseToBones();
        skel.updateBonePose();
        skel.updateSkinPose();
    }

    public GVRPose computePose(float timeInSec, GVRPose pose)
    {
        Matrix4f temp = new Matrix4f();
        GVRSkeleton skel = getSkeleton();
        Vector3f rootOffset = skel.getRootOffset();

        for (int i = 0; i < skel.getNumBones(); ++i)
        {
            GVRAnimationChannel channel = mBoneChannels[i];
            if ((channel != null) &&
                    (skel.getBoneOptions(i) == GVRSkeleton.BONE_ANIMATE))
            {
                channel.animate(timeInSec, temp);
                if (rootOffset != null)
                {
                    temp.m30(rootOffset.x + temp.m30());
                    temp.m31(rootOffset.y + temp.m31());
                    temp.m32(rootOffset.z + temp.m32());
                    rootOffset = null;
                }
                pose.setLocalMatrix(i, temp);
            }
        }

     return pose;
    }
    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRSkeletonAnimation.class.getSimpleName());
        sb.append("[name=" + mName + ", duration=" + getDuration() + ", "
                + mBoneChannels.length + " channels]");
        sb.append(System.lineSeparator());

        for (GVRAnimationChannel nodeAnim : mBoneChannels)
        {
            nodeAnim.prettyPrint(sb, indent + 2);
        }
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

}
