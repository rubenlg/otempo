/*
 * Copyright (C) 2010-2011 Ruben Lopez
 *
 * This file is part of OTempo - Galician Weather
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package org.otempo.view;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.otempo.R;
import org.otempo.StationManager;
import org.otempo.model.Station;
import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationPrediction;
import org.otempo.model.StationShortTermPrediction;
import org.otempo.rss.PredictionsParser;
import org.otempo.util.DateUtils;
import org.otempo.util.ResourceUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Proveedor del widget que se puede integrar en el escritorio
 */
public class StationWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(
            Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Station station = getWidgetStation(context);

        if (station.getPredictions().size() == 0) {
            try {
                // We can't use workers from a BroadcastReceiver, so we can only do this
                // synchronously.
                PredictionsParser.parse(station, context.getCacheDir());
            } catch (Exception e) {
                Log.e("OTempo", "Widget was unable to read station", e);
            }
        }

        if (station.getPredictions().size() > 0) {
            displayStation(context, station);
        } else {
            displayLoading(context, appWidgetManager, appWidgetIds);
        }
    }

    private static Station getWidgetStation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultStationPreference = prefs.getString(
                Preferences.PREF_DEFAULT_STATION, Preferences.DEFAULT_DEFAULT_STATION);
        int defaultStationFixed =
                Integer.valueOf(prefs.getString(Preferences.PREF_DEFAULT_STATION_FIXED, "1"));
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        StationManager tmpStationManager =
                new StationManager(locationManager, defaultStationPreference, defaultStationFixed);
        Station station = tmpStationManager.getStation();
        if (station != null) {
            return station;
        }
        return Station.getKnownStations().get(0);
    }

    /**
     * @param station La estación cuya predicción actual deseamos
     * @return Devuelve la predicción para hoy de las que contiene una estación
     */
    private static StationPrediction getTodayPrediction(Station station) {
        for (StationPrediction prediction : station.getPredictions()) {
            final Calendar predictionDate = prediction.getDate();
            if (predictionDate != null && DateUtils.isFromToday(predictionDate)) {
                return prediction;
            }
        }
        return null;
    }

    /**
     * Actualiza el widget con la información de la estación proporcionada
     *
     * @param context Contexto válido de Android
     * @param station La estación que deseamos mostrar en el widget
     */
    public static void displayStation(Context context, Station station) {
        String dayName = DateUtils.weekDayFormat.format(new Date());
        RemoteViews rviews;

        // Si no hay estación -> Error
        String stationName = station.getName();
        if (station.getPredictions().size() == 0) {
            return;
        }
        StationPrediction prediction = getTodayPrediction(station);
        if (prediction == null) {
            rviews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_err);
            rviews.setTextViewText(R.id.widgetError, context.getString(R.string.internet_error));
        } else {
            if (prediction instanceof StationShortTermPrediction) {
                StationShortTermPrediction pred = (StationShortTermPrediction) prediction;
                rviews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                rviews.setTextViewText(
                        R.id.widgetTemps,
                        pred.getMinTemp() + " - " + pred.getMaxTemp() + " C");
                rviews.setImageViewResource(
                        R.id.widgetMorning,
                        ResourceUtils.getResource(pred.getSkyStateMorning(), true));
                rviews.setImageViewResource(
                        R.id.widgetAfternoon,
                        ResourceUtils.getResource(pred.getSkyStateAfternoon(), true));
                rviews.setImageViewResource(
                        R.id.widgetEvening,
                        ResourceUtils.getResource(pred.getSkyStateNight(), false));
            } else if (prediction instanceof StationMediumTermPrediction) {
                StationMediumTermPrediction predMed = (StationMediumTermPrediction) prediction;
                rviews = new RemoteViews(
                        context.getPackageName(), R.layout.widget_layout_medium_term);
                rviews.setImageViewResource(
                        R.id.widgetMorning,
                        ResourceUtils.getResource(predMed.getSkyState(), true));
            } else {
                rviews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_err);
                rviews.setTextViewText(R.id.widgetError, context.getString(R.string.internal_error));
            }
        }
        rviews.setTextViewText(R.id.widgetStation, stationName);
        rviews.setTextViewText(R.id.widgetCurrentDay, dayName);

        ComponentName widget = new ComponentName(context, StationWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        Intent otempoIntent = new Intent(context, StationActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, otempoIntent, 0);
        rviews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);
        manager.updateAppWidget(widget, rviews);
    }

    private static void displayLoading(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("OTempo", "Display loading on the widget");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews rviews =
                    new RemoteViews(context.getPackageName(), R.layout.widget_layout_err);
            rviews.setTextViewText(R.id.widgetError, context.getString(R.string.loading_data));

            Intent otempoIntent = new Intent(context, StationActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(context, 0, otempoIntent, 0);
            rviews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, rviews);
        }
    }
}
