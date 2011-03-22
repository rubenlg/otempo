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

/**
 * Predicción a corto plazo de una estación
 */
public class StationShortTermPrediction extends StationPrediction {
    //// Setters/getters ////
    public void setMaxTemp(int maxTemp) {
        _maxTemp = maxTemp;
    }
    public int getMaxTemp() {
        return _maxTemp;
    }
    public int getMinTemp() {
        return _minTemp;
    }
    public void setMinTemp(int minTemp) {
        _minTemp = minTemp;
    }
    public SkyState getSkyStateMorning() {
        return _skyStateMorning;
    }
    public void setSkyStateMorning(SkyState skyStateMorning) {
        _skyStateMorning = skyStateMorning;
    }
    public SkyState getSkyStateAfternoon() {
        return _skyStateAfternoon;
    }
    public void setSkyStateAfternoon(SkyState skyStateAfternoon) {
        _skyStateAfternoon = skyStateAfternoon;
    }
    public SkyState getSkyStateNight() {
        return _skyStateNight;
    }
    public void setSkyStateNight(SkyState skyStateNight) {
        _skyStateNight = skyStateNight;
    }
    public String getComment() {
        return _comment;
    }
    public void setComment(String comment) {
        _comment = comment;
    }

    @Override
    public void accept(StationPredictionVisitor visitor, int index) {
        visitor.apply(this, index);
    }

    ///////// ATTRIBUTES /////////////
    private int _maxTemp = 0; ///< Temperatura máxima del día
    private int _minTemp = 0; ///< Temperatura mínima del día

    private SkyState _skyStateMorning = null; ///< Estado del cielo por la mañana
    private SkyState _skyStateAfternoon = null; ///< Estado del cielo por la tarde
    private SkyState _skyStateNight = null; ///< Estado del cielo por la noche
    private String _comment = null; ///< Comentario de la predicción
}
