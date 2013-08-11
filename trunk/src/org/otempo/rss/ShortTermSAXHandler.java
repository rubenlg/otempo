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
package org.otempo.rss;

import org.eclipse.jdt.annotation.Nullable;
import org.otempo.model.Station;
import org.otempo.model.StationPrediction;
import org.otempo.model.StationShortTermPrediction;

/**
 * Handler SAX para parsear el RSS de las estaciones de meteogalicia
 */
public class ShortTermSAXHandler extends PredictionSAXHandler {
    public ShortTermSAXHandler(Station station) {
        super(station, true);
    }

    @Override
    public void endElementSpecific(String uri, String localName) {
    	if (localName.equals("ceoM")) {
            getCurrentShortPrediction().setSkyStateMorning(parseSkyState(getCurrentText()));
        } else if (localName.equals("ceoT")) {
            getCurrentShortPrediction().setSkyStateAfternoon(parseSkyState(getCurrentText()));
        } else if (localName.equals("ceoN")) {
            getCurrentShortPrediction().setSkyStateNight(parseSkyState(getCurrentText()));

        } else if (localName.equals("ventoM")) {
            getCurrentShortPrediction().setWindStateMorning(parseWindState(getCurrentText()));
        } else if (localName.equals("ventoT")) {
            getCurrentShortPrediction().setWindStateAfternoon(parseWindState(getCurrentText()));
        } else if (localName.equals("ventoN")) {
            getCurrentShortPrediction().setWindStateNight(parseWindState(getCurrentText()));

        } else if (localName.equals("pChoivaM")) {
            getCurrentShortPrediction().setRainProbabilityMorning(Float.valueOf(getCurrentText()));
        } else if (localName.equals("pChoivaT")) {
            getCurrentShortPrediction().setRainProbabilityAfternoon(Float.valueOf(getCurrentText()));
        } else if (localName.equals("pChoivaN")) {
            getCurrentShortPrediction().setRainProbabilityNight(Float.valueOf(getCurrentText()));
        }
    }

    @Override
    protected boolean hasCurrentPrediction() {
    	return _currentShortPrediction != null;
    }
    
    @Override
    protected void resetCurrentPrediction() {
    	_currentShortPrediction = null;
    }
    
    /**
     * @return La última predicción a corto plazo parseada, o crea una nueva 
     */
    protected StationShortTermPrediction getCurrentShortPrediction() {
    	StationShortTermPrediction current = _currentShortPrediction; 
        if (current == null) {
            _currentShortPrediction = new StationShortTermPrediction();
            return _currentShortPrediction;
        } else {
        	return current;
        }
    }

	@Override
	protected StationPrediction getOrCreateCurrentPrediction() {
		return getCurrentShortPrediction();
	}

    // Predicción a corto plazo actual
    @Nullable private StationShortTermPrediction _currentShortPrediction = null;
}
