package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;
import com.example.background.R;

import java.io.FileNotFoundException;

public class BlurWorker extends Worker {
    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
//            Bitmap picture = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.test);
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input Uri");
                throw new IllegalArgumentException("Invalid input Uri");
            }

            ContentResolver resolver = appContext.getContentResolver();
            Bitmap picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));
            Bitmap outputPicture = WorkerUtils.blurBitmap(picture, appContext);

            Uri outPutUri = WorkerUtils.writeBitmapToFile(appContext, outputPicture);
            WorkerUtils.makeStatusNotification("Output is " + outPutUri.toString(), appContext);
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outPutUri.toString())
                    .build();
            return Result.success(outputData);
        } catch (Throwable e) {
            Log.d(TAG, "Error applying blur", e);
            return Result.failure();
        }
    }
}
