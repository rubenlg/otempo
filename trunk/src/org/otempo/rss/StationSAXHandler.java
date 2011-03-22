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
            String format = attributes.getValue("formato").split(" ")[1];
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
            if (localName.equals("TMax")) {
                getCurrentShortPrediction().setMaxTemp(Integer.valueOf(_currentChars.toString()));
            } else if (localName.equals("TMin")) {
                getCurrentShortPrediction().setMinTemp(Integer.valueOf(_currentChars.toString()));
            } else if (localName.equals("ceoM")) {
                getCurrentShortPrediction().setSkyStateMorning(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("ceoT")) {
                getCurrentShortPrediction().setSkyStateAfternoon(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("ceoN")) {
                getCurrentShortPrediction().setSkyStateNight(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("dataCreacion")
                       && uri.equals("Localidades")) {
                getCurrentShortPrediction().setCreationDate(parseDate(_currentChars.toString(), _creationDateformat));
            } else if (localName.equals("dataPredicion")
                       && uri.equals("Localidades")) {
                getCurrentShortPrediction().setDate(parseDate(_currentChars.toString(), _lastPredFormat));
            } else if (localName.equals("dataCreacion")
                       && uri.equals("LocMP")) {
                getCurrentMediumPrediction().setCreationDate(parseDate(_currentChars.toString(), _creationDateformat));
            } else if (localName.equals("dataPredicion")
                       && uri.equals("LocMP")) {

                getCurrentMediumPrediction().setDate(parseDate(_currentChars.toString(), _lastPredFormat));
            } else if (localName.equals("estadoCeo")) {
                getCurrentMediumPrediction().setSkyState(parseSkyState(_currentChars.toString()));
            } else if (localName.equals("comentario") && uri.equals("Localidades")) {
                getCurrentShortPrediction().setComment(_currentChars.toString());
            }
        } catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing["+_currentChars+"]");
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
     * @param state Estado del cielo como texto
     * @return Devuelve el enumerado correspondiente
     * @TODO Se puede optimizar un poco mediante un HashMap, aunque hay otras optimizaciones más relevantes
     */
    protected StationPrediction.SkyState parseSkyState(String state) {
        if (state.startsWith("despexado")) {
            return StationPrediction.SkyState.CLEAR;
        } else if (state.startsWith("nubes_altas")) {
            return StationPrediction.SkyState.HIGH_CLOUDS;
        } else if (state.startsWith("nubes_e_craros")) {
            return StationPrediction.SkyState.CLOUD_AND_CLEAR;
        } else if (state.startsWith("case_cuberto")) {
            return StationPrediction.SkyState.MOSTLY_CLOUD;
        } else if (state.startsWith("nuboso")) {
            return StationPrediction.SkyState.CLOUDY;
        } else if (state.startsWith("chubasco")) {
            return StationPrediction.SkyState.CHUBASCO;
        } else if (state.startsWith("chuvia")) {
            return StationPrediction.SkyState.RAIN;
        } else if (state.startsWith("treboada")) {
            return StationPrediction.SkyState.STORM;
        } else if (state.startsWith("neve")) {
            return StationPrediction.SkyState.SNOW;
        } else if (state.startsWith("orballo")) {
            return StationPrediction.SkyState.DEW;
        } else if (state.startsWith("neboas")) {
            return StationPrediction.SkyState.FOG;
        } else {
            Log.w("OTempo", "Unable to parse "+state);
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
