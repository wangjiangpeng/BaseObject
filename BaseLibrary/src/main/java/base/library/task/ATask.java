package base.library.task;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象任务
 * <p>
 * Created by wangjiangpeng01 on 2016/12/19.
 */
public abstract class ATask<Progress> {
    private static final String LOG_TAG = "ATask";

    private final static int MESSAGE_POST_RESULT = 0x01;
    private final static int MESSAGE_POST_PROGRESS = 0x02;

    /**
     * 表示当前任务的状态，每个状态，一次生命周期只设置一次
     */
    private enum Status {
        /**
         * 该任务尚未被执行
         */
        PENDING,
        /**
         * 该任务正在执行
         */
        RUNNING,
        /**
         * 该任务执行完毕
         */
        FINISHED,
    }

    private static InternalHandler sHandler;

    private WorkerRunnable<Object> mWorker;
    private FutureTask<Object> mFuture;

    private volatile Status mStatus = Status.PENDING;

    private final AtomicBoolean mCancelled;
    private final AtomicBoolean mTaskInvoked;

    /**
     * 弱引用，防止对象无法释放
     */
    private WeakReference<TaskCallbacks> weakReceiver;

    /**
     * 执行结果
     */
    private Object result;

    private static final Executor sThreadPool = new ThreadPool();
    private static final Executor mSerialExecutor = new SerialExecutor();

    public ATask() {
        mStatus = Status.PENDING;
        mCancelled = new AtomicBoolean();
        mTaskInvoked = new AtomicBoolean();
    }

    private static Handler getHandler() {
        if (sHandler == null) {
            synchronized (AsyncTask.class) {
                if (sHandler == null) {
                    sHandler = new InternalHandler();
                }
            }
        }
        return sHandler;
    }

    /**
     * 执行任务
     *
     * @param callbacks 回调
     * @param objs      执行参数
     */
    public synchronized void execute(TaskCallbacks callbacks, Object... objs) {
        execute(sThreadPool, callbacks, objs);
    }

    /**
     * 执行任务
     * 如果环境初始化任务没有完成，那么其他任务都要依赖它执行完了在执行
     *
     * @param executor  任务池
     * @param callbacks 回调
     * @param objs      执行参数
     */
    public synchronized void execute(Executor executor, TaskCallbacks callbacks, Object... objs) {
        boolean execute = preExecute(callbacks, objs);
        if (execute) {
            /* 保证环境初始化任务优先完成 */
            ATask task = TaskService.getInstance().getTask(EnvInitTask.class);
            if (task.isFinished()) {
                executor.execute(mFuture);

            } else {
                if (!task.isRunning()) {
                    task.executeSerial(null);
                }
                mSerialExecutor.execute(mFuture);
            }
        } else {
            if (callbacks != null) {
                callbacks.onFinished(this, result);
            }
        }
    }

    /**
     * 重新执行任务
     *
     * @param callbacks 回调
     * @param objs      执行参数
     * @return
     */
    public synchronized boolean reExecute(TaskCallbacks callbacks, Object... objs) {
        if (reset()) {
            execute(callbacks, objs);
            return true;
        }
        return false;
    }

    /**
     * 重新执行任务
     *
     * @param executor  任务池
     * @param callbacks 回调
     * @param objs      执行参数
     * @return
     */
    public synchronized boolean reExecute(Executor executor, TaskCallbacks callbacks, Object... objs) {
        if (reset()) {
            execute(executor, callbacks, objs);
            return true;
        }
        return false;
    }

    /**
     * 顺序执行任务，有依赖关系的任务可调用此方法
     *
     * @param objs 执行参数
     */
    public synchronized void executeSerial(TaskCallbacks callbacks, Object... objs) {
        boolean execute = preExecute(callbacks, objs);
        if (execute) {
            mSerialExecutor.execute(mFuture);
        } else {
            if (callbacks != null) {
                callbacks.onFinished(this, result);
            }
        }
    }

    /**
     * 重新顺序执行任务
     *
     * @param callbacks 回调
     * @param objs      执行参数
     * @return
     */
    public synchronized boolean reExecuteSerial(TaskCallbacks callbacks, Object... objs) {
        if (reset()) {
            executeSerial(callbacks, objs);
            return true;
        }
        return false;
    }

    private boolean preExecute(TaskCallbacks callbacks, Object... objs) {
        if (mStatus != Status.PENDING) {
            return false;
        } else {
            mWorker = new WorkerRunnable<Object>() {
                public Object call() throws Exception {
                    mTaskInvoked.set(true);

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    Object result = doInBackground(mParams);
                    Binder.flushPendingCommands();
                    return postResult(result);
                }
            };

            mFuture = new FutureTask<Object>(mWorker) {
                @Override
                protected void done() {
                    try {
                        postResultIfNotInvoked(get());
                    } catch (InterruptedException e) {
                        Log.w(LOG_TAG, e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException("An error occurred while executing doInBackground()",
                                e.getCause());
                    } catch (CancellationException e) {
                        postResultIfNotInvoked(null);
                    }
                }
            };
            mWorker.mParams = objs;
            weakReceiver = new WeakReference(callbacks);

            onPreExecute();

            return true;
        }
    }

    private void postResultIfNotInvoked(Object result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Object postResult(Object result) {
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Object>(this, result));
        message.sendToTarget();
        return result;
    }

    /**
     * 重置任务
     * 重置正在执行的任务，会抛出异常
     */
    public boolean reset() {
        if (mStatus == Status.RUNNING) {
            return false;
        }

        mStatus = Status.PENDING;
        mCancelled.set(false);
        mTaskInvoked.set(false);
        weakReceiver = null;
        result = null;
        mWorker = null;
        mFuture = null;
        return true;
    }

    /**
     * 是否任务被取消
     *
     * @return
     */
    public final boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * 任务是否正在执行
     *
     * @return
     */
    public final boolean isRunning() {
        return mStatus == Status.RUNNING;
    }

    /**
     * 取消任务
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        if (mFuture != null) {
            return mFuture.cancel(mayInterruptIfRunning);
        }
        return true;
    }

    /**
     * 任务是否完成
     *
     * @return
     */
    public final boolean isFinished() {
        return mStatus == Status.FINISHED;
    }

    /**
     * 执行结果
     *
     * @return
     */
    public Object getResult() {
        return result;
    }

    private void finish(Object result) {
        this.result = result;
        if (isCancelled()) {
            onCancelled(result);

        } else {
            onPostExecute(result);

            TaskCallbacks callbacks = weakReceiver.get();
            if (callbacks != null) {
                callbacks.onFinished(this, result);
            }
        }
        mStatus = Status.FINISHED;
    }

    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            AsyncTaskResult result = new AsyncTaskResult<Progress>(this, values);
            getHandler().obtainMessage(MESSAGE_POST_PROGRESS, result).sendToTarget();
        }
    }

    /**
     * 任务生命周期
     * <p>
     * <p>
     * 外部引用publishProgress() -> onProgressUpdate() -> onPostExecute();
     * |
     * onPreExecute() -> onPostExecute()
     * |
     * 外部引用cancel() == true  -> onCancelled()
     */
    protected void onPreExecute() {
    }

    protected void onPostExecute(Object result) {
    }

    protected void onCancelled(Object result) {
    }

    protected void onProgressUpdate(Progress... values) {
    }

    protected abstract Object doInBackground(Object... objs);

    private static class InternalHandler extends Handler {

        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }

    private static class AsyncTaskResult<Data> {
        final ATask mTask;
        final Data[] mData;

        AsyncTaskResult(ATask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }

    private static abstract class WorkerRunnable<Result> implements Callable<Result> {
        Object[] mParams;
    }

    /**
     * 保证线程顺序执行
     */
    private static class SerialExecutor implements Executor {
        private final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        private Runnable mActive;

        public SerialExecutor() {
        }

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
                sThreadPool.execute(mActive);
            }
        }
    }

}
