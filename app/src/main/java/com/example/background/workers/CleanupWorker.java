package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;

import java.io.File;

public class CleanupWorker extends Worker {

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static final String TAG = CleanupWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        try {
            File outputDir = new File(appContext.getFilesDir(), Constants.OUTPUT_PATH);
            if (outputDir.exists()) {
                File[] entries = outputDir.listFiles();
                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("%s - %s", name, deleted));
                        }
                    }
                }
            }
            return Result.success();
        } catch (Exception ex) {
            Log.e(TAG, "error cleaning up", ex);
            return Result.failure();
        }
    }


}
