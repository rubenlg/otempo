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

import org.otempo.R;

import android.content.Context;



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
	public WindState getWindStateMorning() {
		return _windStateMorning;
	}
	public void setWindStateMorning(WindState windStateMorning) {
		_windStateMorning = windStateMorning;
	}
	public WindState getWindStateAfternoon() {
		return _windStateAfternoon;
	}
	public void setWindStateAfternoon(WindState windStateAfternoon) {
		_windStateAfternoon = windStateAfternoon;
	}
	public WindState getWindStateNight() {
		return _windStateNight;
	}
	public void setWindStateNight(WindState windStateNight) {
		_windStateNight = windStateNight;
	}

    public float getRainProbabilityMorning() {
		return _rainProbabilityMorning;
	}
	public void setRainProbabilityMorning(float rainProbabilityMorning) {
		_rainProbabilityMorning = rainProbabilityMorning;
	}
	public float getRainProbabilityAfternoon() {
		return _rainProbabilityAfternoon;
	}
	public void setRainProbabilityAfternoon(float rainProbabilityAfternoon) {
		_rainProbabilityAfternoon = rainProbabilityAfternoon;
	}
	public float getRainProbabilityNight() {
		return _rainProbabilityNight;
	}
	public void setRainProbabilityNight(float rainProbabilityNight) {
		_rainProbabilityNight = rainProbabilityNight;
	}
	
	@Override
	public String createDescription(Context ctx) {
		return String.format(
				ctx.getString(R.string.shortTermDescriptionFormat), 
				ctx.getString(skyStateDescriptionResId(_skyStateMorning)),
				ctx.getString(windStateDescriptionResId(_windStateMorning)),
				_rainProbabilityMorning,
				ctx.getString(skyStateDescriptionResId(_skyStateAfternoon)),
				ctx.getString(windStateDescriptionResId(_windStateAfternoon)),
				_rainProbabilityAfternoon,
				ctx.getString(skyStateDescriptionResId(_skyStateNight)),
				ctx.getString(windStateDescriptionResId(_windStateNight)),
				_rainProbabilityNight);
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
    
    private WindState _windStateMorning = null; //< Estado del viento por la mañana
	private WindState _windStateAfternoon = null; //< Estado del viento por la tarde
    private WindState _windStateNight = null; //< Estado del viento por la noche

    private float _rainProbabilityMorning = 0; //< Probabilidad de lluvia (%) por la mañana
	private float _rainProbabilityAfternoon = 0; //< Probabilidad de lluvia (%) por la tarde
    private float _rainProbabilityNight = 0; //< Probabilidad de lluvia (%) por la noche
}
