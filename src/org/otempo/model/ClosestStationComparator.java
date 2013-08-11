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

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Comparador de estaciones por distancia a unas coordenadas de latitud/longitud dadas
 * Será menor la estación cuya distancia a la coordenada dada sea menor
 * Sirve para ordenar estaciones por distancia a nuestra ubicación, por ejemplo.
 */
@NonNullByDefault(false)
public class ClosestStationComparator implements Comparator<Station> {
    /**
     * Construye el comparador
     * @param lat Latitud de la coordenada a usar como referencia
     * @param lng Longitud de la coordenada a usar como referencia
     */
    public ClosestStationComparator(double lat, double lng) {
        _lat = lat;
        _lng = lng;
    }
    @Override
	public int compare(Station arg0, Station arg1) {
        double distA = Station.distance2(arg0.getLatitude(), arg0.getLongitude(), _lat, _lng);
        double distB = Station.distance2(arg1.getLatitude(), arg1.getLongitude(), _lat, _lng);
        return Double.compare(distA, distB);
    }
    double _lat;
    double _lng;
}