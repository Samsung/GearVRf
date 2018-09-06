package org.gearvrf.widgetlib.widget.layout;

import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

/**
 * Layout utilities
 */
public final class LayoutHelpers {

    /**
     * Calculates the "width", in degrees, of a {@code segment} from a distance
     * of {@code radius} units. This is also referred to as the
     * "angular diameter" or the "visual angle". This is useful in determining
     * the layout of objects in a circle or ring.
     * <p>
     * It is assumed that the {@code segment} is oriented perpendicularly to the
     * radius's origin and parallel to the y-axis.
     *
     * @see <a href="http://bit.ly/1Zf7ZGJ">Angular diameter</a>
     * @see <a href="http://bit.ly/22vjbRB">Visual angle</a>
     *
     * @param segment
     *            The segment whose angular width to calculate.
     *
     * @param radius
     *            Distance between the origin and {@code item}.
     * @return The angular width of {@code item}, in degrees.
     */
    public static float calculateAngularWidth(final float segment, double radius) {
        // The item is perpendicular to the center of the origin at *its*
        // center, like a "T". The triangle, then, is between the origin,
        // the item's center, and the "edge" of the item's bounding box. The
        // length of the "opposite" side, therefore, is only half the item's
        // geometric width.
        final double opposite = segment / 2;
        final double tangent = opposite / radius; // The rho is the
                                                  // "adjacent"
                                                  // side
        final double radians = Math.atan(tangent);

        // The previous calculation only gives us half the angular width,
        // since it is reckoned from the item's center to its edge.
        return (float) Math.toDegrees(radians) * 2;
    }

    /**
     * Calculates the "width", in degrees, of a widget from a distance of
     * {@code radius} units.
     *
     * @see #calculateAngularWidth(Widget, double)
     * @param widget
     *            The {@link Widget} whose angular width to calculate.
     * @param radius
     *            Distance between the origin and {@code widget}.
     * @return The angular width of {@code widget}, in degrees.
     */
    public static float calculateAngularWidth(final Widget widget, double radius) {
        return calculateAngularWidth(widget.getWidth(), radius);
    }

    /**
     * Calculates the length of the edges of {@code item's}
     * {@linkplain GVRMesh#getBoundingBox() bounding box}. These lengths are
     * relative to the object itself, and have the object's scaling applied to
     * it. Rotations are not taken into account.
     *
     * @param item
     *            The {@link GVRSceneObject} to calculate with width for.
     * @return The dimensions of {@code item}.
     */
    public static float[] calculateGeometricDimensions(final GVRSceneObject item) {
        final GVRRenderData renderData = item.getRenderData();
        if (renderData != null) {
            final GVRMesh mesh = renderData.getMesh();
            if (mesh != null) {
                final float[] dimensions = calculateGeometricDimensions(mesh);

                GVRTransform transform = item.getTransform();

                dimensions[0] *= transform.getScaleX();
                dimensions[1] *= transform.getScaleY();
                dimensions[2] *= transform.getScaleZ();

                return dimensions;
            }
        }
        return new float[] {
                0f, 0f, 0f
        };
    }

    /**
     * Calculates the angle of an arc.
     *
     * @param arcLength
     *            Length of the arc.
     * @param radius
     *            Radius of the arc's circle.
     * @return The angle of the arc.
     */
    public static float angleOfArc(float arcLength, float radius) {
        return (float) ((arcLength * 180) / (radius * Math.PI));
    }

    /**
     * Calculates the length of the arc
     * @param angle
     *            angle of an arc
     * @param radius
     *            Radius of the arc's circle.
     * @return length of the arc
     */
    public static float lengthOfArc(float angle, float radius) {
        return  ((float)(angle * Math.PI * radius) /180);
    }


    private static float[] calculateGeometricDimensions(final GVRMesh mesh) {
        GVRMesh boundingBox = mesh.getBoundingBox();
        final float[] vertices = boundingBox.getVertices();
        return calculateGeometricDimensions(vertices);
    }

    private static float[] calculateGeometricDimensions(final float[] vertices) {
        final int numVertices = vertices.length / 3;

        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < numVertices; ++i) {
            final int offset = i * 3;
            final float x = vertices[offset];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            final float y = vertices[offset + 1];
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);

            final float z = vertices[offset + 2];
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        return new float[] {
                maxX - minX, maxY - minY, maxZ - minZ
        };
    }
}