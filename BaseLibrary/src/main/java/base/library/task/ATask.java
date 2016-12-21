package base.library.task;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangjiangpeng01 on 2016/12/19.
 */
public abstract class ATask<Progress, Result>{
    private static final String LOG_TAG = "ATask";

    private final static int MESSAGE_POST_RESULT = 0x01;
    private final static int MESSAGE_POST_PROGRESS = 0x02;

    /**
     * 表示当前任务的状态，每个状态，一次生命周期只设置一次
     */
    public enum Status {
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

    protected final WorkerRunnable<Result> mWorker;
    protected final FutureTask<Result> mFuture;

    protected volatile Status mStatus = Status.PENDING;

    private final AtomicBoolean mCancelled;
    private final AtomicBoolean mTaskInvoked;

    public ATask(){
        mStatus = Status.PENDING;
        mCancelled = new AtomicBoolean();
        mTaskInvoked = new AtomicBoolean();

        mWorker = new WorkerRunnable<Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);

                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Result result = doInBackground(mParams);
                Binder.flushPendingCommands();
                return postResult(result);
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
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

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Result>(this, result));
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

    protected final void preExecute(Object... objs) {
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
        mStatus = ATask.Status.RUNNING;

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

    protected final void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
        mStatus = Status.FINISHED;
    }

    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            AsyncTaskResult result= new AsyncTaskResult<Progress>(this, values);
            getHandler().obtainMessage(MESSAGE_POST_PROGRESS, result).sendToTarget();
        }
    }

    /**
     * 任务生命周期
     *
     *
     *         外部引用publishProgress() -> onProgressUpdate() -> onPostExecute();
     *                |
     * onPreExecute() -> onPostExecute()
     *                |
     *          外部引用cancel() == true  -> onCancelled()
     *
     */
    protected void onPreExecute() {
    }

    protected void onPostExecute(Result result) {
    }

    protected void onCancelled(Result result) {
    }

    protected void onProgressUpdate(Progress... values) {
    }

    protected abstract Result doInBackground(Object... objs);

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

    @SuppressWarnings({"RawUseOfParameterizedType"})
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
