package org.gearvrf.physics;

/**
 * The Collision Matrix defines the collision between groups of {@link GVRRigidBody}. A group
 * of collision is a number between 0 and 15 that is a index in the collision matrix.
 * A {@link GVRRigidBody} that does not belong to a collision group collides with everyone
 * rigid body of the {@link GVRWorld}. By default a {@link GVRRigidBody} that belongs to a group
 * collides only with other rigid body of the same collision group.
 */
public class GVRCollisionMatrix {
    // By default a rigid body that belongs to a group collides only with other rigid
    // body of the same group.
    private int[] mCollisionFilterMasks =  {
            1 << 0, 1 << 1, 1 << 2, 1 << 3,
            1 << 4, 1 << 5, 1 << 6, 1 << 7,
            1 << 8, 1 << 9, 1 << 10, 1 << 11,
            1 << 12, 1 << 13, 1 << 14, 1 << 15
    };

    /**
     * Creates a new Collision Matrix.
     */
    public GVRCollisionMatrix() {
    }

    /**
     * @param groupId A value between 0 and 15 that is a index in the collision matrix.
     * @return (1 << groupId) a short value multiple of 2 that is the internal filter for {#groupId}.
     */
    public static short getCollisionFilterGroup(int groupId) {
        return (short) (1 << groupId);
    }

    /**
     * @param groupId A value between 0 and 15 that is a index in the collision matrix.
     * @return Returns a mask to filter all groups that {#groupId} collides with.
     */
    public short getCollisionFilterMask(int groupId) {
        if (groupId < 0 || groupId > 15) {
            throw new IllegalArgumentException("Group id must be a value between 0 and 15");
        }

        return (short) mCollisionFilterMasks[groupId];
    }

    /**
     * @param groupId A value between 0 and 15 that is a index in the collision matrix.
     * @param mask Mask to filter all groups that {#groupId} collides with.
     */
    public void setCollisionFilterMask(int groupId, short mask) {
        if (groupId < 0 || groupId > 15) {
            throw new IllegalArgumentException("Group id must be a value between 0 and 15");
        }

        mCollisionFilterMasks[groupId] = mask;
    }


    /**
     * Enable the collision between two group of collision.
     * @param groupA id of the first group to collide with {#groupB}
     * @param groupB id of the second group to collide with {#groupA}
     */
    public void enableCollision(int groupA, int groupB) {
        setCollision(groupA, groupB, true);
    }

    /**
     * Disable the collision between two group of collision.
     * @param groupA id of the first group to disable the collision with {#groupB}
     * @param groupB id of the second group to disable the collision with {#groupA}
     */
    public void disableCollision(int groupA, int groupB) {
        setCollision(groupA, groupB, false);
    }

    private void setCollision(int groupA, int groupB, boolean enabled) {
        if (groupA < 0 || groupA > 15 || groupB < 0 || groupB > 15) {
            throw new IllegalArgumentException("Group id must be a value between 0 and 15");
        }

        if (enabled) {
            mCollisionFilterMasks[groupA] |= getCollisionFilterGroup(groupB);
            mCollisionFilterMasks[groupB] |= getCollisionFilterGroup(groupA);
        } else {
            mCollisionFilterMasks[groupA] &= -getCollisionFilterGroup(groupB) - 1;
            mCollisionFilterMasks[groupB] &= -getCollisionFilterGroup(groupA) - 1;
        }
    }
}
