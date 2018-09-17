package org.otempo.service;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.otempo.model.Station;
import org.otempo.rss.PredictionsParser;

import java.io.IOException;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import androidx.work.Worker;


public class FetchWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        int id = getInputData().getInt("stationId", -1);
        Station station = Station.getById(id);
        if (station == null) {
            return Result.FAILURE;
        }
        try {
            PredictionsParser.parse(station, getApplicationContext().getCacheDir());
        } catch (IOException e) {
            return Result.FAILURE;
        }
        return Result.SUCCESS;
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
        WorkManager.getInstance().getStatusById(request.getId())
                .observe(owner, new Observer<WorkStatus>() {
                    @Override
                    public void onChanged(@Nullable WorkStatus status) {
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
