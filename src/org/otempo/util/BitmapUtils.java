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
package org.otempo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.jdt.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Herramientas para bitmaps
 */
public class BitmapUtils {
	/**
	 * Permite decodificar un bitmap de un fichero forzando unas dimensiones máximas. 
	 * Realiza la carga de imágenes que sobrepasarían la memoria disponible para aplicaciones, saltándose filas y columnas al cargarlas. 
	 * @param f Fichero con la imagen
	 * @param maxWidth Máximo ancho de la imagen resultante
	 * @param maxHeight Máximo alto de la imagen resultante
	 * @return La imagen cargada y cumpliendo con los máximos solicitados, o null si no existe el fichero.
	 */
	@Nullable
    public static Bitmap safeDecodeFile(File f, int maxWidth, int maxHeight) {
        System.gc();
        Bitmap b = null;
        try {
            // Comprobamos las dimensiones sin leer todo el fichero
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            
            // Ahora calculamos la escala apropiada al leer
            int scale = 1;
            if (o.outHeight > maxHeight || o.outWidth > maxWidth) {
                int scaleX = (int)Math.pow((int) Math.ceil(Math.log(maxWidth / (double) o.outWidth) / Math.log(0.5)), 2);
                int scaleY = (int)Math.pow((int) Math.ceil(Math.log(maxHeight / (double) o.outHeight) / Math.log(0.5)), 2);
                scale = Math.max(scaleX, scaleY);
            }
            // Finalmente realizamos la carga con el factor de escala oportuno
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return b;
    }
}
