package org.gearvrf.physics;


class GVRCollisionInfo {

    public long bodyA;
    public long bodyB;
    public float normal[];
    public float distance;

    public GVRCollisionInfo(long bodyA, long bodyB, float normal[], float distance){
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.normal = normal;
        this.distance = distance;
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

