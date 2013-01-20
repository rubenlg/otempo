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
 * Predicción a medio plazo de una estación. Dispone de menos información que una predicción a corto plazo.
 */
public class StationMediumTermPrediction extends StationPrediction {
    /**
     * @return El estado predicho del cielo
     */
    public SkyState getSkyState() {
        return _skyState;
    }

    /**
     * Establece el estado predicho del cielo
     * @param skyState El estado predicho del cielo
     */
    public void setSkyState(SkyState skyState) {
        _skyState = skyState;
    }

	public WindState getWindState() {
		return _windState;
	}
	public void setWindState(WindState windState) {
		_windState = windState;
	}
	public float getRainProbability() {
		return _rainProbability;
	}
	public void setRainProbability(float rainProbability) {
		_rainProbability = rainProbability;
	}

	@Override
    public void accept(StationPredictionVisitor visitor, int index) {
        visitor.apply(this, index);
    }

	@Override
	public String createDescription(Context ctx) {
		return String.format(ctx.getString(R.string.mediumTermDescriptionFormat),
				ctx.getString(skyStateDescriptionResId(_skyState)),
				ctx.getString(windStateDescriptionResId(_windState)),
				_rainProbability);
	}
    
    private SkyState _skyState = null; ///< El estado predicho del cielo
	private WindState _windState = null; ///< El estado predicho del viento
    private float _rainProbability = 0; //< Probabilidad de lluvia (%)
}
