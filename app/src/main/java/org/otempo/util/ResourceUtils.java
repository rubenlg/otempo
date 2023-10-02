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
package org.otempo.util;

import org.otempo.R;
import org.otempo.model.StationPrediction.SkyState;

import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Utilidades para gestionar recursos (imágenes)
 */
public class ResourceUtils {
	/**
	 * Dado un estado del cielo, devuelve el icono adecuado desde los recursos
	 * @param state Estado del cielo
	 * @param day true si queremos un icono para día, y false si lo queremos para noche
	 * @return El ID del recurso
	 */
    public static int getResource(@Nullable SkyState state, boolean day) {
        if (state == null) {
            Log.e("OTempo", "getResource: NULL Sky State");
            return R.drawable.clear;
        }
        if (day) {
            switch(state) {
            case CLEAR:
                return R.drawable.clear;
            case CLOUD_AND_CLEAR:
                return R.drawable.cloud_clear;
            case HIGH_CLOUDS:
                return R.drawable.high_clouds;
            case MOSTLY_CLOUDY:
                return R.drawable.mostly_cloud;
            case CLOUDY:
                return R.drawable.cloudy;
            case DEW:
                return R.drawable.orballo;
            case SHOWER:
                return R.drawable.chubasco;
            case RAIN:
                return R.drawable.rain;
            case STORM:
                return R.drawable.storm;
            case FOG:
                return R.drawable.fog;
            case FOG_PATCHES:
                return R.drawable.fog_patches;
            case HAZE:
                return R.drawable.haze;
            case SNOW:
                return R.drawable.snow;
            case HAIL:
            	return R.drawable.hail;
            case LIGHT_RAIN:
            	return R.drawable.light_rain;
            case LIGHT_SHOWER:
            	return R.drawable.light_shower;
            case LIGHT_STORM:
            	return R.drawable.light_storm;
            case MEDIUM_CLOUDS:
            	return R.drawable.medium_clouds;
            case SHOWER_SNOW:
            	return R.drawable.shower_snow;
            case SLEET:
            	return R.drawable.sleet;
            default:
                return R.drawable.clear;
            }
        } else {
            switch(state) {
            case CLEAR:
                return R.drawable.clear_night;
            case CLOUD_AND_CLEAR:
                return R.drawable.cloud_clear_night;
            case HIGH_CLOUDS:
                return R.drawable.high_clouds_night;
            case MOSTLY_CLOUDY:
                return R.drawable.mostly_cloud_night;
            case CLOUDY:
                return R.drawable.cloudy;
            case DEW:
                return R.drawable.orballo;
            case SHOWER:
                return R.drawable.chubasco_night;
            case RAIN:
                return R.drawable.rain;
            case STORM:
                return R.drawable.storm;
            case FOG:
                return R.drawable.fog;
            case FOG_PATCHES:
                return R.drawable.fog_patches_night;
            case HAZE:
                return R.drawable.haze_night;
            case SNOW:
                return R.drawable.snow;
            case HAIL:
            	return R.drawable.hail;
            case LIGHT_RAIN:
            	return R.drawable.light_rain;
            case LIGHT_SHOWER:
            	return R.drawable.light_shower_night;
            case LIGHT_STORM:
            	return R.drawable.light_storm_night;
            case MEDIUM_CLOUDS:
            	return R.drawable.medium_clouds;
            case SHOWER_SNOW:
            	return 0;
            case SLEET:
            	return R.drawable.shower_snow_night;
            default:
                return R.drawable.clear_night;
            }
        }
    }

}
