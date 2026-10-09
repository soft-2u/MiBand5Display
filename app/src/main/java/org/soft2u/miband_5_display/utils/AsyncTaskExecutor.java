package org.soft2u.miband_5_display.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AsyncTaskExecutor<Params> {
    public static final String TAG = "AsyncTaskRunner";

    private final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(5, 128, 1,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsInterrupted = false;

    protected void onPreExecute(){}
    protected abstract Void doInBackground(Params... params);
    protected void onPostExecute(){}
    protected void onCancelled() {}

    @SafeVarargs
    public final void executeAsync(Params... params) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                checkInterrupted();
                mHandler.post(this::onPreExecute);

                checkInterrupted();
                doInBackground(params);

                checkInterrupted();
                mHandler.post(this::onPostExecute);
            } catch (InterruptedException ex) {
                mHandler.post(this::onCancelled);
            } catch (Exception ex) {
                Log.e(TAG, "executeAsync: " + ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
            }
        });
    }

    private void checkInterrupted() throws InterruptedException {
        if (isInterrupted()){
            throw new InterruptedException();
        }
    }

    public void cancel(boolean mayInterruptIfRunning){
        setInterrupted(mayInterruptIfRunning);
    }

    public boolean isInterrupted() {
        return mIsInterrupted;
    }

    public void setInterrupted(boolean interrupted) {
        mIsInterrupted = interrupted;
    }
}
