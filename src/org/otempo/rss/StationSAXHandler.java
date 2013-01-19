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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.otempo.model.Station;
import org.otempo.model.StationMediumTermPrediction;
import org.otempo.model.StationPrediction;
import org.otempo.model.StationShortTermPrediction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Handler SAX para parsear el RSS de las estaciones de meteogalicia
 */
public class StationSAXHandler extends DefaultHandler {
    public StationSAXHandler(Station station) {
        _station = station;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName == "dataPredicion") {
        	String format = attributes.getValue("formato");
            _lastPredFormat = new SimpleDateFormat(format);
        }
        _currentChars.setLength(0);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName == "item") {
            if (_currentShortPrediction != null) {
                _predictions.add(_currentShortPrediction);
                _currentShortPrediction = null;
            } else if (_currentMediumPrediction != null) {
                _predictions.add(_currentMediumPrediction);
                _currentMediumPrediction = null;
            } else {
                // Pasando, es el item que separa las short de las long
            }
        }
        if (localName == "rss") {
            _station.setPredictions(_predictions);
        }
        if (localName == null) return;
        try {
            if (localName.equals("tMax")) {
                getCurrentShortPrediction().setMaxTemp(Integer.valueOf(_currentChars.toString()));
            } else if (localName.equals("tMin")) {
                getCurrentShortPrediction().setMinTemp(Integer.valueOf(_currentChars.toString()));

            } else if (localName.equals("ceoM")) {
                getCurrentShortPrediction().setSkyStateMorning(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("ceoT")) {
                getCurrentShortPrediction().setSkyStateAfternoon(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("ceoN")) {
                getCurrentShortPrediction().setSkyStateNight(parseSkyState(_currentChars.toString()));

            } else if (localName.equals("ventoM")) {
                getCurrentShortPrediction().setWindStateMorning(parseWindState(_currentChars.toString()));
            } else if (localName.equals("ventoT")) {
                getCurrentShortPrediction().setWindStateAfternoon(parseWindState(_currentChars.toString()));
            } else if (localName.equals("ventoN")) {
                getCurrentShortPrediction().setWindStateNight(parseWindState(_currentChars.toString()));

            } else if (localName.equals("pChoivaM")) {
                getCurrentShortPrediction().setRainProbabilityMorning(Float.valueOf(_currentChars.toString()));
            } else if (localName.equals("pChoivaT")) {
                getCurrentShortPrediction().setRainProbabilityAfternoon(Float.valueOf(_currentChars.toString()));
            } else if (localName.equals("pChoivaN")) {
                getCurrentShortPrediction().setRainProbabilityNight(Float.valueOf(_currentChars.toString()));

            } else if (localName.equals("dataCreacion")
                       && uri.equals("Concellos")) {
                getCurrentShortPrediction().setCreationDate(parseDate(_currentChars.toString(), _creationDateformat));
            } else if (localName.equals("dataPredicion")
                       && uri.equals("Concellos")) {
                getCurrentShortPrediction().setDate(parseDate(_currentChars.toString(), _lastPredFormat));
            /*} else if (localName.equals("dataCreacion")
                       && uri.equals("LocMP")) {
                getCurrentMediumPrediction().setCreationDate(parseDate(_currentChars.toString(), _creationDateformat));
            } else if (localName.equals("dataPredicion")
                       && uri.equals("LocMP")) {

                getCurrentMediumPrediction().setDate(parseDate(_currentChars.toString(), _lastPredFormat));
            } else if (localName.equals("estadoCeo")) {
                getCurrentMediumPrediction().setSkyState(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("comentario") && uri.equals("Localidades")) {
                getCurrentShortPrediction().setComment(_currentChars.toString());*/
            }
        } catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing["+_currentChars+"]");
        }
    }



    private StationPrediction.WindState parseWindState(String stateString) {
    	try {
    		int state = Integer.valueOf(stateString);
        	// Lo convertimos a algo indexable (0..N)
        	state -= 299;
        	if (state < 0 || state >= StationPrediction.WindState.values().length) {
        		Log.w("OTempo", "Unable to parse wind " + stateString);
                return null; 
        	}
        	return StationPrediction.WindState.values()[state];
    	} catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing wind state: ["+stateString+"]");
            return null;
        }
	}

	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        _currentChars.append(ch, start, length);
    }

    /**
     * Permite parsear una fecha mediante un formato dado
     * @param string Fecha como texto
     * @param format Formato de fecha esperado
     * @return Un objeto Calendar con la fecha
     */
    private Calendar parseDate(String string, SimpleDateFormat format) {
        if (format == null) return null;
        try {
            Calendar c = new GregorianCalendar();
            c.setTime(format.parse(string));
            return c;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Permite parsear un estado del cielo
     * @param stateString Estado del cielo como texto
     * @return Devuelve el enumerado correspondiente
     * @TODO Se puede optimizar un poco mediante un HashMap, aunque hay otras optimizaciones más relevantes
     * @TODO(ryu): Añadir nuevos iconos de meteogalicia
     */
    protected StationPrediction.SkyState parseSkyState(String stateString) {
    	StationPrediction.SkyState[] knownStates = {
    			/*101*/StationPrediction.SkyState.CLEAR,
    			/*102*/StationPrediction.SkyState.HIGH_CLOUDS,
    			/*103*/StationPrediction.SkyState.CLOUD_AND_CLEAR,
    			/*104*/StationPrediction.SkyState.MOSTLY_CLOUDY,
    			/*105*/StationPrediction.SkyState.CLOUDY,
    			/*106*/StationPrediction.SkyState.FOG,
    			/*107*/StationPrediction.SkyState.SHOWER,
    			/*108*/StationPrediction.SkyState.SHOWER,
    			/*109*/StationPrediction.SkyState.SHOWER_SNOW,
    			/*100*/StationPrediction.SkyState.DEW,
    			/*111*/StationPrediction.SkyState.RAIN,
    			/*112*/StationPrediction.SkyState.SNOW,
    			/*113*/StationPrediction.SkyState.STORM,
    			/*114*/StationPrediction.SkyState.HAZE,
    			/*115*/StationPrediction.SkyState.FOG_PATCHES,
    			/*116*/StationPrediction.SkyState.MEDIUM_CLOUDS,
    			/*117*/StationPrediction.SkyState.LIGHT_RAIN,
    			/*118*/StationPrediction.SkyState.LIGHT_SHOWER,
    			/*119*/StationPrediction.SkyState.LIGHT_STORM,
    			/*120*/StationPrediction.SkyState.SLEET,
    			/*121*/StationPrediction.SkyState.HAIL
    	};
    	try {
    		int state = Integer.valueOf(stateString);
        	if (state > 200) {
        		// No distinguimos entre estado diurno y nocturno a estas alturas, 
        		// eso se hace automáticamente más tarde.
        		state -= 100;
        	}
        	// Lo convertimos a algo indexable (0..N)
        	state -= 101;
        	if (state < 0 || state >= knownStates.length) {
        		Log.w("OTempo", "Unable to parse sky " + stateString);
                return null; 
        	}
        	return knownStates[state];
    	} catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing sky state: ["+stateString+"]");
            return null;
        }
    }

    /**
     * @return La última predicción a corto plazo parseada, o crea una nueva 
     */
    private StationShortTermPrediction getCurrentShortPrediction() {
        if (_currentShortPrediction == null) {
            _currentShortPrediction = new StationShortTermPrediction();
        }
        return _currentShortPrediction;
    }

    /**
     * @return La última predicción a medio plazo parseada, o crea una nueva 
     */
    private StationMediumTermPrediction getCurrentMediumPrediction() {
        if (_currentMediumPrediction == null) {
            _currentMediumPrediction = new StationMediumTermPrediction();
        }
        return _currentMediumPrediction;
    }
    // Estación que se está parseando
    private Station _station = null;

    // Constructor de string para acumular texto a medida que nos va llegando
    private StringBuilder _currentChars = new StringBuilder();
    // Lista de predicciones de la estación
    private List<StationPrediction> _predictions = new ArrayList<StationPrediction>();
    // Predicción a corto plazo actual
    private StationShortTermPrediction _currentShortPrediction = null;
    // Predicción a medio plazo actual
    private StationMediumTermPrediction _currentMediumPrediction = null;

    // Último formato de fecha de predicción declarado en el RSS de meteogalicia 
    private SimpleDateFormat _lastPredFormat = null;

    // Formato de fecha de creación (no se especifica en el RSS)
    private SimpleDateFormat _creationDateformat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
}
