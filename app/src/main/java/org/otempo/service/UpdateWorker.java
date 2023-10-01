package org.otempo.service;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.util.Log;

import org.otempo.model.FavoritesStationComparator;
import org.otempo.model.Station;
import org.otempo.rss.PredictionsParser;
import org.otempo.view.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Station updater based on WorkerManager
 */
public class UpdateWorker extends Worker {
    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<Station> stationsToUpdate = getStationsToUpdate();
            Log.d("OTempo", "Updating " + stationsToUpdate.size() + " stations");
            for (Station station : stationsToUpdate) {
                PredictionsParser.parse(station, getApplicationContext().getCacheDir(), false);
            }
        } catch (IOException e) {
            return Result.failure();
        }
        return Result.success();
    }

    private List<Station> getStationsToUpdate() {
        int updateAmount = maxStationsToUpdate();
        // Las estaciones ya están ordenadas según el criterio del usuario, así que se van a
        // actualizar las más prioritarias para él.
        List<Station> known = Station.getKnownStations();
        List<Station> updated = new ArrayList<>();
        for (int i = 0; i < updateAmount && i < known.size(); i++) {
            updated.add(known.get(i));
        }

        // Aunque elegimos las estaciones que más interesan al usuario, vamos a ordenarlas por
        // frecuencia de uso, por si se corta Internet a medio camino.
        Collections.sort(updated, new FavoritesStationComparator());
        return updated;

    }

    private int maxStationsToUpdate() {
        int updateAmount = Integer.valueOf(
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(Preferences.PREF_UPDATE_AMOUNT, "0"));
        if (updateAmount == 0) {
            return MAX_UPDATE_AMOUNT;
        }
        return Math.min(MAX_UPDATE_AMOUNT, updateAmount);
    }

    private final static int MAX_UPDATE_AMOUNT = 15;
}
