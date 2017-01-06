package base.library.task;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import base.library.task.TaskManager.ResultCallbacks;

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

    private final WorkerRunnable<Object> mWorker;
    private final FutureTask<Object> mFuture;

    private volatile Status mStatus = Status.PENDING;

    private final AtomicBoolean mCancelled;
    private final AtomicBoolean mTaskInvoked;

    /**
     * 弱引用，防止对象无法释放
     */
    private WeakReference<ResultCallbacks> weakReceiver;

    public ATask() {
        mStatus = Status.PENDING;
        mCancelled = new AtomicBoolean();
        mTaskInvoked = new AtomicBoolean();

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
                    android.util.Log.w(LOG_TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
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

    public void setResultCallbacks(ResultCallbacks callbacks) {
        weakReceiver = new WeakReference(callbacks);
    }

    /**
     * 执行任务
     *
     * @param objs 执行参数
     */
    public void execute(Object... objs) {
        preExecute(objs);
        TaskThreadPool.execute(mFuture);
    }

    /**
     * 顺序执行任务，有依赖关系的任务可调用此方法
     *
     * @param objs 执行参数
     */
    public void executeSerial(Object... objs) {
        preExecute(objs);
        TaskThreadPool.executeSerial(mFuture);
    }

    private void preExecute(Object... objs) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }
        mStatus = Status.RUNNING;
        mWorker.mParams = objs;

        onPreExecute();
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    private void finish(Object result) {
        if (isCancelled()) {
            onCancelled(result);

        } else {
            onPostExecute(result);

            if (weakReceiver != null) {
                ResultCallbacks callbacks = weakReceiver.get();
                if (callbacks != null) {
                    callbacks.onFinished(this, result);
                }
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

}
