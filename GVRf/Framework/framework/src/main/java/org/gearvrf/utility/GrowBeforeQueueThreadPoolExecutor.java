package org.gearvrf.utility;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GrowBeforeQueueThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();

    private int minCorePoolSize;
    private AtomicInteger taskCount = new AtomicInteger(0);

    public GrowBeforeQueueThreadPoolExecutor(final String prefix) {
        super(
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
        
        minCorePoolSize = Math.min(2, NUM_CPUS);
    }

    @Override
    public void execute(Runnable runnable) {
        setCorePoolSizeToTaskCountWithinBounds(taskCount.incrementAndGet());

        super.execute(runnable);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);

        setCorePoolSizeToTaskCountWithinBounds(taskCount.decrementAndGet());
    }

    private synchronized void setCorePoolSizeToTaskCountWithinBounds(int taskCount) {
        int corePoolSize = getCorePoolSize();
        if (taskCount > corePoolSize) {
            setCorePoolSize(Math.min(getMaximumPoolSize(), (corePoolSize << 1)));
        }
        else if (taskCount < (corePoolSize >> 1)) {
            setCorePoolSize(Math.max(minCorePoolSize, (corePoolSize >> 1)));
        }
    }
}
