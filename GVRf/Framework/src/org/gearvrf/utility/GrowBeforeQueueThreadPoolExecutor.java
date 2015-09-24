package org.gearvrf.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GrowBeforeQueueThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();

    private int userSpecifiedCorePoolSize;
    private int taskCount;
    private Object syncObj = new Object();

    public GrowBeforeQueueThreadPoolExecutor(final String prefix) {
        this(
        /* core size    */Math.min(2, NUM_CPUS),
        /* max  size    */Math.max(Math.min(2, NUM_CPUS), 2 * NUM_CPUS),
        /* idle timeout */60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>(),
        new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public final Thread newThread(final Runnable r) {
                return new Thread(r, prefix + "-" + threadNumber.getAndIncrement());
            }
        });
    }

    /*package*/ GrowBeforeQueueThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        userSpecifiedCorePoolSize = corePoolSize;
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (syncObj) {
            taskCount++;
            setCorePoolSizeToTaskCountWithinBounds();
        }
        super.execute(runnable);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        synchronized (syncObj) {
            taskCount--;
            setCorePoolSizeToTaskCountWithinBounds();
        }
    }

    private void setCorePoolSizeToTaskCountWithinBounds() {
        int threads = taskCount;
        if (threads < userSpecifiedCorePoolSize) {
            threads = userSpecifiedCorePoolSize;
        }
        if (threads > getMaximumPoolSize()) {
            threads = getMaximumPoolSize();
        }
        setCorePoolSize(threads);
    }
}
