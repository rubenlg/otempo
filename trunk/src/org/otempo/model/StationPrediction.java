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

import java.util.Calendar;

import org.otempo.util.DateUtils;

/**
 * Predicción de una estación. Es una clase abstracta porque tenemos dos tipos de predicciones (corto/medio plazo) implementadas por subclases.
 */
public abstract class StationPrediction {
    /**
     * @return La fecha para la que se aplica la predicción
     */
    public Calendar getDate() {
        return _date;
    }

    /**
     * Establece la fecha para la que se aplica la predicción
     * @param date la fecha para la que se aplica la predicción
     */
    public void setDate(Calendar date) {
        _date = date;
    }

    /**
     * Establece la fecha en que se realizó la predicción
     * @param creationDate La fecha en que se realizó la predicción
     */
    public void setCreationDate(Calendar creationDate) {
        _creationDate = creationDate;
    }

    /**
     * @return La fecha en que se realizó la predicción
     */
    public Calendar getCreationDate() {
        return _creationDate;
    }

    /**
     * Devuelve la edad de una predicción en milisegundos desde que se creó hasta ahora mismo
     */
    public long getAge() {
        return DateUtils.getDifference(_creationDate, Calendar.getInstance());
    }

    /**
     * Patrón visitante
     * @param visitor Visitante de predicciones
     * @param index Índice de esta predicción en la lista de predicciones
     */
    public abstract void accept(StationPredictionVisitor visitor, int index);

    public enum SkyState {
        CLEAR,
        HIGH_CLOUDS,
        CLOUD_AND_CLEAR,
        MOSTLY_CLOUD,
        CLOUDY,
        CHUBASCO,
        RAIN,
        STORM,
        SNOW,
        DEW, // ORBALLO
        FOG
    }

    private Calendar _date = null;
    private Calendar _creationDate = null;
}