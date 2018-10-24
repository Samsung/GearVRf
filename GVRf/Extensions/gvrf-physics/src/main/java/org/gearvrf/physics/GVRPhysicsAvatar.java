
/* Copyright 2018 Samsung Electronics Co., LTD
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
package org.gearvrf.physics;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVRSkeleton;

import java.io.IOException;

public class GVRPhysicsAvatar extends GVRAvatar
{
    protected GVRWorld.IPhysicsEvents mPhysicsListener = new GVRWorld.IPhysicsEvents()
    {
        @Override
        public void onAddRigidBody(GVRWorld world, GVRRigidBody body)
        {

        }

        @Override
        public void onRemoveRigidBody(GVRWorld world, GVRRigidBody body)
        {

        }

        @Override
        public void onStepPhysics(GVRWorld world)
        {
            if (mSkeleton != null)
            {
                mSkeleton.poseFromBones(GVRSkeleton.BONE_PHYSICS);
            }
        }
    };

    public GVRPhysicsAvatar(GVRContext ctx, String name)
    {
        super(ctx, name);
    }

    /**
     * Load physics information for the current avatar
     * @param filename  name of physics file
     * @param scene     scene the avatar is part of
     * @throws IOException if physics file cannot be parsed
     */
    public void loadPhysics(String filename, GVRScene scene) throws IOException
    {
        GVRPhysicsLoader.loadPhysicsFile(getGVRContext(), filename, true, scene);
    }
};
