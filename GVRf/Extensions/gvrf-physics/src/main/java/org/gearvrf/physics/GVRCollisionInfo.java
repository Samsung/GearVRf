package org.gearvrf.physics;

class GVRCollisionInfo {

    public final long bodyA;
    public final long bodyB;
    public final float[] normal;
    public final float distance;
    public final boolean isHit;

    public GVRCollisionInfo(long bodyA, long bodyB, float normal[], float distance, boolean isHit) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.normal = normal;
        this.distance = distance;
        this.isHit = isHit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GVRCollisionInfo))
            return false;
        if (obj == this)
            return true;

        GVRCollisionInfo cp = (GVRCollisionInfo) obj;
        return (this.bodyA == cp.bodyA && this.bodyB == cp.bodyB);
    }
}

