package org.otempo.rss;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.otempo.model.Station;
import org.otempo.model.StationPrediction;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

public abstract class PredictionSAXHandler extends DefaultHandler {

	PredictionSAXHandler(Station station, boolean clearPredictions) {
		_station = station;
		_clearPredictions  = clearPredictions;
	}

	@Nullable
    final StationPrediction.WindState parseWindState(String stateString) {
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
	@Nullable
    private Calendar parseDate(String string, SimpleDateFormat format) {
	    if (format == null) return null;
	    try {
	        Calendar c = new GregorianCalendar();
	        c.setTime(format.parse(string));
	        return c;
	    } catch (Exception e) {
	        return null;
	    }
	}

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (localName.equals("dataPredicion")) {
        	String format = attributes.getValue("formato");
            _lastPredFormat = new SimpleDateFormat(format, SPANISH);
        }
        _currentChars.setLength(0);
    }

    @Override
    public final void characters(char[] ch, int start, int length) {
        _currentChars.append(ch, start, length);
    }
    
    @Override
    public final void endElement(String uri, String localName, String qName) {
		if (localName == null) return;
        if (localName.equals("item")) {
        	if (hasCurrentPrediction()) {
                _predictions.add(getOrCreateCurrentPrediction());
                resetCurrentPrediction();
            }
        }
        if (localName.equals("rss")) {
            _station.setPredictions(_predictions, _clearPredictions);
        }
        try {
            if (localName.equals("tMax")) {
                getOrCreateCurrentPrediction().setMaxTemp(Integer.valueOf(getCurrentText()));
            } else if (localName.equals("tMin")) {
                getOrCreateCurrentPrediction().setMinTemp(Integer.valueOf(getCurrentText()));

            } else if (localName.equals("dataCreacion")) {
                getOrCreateCurrentPrediction().setCreationDate(parseDate(getCurrentText(), _creationDateformat));
            } else if (localName.equals("dataPredicion")) {
                getOrCreateCurrentPrediction().setDate(parseDate(getCurrentText(), _lastPredFormat));

            } else {
            	endElementSpecific(uri, localName);
            }
        } catch (NumberFormatException e) {
            Log.w("OTempo", "NumberFormatException parsing[" + getCurrentText() + "]");
        }
    }

    // Process specific short/medium elements in the subclasses.
    protected abstract void endElementSpecific(@NonNull String uri, @NonNull String localName);
    
    // Returns currently accumulated text in the active element
    final String getCurrentText() { return _currentChars.toString(); }
    
    /**
	 * Permite parsear un estado del cielo
	 * @param stateString Estado del cielo como texto
	 * @return Devuelve el enumerado correspondiente
	 * @TODO Se puede optimizar un poco mediante un HashMap, aunque hay otras optimizaciones más relevantes
	 * @TODO(ryu): Añadir nuevos iconos de meteogalicia
	 */
    @Nullable
    final StationPrediction.SkyState parseSkyState(String stateString) {
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
    // Obtiene la predicción actual, creándola si no existe
	protected abstract StationPrediction getOrCreateCurrentPrediction();
	
	// Comprueba si existe una predicción actual. Algunos items in el RSS 
	// no tienen predicciones, y para ellos no se crea.  
	protected abstract boolean hasCurrentPrediction();
	
	// Borra la predicción actual (usado al cerrar un item) 
	protected abstract void resetCurrentPrediction();
	
	private static final Locale SPANISH = new Locale("es");
	
    // Lista de predicciones de la estación
    private final List<StationPrediction> _predictions = new ArrayList<>();
	
	// Constructor de string para acumular texto a medida que nos va llegando
    private final StringBuilder _currentChars = new StringBuilder();

	// Estación que se está parseando
    private Station _station;

    // Último formato de fecha de predicción declarado en el RSS de meteogalicia 
	private SimpleDateFormat _lastPredFormat = null;
	
    // Formato de fecha de creación (no se especifica en el RSS)
    private static final SimpleDateFormat _creationDateformat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", SPANISH);

    // Borrar las predicciones antes de cargar las nuevas?
	private boolean _clearPredictions;
}