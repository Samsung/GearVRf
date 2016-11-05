package org.gearvrf.io.cursor3d;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.SparseArray;
import android.view.KeyEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.BoundingVolume;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import java.util.HashMap;

/**
 * This class defines a {@link GVRBehavior} that can be attached to a {@link GVRSceneObject}, it
 * handles all the {@link CursorEvent}s on the associated {@link GVRSceneObject} and
 * maintains the correct {@link ObjectState} based on the {@link CursorEvent}s received. This
 * class can associate a child {@link GVRSceneObject} with each {@link ObjectState} and is
 * responsible for making the correct {@link GVRSceneObject} visible according to the
 * {@link ObjectState}.
 */
public class SelectableBehavior extends GVRBehavior {
    private static final String TAG = SelectableBehavior.class.getSimpleName();
    static private long TYPE_SELECTABLE = newComponentType(SelectableBehavior.class);
    private static final String DEFAULT_ASSET_NEEDED = "Asset for Default state should be " +
            "specified";
    private GVRSwitch gvrSwitch;
    private ObjectState state = ObjectState.DEFAULT;
    private HashMap<ObjectState, Integer> stateIndexMap;
    private boolean previousActive;
    private boolean previousOver;
    private SparseArray<ObjectState> states;
    protected ObjectState currentState = ObjectState.DEFAULT;
    private StateChangedListener stateChangedListener;
    private CursorManager cursorManager;

    /**
     * Set a {@link StateChangedListener} using
     * {@link SelectableBehavior#setStateChangedListener(StateChangedListener)} to get updates about
     * the change of the state of the {@link SelectableBehavior}
     */
    public interface StateChangedListener {
        /**
         * is called whenever the state of the {@link SelectableBehavior} changes.
         *
         * @param behavior the instance of {@link SelectableBehavior} associated with the
         *                 GVRSceneObject
         * @param previous    the previous state
         * @param current current state to be set.
         * @param cursor the instance of {@link Cursor} that caused the state change
         */
        void onStateChanged(SelectableBehavior behavior, ObjectState previous, ObjectState current,
                            Cursor cursor);
    }

    /**
     * Define all possible states the {@link SelectableBehavior} can be in
     */
    public enum ObjectState {
        /**
         * The state in which the {@link SelectableBehavior} is initially. This represents the
         * normal
         * appearance of the associated {@link GVRSceneObject}
         */
        DEFAULT,
        /**
         * This state means that a {@link Cursor} is behind the associated {@link GVRSceneObject}.
         * Recommended appearance of the associated {@link GVRSceneObject} in this state is a wire
         * frame or a transparent texture, to allow the {@link Cursor} to be visible to the user.
         */
        BEHIND,
        /**
         * This state means that a
         * {@link Cursor} is intersecting with the associated {@link GVRSceneObject}
         * It is recommended that the appearance of the associated {@link GVRSceneObject} in this
         * state changes from the {@link ObjectState#DEFAULT} to make the user aware of a collision.
         */
        COLLIDING,
        /**
         * This state means that a {@link Cursor} is intersecting with the associated
         * {@link GVRSceneObject} and {@link CursorEvent#isActive} returns true. It is recommended
         * that the appearance of the associated {@link GVRSceneObject} in this state changes
         * from the {@link ObjectState#DEFAULT} to make the user aware of the clicked state.
         */
        CLICKED
    }

    /**
     * Creates a {@link SelectableBehavior} to be attached to any {@link GVRSceneObject}. The
     * instance thus created will not change the appearance of the linked {@link GVRSceneObject}
     * according to the {@link ObjectState}.
     *
     * @param cursorManager
     */
    public SelectableBehavior(CursorManager cursorManager) {
        this(cursorManager, false);
    }

    /**
     * Creates a {@link SelectableBehavior} which is to be attached to a {@link GVRSceneObject}
     * with a specific hierarchy where, the attached {@link GVRSceneObject} has a separate child
     * {@link GVRSceneObject} for each {@link ObjectState}. The order of the child nodes has to
     * follow {@link ObjectState#DEFAULT}, {@link ObjectState#BEHIND}, {@link ObjectState#COLLIDING},
     * and {@link ObjectState#CLICKED} from left to right where {@link ObjectState#DEFAULT}
     * corresponds to the first/left most child. The {@link SelectableBehavior} handles all the
     * {@link CursorEvent}s on the linked {@link GVRSceneObject} and maintains the
     * {@link ObjectState}. It also makes the correct child {@link GVRSceneObject} visible according
     * to the {@link ObjectState}. It is recommended that the different nodes representing different
     * {@link ObjectState} share a common set of vertices if possible. Not having the needed
     * hierarchy will result in an {@link IllegalArgumentException} when calling
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
    public SelectableBehavior(CursorManager cursorManager, boolean initializeAllStates) {
        this(cursorManager, initializeAllStates ? new ObjectState[]{ObjectState.DEFAULT,
                ObjectState.BEHIND, ObjectState.COLLIDING, ObjectState.CLICKED} : new ObjectState[]{
                ObjectState.DEFAULT});
    }

    /**
     * Creates a {@link SelectableBehavior} which is to be attached to a {@link GVRSceneObject}
     * with a specific hierarchy where, the {@link GVRSceneObject} to be attached has a root node at
     * the top and a child for each of the states of the {@link SelectableBehavior}.
     * The {@link SelectableBehavior} handles all the {@link CursorEvent}s on the linked
     * {@link GVRSceneObject} and maintains the {@link ObjectState}. It also makes the correct child
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
    public SelectableBehavior(CursorManager cursorManager, ObjectState[] objectStates) {
        super(cursorManager.getGVRContext());
        mType = getComponentType();
        this.cursorManager = cursorManager;
        states = new SparseArray<ObjectState>();
        stateIndexMap = new HashMap<ObjectState, Integer>();
        gvrSwitch = new GVRSwitch(cursorManager.getGVRContext());

        boolean defaultState = false;
        for (int i = 0; i < objectStates.length; i++) {
            if (objectStates[i] == ObjectState.DEFAULT) {
                defaultState = true;
            }
            stateIndexMap.put(objectStates[i], i);
        }

        if (!defaultState) {
            throw new IllegalArgumentException(DEFAULT_ASSET_NEEDED);
        }
        setState(ObjectState.DEFAULT, null);
    }

    @Override
    public void onAttach(GVRSceneObject sceneObject) {
        super.onAttach(sceneObject);
        if (stateIndexMap.size() > 1 && sceneObject.getChildrenCount() != stateIndexMap.size()) {
            throw new IllegalArgumentException("Num of children in model:" + sceneObject
                    .getChildrenCount() + " does not match the states array:" + stateIndexMap
                    .size());
        }

        if (stateIndexMap.size() > 1) {
            if(!sceneObject.attachComponent(gvrSwitch)) {
                throw new IllegalArgumentException("Cannot attach selectable behavior on a scene" +
                        " object with a GVRSwitch component");
            }
        }
        cursorManager.addSelectableObject(sceneObject);
    }

    @Override
    public void onDetach(GVRSceneObject sceneObject) {
        super.onDetach(sceneObject);
        sceneObject.detachComponent(GVRSwitch.getComponentType());
        cursorManager.removeSelectableObject(sceneObject);
    }

    /**
     * Gets the current {@link ObjectState} of the {@link SelectableBehavior}
     *
     * @return the {@link ObjectState} associated with this {@link SelectableBehavior}
     */
    public ObjectState getState() {
        return state;
    }

    private boolean isHigherOrEqualStatePresent(ObjectState targetState) {

        for (int i = 0; i < states.size(); i++) {
            if (states.get(i) != null && targetState.ordinal() <= states.get(i).ordinal()) {
                return true;
            }
        }
        return false;
    }

    private void setButtonPress(Cursor cursor) {
        int cursorId = cursor.getId();
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(ObjectState.CLICKED)) {
            currentState = ObjectState.CLICKED;
            setState(currentState, cursor);
        }
        states.put(cursorId, ObjectState.CLICKED);
    }

    private void setIntersect(Cursor cursor) {
        int cursorId = cursor.getId();
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(ObjectState.CLICKED)) {
            currentState = ObjectState.COLLIDING;
            setState(currentState, cursor);
        }
        states.put(cursorId, ObjectState.COLLIDING);
    }

    private void setWireFrame(Cursor cursor) {
        int cursorId = cursor.getId();
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(ObjectState.BEHIND)) {
            currentState = ObjectState.BEHIND;
            setState(currentState, cursor);
        }
        states.put(cursorId, ObjectState.BEHIND);
    }

    private ObjectState getHighestPriorityState() {
        ObjectState highestPriority = ObjectState.DEFAULT;
        for (int i = 0; i < states.size(); i++) {
            ObjectState state = states.get(i);

            if (state != null && state.ordinal() > highestPriority.ordinal()) {
                highestPriority = state;
            }
        }
        return highestPriority;
    }

    private void setDefault(Cursor cursor) {
        int cursorId = cursor.getId();
        states.remove(cursorId);
        ObjectState highestPriority = getHighestPriorityState();
        if (currentState != highestPriority) {
            currentState = highestPriority;
            setState(currentState, cursor);
        }
        states.put(cursorId, ObjectState.DEFAULT);
    }

    private void setState(ObjectState state, Cursor cursor) {
        ObjectState prevState = this.state;
        this.state = state;
        Integer childIndex = stateIndexMap.get(state);
        if (childIndex != null && gvrSwitch != null) {
            gvrSwitch.setSwitchIndex(childIndex);
        }
        if (prevState != this.state && stateChangedListener != null) {
            stateChangedListener.onStateChanged(this, prevState, currentState, cursor);
        }
    }

    void handleCursorEvent(CursorEvent event) {
        Cursor cursor = event.getCursor();
        float cursorDistance = getDistance(cursor.getPositionX(), cursor.getPositionY(), cursor
                .getPositionZ());
        float soDistance = getDistance(getOwnerObject());
        boolean isOver = event.isOver();
        boolean isActive = event.isActive();
        boolean isColliding = event.isColliding();
        int cursorId = event.getCursor().getId();
        KeyEvent keyEvent = event.getKeyEvent();

        ObjectState state = states.get(cursorId);
        if (state == null) {
            return;
        }
        switch (state) {
            case DEFAULT:
                if (!isOver) {
                    break;
                }
                if (isColliding) {
                    if (isActive && previousOver && !previousActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursor);
                            handleClickEvent(event);
                        }
                    } else if (!isActive) {
                        setIntersect(cursor);
                    }
                } else if (cursorDistance > soDistance) {
                    setWireFrame(cursor);
                }
                break;
            case CLICKED:
                if (isOver && isColliding) {
                    if (isActive) {
                        handleDragEvent(event);
                    } else {
                        setIntersect(cursor);
                        handleClickReleased(event);
                    }
                } else {
                    if (isActive) {
                        if (event.getCursor().getCursorType() == CursorType.OBJECT) {
                            setDefault(cursor);
                        }
                        handleCursorLeave(event);
                    } else {
                        setDefault(cursor);
                        handleClickReleased(event);
                    }
                }
                break;
            case COLLIDING:
                if (!isOver) {
                    setDefault(cursor);
                    break;
                }
                if (isColliding) {
                    if (isActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursor);
                            handleClickEvent(event);
                        }
                    }
                } else {
                    if (cursorDistance > soDistance) {
                        setWireFrame(cursor);
                    } else if (cursorDistance < soDistance) {
                        setDefault(cursor);
                    }
                }
                break;
            case BEHIND:
                if (!isOver) {
                    setDefault(cursor);
                    break;
                }
                if (isColliding) {
                    if (isActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursor);
                            handleClickEvent(event);
                        }
                    } else {
                        setIntersect(cursor);
                    }
                } else if (cursorDistance < soDistance) {
                    setDefault(cursor);
                }
                break;
        }
        previousOver = event.isOver();
        previousActive = event.isActive();
    }

    void handleClickEvent(CursorEvent event) {
    }

    void handleClickReleased(CursorEvent event) {
    }

    void handleCursorLeave(CursorEvent event) {
    }

    void handleDragEvent(CursorEvent event) {
    }

    float getDistance(GVRSceneObject object) {
        GVRTransform transform = object.getTransform();
        float x = transform.getPositionX();
        float y = transform.getPositionY();
        float z = transform.getPositionZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    float getDistance(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    void onCursorDeactivated(Cursor cursor) {
        int cursorId = cursor.getId();
        ObjectState state = states.get(cursorId);
        if (state != null) {
            states.remove(cursorId);
            if (currentState == state) {
                ObjectState highestPriorityState = getHighestPriorityState();
                setState(highestPriorityState, cursor);
            }
        }

        if (getOwnerObject().getParent() == cursor.getSceneObject()) {
            cursor.getSceneObject().removeChildObject(getOwnerObject());
        }
    }

    void onCursorActivated(Cursor cursor) {
        states.put(cursor.getId(), ObjectState.DEFAULT);
    }

    /**
     * Set the {@link StateChangedListener} to be associated with the {@link SelectableBehavior}.
     * The {@link StateChangedListener#onStateChanged(SelectableBehavior, ObjectState, ObjectState, Cursor)}
     * is called every time the state of the {@link SelectableBehavior} is changed.
     *
     * @param listener the {@link StateChangedListener}
     */
    public void setStateChangedListener(StateChangedListener listener) {
        stateChangedListener = listener;
    }

    /**
     *  Returns a unique long value Associated with the {@link SelectableBehavior} class. Each
     *  subclass of  {@link GVRBehavior} needs a unique component type value. Use this value to
     *  get the instance of {@link SelectableBehavior} attached to any {@link GVRSceneObject}
     *  using {@link GVRSceneObject#getComponent(long)}
     * @return the component type value.
     */
    public static long getComponentType() {
        return TYPE_SELECTABLE;
    }
}
