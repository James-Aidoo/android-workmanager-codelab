/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

import java.util.List;
import java.util.concurrent.CancellationException;

public class BlurViewModel extends AndroidViewModel {

    private Uri mImageUri;
    private WorkManager mWorkManager;
    private LiveData<List<WorkInfo>> mSavedWorkInfo;
    private Uri moutputUri;

    public BlurViewModel(@NonNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);
        mSavedWorkInfo = mWorkManager.getWorkInfosByTagLiveData(Constants.TAG_OUTPUT);
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
        WorkContinuation continuation = mWorkManager.beginUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE, OneTimeWorkRequest.from(CleanupWorker.class));

        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurRequest = new OneTimeWorkRequest.Builder(BlurWorker.class);
            if (i == 0) {
                blurRequest.setInputData(createInputDataForUri());
            }
            continuation = continuation.then(blurRequest.build());
        }

        Constraints constraint = new Constraints.Builder()
                .setRequiresCharging(true)
//                .setRequiresStorageNotLow(true)
                .build();

        OneTimeWorkRequest saveRequest = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .setConstraints(constraint)
                .addTag(Constants.TAG_OUTPUT)
                .build();
        continuation = continuation.then(saveRequest);
        continuation.enqueue();
    }

    void cancelWork() {
        try {
            mWorkManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME);
        } catch (CancellationException e) {
            e.printStackTrace();
            Toast.makeText(getApplication().getApplicationContext(), "Work was cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null){
            builder.putString(Constants.KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    void setOutputUri(String outputUri) { moutputUri = uriOrNull(outputUri); }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    LiveData<List<WorkInfo>> getOutputWorkInfo() {
        return mSavedWorkInfo;
    }

    Uri getOutputUri() { return moutputUri; }

}