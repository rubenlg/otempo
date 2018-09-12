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

import android.support.annotation.Nullable;
import org.otempo.R;

import android.content.Context;

/**
 * Predicción de una estación. Es una clase abstracta porque tenemos dos tipos de predicciones (corto/medio plazo) implementadas por subclases.
 */
public abstract class StationPrediction {
    /**
     * @return La fecha para la que se aplica la predicción
     */
    public @Nullable Calendar getDate() {
        return _date;
    }

    /**
     * Establece la fecha para la que se aplica la predicción
     * @param date la fecha para la que se aplica la predicción
     */
    public void setDate(@Nullable Calendar date) {
        _date = date;
    }

    /**
     * Establece la fecha en que se realizó la predicción
     * @param creationDate La fecha en que se realizó la predicción
     */
    public void setCreationDate(@Nullable Calendar creationDate) {
        _creationDate = creationDate;
    }

    /**
     * @return La fecha en que se realizó la predicción
     */
    public @Nullable Calendar getCreationDate() {
        return _creationDate;
    }

    public void setMaxTemp(int maxTemp) { _maxTemp = maxTemp; }
    public int getMaxTemp() { return _maxTemp; }
    public int getMinTemp() { return _minTemp; }
    public void setMinTemp(int minTemp) { _minTemp = minTemp; }

	int skyStateDescriptionResId(@Nullable SkyState state) {
		if (state == null) {
			return R.string.sky_unknown;
		}
        switch(state) {
        case CLEAR: return R.string.sky_clear;
        case CLOUD_AND_CLEAR: return R.string.sky_cloud_and_clear;
        case HIGH_CLOUDS: return R.string.sky_high_clouds;
        case MOSTLY_CLOUDY: return R.string.sky_mostly_cloudy;
        case CLOUDY: return R.string.sky_cloudy;
        case DEW: return R.string.sky_dew;
        case SHOWER: return R.string.sky_shower;
        case RAIN: return R.string.sky_rain;
        case STORM: return R.string.sky_storm;
        case FOG: return R.string.sky_fog;
        case FOG_PATCHES: return R.string.sky_fog_patches;
        case HAZE: return R.string.sky_haze;
        case SNOW: return R.string.sky_snow;
        case HAIL: return R.string.sky_hail;
        case LIGHT_RAIN: return R.string.sky_light_rain;
        case LIGHT_SHOWER: return R.string.sky_light_shower;
        case LIGHT_STORM: return R.string.sky_light_storm;
        case MEDIUM_CLOUDS: return R.string.sky_medium_clouds;
        case SHOWER_SNOW: return R.string.sky_shower_snow;
        case SLEET: return R.string.sky_sleet;
        default: return R.string.sky_unknown;
        }
	}
	
	int windStateDescriptionResId(@Nullable WindState state) {
		if (state == null) {
			return R.string.wind_unknown;
		}
		switch (state) {
		case VARIABLE: return R.string.wind_variable;
		case CALM: return R.string.wind_calm;

		case LIGHT_EAST: return R.string.wind_light_east;
		case LIGHT_NORTH: return R.string.wind_light_north;
		case LIGHT_NORTHEAST: return R.string.wind_light_northeast;
		case LIGHT_NORTHWEST: return R.string.wind_light_northwest;
		case LIGHT_SOUTH: return R.string.wind_light_south;
		case LIGHT_SOUTHEAST: return R.string.wind_light_southeast;
		case LIGHT_SOUTHWEST: return R.string.wind_light_southwest;
		case LIGHT_WEST: return R.string.wind_light_west;

		case MILD_EAST: return R.string.wind_mild_east;
		case MILD_NORTH: return R.string.wind_mild_north;
		case MILD_NORTHEAST: return R.string.wind_mild_northeast;
		case MILD_NORTHWEST: return R.string.wind_mild_northwest;
		case MILD_SOUTH: return R.string.wind_mild_south;
		case MILD_SOUTHEAST: return R.string.wind_mild_southeast;
		case MILD_SOUTHWEST: return R.string.wind_mild_southwest;
		case MILD_WEST: return R.string.wind_mild_west;

		case STRONG_EAST: return R.string.wind_strong_east;
		case STRONG_NORTH: return R.string.wind_strong_north;
		case STRONG_NORTHEAST: return R.string.wind_strong_northeast;
		case STRONG_NORTHWEST: return R.string.wind_strong_northwest;
		case STRONG_SOUTH: return R.string.wind_strong_south;
		case STRONG_SOUTHEAST: return R.string.wind_strong_southeast;
		case STRONG_SOUTHWEST: return R.string.wind_strong_southwest;
		case STRONG_WEST: return R.string.wind_strong_west;

		case VERY_STRONG_EAST: return R.string.wind_very_strong_east;
		case VERY_STRONG_NORTH: return R.string.wind_very_strong_north;
		case VERY_STRONG_NORTHEAST: return R.string.wind_very_strong_northeast;
		case VERY_STRONG_NORTHWEST: return R.string.wind_very_strong_northwest;
		case VERY_STRONG_SOUTH: return R.string.wind_very_strong_south;
		case VERY_STRONG_SOUTHEAST: return R.string.wind_very_strong_southeast;
		case VERY_STRONG_SOUTHWEST: return R.string.wind_very_strong_southwest;
		case VERY_STRONG_WEST: return R.string.wind_very_strong_west;
		default: return R.string.wind_unknown;
		}
	}
    
	public abstract String createDescription(Context ctx);
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
        MOSTLY_CLOUDY,
        CLOUDY,
        SHOWER, // CHUBASCO
        SHOWER_SNOW,
        RAIN,
        STORM,
        HAZE, // BRÉTEMA, NEBLINA
        FOG_PATCHES,
        MEDIUM_CLOUDS,
        LIGHT_RAIN,
        LIGHT_SHOWER, // CHUBASCO DÉBIL
        LIGHT_STORM,
        SLEET, // AGUA NIEVE
        HAIL, // SARABIA, GRANIZO
        SNOW,
        DEW, // ORBALLO
        FOG
    }

    public enum WindState {
    	/*299*/CALM,
    	/*300*/VARIABLE,
    	/*301*/LIGHT_NORTH,
    	/*302*/LIGHT_NORTHEAST,
    	/*303*/LIGHT_EAST,
    	/*304*/LIGHT_SOUTHEAST,
    	/*305*/LIGHT_SOUTH,
    	/*306*/LIGHT_SOUTHWEST,
    	/*307*/LIGHT_WEST,
    	/*308*/LIGHT_NORTHWEST,
    	
    	/*309*/MILD_NORTH,
    	/*310*/MILD_NORTHEAST,
    	/*311*/MILD_EAST,
    	/*312*/MILD_SOUTHEAST,
    	/*313*/MILD_SOUTH,
    	/*314*/MILD_SOUTHWEST,
    	/*315*/MILD_WEST,
    	/*316*/MILD_NORTHWEST,
    	
    	/*317*/STRONG_NORTH,
    	/*318*/STRONG_NORTHEAST,
    	/*319*/STRONG_EAST,
    	/*320*/STRONG_SOUTHEAST,
    	/*321*/STRONG_SOUTH,
    	/*322*/STRONG_SOUTHWEST,
    	/*323*/STRONG_WEST,
    	/*324*/STRONG_NORTHWEST,
    	
    	/*325*/VERY_STRONG_NORTH,
    	/*326*/VERY_STRONG_NORTHEAST,
    	/*327*/VERY_STRONG_EAST,
    	/*328*/VERY_STRONG_SOUTHEAST,
    	/*329*/VERY_STRONG_SOUTH,
    	/*330*/VERY_STRONG_SOUTHWEST,
    	/*331*/VERY_STRONG_WEST,
    	/*332*/VERY_STRONG_NORTHWEST,
    }
 
    
    @Nullable private Calendar _date = null;
    @Nullable private Calendar _creationDate = null;
	private int _maxTemp = 0; ///< Temperatura máxima del día
	private int _minTemp = 0; ///< Temperatura mínima del día
}