/*
 * Copyright (C) 2010-2013 Ruben Lopez
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
package org.otempo.rss;

import android.support.annotation.Nullable;

import org.otempo.model.Station;
import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationPrediction;

/**
 * Handler SAX para parsear el RSS de las estaciones de meteogalicia
 */
public class MediumTermSAXHandler extends PredictionSAXHandler {
    public MediumTermSAXHandler(Station station) {
        super(station, false);
    }

    @Override
    public void endElementSpecific(String uri, String localName) {
    	if (localName.equals("ceo")) {
            getCurrentMediumPrediction().setSkyState(parseSkyState(getCurrentText()));
        } else if (localName.equals("vento")) {
            getCurrentMediumPrediction().setWindState(parseWindState(getCurrentText()));
        } else if (localName.equals("pChoiva")) {
            getCurrentMediumPrediction().setRainProbability(Float.valueOf(getCurrentText()));
        }
    }
    
    @Override
    protected boolean hasCurrentPrediction() {
    	return _currentMediumPrediction != null;
    }

    @Override
    protected void resetCurrentPrediction() {
    	_currentMediumPrediction = null;
    }

    /**
     * @return La última predicción a medio plazo parseada, o crea una nueva 
     */
    private StationMediumTermPrediction getCurrentMediumPrediction() {
    	StationMediumTermPrediction current = _currentMediumPrediction; 
        if (current == null) {
        	_currentMediumPrediction = new StationMediumTermPrediction();
        	return _currentMediumPrediction;
        } else {
        	return current;
        }
    }
    
	@Override
	protected StationPrediction getOrCreateCurrentPrediction() {
		return getCurrentMediumPrediction();
	}

    // Predicción a medio plazo actual
    @Nullable
    private StationMediumTermPrediction _currentMediumPrediction = null;

}
