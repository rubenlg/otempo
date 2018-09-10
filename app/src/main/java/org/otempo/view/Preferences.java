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

import java.util.List;

import org.otempo.R;
import org.otempo.model.Station;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

/**
 * Actividad para gestionar las preferencias
 * Además, contiene las constantes para acceder a cada preferencia sin repetir strings por todas partes.
 */
public class Preferences extends PreferenceActivity {
    public static final String DEFAULT_DEFAULT_STATION = "nearest";
    public static final String PREF_DEFAULT_STATION_FIXED = "defaultStationFixed";
    public static final String PREF_DEFAULT_STATION = "defaultStation";
    public static final String PREF_UPDATE_AMOUNT = "updateAmount";
    public static final String PREF_STATION_ORDERING = "stationOrdering";
    public static final String PREF_PREDICTED_TIME_WHY_CLICKED = "predictedTimeWhyClicked"; // Se ha pulsado ya el "por qué?" de la hora de predicción?
    public static final String PREF_BACKGROUND = "background";
    public static final String PREF_BACKGROUND_USER_IMAGE = "bgUserImage";
    public static final String DEFAULT_STATION_ORDERING = "alphabetic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreference defaultStationFixed = (ListPreference) findPreference(PREF_DEFAULT_STATION_FIXED);
        List<Station> stations = Station.getKnownStations();
        CharSequence[] stationEntries = new CharSequence[stations.size()];
        CharSequence[] stationEntryValues = new CharSequence[stations.size()];
        int index = 0;
        for (Station s: stations) {
            stationEntries[index] = s.getName();
            stationEntryValues[index] = String.valueOf(s.getId());
            index++;
        }
        defaultStationFixed.setEntries(stationEntries);
        defaultStationFixed.setEntryValues(stationEntryValues);

        String defaultStation = getPreferenceManager().getSharedPreferences().getString(PREF_DEFAULT_STATION, DEFAULT_DEFAULT_STATION);
        defaultStationFixed.setEnabled(defaultStation.equals("fixed"));
        findPreference(PREF_DEFAULT_STATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
                findPreference(PREF_DEFAULT_STATION_FIXED).setEnabled(arg1.equals("fixed"));
                return true;
            }
        });

        findPreference(PREF_BACKGROUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
                if (arg1.equals("user_image")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                                               android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(intent, ACTIVITY_SELECT_BG_IMAGE);
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == ACTIVITY_SELECT_BG_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = intent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                Editor editor = prefs.edit();
                editor.putString(Preferences.PREF_BACKGROUND_USER_IMAGE, filePath);
                editor.commit();
            }
        }
    }

    private final static int ACTIVITY_SELECT_BG_IMAGE = 1;
}
