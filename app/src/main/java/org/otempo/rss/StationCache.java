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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Caché de estaciones. Almacena en la SD una copia de los RSS solicitados a meteogalicia, y evita volver a descargarlas de Internet si la copia es muy reciente.
 */
public class StationCache {
    private static final String DATA_DIR = "Android/data/org.otempo/cache/"; ///< Ubicación de la caché
    private static final long MAX_STORAGE_HOURS = 1; ///< Máxima edad permitida para una copia en caché, a partir de ahí se vuelve a descargar (en horas)
    private static final long MAX_STORAGE_AGE = 1000 * 3600 * MAX_STORAGE_HOURS; ///< Máxima edad permitida para una copia en caché, a partir de ahí se vuelve a descargar (en ms)

    /**
     * Obtiene el RSS de una estación, decidiendo si servirlo directamente desde la SD, o desde Internet
     * @param stationId ID de la estación
     * @param forceStorage Permite forzar que deseamos cargarlo desde la SD (por ej: si no hay conexión a Internet)
     * @return Un flujo del que leer el RSS
     */
    @Nullable
    public static InputStream getStationRSS(int stationId, boolean shortTerm, boolean forceStorage) {
        InputStream stream = null;
        long storageAge = getStorageAge(stationId, shortTerm);
        // Si la edad de la caché no es buena, intentamos coger de internet
        if ((storageAge < 0 || storageAge > MAX_STORAGE_AGE) && !forceStorage) {
            stream = getFromInternet(stationId, shortTerm);
        }
        // Si en internet no se puede, o la caché es buena, pues de la caché
        if (stream == null) {
            stream = getFromStorage(stationId, shortTerm);
        }
        // Puede que devolvamos null a pesar de todo
        return stream;
    }

    /**
     * @param stationId ID de estación
     * @return Edad de la cache en milisegundos para una estación
     */
    private static long getStorageAge(int stationId, boolean shortTerm) {
        File sdDir = Environment.getExternalStorageDirectory();
        File cache = new File(sdDir, DATA_DIR + makeFileName(stationId, shortTerm));
        if (cache.exists()) {
            Date d = new Date();
            return d.getTime() - cache.lastModified();
        } else {
            return -1;
        }
    }

    /**
     * @param stationId ID de la estación
     * @return Obtiene el RSS de una estación desde la SD, o null si no hay.
     */
    @Nullable
    private static InputStream getFromStorage(int stationId, boolean shortTerm) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {
            File sdDir = Environment.getExternalStorageDirectory();
            File cache = new File(sdDir, DATA_DIR + makeFileName(stationId, shortTerm));
            try {
                return new FileInputStream(cache);
            } catch (FileNotFoundException e) {
                return null;
            }
        } else {
            Log.w("OTempo", "SD CARD STATE: "+state.toString());
            return null;
        }
    }

    /**
     * Invalida la caché de una estación (si no se puede parsear, por ej)
     * @param stationId Id de la estación a invalidar
     */
    public static boolean removeCached(int stationId, boolean shortTerm) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdDir = Environment.getExternalStorageDirectory();
            File dataDir = new File(sdDir, DATA_DIR);
            if (! dataDir.exists()) return true;
            File cache = new File(dataDir, makeFileName(stationId, shortTerm));
            cache.delete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Almacena en caché el rss de una estación
     * @param stationId ID de la estación a almacenar
     * @param rss flujo del que se puede leer el RSS
     * @return true si se pudo guardar correctamente, y false en todos los demás casos
     */
    private static boolean saveCached(int stationId, boolean shortTerm, InputStream rss) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdDir = Environment.getExternalStorageDirectory();
            File dataDir = new File(sdDir, DATA_DIR);
            try {
                if (! dataDir.exists()) dataDir.mkdirs();
                File cache = new File(dataDir, makeFileName(stationId, shortTerm));
                OutputStream outputStream = new FileOutputStream(cache);
                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = rss.read(buffer))) {
                    outputStream.write(buffer, 0, n);
                }
                outputStream.close();
                return true;
            } catch (FileNotFoundException e) {
                Log.e("OTempo", e.getMessage(), e);
                return false;
            } catch (IOException e) {
                Log.e("OTempo", e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Crea el nombre de fichero en el que cacheamos un RSS dado su ID de
     * estación y si es o no a corto plazo. 
     */
    private static String makeFileName(int stationId, boolean shortTerm) {
    	if (shortTerm) {
    		return String.format(Locale.US, "%d_short.rss", stationId);
    	} else {
    		return String.format(Locale.US, "%d_medium.rss", stationId);
    	}
    }

    /**
     * @param stationId ID de la estación
     * @return Obtiene el RSS de una estación directamente desde Internet
     */
    @Nullable
    private static InputStream getFromInternet(int stationId, boolean shortTerm) {
        URL url = null;
        try {
        	if (shortTerm) {
        		url = new URL("http://servizos.meteogalicia.es/rss/predicion/rssLocalidades.action?idZona="+stationId+"&dia=-1");
        	} else {
        		url = new URL("http://servizos.meteogalicia.es/rss/predicion/rssConcellosMPrazo.action?idZona="+stationId+"&dia=-1");
        	}
            URLConnection conn = url.openConnection();
            InputStream stream = conn.getInputStream();
            if (saveCached(stationId, shortTerm, stream)) {
                return getFromStorage(stationId, shortTerm);
            } else {
                return stream;
            }
        } catch (MalformedURLException e) {
            Log.e("OTempo", "BAD URL: "+e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Log.e("OTempo", "IOException: " + e.getMessage(), e);
            return null;
        }
    }

}
