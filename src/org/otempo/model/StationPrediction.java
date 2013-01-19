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

import org.otempo.R;
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

	public String skyStateDescription(SkyState state) {
        switch(state) {
        case CLEAR:
            return "Cielo despejado";
        case CLOUD_AND_CLEAR:
            return "Nubes y claros";
        case HIGH_CLOUDS:
            return "Nubes altas";
        case MOSTLY_CLOUDY:
            return "Mayormente nublado";
        case CLOUDY:
            return "Cielo cubierto";
        case DEW:
            return "Llovizna";
        case SHOWER:
            return "Chubascos";
        case RAIN:
            return "Lluvia";
        case STORM:
            return "Tormenta";
        case FOG:
            return "Niebla";
        case FOG_PATCHES:
            return "Bancos de niebla";
        case HAZE:
            return "Neblina";
        case SNOW:
            return "Nieve";
        case HAIL:
        	return "Granizo";
        case LIGHT_RAIN:
        	return "Lluvia ligera";
        case LIGHT_SHOWER:
        	return "Chubascos ligeros";
        case LIGHT_STORM:
        	return "Tormenta con pocas nubes";
        case MEDIUM_CLOUDS:
        	return "Nubes medias";
        case SHOWER_SNOW:
        	return "Chubascos de nieve";
        case SLEET:
        	return "Aguanieve";
        default:
            return "Estado del cielo desconocido";
        }
	}
	
	public String windStateDescription(WindState state) {
		switch (state) {
		case VARIABLE: return "variable";
		case CALM: return "en calma";
		case LIGHT_EAST: return "suave del este";

		case LIGHT_NORTH: return "suave del norte";
		case LIGHT_NORTHEAST: return "suave del nordeste";
		case LIGHT_NORTHWEST: return "suave del noroeste";
		case LIGHT_SOUTH: return "suave del sur";
		case LIGHT_SOUTHEAST: return "suave del sudeste";
		case LIGHT_SOUTHWEST: return "suave del suroeste";
		case LIGHT_WEST: return "suave del oeste";

		case MILD_NORTH: return "moderado del norte";
		case MILD_NORTHEAST: return "moderado del nordeste";
		case MILD_NORTHWEST: return "moderado del noroeste";
		case MILD_SOUTH: return "moderado del sur";
		case MILD_SOUTHEAST: return "moderado del sudeste";
		case MILD_SOUTHWEST: return "moderado del suroeste";
		case MILD_WEST: return "moderado del oeste";

		case STRONG_NORTH: return "fuerte del norte";
		case STRONG_NORTHEAST: return "fuerte del nordeste";
		case STRONG_NORTHWEST: return "fuerte del noroeste";
		case STRONG_SOUTH: return "fuerte del sur";
		case STRONG_SOUTHEAST: return "fuerte del sudeste";
		case STRONG_SOUTHWEST: return "fuerte del suroeste";
		case STRONG_WEST: return "fuerte del oeste";

		case VERY_STRONG_NORTH: return "muy fuerte del norte";
		case VERY_STRONG_NORTHEAST: return "muy fuerte del nordeste";
		case VERY_STRONG_NORTHWEST: return "muy fuerte del noroeste";
		case VERY_STRONG_SOUTH: return "muy fuerte del sur";
		case VERY_STRONG_SOUTHEAST: return "muy fuerte del sudeste";
		case VERY_STRONG_SOUTHWEST: return "muy fuerte del suroeste";
		case VERY_STRONG_WEST: return "muy fuerte del oeste";
		default: return "desconocido";
		}
	}
    
	public abstract String createDescription();
	
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
    
    private Calendar _date = null;
    private Calendar _creationDate = null;
}