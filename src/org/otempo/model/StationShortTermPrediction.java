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

import org.eclipse.jdt.annotation.Nullable;
import org.otempo.R;

import android.content.Context;



/**
 * Predicción a corto plazo de una estación
 */
public class StationShortTermPrediction extends StationPrediction {
    //// Setters/getters ////
	@Nullable 
    public SkyState getSkyStateMorning() {
        return _skyStateMorning;
    }
    public void setSkyStateMorning(@Nullable  SkyState skyStateMorning) {
        _skyStateMorning = skyStateMorning;
    }
    @Nullable 
    public SkyState getSkyStateAfternoon() {
        return _skyStateAfternoon;
    }
    public void setSkyStateAfternoon(@Nullable SkyState skyStateAfternoon) {
        _skyStateAfternoon = skyStateAfternoon;
    }
    @Nullable 
    public SkyState getSkyStateNight() {
        return _skyStateNight;
    }
    public void setSkyStateNight(@Nullable SkyState skyStateNight) {
        _skyStateNight = skyStateNight;
    }
    @Nullable 
	public WindState getWindStateMorning() {
		return _windStateMorning;
	}
	public void setWindStateMorning(@Nullable WindState windStateMorning) {
		_windStateMorning = windStateMorning;
	}
	@Nullable 
	public WindState getWindStateAfternoon() {
		return _windStateAfternoon;
	}
	public void setWindStateAfternoon(@Nullable WindState windStateAfternoon) {
		_windStateAfternoon = windStateAfternoon;
	}
	@Nullable 
	public WindState getWindStateNight() {
		return _windStateNight;
	}
	public void setWindStateNight(@Nullable WindState windStateNight) {
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

    @Nullable private SkyState _skyStateMorning = null; ///< Estado del cielo por la mañana
    @Nullable private SkyState _skyStateAfternoon = null; ///< Estado del cielo por la tarde
    @Nullable private SkyState _skyStateNight = null; ///< Estado del cielo por la noche
    
    @Nullable private WindState _windStateMorning = null; //< Estado del viento por la mañana
    @Nullable private WindState _windStateAfternoon = null; //< Estado del viento por la tarde
    @Nullable private WindState _windStateNight = null; //< Estado del viento por la noche

    private float _rainProbabilityMorning = 0; //< Probabilidad de lluvia (%) por la mañana
	private float _rainProbabilityAfternoon = 0; //< Probabilidad de lluvia (%) por la tarde
    private float _rainProbabilityNight = 0; //< Probabilidad de lluvia (%) por la noche
}
