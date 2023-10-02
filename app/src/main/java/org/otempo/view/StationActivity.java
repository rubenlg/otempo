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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.otempo.R;
import org.otempo.StationManager;
import org.otempo.favorites.FavoritesDB;
import org.otempo.model.AlphabeticStationComparator;
import org.otempo.model.FavoritesStationComparator;
import org.otempo.model.Station;
import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationPrediction;
import org.otempo.model.StationPredictionVisitor;
import org.otempo.model.StationShortTermPrediction;
import org.otempo.service.FetchWorker;
import org.otempo.service.UpdateWorker;
import org.otempo.util.BitmapUtils;
import org.otempo.util.DateUtils;
import org.otempo.util.LayoutUtils;
import org.otempo.util.Nullness;
import org.otempo.util.ResourceUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

/**
 * Actividad principal, donde se muestran los datos de la estación actual
 */
public class StationActivity extends Activity implements OnSharedPreferenceChangeListener, StationManager.Listener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        lastUpdateFormat = new SimpleDateFormat(getString(R.string.predicted_at), Locale.getDefault());
        PeriodicWorkRequest updateWork = new PeriodicWorkRequest.Builder(UpdateWorker.class, updatePeriodMs(),
                TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build();
        // Make sure there is nothing else running at this point. Not even the widget. The attention
        // of the user is on the activity.
        WorkManager.getInstance().cancelAllWork();
        // Now start polling in the background
        WorkManager.getInstance().enqueueUniquePeriodicWork("SyncStations", ExistingPeriodicWorkPolicy.KEEP, updateWork);

        setContentView(R.layout.main);

        if (_favoritesDB == null) {
            _favoritesDB = new FavoritesDB(getApplicationContext());
        }
        Spinner stationSpinner = findViewById(R.id.stationSpinner);
        _stationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        initStationManager();
        fillStationAdapter();
        _stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(_stationAdapter);
        if (_stationManager.getStation() != null) {
            stationSpinner.setSelection(_stationAdapter.getPosition(_stationManager.getStation()));
        }
        stationSpinner.setOnItemSelectedListener(new OnStationSelectedListener());
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);

        reloadPreferences();

        final Button predictionHelpButton = findViewById(R.id.predictionHelpButton);
        if (predictionHelpButton != null) { // en landscape no hay
            predictionHelpButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    showDialog(DIALOG_PREDICTION_EXPLAIN_ID);
                    // Marcando la preferencia
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    Editor editor = prefs.edit();
                    editor.putBoolean(Preferences.PREF_PREDICTED_TIME_WHY_CLICKED, true);
                    editor.commit();
                }
            });
        }

        final Button hamburgerButton = findViewById(R.id.hamburgerButton);
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        final ListView sideMenu = findViewById(R.id.sideMenu);
        if (hamburgerButton != null && drawerLayout != null && sideMenu != null) {
            hamburgerButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.openDrawer(sideMenu);
                }
            });
            fillSideMenu(sideMenu, drawerLayout);
        }
    }

    int updatePeriodMs() {
        return Integer.valueOf(
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(Preferences.PREF_UPDATE_PERIOD, "3600000"));
    }

    static class SideMenuItem {
        final int id;
        final int title_id;
        final int icon_id;

        SideMenuItem(int id, int title_id, int icon_id) {
            this.id = id;
            this.title_id = title_id;
            this.icon_id = icon_id;
        }
    }

    static class SideMenuListAdapter extends ArrayAdapter<SideMenuItem> {
        SideMenuListAdapter(Context context, int resource, List<SideMenuItem> items) {
            super(context, resource, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.side_menu_item, null);
            }

            SideMenuItem item = getItem(position);
            if (item != null) {
                ImageView icon = v.findViewById(R.id.side_menu_item_icon);
                if (icon != null) {
                    icon.setImageResource(item.icon_id);
                }
                TextView title = v.findViewById(R.id.side_menu_item_title);
                if (title != null) {
                    title.setText(item.title_id);
                }
            }
            return v;
        }
    }

    private void fillSideMenu(ListView sideMenu, final DrawerLayout drawerLayout) {
        final List<SideMenuItem> items = new ArrayList<>();
        items.add(new SideMenuItem(R.id.settings, R.string.settings, android.R.drawable.ic_menu_preferences));
        items.add(new SideMenuItem(R.id.syncNow, R.string.syncMenu, R.drawable.menu_update));
        items.add(new SideMenuItem(R.id.shareFbook, R.string.shareMenu, android.R.drawable.ic_menu_share));
        items.add(new SideMenuItem(R.id.source, R.string.source, android.R.drawable.ic_menu_info_details));
        items.add(new SideMenuItem(R.id.myLocation, R.string.my_location, android.R.drawable.ic_menu_mylocation));
        items.add(new SideMenuItem(R.id.changelogMenu, R.string.changelogMenu, android.R.drawable.ic_menu_recent_history));
        sideMenu.setAdapter(new SideMenuListAdapter(this, R.layout.side_menu_item, items));

        sideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                drawerLayout.closeDrawers();
                onMenuItemSelected(items.get(pos).id);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybeRequestLocationPermission();
        Station station = _stationManager.getStation();
        if (station != null) {
            fetchThenShow(station, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onMenuItemSelected(item.getItemId());
        return true;
    }

    private void onMenuItemSelected(int id) {
        if (id == R.id.settings) {
            showPreferences();
        } else if (id == R.id.source) {
            gotoMeteogalicia();
        } else if (id == R.id.myLocation) {
            _stationManager.setClosestStation();
        } else if (id == R.id.changelogMenu) {
            showChangeLog();
        } else if (id == R.id.syncNow) {
            Station station = _stationManager.getStation();
            if (station != null) {
                fetchThenShow(station, true);
            }
        } else if (id == R.id.shareFbook) {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://www.facebook.com/otempo");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
        }
    }

    /**
     * Lanza la actividad del registro de cambios
     */
    private void showChangeLog() {
        Intent i = new Intent(this, ChangeLog.class);
        startActivity(i);

    }

    /**
     * Lanza el navegador en la web de meteogalicia
     */
    private void gotoMeteogalicia() {
        Station station = _stationManager.getStation();
        if (station != null) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.meteogalicia.es/web/predicion/localidades/localidadesIndex.action?idZona=" + station.getId()));
            startActivity(i);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.meteogalicia.es"));
            startActivity(i);
        }
    }

    /**
     * Recarga las preferencias, por si han cambiado
     */
    private void reloadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String stationOrdering = prefs.getString(Preferences.PREF_STATION_ORDERING, Preferences.DEFAULT_STATION_ORDERING);
        if (stationOrdering.equals("favorites")) {
            Station.sortStations(new FavoritesStationComparator());
        } else if (stationOrdering.equals("alphabetic")) {
            Station.sortStations(new AlphabeticStationComparator());
        }
        _stationManager.setAutoSortStations(stationOrdering.equals("distance"));
        fillStationAdapter();
        Spinner stationSpinner = findViewById(R.id.stationSpinner);
        stationSpinner.setSelection(_stationAdapter.getPosition(_stationManager.getStation()));
        String background = prefs.getString(Preferences.PREF_BACKGROUND, "default");
        boolean mustUpdate = !_background.equals(background);
        _background = background;
        if (_background.equals("black")) {
            findViewById(R.id.scrollView).setBackgroundColor(Color.BLACK);
        } else if (_background.equals("default")) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                findViewById(R.id.scrollView).setBackgroundResource(R.drawable.background_land);
            } else {
                findViewById(R.id.scrollView).setBackgroundResource(R.drawable.background);
            }
        } else if (_background.equals("user_image")) {
            maybeRequestStoragePermission();
            String fileName = prefs.getString(Preferences.PREF_BACKGROUND_USER_IMAGE, "");
            // No permitimos imágenes de más de 1000 pixeles de ancho o alto, para no petar la memoria con esta imagen
            int maxW = Math.max((int) (getResources().getDisplayMetrics().widthPixels * 1.5), 1000);
            int maxH = Math.max((int) (getResources().getDisplayMetrics().heightPixels * 1.5), 1000);
            Bitmap bitmap = BitmapUtils.safeDecodeFile(new File(fileName), maxW, maxH);
            if (bitmap != null) {
                BitmapDrawable image = new BitmapDrawable(getResources(), bitmap);
                findViewById(R.id.scrollView).setBackground(image);
            }
        }
        if (mustUpdate) {
            updateLayout();
        }
        boolean predictedTimeWhyClicked = prefs.getBoolean(Preferences.PREF_PREDICTED_TIME_WHY_CLICKED, false);
        if (predictedTimeWhyClicked) {
            View predButton = findViewById(R.id.predictionHelpButton);
            if (predButton != null) {
                predButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String pref) {
        reloadPreferences();
    }

    /**
     * Devuelve un TextView con el nombre de un día para poner sobre las predicciones
     */
    private TextView getDayName(Calendar date) {
        TextView dayName = new TextView(this);
        if (date != null) {
            dayName.setText(DateUtils.weekDayFormat.format(date.getTime()));

        } else {
            dayName.setText("???");
        }
        dayName.setGravity(Gravity.CENTER_HORIZONTAL);
        if (_background.equals("black")) {
            dayName.setTextColor(Color.rgb(255, 255, 255));
        } else {
            dayName.setTextColor(Color.rgb(0, 0, 0));

        }
        return dayName;
    }

    /**
     * @param predictionTime Fecha de la última actualización
     * @return Devuelve el string que indica la fecha de la última actualización
     */
    private String getPredictionTimeString(Calendar predictionTime) {
        if (predictionTime == null) {
            return "";
        } else {
            return lastUpdateFormat.format(predictionTime.getTime());
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case DIALOG_LOADING_ID: {
                // No usar nunca esta forma, peta en 1.5, porque el show se hace dos veces:
                //dialog = ProgressDialog.show(this, "", getString(R.string.loading_data), true);
                ProgressDialog progress = new ProgressDialog(this);
                progress.setMessage(getString(R.string.loading_data));
                progress.setIndeterminate(true);
                dialog = progress;
                _dialogLoadingShown = true;
                break;
            }
            case DIALOG_PREDICTION_EXPLAIN_ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.predicted_time_explain))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            }
            default:
                if (id >= DIALOG_DAY_COMMENT_MASK) {
                    Station station = _stationManager.getStation();
                    if (station != null) {
                        int day = id - DIALOG_DAY_COMMENT_MASK - station.getId() * MAX_PREDICTED_DAYS;
                        if (day >= station.getPredictions().size()) {
                            // Avoid overflow if tapping past the last prediction.
                            return null;
                        }
                        StationPrediction prediction = station.getPredictions().get(day);
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        Calendar predictionDate = prediction.getDate();
                        String header = "";
                        if (predictionDate != null) {
                            header = DateUtils.weekDayFormat.format(predictionDate.getTime());
                        }
                        builder.setMessage(header + ":\n\n"
                                + prediction.createDescription(this))
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        return builder.create();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(getString(R.string.loading_data))
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        return builder.create();
                    }
                }
                return super.onCreateDialog(id);
        }
        return dialog;
    }

    /**
     * Actualiza la parte del layout que visualiza predicciones
     */
    private void updateLayout() {

        final LinearLayout scrolled = findViewById(R.id.scrolled);
        scrolled.removeAllViews();
        final LinearLayout predictedGroup = findViewById(R.id.predictedGroup);
        if (predictedGroup != null) { // en landscape no hay
            predictedGroup.setVisibility(LinearLayout.INVISIBLE);
        }
        final LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LayoutUtils.dips(75, this),
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        Station currentStation = _stationManager.getStation();
        if (currentStation == null) {
            // Si no hay estación elegida, nada más que hacer...
            return;
        }
        final int currentStationId = currentStation.getId();
        if (currentStation.getPredictions().size() > 0) {
            removeDialog(DIALOG_LOADING_ID);
            if (predictedGroup != null) { // en landscape no hay
                final TextView predictionTime = findViewById(R.id.predictionTime);
                predictionTime.setText(getPredictionTimeString(currentStation.getLastCreationDate()));
                predictedGroup.setVisibility(LinearLayout.VISIBLE);
            }
            currentStation.acceptPredictionVisitor(new StationPredictionVisitor() {
                @Override
                public void apply(@NonNull StationShortTermPrediction shortPred, final int index) {
                    Calendar predictionDate = shortPred.getDate();
                    if (predictionDate == null || !DateUtils.isFromToday(predictionDate)) {
                        return;
                    }
                    LinearLayout day = new LinearLayout(StationActivity.this);
                    day.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            showDialog(DIALOG_DAY_COMMENT_MASK + index + currentStationId * MAX_PREDICTED_DAYS);
                        }
                    });
                    day.setOrientation(LinearLayout.VERTICAL);
                    TextView dayName = getDayName(shortPred.getDate());
                    day.addView(dayName);
                    ImageView morningIcon = new ImageView(StationActivity.this);
                    morningIcon.setImageResource(ResourceUtils.getResource(shortPred.getSkyStateMorning(), true));
                    day.addView(morningIcon);
                    ImageView afternoonIcon = new ImageView(StationActivity.this);
                    afternoonIcon.setImageResource(ResourceUtils.getResource(shortPred.getSkyStateAfternoon(), true));
                    day.addView(afternoonIcon);
                    ImageView nightIcon = new ImageView(StationActivity.this);
                    nightIcon.setImageResource(ResourceUtils.getResource(shortPred.getSkyStateNight(), false));
                    day.addView(nightIcon);
                    TextView temps = new TextView(StationActivity.this);
                    temps.setText(shortPred.getMinTemp() + " - " + shortPred.getMaxTemp() + " C");
                    temps.setGravity(Gravity.CENTER_HORIZONTAL);
                    day.addView(temps);
                    if (DateUtils.isToday(predictionDate)) {
                        day.setBackgroundResource(R.drawable.today_bg);
                        temps.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                        dayName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    }
                    scrolled.addView(day, params);
                }

                @Override
                public void apply(@NonNull StationMediumTermPrediction medPred, final int index) {
                    Calendar predictionDate = medPred.getDate();
                    if (predictionDate == null || !DateUtils.isFromToday(predictionDate)) {
                        return;
                    }
                    LinearLayout day = new LinearLayout(StationActivity.this);
                    day.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            showDialog(DIALOG_DAY_COMMENT_MASK + index + currentStationId * MAX_PREDICTED_DAYS);
                        }
                    });
                    day.setOrientation(LinearLayout.VERTICAL);
                    TextView dayName = getDayName(medPred.getDate());
                    day.addView(dayName);
                    ImageView morningIcon = new ImageView(StationActivity.this);
                    morningIcon.setImageResource(ResourceUtils.getResource(medPred.getSkyState(), true));
                    day.addView(morningIcon);
                    TextView temps = new TextView(StationActivity.this);
                    temps.setText(medPred.getMinTemp() + " - " + medPred.getMaxTemp() + " C");
                    temps.setGravity(Gravity.CENTER_HORIZONTAL);
                    temps.setTextColor(Color.WHITE);
                    day.addView(temps);
                    scrolled.addView(day, params);
                }
            });
        } else {
            // Ahora mostramos el diálogo si hace falta, y sino borramos la marca de skip
            if (_skipDialog) {
                _skipDialog = false;
            } else {
                if (!this.isFinishing()) {
                    showDialog(DIALOG_LOADING_ID);
                    fetchThenShow(currentStation, false);
                }
            }
        }
    }

    /**
     * Inicializa la estación elegida, mirando en el GPS y sino pillando la primera.
     */
    private void initStationManager() {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String defaultStationPreference = prefs.getString(Preferences.PREF_DEFAULT_STATION, Preferences.DEFAULT_DEFAULT_STATION);
            int defaultStationFixed = Integer.valueOf(prefs.getString(Preferences.PREF_DEFAULT_STATION_FIXED, "1"));
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            _stationManager = new StationManager(locationManager, defaultStationPreference, defaultStationFixed);
            if (locationManager.getBestProvider(new Criteria(), false) == null) {
                Toast.makeText(getApplicationContext(), R.string.gps_unavailable, Toast.LENGTH_LONG).show();
            }
        } else {
            final Object[] list = (Object[]) data;
            _stationManager = (StationManager) list[0];
            _skipDialog = true;
        }
        _stationManager.setListener(this);
    }

    /**
     * Llamado por el manager cuando cambiamos de estación por GPS
     */
    @Override
    public void onStationChanged(final @NonNull Station station) {
        Log.d("OTempo", "onStationChanged");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillStationAdapter();
                Spinner stationSpinner = findViewById(R.id.stationSpinner);
                stationSpinner.setSelection(_stationAdapter.getPosition(station));
                updateLayout();
            }
        });
    }

    /**
     * Rellena un adaptador de android con las estaciones, para el combo
     */
    private void fillStationAdapter() {
        _stationAdapter.clear();
        if (_stationManager.getStation() == null) {
            _stationAdapter.add(new Station(getString(R.string.waiting_gps), -1, 0, 0));
        }
        for (Station station : Station.getKnownStations()) {
            _stationAdapter.add(station);
        }
    }

    /**
     * Utilidad para mostrar la actividad con las preferencias
     */
    private void showPreferences() {
        Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
        startActivity(settingsActivity);
    }

    /**
     * Listener del combo de estaciones
     */
    private class OnStationSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Station selected = (Station) parent.getItemAtPosition(pos);
            if (selected.getId() != -1 && selected != _stationManager.getStation()) {
                _stationManager.setStaticStation(selected);
                selected.setAccessCount(selected.getAccessCount() + 1);
                selected.setLastAccess(new Date());
                _favoritesDB.updateFavorite(selected);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // Nada que hacer
        }
    }

    private void fetchThenShow(final Station station, final boolean checkIfWasAlreadyLatest) {
        final Calendar previousPredictionTime = station.getLastCreationDate();
        Log.d("OTempo", "Starting to fetch activity data");
        FetchWorker.run(
                station,
                ProcessLifecycleOwner.get(),
                new FetchWorker.ResultListener() {
                    @Override
                    public void success() {
                        Log.d("OTempo", "Success fetching activity data");
                        updateLayout();
                        if (checkIfWasAlreadyLatest && Nullness.equals(previousPredictionTime, station.getLastCreationDate())) {
                            removeDialog(DIALOG_LOADING_ID);
                            _dialogLoadingShown = false;
                            Toast.makeText(getApplicationContext(), R.string.up_to_date, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void error() {
                        Log.e("OTempo", "Unable to fetch activity data");
                        removeDialog(DIALOG_LOADING_ID);
                        _dialogLoadingShown = false;
                        Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * @return Un objeto con el estado para recuperarlo tras la rotación de pantalla
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final Object[] list = new Object[1];
        list[0] = _stationManager;
        return list;
    }

    private void maybeRequestLocationPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private void maybeRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.d("OTempo", "Got GPS permission");
                initStationManager();
                if (_stationManager.getStation() != null) {
                    Spinner stationSpinner = findViewById(R.id.stationSpinner);
                    stationSpinner.setSelection(_stationAdapter.getPosition(_stationManager.getStation()));
                }
            } else if (permissions[i].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                reloadPreferences();
            }
        }
    }

    private String _background = "default";
    /// La actividad necesita un manager propio por si cambias de estacion a mano, no le sirve el del servicio
    private StationManager _stationManager = null;
    private ArrayAdapter<Station> _stationAdapter = null;
    private static FavoritesDB _favoritesDB = null;

    private final static int DIALOG_LOADING_ID = 0;
    private final static int DIALOG_PREDICTION_EXPLAIN_ID = 1;
    private final static int DIALOG_DAY_COMMENT_MASK = 1024; // por encima de 1024, el diálogo es el comentario de un día
    private final static int MAX_PREDICTED_DAYS = 20; // Cualquier número mayor a la cantidad de días que mostramos estará bien, preferiblemente no muy grande.

    // Lo tenemos aquí y no en dateutils porque necesita los strings para estar traducido
    private static SimpleDateFormat lastUpdateFormat = null;

    /// Usamos esto para no intentar mostrar el diálogo en los cambios de orientación
    private boolean _skipDialog = false;

    /// Marca para saber si el usuario está esperando por el UpdateServiceLegacy y debemos notificar cualquier evento que suceda
    private boolean _dialogLoadingShown = false;
}
