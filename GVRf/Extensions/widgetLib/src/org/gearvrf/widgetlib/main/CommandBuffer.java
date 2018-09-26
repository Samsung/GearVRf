package org.gearvrf.widgetlib.main;

import android.support.annotation.NonNull;

import org.gearvrf.widgetlib.thread.ConcurrentListPool;
import org.gearvrf.widgetlib.thread.ConcurrentObjectPool;
import org.gearvrf.widgetlib.thread.MainThread;
import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;

import java.util.List;

/**
 * A utility class for buffering {@link Runnable} "commands" for batch execution on the {@linkplain
 * GVRContext#runOnGlThread(Runnable) GL thread}.
 * <p>
 * Although instances of {@link Runnable} can be directly {@linkplain CommandBuffer#add(Runnable)
 * added to the buffer}, the {@link Command} class makes management of command instances a bit
 * easier and reduces memory fragmentation and usage by {@linkplain ConcurrentObjectPool pooling}
 * instances.
 * <p>
 * The typical implementation pattern for a command using {@link Command} looks like this:
 * <pre>
 *     static final class MyCommand {
 *         // MyCommand provides its own buffer() method to
 *         // ensure type correctness and proper ordering
 *         // of its parameters
 *         static void buffer(Foo foo, Bar bar) {
 *             Command.buffer(foo, bar);
 *         }
 *
 *         private static final Executor sExecutor = new Executor() {
 *             public void exec(Object... params) {
 *                 // Unpack the command's parameters
 *                 final Foo foo = (Foo) args[0];
 *                 final Bar bar = (Bar) args[1];
 *
 *                 foo.qux(bar);
 *             }
 *         }
 *     }
 * </pre>
 * The typical usage pattern looks like this:
 * <pre>
 *     MyCommand.buffer(aFoo, aBar);
 * </pre>
 */
public class CommandBuffer {

    /**
     * An extension of {@link Runnable} that manages the boiler-plate of releasing back to a
     * {@linkplain ConcurrentObjectPool pool} after running.
     */
    static public final class Command implements Runnable {
        /**
         * Interface for the execution of the command, encapsulating the command's logic.
         */
        public interface Executor {
            /**
             * Everything that is needed for the operation should be passed as part of {@code params}.
             * Code in exec() must be reentrant and should not rely on any non-final data members
             * as it will be allocated and initialized on the {@link MainThread}, but executed on
             * the {@linkplain Widget#runOnGlThread(Runnable) GL thread}.
             *
             * @param params All data and objects necessary for the command to execute.
             */
            void exec(Object... params);
        }

        /**
         * Packages the {@link Executor} and its parameters into an instance of {@link Command}, and
         * {@linkplain CommandBuffer#add(Runnable) adds} it to the current {@link CommandBuffer}.
         *
         * @param executor An implementation of {@link Executor}
         * @param params   The parameters used by {@code executor}.  Since these are raw {@link Object}
         *                 references, an additional interface should be implemented to ensure type
         *                 safety and correct ordering.  See class documentation for {@link
         *                 CommandBuffer}.
         */
        public static void buffer(Executor executor, Object... params) {
            Command command = sPool.acquire();
            command.setup(executor, params);
            WidgetLib.getCommandBuffer().add(command);
        }

        /**
         * Construct an instance.
         */
        private Command() {
        }

        /**
         * {@link Executor#exec(Object...) Executes} the command's logic and releases this instance
         * back to the {@link ConcurrentObjectPool pool}.
         */
        @Override
        public void run() {
            mExecutor.exec(mArgs);
            sPool.release(this);
        }

        private void setup(Executor executor, Object... args) {
            mExecutor = executor;
            mArgs = args;
        }

        private Executor mExecutor;
        private Object[] mArgs;

        private static final ConcurrentObjectPool<Command> sPool = new ConcurrentObjectPool<Command>(Command.class.getSimpleName()) {
            @Override
            protected Command create() {
                return new Command();
            }
        };
    }

    /**
     * Start a new buffer.  Calls to {@code start()} can be nested, so if there is already an
     * active buffer, that buffer will continue to be used.  Calls to {@code start()} must have a
     * matching number of calls to {@link #flush()} in order for the commands in the buffer to get
     * executed.
     */
    public void start() {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                mCurrentBuffer = mBuffers.acquire();
            }
            ++mBufferDepth;
        }
    }

    /**
     * Add a {@link Runnable} to the current buffer.  If no buffer has been {@linkplain #start()
     * started}, {@code command} will be {@linkplain GVRContext#runOnGlThread(Runnable) posted}
     * directly to the GL thread to be executed in the next frame.
     *
     * @param command The command to add to the buffer.  Should be non-null.
     */
    public void add(@NonNull Runnable command) {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                mContext.runOnGlThread(command);
            } else {
                mCurrentBuffer.add(command);
            }
        }
    }

    /**
     * {@linkplain GVRContext#runOnGlThread(Runnable) Post} the current buffer to the GL thread for
     * execution.  The number of calls to {@code flush()} must match the number of calls to {@link
     * #start()}.
     *
     * @throws IllegalStateException if called when there is no active buffer.
     */
    public void flush() {
        synchronized (mBufferLock) {
            if (mCurrentBuffer == null) {
                throw new IllegalStateException("No buffer to flush!");
            }
            --mBufferDepth;
            if (mBufferDepth == 0) {
                final GLRunnable r = mRunnablePool.acquire();
                r.set(mCurrentBuffer);
                mCurrentBuffer = null;
                mContext.runOnGlThread(r);
            }
        }
    }

    /* package */
    CommandBuffer(GVRContext context) {
        mContext = context;
    }

    private final class GLRunnable implements Runnable {
        @Override
        public void run() {
            for (Runnable c : mBuffer) {
                c.run();
            }
            mBuffers.release(mBuffer);
            mBuffer = null;
            mRunnablePool.release(this);
        }

        public void set(List<Runnable> buffer) {
            mBuffer = buffer;
        }

        private List<Runnable> mBuffer;
    }

    private final GVRContext mContext;
    private final ConcurrentListPool<Runnable> mBuffers = new ConcurrentListPool<>(TAG) ;
    private int mBufferDepth;
    private final Object[] mBufferLock = new Object[0];
    private List<Runnable> mCurrentBuffer;
    private final ConcurrentObjectPool<GLRunnable> mRunnablePool = new ConcurrentObjectPool<GLRunnable>("GLRunnable Pool") {
        @Override
        protected GLRunnable create() {
            return new GLRunnable();
        }
    };

    private static final String TAG = Log.tag(CommandBuffer.class);
}
