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
package org.otempo.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.otempo.StationManager;
import org.otempo.StationUpdateListener;
import org.otempo.StationManager.Listener;
import org.otempo.model.FavoritesStationComparator;
import org.otempo.model.Station;
import org.otempo.rss.MediumTermSAXHandler;
import org.otempo.rss.PredictionSAXHandler;
import org.otempo.rss.StationCache;
import org.otempo.rss.ShortTermSAXHandler;
import org.otempo.service.states.CreatedState;
import org.otempo.service.states.ServiceState;
import org.otempo.view.Preferences;
import org.otempo.view.StationWidget;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Servicio encargado de las actualizaciones periódicas
 * Coordina una máquina de estados (patrón estado) que le permite gestionar diferentes eventualidades
 */
public class UpdateService extends Service implements Listener, OnSharedPreferenceChangeListener {

    @Override
    public void onCreate() {
        initWidgetStationManager();
        _workThread = new Thread(new Loader());
        _workThread.start();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new ConnectivityReceiver(), filter);
    }

    @Override
    public void onDestroy() {
        _interrupted = true;
        _workThread.interrupt();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Thread que se queda cargando los datos
     */
    private class Loader implements Runnable {
        public void run() {
            // Condición de salida del thread: _interrupted
            while (!_interrupted) {
                if (_state != null) {
                    _state.update(UpdateService.this);
                    if (!hasConnectivity()) {
                        _binder.deliverInternetOff();
                    }
                } else {
                    stopSelf();
                    return;
                }
            }
        }
    }

    // Función de cambio de estado, para poder depurar
    public void setState(ServiceState state) {
        //Log.d("OTempo", _state.toString()+" -> "+state.toString());
        _state = state;
    }

    /**
     * Inicializa el StationManager del widget
     */
    private void initWidgetStationManager() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String defaultStationPreference = prefs.getString(Preferences.PREF_DEFAULT_STATION, Preferences.DEFAULT_DEFAULT_STATION);
        int defaultStationFixed = Integer.valueOf(prefs.getString(Preferences.PREF_DEFAULT_STATION_FIXED, "1"));
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        _widgetStationManager = new StationManager(locationManager, defaultStationPreference, defaultStationFixed);
        _widgetStationManager.setListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
        initWidgetStationManager();
        Station station = _widgetStationManager.getStation();
        if (station != null) {
            onStationChanged(station);
        }
    }

    /**
     * Llamado cuando cambia la posición del GPS
     */
    @Override
    public void onStationChanged(Station station) {
        // Sólo ordenamos actualizar si la estación a la que cambiamos ya tiene datos
        if (station.getPredictions().size() > 0) {
            updateWidget(station);
        } else if (_state != null) { // En otro caso, pedimos con prioridad cargar la estación
            _state.requestWithPriority(this, station);
        }
    }

    /**
     * Añade al final de la cola una estación
     */
    public synchronized void addStation(Station station) {
        _pendingStations.add(station);
    }

    /**
     * Añade una estación con máxima prioridad
     * @param station La estación a añadir
     */
    public synchronized void addStationMaxPrio(Station station) {
        // Si ya estaba, se quita antes de poner al principio
        if (_pendingStations.contains(station)) {
            _pendingStations.remove(station);
        }
        // Ahora se pone de primera
        _pendingStations.add(0, station);
    }

    /**
     * @return true si quedan estaciones pendientes de ser actualizadas, y false en todos los demás casos
     */
    public synchronized boolean hasPendingStations() {
        return _pendingStations.size() > 0;
    }

    /**
     * Elimina de la lista las estaciones que ya tengan predicción
     */
    private synchronized void clearPredictedStations() {
        Iterator<Station> iStation = _pendingStations.iterator();
        while (iStation.hasNext()) {
            if (iStation.next().getPredictions().size() > 0) {
                iStation.remove();
            }
        }
    }

    /**
     * @return Devuelve el recuento de estaciones pendientes de ser actualizadas
     */
    public synchronized int countPendingStations() {
        return _pendingStations.size();
    }

    /**
     * Devuelve la siguiente estación a procesar de la cola
     * @note Si no hay ninguna, se vuelve a rellenar la cola con más estaciones,
     * pero sólo en el caso de que esté permitido usar datos en segundo plano
     */
    private synchronized Station getNextStation() {
        Station station = _pendingStations.get(0);
        _pendingStations.remove(0);
        return station;
    }

    /**
     * Rellena la lista de estaciones pendientes de ser actualizadas, en base a la configuración del usuario
     */
    public synchronized void fillStations() {
    	// Número de estaciones que deseamos actualizar
        int updateAmount= Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(Preferences.PREF_UPDATE_AMOUNT, "0"));
        if (updateAmount == 0) {
            _pendingStations.addAll(Station.getKnownStations());
        } else {
        	// Las estaciones ya están ordenadas según el criterio del usuario, así que se van a actualizar las más prioritarias para él
            List<Station> known = Station.getKnownStations();
            for (int i=0; i < updateAmount && i < known.size(); i++) {
                _pendingStations.add(known.get(i));
            }
        }
        // Aunque elegimos las estaciones que más interesan al usuario, vamos a ordenarlas por frecuencia de uso, por si se corta Internet a medio camino
        Collections.sort(_pendingStations, new FavoritesStationComparator());
    }

    /**
     * Procesa la siguiente estación de la cola de pendientes
     */
    public void processNextStation(boolean forceStorage) {
        Station station = getNextStation();
        try {
            Calendar oldCreationDate = null;
            if (station.getPredictions().size() > 0) {
                oldCreationDate = station.getLastCreationDate();
            }

            // Parsing short term
            InputStream streamShortTerm = StationCache.getStationRSS(station.getId(), true, forceStorage);
            if (streamShortTerm == null) {
                throw new IOException("Station cache returned a NULL stream for short term");
            }
            PredictionSAXHandler shortTermHandler = new ShortTermSAXHandler(station);
            SAXParserFactory spfShort = SAXParserFactory.newInstance();
            SAXParser parserShort = spfShort.newSAXParser();
            parserShort.parse(streamShortTerm, shortTermHandler);
            
            // Parsing medium term
            InputStream streamMediumTerm = StationCache.getStationRSS(station.getId(), false, forceStorage);
            if (streamMediumTerm == null) {
                throw new IOException("Station cache returned a NULL stream for medium term");
            }
            PredictionSAXHandler mediumTermHandler = new MediumTermSAXHandler(station);
            SAXParserFactory spfMedium = SAXParserFactory.newInstance();
            SAXParser parserMedium = spfMedium.newSAXParser();
            parserMedium.parse(streamMediumTerm, mediumTermHandler);
            
            
            // Si la fecha de creación es la misma, ya no vamos a perder el tiempo con más estaciones de las que ya tenían predicción...
            if (station.getPredictions().size() > 0 && station.getLastCreationDate() != null && station.getLastCreationDate().equals(oldCreationDate)) {
                if (hasConnectivity()) {
                    _binder.deliverUpToDate(station);
                }
                clearPredictedStations();
            } else { // Además, sólo avisamos a la actividad y al widget si hace falta
                _binder.deliverUpdate(station);
                updateWidget(station);
            }
        } catch (MalformedURLException e) {
            Log.e("OTempo", e.getMessage(), e);
            _binder.deliverInternetError();
            addStation(station);
        } catch (IOException e) {
            Log.e("OTempo", e.getMessage(), e);
            _binder.deliverInternetError();
            // No podemos hacer un addStation porque este caso se da cuando estás sin internet, y sino se queda todo el rato lanzando internet errors :(
        } catch (ParserConfigurationException e) {
            Log.e("OTempo", e.getMessage(), e);
            _binder.deliverInternalError();
        } catch (SAXException e) {
            Log.e("OTempo", "Error parsing station "+station.getName() + ": " + e.getMessage(), e);
            _binder.deliverInternetError();
            StationCache.removeCached(station.getId(), true);
            StationCache.removeCached(station.getId(), false);
            // Si falla el parseo, ya ha sucedido que era meteogalicia que tenía el RSS petado, así que no lo volvemos a añadir en pendientes, se queda sin actualizar hasta el siguiente ciclo.
        }
    }

    /**
     * Actualiza el widget si le toca...
     */
    private void updateWidget(Station station) {
        if (_widgetStationManager.getStation() == null
                || _widgetStationManager.getStation().getPredictions().size() == 0
                || _widgetStationManager.getStation() == station) {
            StationWidget.updateStation(this, station);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return _binder;
    }

    /**
     * Conector entre la actividad y el servicio
     */
    public class UpdateServiceBinder extends Binder {
        /**
         * Envía a los listeners un aviso de error de Internet
         */
        private void deliverInternetError() {
            for (StationUpdateListener listener: _listeners) {
                listener.internetError();
            }
        }

        /**
         * Envía a los listeners un aviso de estación correctamente actualizada
         */
        private void deliverUpdate(Station station) {
            for (StationUpdateListener listener: _listeners) {
                listener.onStationUpdate(station);
            }
        }

        /**
         * Envía a los listeners un aviso de error interno
         */
        private void deliverInternalError() {
            for (StationUpdateListener listener: _listeners) {
                listener.internalError();
            }
        }

        /**
         * Envía a los listeners un aviso de indisponibilidad de Internet
         */
        private void deliverInternetOff() {
            for (StationUpdateListener listener: _listeners) {
                listener.internetOff();
            }
        }

        /**
         * Envía a los listeners un aviso de estación que ya estaba actualizada
         * @param station Estación que ya estaba actualizada
         */
        private void deliverUpToDate(Station station) {
            for (StationUpdateListener listener: _listeners) {
                listener.upToDate(station);
            }
        }

        /**
         * Añade un nuevo escuchador.
         * En activities es FUNDAMENTAL llamar a removeListener para no dejar leaks
         */
        public void addListener(StationUpdateListener listener) {
            _listeners.add(listener);
        }

        /**
         * Elimina un escuchador
         */
        public void removeListener(StationUpdateListener listener) {
            _listeners.remove(listener);
        }

        // Patrón Observador
        private Set<StationUpdateListener> _listeners = new HashSet<StationUpdateListener>();

        /**
         * Solicita que una estación se actualice con prioridad máxima frente a cualquier otra (típicamente la que está viendo el usuario)
         * @param station Estación de máxima prioridad
         */
        public void requestWithPriority(Station station) {
            if (_state != null) {
                //Log.d("OTempo", "PRIO: " + station);
                _state.requestWithPriority(UpdateService.this, station);
            }
        }
    }

    /**
     * Consulta si hay conectividad para acceder a Internet
     */
    public boolean hasConnectivity() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED;

    }

    /**
     * Consulta si hay posibilidad de usar datos en segundo plano
     */
    public boolean getBackgroundDataAllowed() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }

    /**
     * Receptor de cambios de estado en conectividad
     */
    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                boolean connectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (connectivity) {
                    if (_state != null) {
                        _state.connectivityAvailable(UpdateService.this);
                    }
                }
            }
        }
    }

    /**
     * @return Devuelve la estación que se debe mostrar en el widget de escritorio
     */
    public Station getWidgetStation() {
        Station widgetStation = _widgetStationManager.getStation();
        if (widgetStation == null) {
            widgetStation = Station.getFavoriteStation();
        }
        return widgetStation;
    }

    /// Widget displayed station
    StationManager _widgetStationManager = null;

    /// Stations pending to be processed
    private List<Station> _pendingStations = new ArrayList<Station>();

    /// Was the service thread killed?
    private boolean _interrupted = false;

    /// Binder to communicate with clients
    private UpdateServiceBinder _binder = new UpdateServiceBinder();

    /// Worker thread
    private Thread _workThread = null;

    /// Estado de la máquina, inicialmente es CREATED
    private ServiceState _state = new CreatedState();
}
