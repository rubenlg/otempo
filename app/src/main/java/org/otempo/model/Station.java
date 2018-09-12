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

import android.support.annotation.Nullable;

import android.util.SparseIntArray;

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
    @Nullable
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
    public synchronized void setPredictions(List<StationPrediction> predictions, boolean clearExisting) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        if (clearExisting) {
        	_predictions.clear();
        }
        for (StationPrediction prediction: predictions) {
        	Calendar creationDate = prediction.getCreationDate();
            if (_lastCreationDate == null || (creationDate != null && creationDate.after(_lastCreationDate))) {
                _lastCreationDate = creationDate;
            }
            Calendar predictionDate = prediction.getDate(); 
            if (predictionDate != null && predictionDate.after(yesterday)) {
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
    @Nullable
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
    @Nullable
    public static Station getById(int stationId) {
    	// Reescribe IDs antiguos. TODO(ryu): Eliminar tras un par de versiones.
    	if (isLegacyId(stationId)) {
    		stationId = _legacyIdMap.get(stationId);
    	}
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

    /**
     * Comprueba si un identificador es uno de los antiguos identificadores de localidades.
     * Para poder migrar bases de datos ya existentes.
     * TODO(ryu): Eliminar tras un par de versiones.
     * @param id Identificador a comprobar.
     * @return True si es legacy
     */
    public static boolean isLegacyId(int id) {
    	return _legacyIdMap.get(id, -1) != -1;
    }
    
    private String _name;
    private int _id;
    private double _latitude;
    private double _longitude;
    private int _accessCount = 0; ///< Viene de BD
    @Nullable private Date _lastAccess = null; ///< Viene de BD
    private final List<StationPrediction> _predictions = new ArrayList<>();
    /// Contiene la fecha de creación más reciente de todas las predicciones
    @Nullable private Calendar _lastCreationDate = null;

    private static final List<Station> _knownStations = new ArrayList<>();
    
    // Mapa de identificador viejo a identificador nuevo para las antiguas localidades de Meteogalicia
    // TODO(ryu): Eliminar después de un par de versiones
    private static final SparseIntArray _legacyIdMap = new SparseIntArray();
    
    static {
    	// Provincia de A Coruña
    	_knownStations.add(new Station("Abegondo", 15001, 43.220064, -8.296572));
    	_knownStations.add(new Station("Ames", 15002, 42.891058, -8.655561));
    	_knownStations.add(new Station("Aranga", 15003, 43.087931, -8.183142));
    	_knownStations.add(new Station("Ares", 15004, 43.429518, -8.244127));
    	_knownStations.add(new Station("Arteixo", 15005, 43.304257, -8.510931));
    	_knownStations.add(new Station("Arzúa", 15006, 42.930316, -8.161732));
    	_knownStations.add(new Station("A Baña", 15007, 42.961989, -8.758187));
    	_knownStations.add(new Station("Bergondo", 15008, 43.309878, -8.233144));
    	_knownStations.add(new Station("Betanzos", 15009, 43.278924, -8.212244));
    	_knownStations.add(new Station("Boimorto", 15010, 43.001054, -8.133083));
    	_knownStations.add(new Station("Boiro", 15011, 42.646287, -8.884336));
    	_knownStations.add(new Station("Boqueixón", 15012, 42.811593, -8.414553));
    	_knownStations.add(new Station("Brión", 15013, 42.866686, -8.6786));
    	_knownStations.add(new Station("Cabana de Bergantiños", 15014, 43.1861, -8.89143));
    	_knownStations.add(new Station("Cabanas", 15015, 43.419029, -8.162361));
    	_knownStations.add(new Station("Camariñas", 15016, 43.13209, -9.182695));
    	_knownStations.add(new Station("Cambre", 15017, 43.292517, -8.341199));
    	_knownStations.add(new Station("A Capela", 15018, 43.5859, -7.770619));
    	_knownStations.add(new Station("Carballo", 15019, 43.213285, -8.692194));
    	_knownStations.add(new Station("Cariño", 15901, 43.741389, -7.869167));
    	_knownStations.add(new Station("Carnota", 15020, 42.82021, -9.089624));
    	_knownStations.add(new Station("Carral", 15021, 43.229414, -8.355094));
    	_knownStations.add(new Station("Cedeira", 15022, 43.65, -8.05));
    	_knownStations.add(new Station("Cee", 15023, 42.955556, -9.19));
    	_knownStations.add(new Station("Cerceda", 15024, 43.188611, -8.470278));
    	_knownStations.add(new Station("Cerdido", 15025, 43.606895, -7.952601));
    	_knownStations.add(new Station("Cesuras", 15026, 43.166667, -8.2));
    	_knownStations.add(new Station("Coirós", 15027, 43.252377, -8.16853));
    	_knownStations.add(new Station("Corcubión", 15028, 42.945833, -9.193333));
    	_knownStations.add(new Station("Coristanco", 15029, 43.190527, -8.760257));
    	_knownStations.add(new Station("A Coruña", 15030, 43.370971, -8.395824));
    	_knownStations.add(new Station("Culleredo", 15031, 43.288369, -8.388858));
    	_knownStations.add(new Station("Curtis", 15032, 43.124182, -8.145956));
    	_knownStations.add(new Station("Dodro", 15033, 42.7169, -8.71508));
    	_knownStations.add(new Station("Dumbría", 15034, 43.01159, -9.118804));
    	_knownStations.add(new Station("Fene", 15035, 43.466667, -8.166667));
    	_knownStations.add(new Station("Ferrol", 15036, 43.488436, -8.222513));
    	_knownStations.add(new Station("Fisterra", 15037, 42.905119, -9.264347));
    	_knownStations.add(new Station("Frades", 15038, 43.0188, -8.252322));
    	_knownStations.add(new Station("Irixoa", 15039, 43.284055, -8.061392));
    	_knownStations.add(new Station("A Laracha", 15041, 43.248611, -8.583333));
    	_knownStations.add(new Station("Laxe", 15040, 43.219722, -9.005));
    	_knownStations.add(new Station("Lousame", 15042, 42.758889, -8.829444));
    	_knownStations.add(new Station("Malpica de Bergantiños", 15043, 43.321853, -8.81344));
    	_knownStations.add(new Station("Mañón", 15044, 43.736111, -7.705556));
    	_knownStations.add(new Station("Mazaricos", 15045, 42.936111, -8.991389));
    	_knownStations.add(new Station("Melide", 15046, 42.916667, -8.016667));
    	_knownStations.add(new Station("Mesía", 15047, 43.1, -8.266667));
    	_knownStations.add(new Station("Miño", 15048, 43.345311, -8.203679));
    	_knownStations.add(new Station("Moeche", 15049, 43.5505, -7.99122));
    	_knownStations.add(new Station("Monfero", 15050, 43.3249, -8.05468));
    	_knownStations.add(new Station("Mugardos", 15051, 43.460556, -8.253611));
    	_knownStations.add(new Station("Muros", 15053, 43.373333, -7.908056));
    	_knownStations.add(new Station("Muxía", 15052, 43.104722, -9.218056));
    	_knownStations.add(new Station("Narón", 15054, 43.537244, -8.180392));
    	_knownStations.add(new Station("Neda", 15055, 43.5013, -8.15627));
    	_knownStations.add(new Station("Negreira", 15056, 42.9095, -8.73625));
    	_knownStations.add(new Station("Noia", 15057, 42.785, -8.887778));
    	_knownStations.add(new Station("Oleiros", 15058, 43.333333, -8.3));
    	_knownStations.add(new Station("Ordes", 15059, 43.076667, -8.407222));
    	_knownStations.add(new Station("Oroso", 15060, 42.983333, -8.433333));
    	_knownStations.add(new Station("Ortigueira", 15061, 43.686337, -7.851941));
    	_knownStations.add(new Station("Outes", 15062, 42.851111, -8.926389));
    	_knownStations.add(new Station("Oza dos Ríos", 15063, 43.216667, -8.183056));
    	_knownStations.add(new Station("Paderne", 15064, 43.283333, -8.174444));
    	_knownStations.add(new Station("Padrón", 15065, 42.739, -8.66054));
    	_knownStations.add(new Station("O Pino", 15066, 42.904772, -8.362344));
    	_knownStations.add(new Station("Pobra do Caramiñal", 15067, 42.6, -8.933333));
    	_knownStations.add(new Station("Ponteceso", 15068, 43.2427, -8.90096));
    	_knownStations.add(new Station("Pontedeume", 15069, 43.4026, -8.15269));
    	_knownStations.add(new Station("As Pontes de García Rodríguez", 15070, 43.433333, -7.833333));
    	_knownStations.add(new Station("Porto do Son", 15071, 43.15, -9.116667));
    	_knownStations.add(new Station("Rianxo", 15072, 42.65, -8.816667));
    	_knownStations.add(new Station("Ribeira", 15073, 42.55, -8.983333));
    	_knownStations.add(new Station("Rois", 15074, 42.778519, -8.723217));
    	_knownStations.add(new Station("Sada", 15075, 43.35, -8.25));
    	_knownStations.add(new Station("San Sadurniño", 15076, 43.5625, -8.05499));
    	_knownStations.add(new Station("Santa Comba", 15077, 43.033333, -8.816667));
    	_knownStations.add(new Station("Santiago de Compostela", 15078, 42.877929, -8.557962));
    	_knownStations.add(new Station("Santiso", 15079, 42.8621, -8.05743));
    	_knownStations.add(new Station("Sobrado", 15080, 43.04, -8.028889));
    	_knownStations.add(new Station("As Somozas", 15081, 43.536053, -7.924339));
    	_knownStations.add(new Station("Teo", 15082, 42.75, -8.5));
    	_knownStations.add(new Station("Toques", 15083, 42.967778, -7.988056));
    	_knownStations.add(new Station("Tordoia", 15084, 43.08805, -8.559722));
    	_knownStations.add(new Station("Touro", 15085, 42.866667, -8.283333));
    	_knownStations.add(new Station("Trazo", 15086, 43.016667, -8.533333));
    	_knownStations.add(new Station("Val do Dubra", 15088, 43.0225, -8.638333));
    	_knownStations.add(new Station("Valdoviño", 15087, 43.6, -8.133056));
    	_knownStations.add(new Station("Vedra", 15089, 42.783333, -8.466667));
    	_knownStations.add(new Station("Vilarmaior", 15091, 43.340556, -8.155556));
    	_knownStations.add(new Station("Vilasantar", 15090, 43.066667, -8.1));
    	_knownStations.add(new Station("Vimianzo", 15092, 43.11, -9.034444));
    	_knownStations.add(new Station("Zas", 15093, 43.098868, -8.915439));

    	// Provincia de Lugo
    	_knownStations.add(new Station("Abadín", 27001, 43.363238, -7.475372));
    	_knownStations.add(new Station("Alfoz", 27002, 43.504114, -7.430217));
    	_knownStations.add(new Station("Antas de Ulla", 27003, 42.782866, -7.891534));
    	_knownStations.add(new Station("Baleira", 27004, 43.016195, -7.245937));
    	_knownStations.add(new Station("Baralla", 27901, 42.894265, -7.250937));
    	_knownStations.add(new Station("Barreiros", 27005, 43.516891, -7.224222));
    	_knownStations.add(new Station("Becerreá", 27006, 42.852813, -7.159763));
    	_knownStations.add(new Station("Begonte", 27007, 43.150487, -7.683717));
    	_knownStations.add(new Station("Bóveda", 27008, 42.623381, -7.483069));
    	_knownStations.add(new Station("Burela", 27902, 43.660141, -7.359344));
    	_knownStations.add(new Station("Carballedo", 27009, 43.055664, -7.345478));
    	_knownStations.add(new Station("Castro de Rei", 27010, 43.209141, -7.400118));
    	_knownStations.add(new Station("Castroverde", 27011, 43.030055, -7.325231));
    	_knownStations.add(new Station("Cervantes", 27012, 42.86958, -7.060733));
    	_knownStations.add(new Station("Cervo", 27013, 43.6711, -7.409678));
    	_knownStations.add(new Station("Chantada", 27016, 42.609643, -7.769995));
    	_knownStations.add(new Station("O Corgo", 27014, 42.943324, -7.432144));
    	_knownStations.add(new Station("Cospeito", 27015, 43.213997, -7.561169));
    	_knownStations.add(new Station("Folgoso do Courel", 27017, 42.589015, -7.195165));
    	_knownStations.add(new Station("A Fonsagrada", 27018, 43.123446, -7.068286));
    	_knownStations.add(new Station("Foz", 27019, 43.5694, -7.257049));
    	_knownStations.add(new Station("Friol", 27020, 43.031788, -7.796302));
    	_knownStations.add(new Station("Guitiriz", 27022, 43.181648, -7.893076));
    	_knownStations.add(new Station("Guntín", 27023, 42.888495, -7.700343));
    	_knownStations.add(new Station("O Incio", 27024, 42.656513, -7.362846));
    	_knownStations.add(new Station("Láncara", 27026, 42.863713, -7.337258));
    	_knownStations.add(new Station("Lourenzá", 27027, 43.472013, -7.301402));
    	_knownStations.add(new Station("Lugo", 27028, 43.012132, -7.555844));
    	_knownStations.add(new Station("Meira", 27029, 43.213496, -7.29445));
    	_knownStations.add(new Station("Mondoñedo", 27030, 43.42852, -7.363715));
    	_knownStations.add(new Station("Monforte de Lemos", 27031, 42.518549, -7.510687));
    	_knownStations.add(new Station("Monterroso", 27032, 42.793322, -7.833767));
    	_knownStations.add(new Station("Muras", 27033, 43.466391, -7.725449));
    	_knownStations.add(new Station("Navia de Suarna", 27034, 42.964745, -7.004128));
    	_knownStations.add(new Station("Negueira de Muñiz", 27035, 43.134549, -6.893653));
    	_knownStations.add(new Station("As Nogais", 27037, 42.809255, -7.109292));
    	_knownStations.add(new Station("Ourol", 27038, 43.564503, -7.642794));
    	_knownStations.add(new Station("Outeiro de Rei", 27039, 43.103819, -7.613633));
    	_knownStations.add(new Station("Palas de Rei", 27040, 42.884769, -7.849045));
    	_knownStations.add(new Station("Pantón", 27041, 42.513994, -7.619405));
    	_knownStations.add(new Station("Paradela", 27042, 42.764469, -7.567799));
    	_knownStations.add(new Station("O Páramo", 27043, 42.840896, -7.497504));
    	_knownStations.add(new Station("A Pastoriza", 27044, 43.305943, -7.36496));
    	_knownStations.add(new Station("Pedrafita do Cebreiro", 27045, 42.357004, -7.49027));
    	_knownStations.add(new Station("A Pobra do Brollón", 27047, 42.556787, -7.392136));
    	_knownStations.add(new Station("Pol", 27046, 43.149438, -7.32949));
    	_knownStations.add(new Station("A Pontenova", 27048, 43.348213, -7.192612));
    	_knownStations.add(new Station("Portomarín", 27049, 42.807476, -7.616229));
    	_knownStations.add(new Station("Quiroga", 27050, 42.475705, -7.269387));
    	_knownStations.add(new Station("Rábade", 27056, 43.121974, -7.623568));
    	_knownStations.add(new Station("Ribadeo", 27051, 43.537396, -7.04303));
    	_knownStations.add(new Station("Ribas de Sil", 27052, 42.466565, -7.287959));
    	_knownStations.add(new Station("Ribeira de Piquín", 27053, 43.196166, -7.197762));
    	_knownStations.add(new Station("Riotorto", 27054, 43.344718, -7.261877));
    	_knownStations.add(new Station("Samos", 27055, 42.73052, -7.326669));
    	_knownStations.add(new Station("Sarria", 27057, 42.780063, -7.413583));
    	_knownStations.add(new Station("O Saviñao", 27058, 42.644566, -7.654381));
    	_knownStations.add(new Station("Sober", 27059, 42.461571, -7.586875));
    	_knownStations.add(new Station("Taboada", 27060, 42.715426, -7.762313));
    	_knownStations.add(new Station("Trabada", 27061, 43.446812, -7.194135));
    	_knownStations.add(new Station("Triacastela", 27062, 42.756561, -7.239518));
    	_knownStations.add(new Station("O Valadouro", 27063, 43.55029, -7.441349));
    	_knownStations.add(new Station("O Vicedo", 27064, 43.732344, -7.673435));
    	_knownStations.add(new Station("Vilalba", 27065, 43.297323, -7.680774));
    	_knownStations.add(new Station("Viveiro", 27066, 43.66148, -7.594527));
    	_knownStations.add(new Station("Xermade", 27021, 43.355562, -7.814337));
    	_knownStations.add(new Station("Xove", 27025, 43.684788, -7.512481));

    	// Provincia de Ourense
    	_knownStations.add(new Station("Allariz", 32001, 42.190214, -7.801759));
    	_knownStations.add(new Station("Amoeiro", 32002, 42.414792, -7.94535));
    	_knownStations.add(new Station("A Arnoia", 32003, 42.256936, -8.139714));
    	_knownStations.add(new Station("Avión", 32004, 42.374969, -8.250552));
    	_knownStations.add(new Station("Baltar", 32005, 41.951368, -7.716246));
    	_knownStations.add(new Station("Bande", 32006, 42.032369, -7.974082));
    	_knownStations.add(new Station("Baños de Molgas", 32007, 42.241656, -7.672344));
    	_knownStations.add(new Station("Barbadás", 32008, 42.298596, -7.887273));
    	_knownStations.add(new Station("O Barco de Valdeorras", 32009, 42.415461, -6.981956));
    	_knownStations.add(new Station("Beade", 32010, 42.329648, -8.127204));
    	_knownStations.add(new Station("Beariz", 32011, 42.467538, -8.273331));
    	_knownStations.add(new Station("Os Blancos", 32012, 41.998523, -7.752145));
    	_knownStations.add(new Station("Boborás", 32013, 42.432793, -8.142684));
    	_knownStations.add(new Station("A Bola", 32014, 42.156588, -7.912207));
    	_knownStations.add(new Station("O Bolo", 32015, 42.307403, -7.100581));
    	_knownStations.add(new Station("Calvos de Randín", 32016, 41.945958, -7.896318));
    	_knownStations.add(new Station("Carballeda de Avia", 32018, 42.320034, -8.165395));
    	_knownStations.add(new Station("Carballeda de Valdeorras", 32017, 42.350679, -6.848997));
    	_knownStations.add(new Station("O Carballiño", 32019, 42.458927, -8.087997));
    	_knownStations.add(new Station("Cartelle", 32020, 42.250138, -8.070405));
    	_knownStations.add(new Station("Castrelo de Miño", 32022, 42.290707, -8.122379));
    	_knownStations.add(new Station("Castrelo do Val", 32021, 41.991219, -7.424336));
    	_knownStations.add(new Station("Castro Caldelas", 32023, 42.37443, -7.415024));
    	_knownStations.add(new Station("Celanova", 32024, 42.152492, -7.957584));
    	_knownStations.add(new Station("Cenlle", 32025, 42.342845, -8.088044));
    	_knownStations.add(new Station("Chandrexa de Queixa", 32029, 42.254498, -7.381557));
    	_knownStations.add(new Station("Coles", 32026, 42.40157, -7.83688));
    	_knownStations.add(new Station("Cortegada", 32027, 42.206555, -8.170745));
    	_knownStations.add(new Station("Cualedro", 32028, 41.98854, -7.593873));
    	_knownStations.add(new Station("Entrimo", 32030, 41.932837, -8.116386));
    	_knownStations.add(new Station("Esgos", 32031, 42.32519, -7.696223));
    	_knownStations.add(new Station("Gomesende", 32033, 42.163133, -8.10349));
    	_knownStations.add(new Station("A Gudiña", 32034, 42.061178, -7.138027));
    	_knownStations.add(new Station("O Irixo", 32035, 42.512143, -8.118213));
    	_knownStations.add(new Station("Larouco", 32038, 42.347031, -7.162295));
    	_knownStations.add(new Station("Laza", 32039, 42.060844, -7.461394));
    	_knownStations.add(new Station("Leiro", 32040, 42.370498, -8.125144));
    	_knownStations.add(new Station("Lobeira", 32041, 41.986546, -8.039442));
    	_knownStations.add(new Station("Lobios", 32042, 41.901175, -8.083409));
    	_knownStations.add(new Station("Maceda", 32043, 42.270736, -7.651984));
    	_knownStations.add(new Station("Manzaneda", 32044, 42.310093, -7.234713));
    	_knownStations.add(new Station("Maside", 32045, 42.412701, -8.026524));
    	_knownStations.add(new Station("Melón", 32046, 42.258731, -8.213893));
    	_knownStations.add(new Station("A Merca", 32047, 42.223782, -7.904023));
    	_knownStations.add(new Station("A Mezquita", 32048, 42.238272, -7.869093));
    	_knownStations.add(new Station("Montederramo", 32049, 42.275904, -7.502634));
    	_knownStations.add(new Station("Monterrei", 32050, 41.947043, -7.449354));
    	_knownStations.add(new Station("Muíños", 32051, 41.954272, -7.98482));
    	_knownStations.add(new Station("Nogueira de Ramuín", 32052, 42.413097, -7.725627));
    	_knownStations.add(new Station("Oímbra", 32053, 41.886832, -7.46899));
    	_knownStations.add(new Station("Ourense", 32054, 42.340057, -7.864653));
    	_knownStations.add(new Station("Paderne de Allariz", 32055, 42.273117, -7.752878));
    	_knownStations.add(new Station("Padrenda", 32056, 42.133333, -8.15));
    	_knownStations.add(new Station("Parada de Sil", 32057, 42.383056, -7.568889));
    	_knownStations.add(new Station("O Pereiro de Aguiar", 32058, 42.333333, -7.8));
    	_knownStations.add(new Station("A Peroxa", 32059, 42.438889, -7.793333));
    	_knownStations.add(new Station("Petín", 32060, 42.382222, -7.125833));
    	_knownStations.add(new Station("Piñor", 32061, 42.497778, -8.005));
    	_knownStations.add(new Station("A Pobra de Trives", 32063, 42.339444, -7.253056));
    	_knownStations.add(new Station("Pontedeva", 32064, 42.168611, -8.139167));
    	_knownStations.add(new Station("Porqueira", 32062, 42.017778, -7.844444));
    	_knownStations.add(new Station("Punxín", 32065, 42.370278, -8.011944));
    	_knownStations.add(new Station("Quintela de Leirado", 32066, 42.138333, -8.101667));
    	_knownStations.add(new Station("Rairiz de Veiga", 32067, 42.083056, -7.832222));
    	_knownStations.add(new Station("Ramirás", 32068, 42.283611, -8.018611));
    	_knownStations.add(new Station("Ribadavia", 32069, 42.287778, -8.1425));
    	_knownStations.add(new Station("Riós", 32071, 41.974167, -7.2825));
    	_knownStations.add(new Station("A Rúa", 32072, 42.4, -7.1));
    	_knownStations.add(new Station("Rubiá", 32073, 42.449722, -6.948889));
    	_knownStations.add(new Station("San Amaro", 32074, 42.373056, -8.073056));
    	_knownStations.add(new Station("San Cibrao das Viñas", 32075, 42.294739, -7.872841));
    	_knownStations.add(new Station("San Cristovo de Cea", 32076, 42.4725, -7.981944));
    	_knownStations.add(new Station("San Xoán de Río", 32070, 42.384444, -7.314167));
    	_knownStations.add(new Station("Sandiás", 32077, 42.111111, -7.756667));
    	_knownStations.add(new Station("Sarreaus", 32078, 42.086667, -7.603333));
    	_knownStations.add(new Station("Taboadela", 32079, 42.240833, -7.825));
    	_knownStations.add(new Station("A Teixeira", 32080, 42.391722, -7.472302));
    	_knownStations.add(new Station("Toén", 32081, 42.314722, -7.953889));
    	_knownStations.add(new Station("Trasmiras", 32082, 42.022778, -7.616667));
    	_knownStations.add(new Station("A Veiga", 32083, 42.249722, -7.025833));
    	_knownStations.add(new Station("Verea", 32084, 42.093889, -7.993611));
    	_knownStations.add(new Station("Verín", 32085, 41.940489, -7.439134));
    	_knownStations.add(new Station("Viana do Bolo", 32086, 42.183282, -7.109405));
    	_knownStations.add(new Station("Vilamarín", 32087, 42.464167, -7.89));
    	_knownStations.add(new Station("Vilamartín de Valdeorras", 32088, 42.415556, -7.059167));
    	_knownStations.add(new Station("Vilar de Barrio", 32089, 42.159722, -7.611667));
    	_knownStations.add(new Station("Vilar de Santos", 32090, 42.085556, -7.796667));
    	_knownStations.add(new Station("Vilardevós", 32091, 41.906944, -7.313056));
    	_knownStations.add(new Station("Vilariño de Conso", 32092, 42.176944, -7.171111));
    	_knownStations.add(new Station("Xinzo de Limia", 32032, 42.063611, -7.723889));
    	_knownStations.add(new Station("Xunqueira de Ambía", 32036, 42.205556, -7.735556));
    	_knownStations.add(new Station("Xunqueira de Espadanedo", 32037, 42.3175, -7.628611));

    	// Provincia de Pontevedra
    	_knownStations.add(new Station("Agolada", 36020, 42.762043, -8.019851));
    	_knownStations.add(new Station("Arbo", 36001, 42.11142, -8.311633));
    	_knownStations.add(new Station("Baiona", 36003, 42.119426, -8.853013));
    	_knownStations.add(new Station("Barro", 36002, 42.554424, -8.622188));
    	_knownStations.add(new Station("Bueu", 36004, 42.325238, -8.785207));
    	_knownStations.add(new Station("Caldas de Reis", 36005, 42.604558, -8.641268));
    	_knownStations.add(new Station("Cambados", 36006, 42.513646, -8.813144));
    	_knownStations.add(new Station("Campo Lameiro", 36007, 42.540402, -8.543847));
    	_knownStations.add(new Station("Cangas", 36008, 42.264543, -8.782117));
    	_knownStations.add(new Station("A Cañiza", 36009, 42.212499, -8.272966));
    	_knownStations.add(new Station("Catoira", 36010, 42.66707, -8.722464));
    	_knownStations.add(new Station("Cerdedo", 36011, 42.532908, -8.390649));
    	_knownStations.add(new Station("Cotobade", 36012, 42.222463, -8.491253));
    	_knownStations.add(new Station("Covelo", 36013, 42.231981, -8.363775));
    	_knownStations.add(new Station("Crecente", 36014, 42.152014, -8.224046));
    	_knownStations.add(new Station("Cuntis", 36015, 42.632656, -8.56343));
    	_knownStations.add(new Station("Dozón", 36016, 42.584473, -8.022618));
    	_knownStations.add(new Station("A Estrada", 36017, 42.689596, -8.490593));
    	_knownStations.add(new Station("Forcarei", 36018, 42.589536, -8.351329));
    	_knownStations.add(new Station("Fornelos de Montes", 36019, 42.339625, -8.452459));
    	_knownStations.add(new Station("Gondomar", 36021, 42.111515, -8.760913));
    	_knownStations.add(new Station("O Grove", 36022, 42.49539, -8.865673));
    	_knownStations.add(new Station("A Guarda", 36023, 41.906813, -8.864521));
    	_knownStations.add(new Station("A Illa de Arousa", 36901, 42.556621, -8.867525));
    	_knownStations.add(new Station("Lalín", 36024, 42.661421, -8.11096));
    	_knownStations.add(new Station("A Lama", 36025, 42.39703, -8.442566));
    	_knownStations.add(new Station("Marín", 36026, 42.391452, -8.701607));
    	_knownStations.add(new Station("Meaño", 36027, 42.439769, -8.775465));
    	_knownStations.add(new Station("Meis", 36028, 42.515037, -8.691994));
    	_knownStations.add(new Station("Moaña", 36029, 42.282262, -8.736719));
    	_knownStations.add(new Station("Mondariz", 36030, 42.233045, -8.454326));
    	_knownStations.add(new Station("Mondariz-Balneario", 36031, 42.226055, -8.469239));
    	_knownStations.add(new Station("Moraña", 36032, 42.571161, -8.583438));
    	_knownStations.add(new Station("Mos", 36033, 42.591669, -8.584916));
    	_knownStations.add(new Station("As Neves", 36034, 42.087809, -8.414865));
    	_knownStations.add(new Station("Nigrán", 36035, 42.149405, -8.809417));
    	_knownStations.add(new Station("Oia", 36036, 42.184427, -8.803059));
    	_knownStations.add(new Station("Pazos de Borbén", 36037, 42.292453, -8.530504));
    	_knownStations.add(new Station("Poio", 36041, 42.434322, -8.65822));
    	_knownStations.add(new Station("Ponte Caldelas", 36043, 42.389907, -8.502529));
    	_knownStations.add(new Station("Ponteareas", 36042, 42.175061, -8.504558));
    	_knownStations.add(new Station("Pontecesures", 36044, 42.717885, -8.651954));
    	_knownStations.add(new Station("Pontevedra", 36038, 42.43366, -8.648051));
    	_knownStations.add(new Station("O Porriño", 36039, 42.16035, -8.618015));
    	_knownStations.add(new Station("Portas", 36040, 42.579598, -8.66882));
    	_knownStations.add(new Station("Redondela", 36045, 42.28223, -8.609726));
    	_knownStations.add(new Station("Ribadumia", 36046, 42.518912, -8.751408));
    	_knownStations.add(new Station("Rodeiro", 36047, 42.648654, -7.946037));
    	_knownStations.add(new Station("O Rosal", 36048, 41.937371, -8.83668));
    	_knownStations.add(new Station("Salceda de Caselas", 36049, 42.100388, -8.560802));
    	_knownStations.add(new Station("Salvaterra de Miño", 36050, 42.110958, -8.474974));
    	_knownStations.add(new Station("Sanxenxo", 36051, 42.403242, -8.811943));
    	_knownStations.add(new Station("Silleda", 36052, 42.701582, -8.24516));
    	_knownStations.add(new Station("Soutomaior", 36053, 42.338864, -8.569016));
    	_knownStations.add(new Station("Tomiño", 36054, 42.002111, -8.737616));
    	_knownStations.add(new Station("Tui", 36055, 42.049038, -8.646805));
    	_knownStations.add(new Station("Valga", 36056, 42.700368, -8.648999));
    	_knownStations.add(new Station("Vigo", 36057, 42.231397, -8.712445));
    	_knownStations.add(new Station("Vila de Cruces", 36059, 42.793574, -8.167287));
    	_knownStations.add(new Station("Vilaboa", 36058, 42.348887, -8.643754));
    	_knownStations.add(new Station("Vilagarcía de Arousa", 36060, 42.593922, -8.765969));
    	_knownStations.add(new Station("Vilanova de Arousa", 36061, 42.562753, -8.827561));
    	
        // TODO(ryu): Eliminar después de un par de versiones
        _legacyIdMap.put(14, 15030); // A Coruña
        _legacyIdMap.put(39, 36015); // Cuntis
        _legacyIdMap.put(1, 15036); // Ferrol
        _legacyIdMap.put(2, 15037); // Fisterra
        _legacyIdMap.put(10, 36024); // Lalín
        _legacyIdMap.put(5, 27028); // Lugo
        _legacyIdMap.put(4, 27031); // Monforte
        _legacyIdMap.put(38, 32009); // O Barco
        _legacyIdMap.put(40, 15061); // Ortigueira
        _legacyIdMap.put(8, 32054); // Ourense
        _legacyIdMap.put(30, 27045); // Pedrafita
        _legacyIdMap.put(11, 36038); // Pontevedra
        _legacyIdMap.put(6, 27051); // Ribadeo
        _legacyIdMap.put(3, 15078); // Santiago
        _legacyIdMap.put(9, 32085); // Verín
        _legacyIdMap.put(12, 36057); // Vigo
        _legacyIdMap.put(13, 36060); // Vilagarcía
        _legacyIdMap.put(7, 27066); // Viveiro
        
    }
}
