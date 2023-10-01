package org.otempo.service;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.util.Log;

import org.otempo.model.Station;
import org.otempo.rss.PredictionsParser;

import java.io.IOException;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class FetchWorker extends Worker {
    public FetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int id = getInputData().getInt("stationId", -1);
        Station station = Station.getById(id);
        if (station == null) {
            return Result.failure();
        }
        try {
            PredictionsParser.parse(station, getApplicationContext().getCacheDir(), false);
        } catch (IOException e) {
            return Result.failure();
        }
        return Result.success();
    }

    public interface ResultListener {
        void success();

        void error();
    }

    public static void run(Station station, LifecycleOwner owner, final ResultListener listener) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(FetchWorker.class)
                .setInputData(new Data.Builder()
                        .putInt("stationId", station.getId())
                        .build())
                .build();
        WorkManager.getInstance().enqueue(request);
        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId())
                .observe(owner, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo status) {
                        if (status == null) {
                            return;
                        }
                        switch (status.getState()) {
                            case SUCCEEDED:
                                listener.success();
                                break;
                            case FAILED:
                                listener.error();
                                break;
                            default:
                                Log.d("OTempo", "Worker now in status " + status.getState());
                        }

                    }
                });
    }
}
