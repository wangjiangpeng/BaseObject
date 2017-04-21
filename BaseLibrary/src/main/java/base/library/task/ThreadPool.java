package base.library.task;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static base.library.task.TaskPool.getTask;

/**
 * 任务线程池
 * <p>
 * Created by wangjiangpeng01 on 2016/12/19.
 */
public class ThreadPool {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private final ThreadFactory mThreadFactory;
    private final BlockingQueue<Runnable> mPoolWorkQueue;
    private final Executor mThreadPoolExecutor;
    private final Executor mSerialExecutor;

    protected ThreadPool() {
        mThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
            }
        };
        mPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
        mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE, TimeUnit.SECONDS, mPoolWorkQueue, mThreadFactory);
        mSerialExecutor = new SerialExecutor();
    }

    /**
     * 执行线程
     * 如果环境初始化任务没有完成，那么其他任务都要依赖它执行完了在执行
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        ATask task = TaskPool.getTask(EnvInitTask.class);
        if (task.isFinished()) {
            mThreadPoolExecutor.execute(runnable);

        } else {
            if (!task.isRunning()) {
                task.executeSerial(null);
            }
            mSerialExecutor.execute(runnable);
        }
    }

    /**
     * 顺序执行线程
     * 可以当成任务依赖来使用，前面一个任务执行完了，后面一个任务才能执行
     *
     * @param runnable
     */
    public void executeSerial(Runnable runnable) {
        mSerialExecutor.execute(runnable);
    }

    /**
     * 保证线程顺序执行
     */
    private class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                mThreadPoolExecutor.execute(mActive);
            }
        }
    }

}
