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
package org.otempo.favorites;

import java.util.Date;

import org.otempo.R;
import org.otempo.model.Station;
import org.otempo.util.Nullness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

/**
 * Utilidad para acceder a la base de datos de estaciones favoritas (más accedidas)
 */
public class FavoritesDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "OTempo";
    private static final String FAVORITES_TABLE_NAME = "favorites";
    private static final String STATION_ID_COL = "station_id";
    private static final String ACCESS_COUNT_COL = "accessCount";
    private static final String LAST_ACCESS_COL = "last_access";
    @Nullable
    private String _createSQL;

    /**
     * Construye la utilidad
     * @param context Contexto de actividad o aplicación a usar para el acceso a la base de datos
     */
    public FavoritesDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _createSQL = context.getString(R.string.database_sql);
        refreshFavorites();
    }

    /**
     * Actualiza un favorito (lo crea si no existe)
     * @param station Estación a actualizar
     */
    public void updateFavorite(Station station) {
    	Date access = station.getLastAccess();
    	if (access != null) {
	        SQLiteDatabase db = getWritableDatabase();
	        ContentValues values = new ContentValues();
	        values.put(STATION_ID_COL, station.getId());
	        values.put(ACCESS_COUNT_COL, station.getAccessCount());
	        values.put(LAST_ACCESS_COL, access.getTime());
	        db.replace(FAVORITES_TABLE_NAME, "last_access", values);
    	}
    }

    /**
     * Borra un favorito que use un ID antiguo, para dejar sitio a los nuevos IDs.
     * TODO(ryu): Borrar después de un par de versiones.  
     * @param legacyId El id a borrar.
     */
    private void deleteLegacyFavorite(int legacyId) {
        SQLiteDatabase db = getWritableDatabase();
    	db.delete(FAVORITES_TABLE_NAME, "station_id = ?", new String[]{ String.valueOf(legacyId) });
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(_createSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // Por ahora sólo hay una versión de la base de datos, así que no hay nada que actualizar
    }

    /**
     * Accede a la base de datos y relee la información de accesos
     */
    private void refreshFavorites() {
        String[] columns = {STATION_ID_COL, ACCESS_COUNT_COL, LAST_ACCESS_COL};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(FAVORITES_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int accessCount = cursor.getInt(1);
                Date lastAccess = new Date(cursor.getLong(2));
                Station station = Nullness.checkNotNull(Station.getById(id));
                station.setLastAccess(lastAccess);
                station.setAccessCount(accessCount);
                // TODO(ryu): Borrar después de un par de versiones.
                if (Station.isLegacyId(id)) {
                	deleteLegacyFavorite(id);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
