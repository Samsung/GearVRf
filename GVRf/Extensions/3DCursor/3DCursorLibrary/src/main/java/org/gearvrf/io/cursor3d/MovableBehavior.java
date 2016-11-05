package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * This class defines a {@link SelectableBehavior} that can be attached to a {@link GVRSceneObject}.
 * In addition to all the functionality of a {@link SelectableBehavior} this makes the associated
 * {@link GVRSceneObject} movable with a {@link Cursor}.
 */
public class MovableBehavior extends SelectableBehavior {
    public static final String TAG = MovableBehavior.class.getSimpleName();
    static private long TYPE_MOVABLE = newComponentType(MovableBehavior.class);
    private Vector3f prevCursorPosition;
    private Quaternionf rotation;
    private Vector3f cross;

    private GVRSceneObject selected;
    private final Object selectedLock = new Object();
    private GVRSceneObject cursorSceneObject;
    private Cursor cursor;
    private CursorManager cursorManager;
    private GVRSceneObject ownerObject;
    private GVRSceneObject ownerParent;

    /**
     * Creates a {@link MovableBehavior} to be attached to any {@link GVRSceneObject}. The
     * instance thus created will not change the appearance of the linked {@link GVRSceneObject}
     * according to the {@link ObjectState}.
     *
     * @param cursorManager the {@link CursorManager} instance
     */
    public MovableBehavior(CursorManager cursorManager) {
        this(cursorManager, false);
    }

    /**
     * Creates a {@link MovableBehavior} which is to be attached to a {@link GVRSceneObject}
     * with a specific hierarchy where, the {@link GVRSceneObject} to be attached has a root node at
     * the top and a child for each of the states of the {@link MovableBehavior}.
     * The order of the child nodes has to follow {@link ObjectState#DEFAULT},
     * {@link ObjectState#BEHIND}, {@link ObjectState#COLLIDING}, and {@link ObjectState#CLICKED}
     * from left to right.The {@link MovableBehavior} handles all the {@link CursorEvent}s on the
     * linked {@link GVRSceneObject} and maintains the {@link ObjectState} as well as moving the
     * {@link GVRSceneObject} with the movement of a {@link Cursor}. It also makes the correct child
     * {@link GVRSceneObject} visible according to the {@link ObjectState}. It is recommended that
     * the different nodes representing different {@link ObjectState} share a common set of vertices
     * if possible. Not having the needed hierarchy will result in an
     * {@link IllegalArgumentException} when calling
     * {@link GVRSceneObject#attachComponent(GVRComponent)}
     *
     * @param cursorManager       the {@link CursorManager} instance
     * @param initializeAllStates flag to indicate whether to initialize all
     *                            possible {@link ObjectState}s. <code>true</code> initialize all
     *                            states in order from left to right:{@link ObjectState#DEFAULT},
     *                            {@link ObjectState#BEHIND}, {@link ObjectState#COLLIDING}, and
     *                            {@link ObjectState#CLICKED}. <code>false</code> initializes only
     *                            the {@link ObjectState#DEFAULT} state. Which does not require the
     *                            attached  {@link GVRSceneObject} to have a hierarchy.
     */
    public MovableBehavior(CursorManager cursorManager, boolean initializeAllStates) {
        super(cursorManager, initializeAllStates);
        initialize(cursorManager);
    }

    /**
     * Creates a {@link MovableBehavior} which is to be attached to a {@link GVRSceneObject}
     * with a specific hierarchy where, the {@link GVRSceneObject} to be attached has a root node at
     * the top and a child for each of the states of the {@link MovableBehavior}.
     * The {@link MovableBehavior} handles all the {@link CursorEvent}s on the linked
     * {@link GVRSceneObject} and maintains the {@link ObjectState} as well as moves the associated
     * {@link GVRSceneObject} with the {@link Cursor}. It also makes the correct child
     * {@link GVRSceneObject} visible according to the {@link ObjectState}. It is recommended to
     * have a child for each {@link ObjectState} value, however it is possible to have lesser
     * children as long as the mapping is correctly specified in {@param objectStates}. If the
     * {@param objectStates} does not match the {@link GVRSceneObject} hierarchy it will result in
     * an {@link IllegalArgumentException} when calling
     * {@link GVRSceneObject#attachComponent(GVRComponent)}. To save on memory it is suggested that
     * the children of the {@link GVRSceneObject} representing different {@link ObjectState}s share
     * a common set of vertices if possible.
     *
     * @param cursorManager the {@link CursorManager} instance
     * @param objectStates  array of {@link ObjectState}s that maps each child of the attached
     *                      {@link GVRSceneObject} with an {@link ObjectState}. Where the first
     *                      element of the array maps to the left most/first child of the attached
     *                      {@link GVRSceneObject}. This array should contain
     *                      {@link ObjectState#DEFAULT} as one of its elements else it will result
     *                      in an {@link IllegalArgumentException}.
     */
    public MovableBehavior(CursorManager cursorManager, ObjectState[] objectStates) {
        super(cursorManager, objectStates);
        initialize(cursorManager);
    }

    private void initialize(CursorManager cursorManager) {
        prevCursorPosition = new Vector3f();
        rotation = new Quaternionf();
        cross = new Vector3f();
        this.cursorManager = cursorManager;
        mType = getComponentType();
    }

    @Override
    void handleClickEvent(CursorEvent event) {
       synchronized (selectedLock) {
            if (selected != null && cursor != event.getCursor()) {
                // We have a selected object but not the correct cursor
                return;
            }

            cursor = event.getCursor();
            cursorSceneObject = event.getCursor().getSceneObject();
            prevCursorPosition.set(cursor.getPositionX(), cursor.getPositionY(), cursor
                    .getPositionZ());
            selected = getOwnerObject();
            if (cursor.getCursorType() == CursorType.OBJECT) {
                Vector3f position = new Vector3f(cursor.getPositionX(), cursor.getPositionY(),
                        cursor.getPositionZ());

                selected.getTransform().setPosition(-position.x + selected.getTransform()
                        .getPositionX(), -position.y + selected.getTransform()
                        .getPositionY(), -position.z + selected.getTransform().getPositionZ());
                ownerParent = selected.getParent();
                ownerParent.removeChildObject(selected);
                cursorSceneObject.addChildObject(selected);
            }
        }
    }

    @Override
    void handleDragEvent(CursorEvent event) {
        if (cursor.getCursorType() == CursorType.LASER && cursor == event.getCursor()) {
            Cursor cursor = event.getCursor();
            Vector3f cursorPosition = new Vector3f(cursor.getPositionX(), cursor.getPositionY
                    (), cursor.getPositionZ());
            rotateObjectToFollowCursor(cursorPosition);
            prevCursorPosition = cursorPosition;
        }
    }

    @Override
    void handleCursorLeave(CursorEvent event) {
        if (event.isActive() && cursor == event.getCursor()) {
            if (cursor.getCursorType() == CursorType.LASER) {
                Vector3f cursorPosition = new Vector3f(cursor.getPositionX(), cursor
                        .getPositionY(), cursor.getPositionZ());
                rotateObjectToFollowCursor(cursorPosition);
                prevCursorPosition = cursorPosition;
            } else if (cursor.getCursorType() == CursorType.OBJECT) {
                handleClickReleased(event);
            }
        }
    }

    @Override
    void handleClickReleased(CursorEvent event) {
        synchronized (selectedLock) {
            if (selected != null && cursor != event.getCursor()) {
                // We have a selected object but not the correct cursor
                return;
            }

            if (selected != null && cursor.getCursorType() == CursorType.OBJECT) {
                Vector3f position = new Vector3f(cursor.getPositionX(), cursor.getPositionY
                        (), cursor.getPositionZ());
                cursorSceneObject.removeChildObject(selected);
                ownerParent.addChildObject(selected);
                selected.getTransform().setPosition(+position.x + selected.getTransform()
                        .getPositionX(), +position.y + selected.getTransform()
                        .getPositionY(), +position.z + selected.getTransform().getPositionZ());
            }
            selected = null;
            // object has been moved, invalidate all other cursors to check for events
            for (Cursor remaining : cursorManager.getActiveCursors()) {
                if (cursor != remaining) {
                    remaining.invalidate();
                }
            }
        }
    }

    private void rotateObjectToFollowCursor(Vector3f cursorPosition) {
        computeRotation(prevCursorPosition, cursorPosition);
        ownerObject.getTransform().rotateWithPivot(rotation.w, rotation.x, rotation.y,
                rotation.z, 0,
                0, 0);
        ownerObject.getTransform().setRotation(1, 0, 0, 0);
    }

    /*
    formulae for quaternion rotation taken from
    http://lolengine.net/blog/2014/02/24/quaternion-from-two-vectors-final
    */
    private void computeRotation(Vector3f start, Vector3f end) {
        float norm_u_norm_v = (float) Math.sqrt(start.dot(start) * end.dot(end));
        float real_part = norm_u_norm_v + start.dot(end);

        if (real_part < 1.e-6f * norm_u_norm_v) {
        /* If u and v are exactly opposite, rotate 180 degrees
         * around an arbitrary orthogonal axis. Axis normalisation
         * can happen later, when we normalise the quaternion. */
            real_part = 0.0f;
            if (Math.abs(start.x) > Math.abs(start.z)) {
                cross = new Vector3f(-start.y, start.x, 0.f);
            } else {
                cross = new Vector3f(0.f, -start.z, start.y);
            }
        } else {
                /* Otherwise, build quaternion the standard way. */
            start.cross(end, cross);
        }
        rotation.set(cross.x, cross.y, cross.z, real_part).normalize();
    }

    /**
     *  Returns a unique long value associated with the {@link MovableBehavior} class. Each
     *  subclass of  {@link GVRBehavior} needs a unique component type value. Use this value to
     *  get the instance of {@link MovableBehavior} attached to any {@link GVRSceneObject}
     *  using {@link GVRSceneObject#getComponent(long)}
     * @return the component type value.
     */
    public static long getComponentType() {
        return TYPE_MOVABLE;
    }

    @Override
    public void onAttach(GVRSceneObject sceneObject) {
        super.onAttach(sceneObject);
        ownerObject = sceneObject;
    }

    @Override
    public void onDetach(GVRSceneObject sceneObject) {
        super.onDetach(sceneObject);
        ownerObject = null;
    }
}

