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
package org.otempo.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Clase que encapsula una estación meteorológica de meteogalicia
 */
public class Station {
    /**
     * @param name Nombre de la estación en Meteogalicia
     * @param id Identificador de la estación en Meteogalicia
     * @param latitude Latitud de la estación
     * @param longitude Longitud de la estación
     */
    public Station(String name, int id, double latitude, double longitude) {
        _name = name;
        _id = id;
        _latitude = latitude;
        _longitude = longitude;
    }

    /**
     * @return Latitud de la estación
     */
    public double getLatitude() {
        return _latitude;
    }

    /**
     * @return Longitud de la estación
     */
    public double getLongitude() {
        return _longitude;
    }

    @Override
    public String toString() {
        return _name;
    }

    /**
     * @return Nombre de la estación en Meteogalicia
     */
    public String getName() {
        return _name;
    }

    /**
     * @return El identificador de la estación en Meteogalicia
     */
    public int getId() {
        return _id;
    }

    /**
     * @return Recuento de accesos realizados a la estación
     */
    public int getAccessCount() {
        return _accessCount;
    }

    /**
     * Cambia el recuento de accesos realizados a la estación
     * @param accessCount recuento de accesos realizados a la estación
     */
    public void setAccessCount(int accessCount) {
        _accessCount = accessCount;
    }

    /**
     * @return Fecha del último acceso realizado a la estación
     */
    public Date getLastAccess() {
        return _lastAccess;
    }

    /**
     * Cambia la fecha del último acceso realizado a la estación
     * @param lastAccess fecha del último acceso realizado a la estación
     */
    public void setLastAccess(Date lastAccess) {
        _lastAccess = lastAccess;
    }

    /**
     * @return Las predicciones de la estación
     */
    public synchronized List<StationPrediction> getPredictions() {
        return _predictions;
    }

    /**
     * Establece las predicciones de la estación
     * @param predictions Las nuevas predicciones
     */
    public synchronized void setPredictions(List<StationPrediction> predictions) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        _predictions.clear();
        for (StationPrediction prediction: predictions) {
            if (_lastCreationDate == null || prediction.getCreationDate().after(_lastCreationDate)) {
                _lastCreationDate = prediction.getCreationDate();
            }
            if (prediction.getDate().after(yesterday)) {
                _predictions.add(prediction);
            }
        }
    }

    /**
     * Aplica un visitante a las predicciones
     * @param visitor Visitante de predicciones
     */
    public synchronized void acceptPredictionVisitor(StationPredictionVisitor visitor) {
        int index = 0;
        for (StationPrediction prediction: _predictions) {
            prediction.accept(visitor, index);
            index++;
        }
    }

    /**
     * @return la fecha de creación más reciente de todas las predicciones
     */
    public synchronized Calendar getLastCreationDate() {
        return _lastCreationDate;
    }


    /* *********** Zona estática ************ */

    /**
     * Utilidad que devuelve la lista de estaciones conocida por Meteogalicia
     */
    public static List<Station> getKnownStations() {
        return _knownStations;
    }

    /**
     * Devuelve la distancia al cuadrado entre dos coordenadas. Rápido y útil para comparar
     */
    public static double distance2(double lat1, double lng1, double lat2, double lng2) {
        double latDist = lat2-lat1;
        double lngDist = lng2-lng1;
        return latDist * latDist + lngDist * lngDist;
    }

    /**
     * Devuelve la estación más cercana a una coordenada dadas
     * @param lat Latitud de la coordenada de referencia
     * @param lng Longitud de la coordenada de referencia
     * @return La estación más próxima a (lat, long)
     */
    public static Station getClosestStation(double lat, double lng) {
        Station closest = _knownStations.get(0);
        double minDist = distance2(closest.getLatitude(), closest.getLongitude(), lat, lng);
        for (Station station: _knownStations) {
            double dist = distance2(station.getLatitude(), station.getLongitude(), lat, lng);
            if (dist < minDist) {
                minDist = dist;
                closest = station;
            }
        }
        return closest;
    }

    /**
     * @return Devuelve la estación favorita: la más accedida
     */
    public static Station getFavoriteStation() {
        Station favorite = _knownStations.get(0);
        for (Station station: _knownStations) {
            if (station.getAccessCount() > favorite.getAccessCount()) {
                favorite = station;
            }
        }
        return favorite;
    }

    /**
     * Devuelve la estación correspondiente a un ID dado
     * @param stationId ID de la estación buscada
     * @return La estación que corresponde con el ID, o null si ninguna corresponde
     */
    public static Station getById(int stationId) {
        for (Station station: _knownStations) {
            if (station.getId() == stationId) {
                return station;
            }
        }
        return null;
    }

    /**
     * Ordena las estaciones con el comparador proporcionado
     * @param comparator Comparador que indica la relación de orden entre dos estaciones
     */
    public static void sortStations(Comparator<Station> comparator) {
        Collections.sort(_knownStations, comparator);
    }


    private String _name;
    private int _id;
    private double _latitude;
    private double _longitude;
    private int _accessCount = 0; ///< Viene de BD
    private Date _lastAccess = null; ///< Viene de BD
    private List<StationPrediction> _predictions = new ArrayList<StationPrediction>();
    /// Contiene la fecha de creación más reciente de todas las predicciones
    private Calendar _lastCreationDate = null;

    private static List<Station> _knownStations = new ArrayList<Station>();
    static {
        _knownStations.add(new Station("A Coruña", 14, 43.370971, -8.395824));
        _knownStations.add(new Station("Cuntis", 39, 42.632656, -8.563430));
        _knownStations.add(new Station("Ferrol", 1, 43.488436, -8.222513));
        _knownStations.add(new Station("Fisterra", 2, 42.905119, -9.264347));
        _knownStations.add(new Station("Lalín", 10, 42.661421, -8.110960));
        _knownStations.add(new Station("Lugo", 5, 43.012132,-7.555844));
        _knownStations.add(new Station("Monforte", 4, 42.518549,-7.510687));
        _knownStations.add(new Station("O Barco", 38, 42.415461,-6.981956));
        _knownStations.add(new Station("Ortigueira", 40, 43.686337,-7.851941));
        _knownStations.add(new Station("Ourense", 8, 42.340057,-7.864653));
        _knownStations.add(new Station("Pedrafita", 30, 42.357004,-7.49027));
        _knownStations.add(new Station("Pontevedra", 11, 42.43366,-8.648051));
        _knownStations.add(new Station("Ribadeo", 6, 43.537396,-7.04303));
        _knownStations.add(new Station("Santiago", 3, 42.877929,-8.557962));
        _knownStations.add(new Station("Verín", 9, 41.940489,-7.439134));
        _knownStations.add(new Station("Vigo", 12, 42.231397,-8.712445));
        _knownStations.add(new Station("Vilagarcía", 13, 42.593922,-8.765969));
        _knownStations.add(new Station("Viveiro", 7, 43.66148,-7.594527));
    }
}
