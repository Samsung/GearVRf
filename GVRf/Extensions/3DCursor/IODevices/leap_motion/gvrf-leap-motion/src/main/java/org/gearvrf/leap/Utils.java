package org.gearvrf.leap;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Utils {
    private static final Vector3f FORWARD_VECTOR = new Vector3f(0f, 0f, -1f);
    private static final Vector3f UP_VECTOR = new Vector3f(0f, 1f, 0f);
    private static Vector3f forward = new Vector3f();
    private static Vector3f up = new Vector3f();

    /**
     * This method is a clone of {@link Quaternionf#lookRotate(Vector3f, Vector3f)} with the only
     * difference that it works along the negative Z axis.
     *
     * Apply a rotation to this quaternion that maps the given direction to the negative Z axis,
     * and store the result in <code>dest</code>.
     * <p>
     * Because there are multiple possibilities for such a rotation, this method will choose the
     * one that ensures the given up direction to remain
     * parallel to the plane spanned by the <tt>up</tt> and <tt>dir</tt> vectors.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion
     * representing the
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when
     * transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * <p>
     * Reference:
     * <a href="http://answers.unity3d.com/questions/467614/what-is-the-source-code-of-quaternionlookrotation.html">http://answers.unity3d.com</a>
     *
     * @param dirX the x-coordinate of the direction to look along
     * @param dirY the y-coordinate of the direction to look along
     * @param dirZ the z-coordinate of the direction to look along
     * @param upX  the x-coordinate of the up vector
     * @param upY  the y-coordinate of the up vector
     * @param upZ  the z-coordinate of the up vector
     * @param dest will hold the result
     * @return dest
     */
    public static Quaternionf lookRotate(float dirX, float dirY, float dirZ, float upX, float upY,
                                         float upZ, Quaternionf dest) {
        // Normalize direction
        float invDirLength = (float) (1.0 / Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ));
        float dirnX = dirX * invDirLength;
        float dirnY = dirY * invDirLength;
        float dirnZ = dirZ * invDirLength;
        // left = up x dir
        float leftX, leftY, leftZ;
        leftX = upY * dirnZ - upZ * dirnY;
        leftY = upZ * dirnX - upX * dirnZ;
        leftZ = upX * dirnY - upY * dirnX;
        // normalize left
        float invLeftLength = (float) (1.0 / Math.sqrt(leftX * leftX + leftY * leftY + leftZ *
                leftZ));
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirnY * leftZ - dirnZ * leftY;
        float upnY = dirnZ * leftX - dirnX * leftZ;
        float upnZ = dirnX * leftY - dirnY * leftX;

        /* Convert orthonormal basis vectors to quaternion */
        float x, y, z, w;
        double t;
        double tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = Math.sqrt(tr + 1.0);
            w = (float) (t * 0.5);
            t = 0.5 / t;
            x = (float) ((upnZ - dirnY) * t);
            y = (float) ((dirnX - leftZ) * t);
            z = (float) ((leftY - upnX) * t);
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = Math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (float) (t * 0.5);
                t = 0.5 / t;
                y = (float) ((leftY + upnX) * t);
                z = (float) ((dirnX + leftZ) * t);
                w = (float) ((upnZ - dirnY) * t);
            } else if (upnY > dirnZ) {
                t = Math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((leftY + upnX) * t);
                z = (float) ((upnZ + dirnY) * t);
                w = (float) ((dirnX - leftZ) * t);
            } else {
                t = Math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((dirnX + leftZ) * t);
                y = (float) ((upnZ + dirnY) * t);
                w = (float) ((leftY - upnX) * t);
            }
        }
        dest.w = w;
        dest.x = x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    public static Quaternionf matrixRotation(Matrix4f matrix, Quaternionf rotation,
                                             Quaternionf dest) {
        rotation.transform(FORWARD_VECTOR, forward);
        rotation.transform(UP_VECTOR, up);
        forward.mulDirection(matrix);
        up.mulDirection(matrix);
        return lookRotate(forward.x, forward.y, forward.z, up.x, up.y, up.z, dest);
    }

}
