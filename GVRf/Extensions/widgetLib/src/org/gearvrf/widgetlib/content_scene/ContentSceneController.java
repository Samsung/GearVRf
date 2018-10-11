package org.gearvrf.widgetlib.content_scene;

import java.util.LinkedList;

import org.gearvrf.GVRContext;

import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.thread.MainThread;
import org.gearvrf.widgetlib.thread.ExecutionChain;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.FlingHandler;
import org.gearvrf.widgetlib.widget.TouchManager;

import static org.gearvrf.utility.Log.tag;

/**
 * ContentSceneController manages the content scenes life cycle. Each content scene is the set of UI
 * elements potentially interacting with each other.  All content scenes are arranged in a stack in
 * the order in which each content scene is shown. Content scenes in the stack are never rearranged,
 * only pushed and popped from the stack—pushed onto the stack when shown up and popped off when back
 * action is executed. As such, the back stack operates as a "last in, first out" object structure.
 *
 * This is the list of commands to manage the content scenes :
 * {@link #goTo} Move the current content scene, if any running, to invisible state and show new
 * content scene, push it to the stack if it is not yet or run {@link #goBackTo} to roll stack back
 * until the specified content scene is reached.
 *
 * {@link #goBack} current content scene is finished and popped off the stack.
 *
 * {@link #goBackTo} current content scene is finished and the stack rolled back until the specified
 * content scene is reached.
 *
 * Every time new content scene goes, {@link ContentScene#hide()} is called for the currently
 * running content scene to move it to invisible state. It is up to content scene to retain
 * its state (such as scroll position and Widget focus, etc); if {@link #goBack} is executed the
 * content scene should be able to restore its state if needed. Then, {@link ContentScene#show()} is
 * called for new content scene. It is up to content scene to make all preparation for showing the
 * content scene content.
 */
public class ContentSceneController {

    /**
     * An entity that can be subjected to management by the
     * ContentSceneController
     */
    public interface ContentScene {
        /**
         * Make this content scene visible and interact-able by the user
         * It is a good place to restore the content scene state (such as scroll position and Widget
         * focus, etc) if it was retained before in {@link #hide()}.
         */
        void show();

        /**
         * Make this content scene invisible and un-interact-able by the user
         * It is a good place to retain the content scene state (such as scroll position and Widget
         * focus, etc) to restore its later when {@link #show()} is called.
         */
        void hide();

        /**
         * Callback on removing system dialog. The content scene behaviour might be changed: like
         * widget focus, ability to interact with widgets in content scene and etc..
         */
        void onSystemDialogRemoved();

        /**
         * Callback on posting system dialog. The content scene behaviour might be changed: like
         * widget focus, ability to interact with widgets in content scene and etc..
         */
        void onSystemDialogPosted();

        /**
         * Callback on proximity changed. Some layout/services rearrangement might be required to
         * resume the work.
         * @param onProximity TRUE if proximity sensor detects the headset is on the face and all
         *                             app services should be resumed
         *                    FALSE if proximity sensor detects the headset is off the face and all
         *                             app services should be paused
         */
        void onProximityChange(boolean onProximity);

        /**
         * Get the content scene name
         */
        String getName();

        /**
         * Return {@link FlingHandler}. It might be null if fling is not supported by content scene
         * @return
         */
        FlingHandler getFlingHandler();
    }

    /**
     * Creates ContentSceneController
     * @param gvrContext
     */
    public ContentSceneController(GVRContext gvrContext) {
        mGvrContext = gvrContext;
    }

    /**
     * Move to the new content scene. Move the current content scene, if any running, to invisible
     * state (see {@link ContentScene#hide()}); push the new content scene to the stack if it is not
     * yet; or run {@link #goBackTo} to roll stack back until the specified content scene is reached,
     * then move new content scene to visible state (see {@link ContentScene#show()})
     *
     * @param contentScene new content scene
     */
    public void goTo(final ContentScene contentScene) {
        Log.d(TAG, "Go to %s", contentScene.getName());

        if (!goBackTo(contentScene)) {
            mContentSceneViewStack.push(contentScene);
        }

        executeHideShowCycle(contentScene);
    }

    /**
     * Go back to the previous stacked content scene.
     * The currently active content scene is popped from the stack and moved to invisible state.
     * (see {@link ContentScene#hide()}). The next content scene in the stack is moved to visible
     * state (see {@link ContentScene#show()}).
     * if you continue to run {@link #goBack}, then each content scene in the stack is popped off to
     * reveal the previous one, until the last one is left in the stack.
     */
    public void goBack() {
        if (mContentSceneViewStack.size() < 2) {
            return;
        }

        mContentSceneViewStack.pop();

        Log.d(TAG, "Go back to %s", mContentSceneViewStack.peek().getName());
        executeHideShowCycle(mContentSceneViewStack.peek());
    }

    public void refreshFlingHandler() {
        new ExecutionChain(mGvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!mContentSceneViewStack.isEmpty()) {
                    WidgetLib.getTouchManager().setFlingHandler(
                            mContentSceneViewStack.peek().getFlingHandler());
                }
            }
        }).execute();
    }

    // Internal API: only used by goTo() to go to an exiting contentScene in
    // stack
    private boolean goBackTo(final ContentScene contentScene) {
        final int index;

        if (mContentSceneViewStack.isEmpty()
                || (index = mContentSceneViewStack.indexOf(contentScene)) == -1) {
            return false;
        }

        for (int i = 0; i < index; ++i) {
            mContentSceneViewStack.pop();
        }

        return true;
    }

    /**
     * Called on system pause.  The current active content scene moves to invisible state -- see
     * {@link ContentScene#hide()}
     */
    public void pause() {
        new ExecutionChain(mGvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!mContentSceneViewStack.isEmpty()) {
                    mContentSceneViewStack.peek().hide();
                    WidgetLib.getTouchManager().setFlingHandler(null);
                }
            }
        }).execute();
    }

    /**
     * Called on system resume.  The top content scene in theh stack moves to visible state -- see
     * {@link ContentScene#show()}
     */
    public void resume() {
        new ExecutionChain(mGvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!mContentSceneViewStack.isEmpty()) {
                    ContentScene contentScene = mContentSceneViewStack.peek();
                    contentScene.show();
                    WidgetLib.getTouchManager().setFlingHandler(
                            contentScene.getFlingHandler());

                }
            }
        }).execute();
    }

    /**
     * Callback on removing system dialog. It calls {@link ContentScene#onSystemDialogRemoved()}
     * for the currently active content scene
     */
    public void onSystemDialogRemoved() {
        if (null != curContentScene) {
            curContentScene.onSystemDialogRemoved();
        }
    }

    /**
     * Callback on posting system dialog. It calls {@link ContentScene#onSystemDialogPosted()}
     * for the currently active content scene
     */
    public void onSystemDialogPosted() {
        if (null != curContentScene) {
            curContentScene.onSystemDialogPosted();
        }
    }

    /**
     * Callback on proximity changed. It calls {@link ContentScene#onProximityChange} for the
     * currently active content scene
     * @param onProximity TRUE if proximity sensor detects the headset is on the face and all
     *                             app services should be resumed
     *                    FALSE if proximity sensor detects the headset is off the face and all
     *                             app services should be paused
     */
    public void onProximityChange(final boolean onProximity) {
        new ExecutionChain(mGvrContext).runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!mContentSceneViewStack.isEmpty()) {
                    mContentSceneViewStack.peek().onProximityChange(onProximity);
                }
            }
        }).execute();
    }

    /**
     * Execute a task in the {@linkplain MainThread#runOnMainThread(Runnable)
     * main thread} and {@link ContentScene#hide() hide()} has been called on
     * the {@link ContentScene} at the top of the view stack (if there is one).
     * <p>
     * The task's {@link Runnable#run() run()} method may freely manipulate the
     * {@code ContentScene} view stack.
     * <p>
     * After the task has been run, if there is a {@code ContentScene} on the
     * view stack, {@code drawFrameListener} will be re-registered and
     * {@link ContentScene#show() show()} will be called on the
     * {@code ContentScene}.
     *
     * @param contentScene
     *            The {@link Runnable} to execute.
     */
    private void executeHideShowCycle(final ContentScene contentScene) {
        // When there is no ongoing show-hide execution chain, create a new one
        if (nextContentScene == null) {
            nextContentScene = contentScene;
            new ExecutionChain(mGvrContext).runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ContentScene localNextContentScene = null;

                    // Recursively execute hide-show cycle until stablized
                    while (nextContentScene != localNextContentScene) {
                        localNextContentScene = nextContentScene;

                        if (curContentScene == localNextContentScene) {
                            Log.d(TAG,
                                    "skip the same scene to show %s",
                                    curContentScene.getName());
                            break;
                        }

                        // Hide current contentScene
                        if (curContentScene != null) {
                            Log.d(TAG,
                                    "executeHideShowCycle(): hiding %s",
                                    curContentScene.getName());
                            curContentScene.hide();
                            WidgetLib.getTouchManager().setFlingHandler(null);
                        }

                        // Show next contentScene
                        if (localNextContentScene != null) {
                            Log.d(TAG,
                                    "executeHideShowCycle(): showing %s",
                                    localNextContentScene.getName());
                            localNextContentScene.show();
                            WidgetLib.getTouchManager().setFlingHandler(
                                    localNextContentScene.getFlingHandler());

                            curContentScene = localNextContentScene;
                        }
                    }

                    nextContentScene = null;
                }
            }).execute();
        } else {
            nextContentScene = contentScene;
        }
    }

    private ContentScene curContentScene = null;
    private ContentScene nextContentScene = null;

    private final LinkedList<ContentScene> mContentSceneViewStack = new LinkedList<ContentScene>();

    @SuppressWarnings("unused")
    private void printContentSceneViewStack() {
        Log.d(TAG, "Print ContentSceneViewStack from top to bottom: ");
        for (int i = 0; i < mContentSceneViewStack.size(); i++) {
            Log.d(TAG, "        Level %d: %s", i, mContentSceneViewStack.get(i)
                    .getName());
        }
    }

    /**
     * Modifications to the {@code ContentScene} view stack must always be done:
     * <ul>
     * <li>On the {@linkplain MainThread#runOnMainThread(Runnable) main thread}</li>
     * </ul>
     * In other words, only modify from the {@link Runnable#run() run()} method
     * of a task passed to {@link #executeHideShowCycle}.
     */
    private GVRContext mGvrContext;

    private static final String TAG = tag(ContentSceneController.class);
}
