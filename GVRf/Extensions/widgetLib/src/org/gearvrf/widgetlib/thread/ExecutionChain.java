package org.gearvrf.widgetlib.thread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.gearvrf.GVRContext;

import android.app.Activity;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;

import static org.gearvrf.utility.Threads.spawn;

/**
 * Utility for ordering operations that will be executed on multiple threads,
 * but need to be run sequentially, not concurrently.
 * <p>
 * For example:
 * <ul>
 * <li>Background thread: load bitmaps</li>
 * <li>GL thread: create GVRBitmapTextures for those bitmaps</li>
 * <li>Main thread: non-trivial set-up of quads that use the textures</li>
 * </ul>
 * <p>
 * Using {@code ExecutionChain} this would be structured as:
 * <p>
 *
 * <pre>
 * {@code
 *  new ExecutionChain(gvrContext)
 *      .runOnBackgroundThread(bitmapLoadingTask)
 *      .runOnGLThread(bitmapTextureTask)
 *      .runOnMainThread(quadSetupTask)
 *      .execute();
 * }
 * </pre>
 * <p>
 * The chain of execution can be {@linkplain #cancel() cancelled} at any time (a
 * {@link Runnable} can optionally be called when the cancellation has
 * completed). At a minimum, execution will halt as soon as the current task has
 * finished running. {@linkplain Task Tasks} can support finer-grained
 * cancellation by intermittently checking {@link Task#isCancelled()} and
 * returning sooner.
 * <p>
 * When a task throws an exception from run(), the exception is caught and
 * saved. If the next task in the chain of execution calls
 * {@link Task#getResult() getResult()}, the exception will be re-thrown. In
 * this way, the exception will be propagated through the chain of execution
 * until it is handled or until the chain is done executing. The next task can
 * check for an exception in the previous task by called
 * {@link Task#hasException()}, and can get the exception using
 * {@link Task#getException()}. Calling {@code getException()} clears the
 * exception state: calling {@link Task#getResult()} on the previous task will
 * <em>not</em> re-throw the exception and responsibility for handling the
 * exception now falls on the current task. An important thing to note is that
 * if the task after a throwing task doesn't call {@code Task#getResult()} or
 * handle the exception explicitly, the exception is "dropped".
 * <p>
 * If there is a need to avoid dropped exceptions, an {@link ErrorCallback} can
 * be {@link #setErrorCallback(ErrorCallback) installed}. It will be called for
 * any exceptions that are thrown and can process the exception in any way
 * necessary; if appropriate, the callback can {@link #cancel() stop further
 * execution}.
 * <p>
 * {@code ExecutionChain} supports running tasks on the
 * {@link Activity#runOnUiThread(Runnable) UI thread}, {@link MainThread main
 * thread}, {@link GVRContext#runOnGlThread(Runnable) OpenGL thread}, and on
 * {@link org.gearvrf.utility.Threads#spawn(Runnable) background threads}.
 */
public class ExecutionChain {
    public enum State {
        /**
         * The chain of execution is not currently running. {@link Task Tasks}
         * can be added to it and it can be {@link ExecutionChain#execute()
         * started}.
         */
        STOPPED,
        /**
         * The chain of execution is currently running. It may be
         * {@link ExecutionChain#cancel() cancelled}, but no further
         * {@link Task tasks} can be added to it.
         */
        RUNNING,
        /**
         * The chain of execution was running, but has been cancelled.
         * Otherwise, this is the same as {@link #STOPPED}.
         */
        CANCELLED,
        /**
         * The chain of execution was running, but was halted due to an
         * exception being thrown by one of the tasks. Otherwise, this is the
         * same as {@link #STOPPED}.
         */
        ERROR
    }

    /**
     * Interface to implement for handling exceptions thrown while
     * {@code ExecutionChain} is running.
     */
    public interface ErrorCallback {
        /**
         * Called when an exception is thrown by a running task. If the
         * exception is handled completely, return {@code true}, and the current
         * task's exception state will remain clear. To set the current task's
         * exception state and propagate the exception further, return
         * {@code false}.
         *
         * @param t
         *            The exception that was thrown
         * @param task
         *            The {@link Task} that threw the exception
         * @return Return {@code true} if the exception was completely handled,
         *         or {@code false} if it was not.
         */
        boolean onException(Throwable t, Task<?, ?> task);
    }

    /**
     * Extend and implement {@link #run(Task)} to create tasks to run on
     * {@code ExecutionChain}.
     *
     * @param <T>
     *            The type of the value returned by {@link #run(Task)}.
     * @param <U>
     *            The expected type of the value returned by
     *            {@link #getResult()} from the {@code Task} passed to
     *            {@link #run(Task)}.
     */
    static public abstract class Task<T, U> {
        // TODO: Thinking about adding support for naming tasks
        /**
         * Override this method to implement your task. The Task that was run
         * immediately prior to this one is passed so that you can get its
         * result or check its exception state.
         *
         * @param task
         *            The {@link Task} that was run immediately before this one.
         * @return Return the result of this task.
         */
        public abstract T run(Task<U, ?> task);

        /**
         * Get the result of this task's execution.
         * <p>
         * If an exception was thrown during the task's execution and has not
         * been cleared with {@link #getException()}, it will be wrapped in a
         * {@link RuntimeException} and re-thrown by this method.
         * <p>
         * The task's exception state is cleared by this method.
         *
         * @return Returns the task's result.
         */
        final public T getResult() {
            Throwable t = getException();
            if (t != null) {
                throw new RuntimeException(t);
            }
            return result;
        }

        /**
         * @return Returns {@code true} if the chain of execution has been
         *         cancelled by a call to {@link ExecutionChain#cancel()
         *         cancel()} or {@link ExecutionChain#cancel(Runnable)
         *         cancel(Runnable)}. Returns {@code false} otherwise.
         */
        final public boolean isCancelled() {
            return state.get() == State.CANCELLED;
        }

        /**
         * @return Returns {@code true} if an exception was throw during the
         *         task's execution and has not been cleared by
         *         {@link #getException()}, {@code false} otherwise.
         */
        final public boolean hasException() {
            return exception != null;
        }

        /**
         * Get an exception thrown during the task's execution, if any. Calling
         * this method clears the exception state on this task:
         * {@link #hasException()} will return {@code false},
         * {@link #getResult()} will not throw, and subsequent calls to this
         * method will return {@code null}. Further handling of the exception is
         * the responsibility of the caller.
         *
         * @return An exception if any was thrown during the task's execution.
         */
        final public Throwable getException() {
            Throwable t = exception;
            exception = null;
            return t;
        }

        private void execute(Task<U, ?> task, AtomicReference<State> state,
                ErrorCallback errorCallback) {
            this.state = state;
            try {
                result = run(task);
            } catch (Throwable runError) {
                boolean handled = false;
                if (errorCallback != null) {
                    try {
                        handled = errorCallback.onException(runError, task);
                    } catch (Throwable callbackError) {
                        callbackError.printStackTrace();
                        Log.e(TAG, callbackError, "execute()");
                    }
                }
                if (!handled) {
                    exception = runError;
                    runError.printStackTrace();
                    Log.e(TAG, runError, "execute()");
                }
            }
            this.state = null;
        }

        private T result;
        private Throwable exception;
        private AtomicReference<State> state;
    }

    /**
     * Construct a new ExecutionChain.
     *
     * @param gvrContext
     *            A valid GVRContext.
     */
    public ExecutionChain(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
    }

    /**
     * Add a {@link Task} to be run on Android's
     * {@link Activity#runOnUiThread(Runnable) UI thread}. It will be run after
     * all Tasks added prior to this call.
     *
     * @param task
     *            {@code Task} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnUIThread(Task<T, U> task) {
        runOnThread(Context.Type.UI, task);
        return this;
    }

    /**
     * Add a {@link Task} to be run on the
     * {@link MainThread#runOnMainThread(Runnable) main thread}. It will be run
     * after all Tasks added prior to this call.
     *
     * @param task
     *            {@code Task} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnMainThread(Task<T, U> task) {
        runOnThread(Context.Type.MAIN, task);
        return this;
    }

    /**
     * Add a {@link Task} to be run on the
     * {@link GVRContext#runOnGlThread(Runnable) OpenGL thread}. It will be run
     * after all Tasks added prior to this call.
     *
     * @param task
     *            {@code Task} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnGLThread(Task<T, U> task) {
        runOnThread(ExecutionChain.Context.Type.GL, task);
        return this;
    }

    /**
     * Add a {@link Task} to be run on a {@link org.gearvrf.utility.Threads#spawn(Runnable)
     * background thread}. It will be run after all Tasks added prior to this
     * call.
     *
     * @param task
     *            {@code Task} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnBackgroundThread(Task<T, U> task) {
        runOnThread(Context.Type.BACKGROUND, task);
        return this;
    }

    /**
     * A convenience method to wrap a {@link Runnable} in a {@link Task} to be
     * run on the {@link Activity#runOnUiThread(Runnable) UI thread}.
     *
     * @param r
     *            {@code Runnable} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnUIThread(Runnable r) {
        runOnThread(Context.Type.UI, new RunnableTask(r));
        return this;
    }

    /**
     * A convenience method to wrap a {@link Runnable} in a {@link Task} to be
     * run on the {@link MainThread#runOnMainThread(Runnable) main thread}.
     *
     * @param r
     *            {@code Runnable} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnMainThread(Runnable r) {
        runOnThread(Context.Type.MAIN, new RunnableTask(r));
        return this;
    }

    /**
     * A convenience method to wrap a {@link Runnable} in a {@link Task} to be
     * run on the {@link GVRContext#runOnGlThread(Runnable) GL thread}.
     *
     * @param r
     *            {@code Runnable} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnGLThread(Runnable r) {
        runOnThread(ExecutionChain.Context.Type.GL, new RunnableTask(r));
        return this;
    }

    /**
     * A convenience method to wrap a {@link Runnable} in a {@link Task} to be
     * run on a {@link org.gearvrf.utility.Threads#spawn(Runnable) background thread}.
     *
     * @param r
     *            {@code Runnable} to run
     * @return Reference to the {@code ExecutionChain}.
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public <T, U> ExecutionChain runOnBackgroundThread(Runnable r) {
        runOnThread(Context.Type.BACKGROUND, new RunnableTask(r));
        return this;
    }

    /**
     * Set a callback to handle any exceptions in the chain of execution.
     *
     * @param callback
     *            Instance of {@link ErrorCallback}.
     * @return Reference to the {@code ExecutionChain}
     * @throws IllegalStateException
     *             if the chain of execution has already been {@link #execute()
     *             started}.
     */
    public ExecutionChain setErrorCallback(ErrorCallback callback) {
        if (state.get() == State.RUNNING) {
            throw new IllegalStateException(
                    "Invalid while ExecutionChain is running");
        }
        errorCallback = callback;
        return this;
    }

    /**
     * Start the chain of execution running.
     *
     * @throws IllegalStateException
     *             if the chain of execution has already been started.
     */
    public void execute() {
        State currentState = state.getAndSet(State.RUNNING);
        if (currentState == State.RUNNING) {
            throw new IllegalStateException(
                    "ExecutionChain is already running!");
        }
        executeRunnable = new ExecuteRunnable();
    }

    /**
     * Cancels the chain of execution. At a minimum, execution will stop after
     * the current task is done running; tasks that check
     * {@link Task#isCancelled() isCancelled()} during {@link Task#run(Task)}
     * may stop sooner.
     *
     * @return Returns {@code true} if the {@code ExecutionChain} was
     *         {@linkplain State#RUNNING running} and will be cancelled, and
     *         {@code false} if execution has already completed, has not been
     *         started, or has ended because of an exception.
     */
    public boolean cancel() {
        return cancel(null);
    }

    /**
     * Cancels the chain of execution. An optional {@link Runnable} maybe passed
     * to be run when execution has stopped. At a minimum, execution will stop
     * after the current task is done running; tasks that check
     * {@link Task#isCancelled() isCancelled()} during {@link Task#run(Task)}
     * may stop sooner.
     *
     * @param r
     *            An optional {@code Runnable} to run when execution has
     *            stopped.
     * @return Returns {@code true} if the {@code ExecutionChain} was
     *         {@linkplain State#RUNNING running} and will be cancelled, and
     *         {@code false} if execution has already completed, has not been
     *         started, or has ended because of an exception.
     */
    public boolean cancel(Runnable r) {
        executeRunnable.cancelRunnable.compareAndSet(null, r);
        boolean running = state.compareAndSet(State.RUNNING, State.CANCELLED);
        if (!running) {
            executeRunnable.cancelRunnable.set(null);
            return false;
        }
        return true;
    }

    /**
     * Get the current {@link State state} of the chain of execution.
     *
     * @return Returns the current state.
     */
    public State getState() {
        return state.get();
    }

    private <T, U> void runOnThread(Context.Type type, Task<T, U> task) {
        State currentState = state.get();
        if (currentState == State.RUNNING) {
            throw new IllegalStateException(
                    "Execution chain is already running!");
        }
        tasks.add(new Context<T, U>(type, task));
    }

    private class RunnableTask extends Task<Void, Void> {
        public RunnableTask(Runnable r) {
            runnable = r;
        }

        @Override
        public Void run(Task<Void, ?> task) {
            runnable.run();
            return null;
        }

        private Runnable runnable;
    }

    private static class Context<T, U> {
        private enum Type {
            UI, MAIN, GL, BACKGROUND
        }

        final Type type;
        final Task<T, U> task;

        Context(Type type, Task<T, U> task) {
            this.type = type;
            this.task = task;
        }

        @SuppressWarnings("unchecked")
        void run(Task<?, ?> previousTask, AtomicReference<State> state,
                ErrorCallback errorCallback) {
            task.execute((Task<U, ?>) previousTask, state, errorCallback);
        }
    }

    private final class ExecuteRunnable implements Runnable {
        Context<?, ?> currentContext;
        Context<?, ?> previousContext;
        AtomicReference<Runnable> cancelRunnable = new AtomicReference<Runnable>();
        Iterator<Context<?, ?>> taskIter;

        private ExecuteRunnable() {
            Log.d(TAG, "ExecuteRunnable(): tasks: %d", tasks.size());
            taskIter = tasks.iterator();
            runNext();
        }

        @Override
        public void run() {
            if (state.get() == State.CANCELLED) {
                Log.d(TAG,
                      "Execution was cancelled; skipping task %s on thread %s",
                      currentContext, Thread.currentThread().getName());
                handleCancel();
                return;
            }


            Log.d(TAG, "run(%s): running task %s", Thread.currentThread()
                    .getName(), currentContext);
            Task<?, ?> previousTask = previousContext != null ? previousContext.task
                    : null;

            FPSCounter.timeCheck("ExecutionChain. " + currentContext.type + " <START>, " + currentContext);
            currentContext.run(previousTask, state, errorCallback);
            FPSCounter.timeCheck("ExecutionChain. " + currentContext.type + " <END>, " + currentContext);

            runNext();
        }

        private void runNext() {
            previousContext = currentContext;
            if (taskIter.hasNext()) {
                currentContext = taskIter.next();
                Log.d(TAG, "runNext(%s): next task is %s", Thread
                        .currentThread().getName(), currentContext);

                switch (currentContext.type) {
                    case UI:
                        ((Activity) gvrContext.getContext())
                                .runOnUiThread(this);
                        break;
                    case MAIN:
                        WidgetLib.getMainThread().runOnMainThread(this);
                        break;
                    case GL:
                        gvrContext.runOnGlThread(this);

                        break;
                    case BACKGROUND:
                        spawn(this);
                        break;
                }
            } else {
                if (previousContext != null
                        && previousContext.task.hasException()) {
                    state.set(State.ERROR);
                    Log.e(TAG, "Exception in the execution chain: %s",
                          previousContext.task.exception);
                    previousContext.task.exception.printStackTrace();
                } else {
                    state.set(State.STOPPED);
                }
            }
        }

        private void handleCancel() {
            try {
                final Runnable runnable = cancelRunnable.get();
                if (runnable != null) {
                    runnable.run();
                }
            } catch (Throwable t) {
            }
        }
    }

    private final GVRContext gvrContext;
    private final List<Context<?, ?>> tasks = new ArrayList<Context<?, ?>>();
    private final AtomicReference<State> state = new AtomicReference<State>(
            State.STOPPED);
    private ExecuteRunnable executeRunnable;
    private ErrorCallback errorCallback;

    private static final String TAG = ExecutionChain.class.getSimpleName();
}
