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
 * Comparador de estaciones por favoritismo. La estación más accedida será menor
 */
@NonNullByDefault(false)
public class FavoritesStationComparator implements Comparator<Station> {

    @Override
    public int compare(Station arg0, Station arg1) {
        Integer accessCount0 = arg0.getAccessCount();
        Integer accessCount1 = arg1.getAccessCount();
        // Ordenamos al revés, primero la más visitada, por eso invertimos
        return accessCount1.compareTo(accessCount0);
        //TODO: Utilizar también las fechas de último acceso
    }

}
