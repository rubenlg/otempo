package org.otempo.rss;

import android.util.Log;

import org.otempo.model.Station;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PredictionsParser {
    /**
     * Parsea la estación indicada.
     * TODO: StationCache.getStationRSS es una chapuza. Hace demasiadas cosas que no son evidentes.
     * Este método debería limitarse a parsear, y no andar mirando en la cache.
     */
    public static void parse(Station station, File cacheDir, boolean forceStorage) throws IOException {
        try {
            // Parsing short term
            InputStream streamShortTerm = StationCache.getStationRSS(station.getId(), true, forceStorage, cacheDir);
            if (streamShortTerm == null) {
                throw new IOException("Station cache returned a NULL stream for short term");
            }
            PredictionSAXHandler shortTermHandler = new ShortTermSAXHandler(station);
            SAXParserFactory spfShort = SAXParserFactory.newInstance();
            SAXParser parserShort = spfShort.newSAXParser();
            parserShort.parse(streamShortTerm, shortTermHandler);

            // Parsing medium term
            InputStream streamMediumTerm = StationCache.getStationRSS(station.getId(), false, forceStorage, cacheDir);
            if (streamMediumTerm == null) {
                throw new IOException("Station cache returned a NULL stream for medium term");
            }
            PredictionSAXHandler mediumTermHandler = new MediumTermSAXHandler(station);
            SAXParserFactory spfMedium = SAXParserFactory.newInstance();
            SAXParser parserMedium = spfMedium.newSAXParser();
            parserMedium.parse(streamMediumTerm, mediumTermHandler);
        } catch (MalformedURLException e) {
            Log.e("OTempo", e.getMessage(), e);
            throw new IOException(e);
        } catch (IOException e) {
            Log.e("OTempo", e.getMessage(), e);
            throw e;
        } catch (ParserConfigurationException e) {
            Log.e("OTempo", e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            Log.e("OTempo", "Error parsing station "+station.getName() + ": " + e.getMessage(), e);
            // Remove bogus data
            StationCache.removeCached(station.getId(), true, cacheDir);
            StationCache.removeCached(station.getId(), false, cacheDir);
            throw new IOException(e);
        }
    }
}
