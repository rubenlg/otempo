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
package org.otempo;

import org.otempo.model.ClosestStationComparator;
import org.otempo.model.Station;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Encapsula la lógica para cambiar de estación manualmente o automáticamente
 */
public class StationManager implements LocationListener {
    /**
     * Callback para informar que se ha cambiado de estación activa
     * @note Llamado tanto cuando se cambia a mano como en automático
     */
    public interface Listener {
        void onStationChanged(@NonNull Station station);
    }

    /**
     * Constructor
     * @param locManager Recibe un LocationManager para registrarse como Listener
     * @param defaultStationPreference Preferencia de estación por defecto, tal como se guarda en la configuración
     * @param defaultStationFixed Configuración de estación fija, en caso de que esa sea la preferencia
     */
    public StationManager(LocationManager locManager, String defaultStationPreference, int defaultStationFixed) {
        String provider = locManager.getBestProvider(new Criteria(), false);
        if (provider != null) {
            try {
                _lastLocation = locManager.getLastKnownLocation(provider);
                locManager.requestLocationUpdates(provider, MIN_UPDATE_MINUTES * 60000, MIN_UPDATE_DISTANCE, this);
            } catch (SecurityException e) {
                // No permissions... pass.
            }
        }
        if (defaultStationPreference.equals("nearest")) {
            _followLocation = true;
            final Location lastLocation = _lastLocation;
            if (lastLocation != null) {
                _station = Station.getClosestStation(lastLocation.getLatitude(), lastLocation.getLongitude());
            } else {
                // There is no known location
                _station = null;
            }
        } else if (defaultStationPreference.equals("favorite")) {
            _station = Station.getFavoriteStation();
        } else  if (defaultStationPreference.equals("fixed")) {
            _station = Station.getById(defaultStationFixed);
        }
    }

    /**
     * Obtiene la estación activa
     */
    @Nullable
    public Station getStation() {
        return _station;
    }

    /**
     * Establece la estación activa estática (no cambia al moverse el usuario)
     */
    public void setStaticStation(Station station) {
        _followLocation = false;
        _station = station;
        notifyChange();
    }


    /**
     * Permite que la estación activa sea la más cercana y vaya cambiando con la posición del usuario
     */
    public void setClosestStation() {
        _followLocation = true;
        final Location lastLocation = _lastLocation;
        if (lastLocation != null) {
            Station station = Station.getClosestStation(lastLocation.getLatitude(), lastLocation.getLongitude());
            if (station != _station) {
                _station = station;
                notifyChange();
            }
        }
    }

    /**
     * Ordena reordenar automáticamente las estaciones cada vez que cambian las coordenadas GPS
     * @param autoSortStations true si se desea reordenar, false en todos los demás casos
     */
    public void setAutoSortStations(boolean autoSortStations) {
        _autoSortStations = autoSortStations;
        final Location lastLocation = _lastLocation;
        if (_autoSortStations && lastLocation != null) {
            Station.sortStations(new ClosestStationComparator(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
    }

    /**
     * Establece un listener que recibirá avisos cuando se cambie de estación
     */
    public void setListener(Listener listener) {
        _listener = listener;
    }

    /**
     * Notifica un cambio al listener que se haya registrado
     */
    private void notifyChange() {
        if (_listener != null && _station != null) {
            _listener.onStationChanged(_station);
        }
    }

    /**
     * Recibe el aviso de nueva localización
     */
    @Override
	public void onLocationChanged(@Nullable Location location) {
        _lastLocation = location;
        if (_followLocation) {
            if (_autoSortStations && location != null) {
                Station.sortStations(new ClosestStationComparator(location.getLatitude(), location.getLongitude()));
            }
            setClosestStation();
        }
    }

    @Override
	public void onProviderDisabled(@Nullable String provider) {
    }

    @Override
	public void onProviderEnabled(@Nullable String provider) {
    }

    @Override
	public void onStatusChanged(@Nullable String provider, int status, @Nullable Bundle extras) {
    }

    @Nullable
    private Listener _listener = null; ///< Escuchador de cambios de estación
    @Nullable private Station _station = null; ///< Estación activa
    private boolean _followLocation = false; ///< Cambio automático de localización
    @Nullable private Location _lastLocation = null; ///< Última localización conocida
    private static final int MIN_UPDATE_MINUTES = 10; // 10 minutos
    private static final float MIN_UPDATE_DISTANCE = 10000.0f; // 10 Km
    private static boolean _autoSortStations = false; ///< Reordenar estaciones al moverse
}
