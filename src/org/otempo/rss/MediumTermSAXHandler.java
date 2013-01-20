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

import org.otempo.model.Station;
import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationPrediction;

/**
 * Handler SAX para parsear el RSS de las estaciones de meteogalicia
 */
public class MediumTermSAXHandler extends PredictionSAXHandler {
    public MediumTermSAXHandler(Station station) {
        super(station);
    }

    @Override
    public void endElementSpecific(String uri, String localName) {
    	if (localName.equals("ceo")) {
            getCurrentMediumPrediction().setSkyState(parseSkyState(getCurrentText()));
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
    protected StationMediumTermPrediction getCurrentMediumPrediction() {
        if (_currentMediumPrediction == null) {
            _currentMediumPrediction = new StationMediumTermPrediction();
        }
        return _currentMediumPrediction;
    }
    
	@Override
	protected StationPrediction getCurrentPrediction() {
		return getCurrentMediumPrediction();
	}

    // Predicción a medio plazo actual
    private StationMediumTermPrediction _currentMediumPrediction = null;

}
