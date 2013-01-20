package org.otempo.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.otempo.model.Station;
import org.otempo.model.StationPrediction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public abstract class PredictionSAXHandler extends DefaultHandler {

	public PredictionSAXHandler(Station station) {
		_station = station;
	}

	protected final StationPrediction.WindState parseWindState(String stateString) {
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

	/**
	 * Permite parsear una fecha mediante un formato dado
	 * @param string Fecha como texto
	 * @param format Formato de fecha esperado
	 * @return Un objeto Calendar con la fecha
	 */
	protected final Calendar parseDate(String string, SimpleDateFormat format) {
	    if (format == null) return null;
	    try {
	        Calendar c = new GregorianCalendar();
	        c.setTime(format.parse(string));
	        return c;
	    } catch (ParseException e) {
	        return null;
	    }
	}

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName == "dataPredicion") {
        	String format = attributes.getValue("formato");
            _lastPredFormat = new SimpleDateFormat(format);
        }
        _currentChars.setLength(0);
    }

    @Override
    public final void characters(char[] ch, int start, int length) throws SAXException {
        _currentChars.append(ch, start, length);
    }
    
    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName == "item") {
        	if (hasCurrentPrediction()) {
                _predictions.add(getCurrentPrediction());
                resetCurrentPrediction();
            }
        }
        if (localName == "rss") {
            _station.setPredictions(_predictions);
        }
        if (localName == null) return;
        try {
            if (localName.equals("tMax")) {
                getCurrentPrediction().setMaxTemp(Integer.valueOf(getCurrentText()));
            } else if (localName.equals("tMin")) {
                getCurrentPrediction().setMinTemp(Integer.valueOf(getCurrentText()));

            } else if (localName.equals("dataCreacion")) {
                getCurrentPrediction().setCreationDate(parseDate(getCurrentText(), _creationDateformat));
            } else if (localName.equals("dataPredicion")) {
                getCurrentPrediction().setDate(parseDate(getCurrentText(), _lastPredFormat));

            } else {
            	endElementSpecific(uri, localName);
            }
        } catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing[" + getCurrentText() + "]");
        }
    }

    // Process specific short/medium elements in the subclasses.
    public abstract void endElementSpecific(String uri, String localName);
    
    // Returns currently accumulated text in the active element
    protected final String getCurrentText() { return _currentChars.toString(); }
    
    /**
	 * Permite parsear un estado del cielo
	 * @param stateString Estado del cielo como texto
	 * @return Devuelve el enumerado correspondiente
	 * @TODO Se puede optimizar un poco mediante un HashMap, aunque hay otras optimizaciones más relevantes
	 * @TODO(ryu): Añadir nuevos iconos de meteogalicia
	 */
	protected final StationPrediction.SkyState parseSkyState(String stateString) {
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
    // Gets the current prediction
	protected abstract StationPrediction getCurrentPrediction();
	
	// Checks if there is a current prediction. For items which doesn't contain
	// weather info, like overall comments, we don't want to create Prediction objects.
	protected abstract boolean hasCurrentPrediction();
	
	// Resets the current prediction, so that the next item will start in a new one
	protected abstract void resetCurrentPrediction();
	
	
	
    // Lista de predicciones de la estación
    private List<StationPrediction> _predictions = new ArrayList<StationPrediction>();
	
	// Constructor de string para acumular texto a medida que nos va llegando
    private StringBuilder _currentChars = new StringBuilder();

	// Estación que se está parseando
    private Station _station = null;

    // Último formato de fecha de predicción declarado en el RSS de meteogalicia 
	private SimpleDateFormat _lastPredFormat = null;
	
    // Formato de fecha de creación (no se especifica en el RSS)
    private static final SimpleDateFormat _creationDateformat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
}