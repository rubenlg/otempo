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
package org.otempo;

import org.otempo.model.Station;

/**
 * Recibe actualizaciones cuando cambia la estación activa, o bien porque
 * pasa a ser otra diferente, o bien porque tiene datos más nuevos.
 */
public interface StationUpdateListener {
    /**
     * Se llama aquí cuando cambia la estación activa, o bien porque
     * pasa a ser otra diferente, o bien porque tiene datos más nuevos.
     * @param station La estación que se debe mostrar
     */
    void onStationUpdate(Station station);

    /**
     * Se llama aquí cuando falla la carga por error de internet
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void internetError();

    /**
     * Se llama aquí cuando falla la carga por error del programador
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void internalError();

    /**
     * Se llama aquí cuando una estación que se intentó actualizar ya estaba actualizada
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void upToDate(Station station);

    /**
     * Se llama aquí cuando se desactiva el acceso a internet durante una actualización
     * Llamado desde un thread diferente al de la UI, CUIDADO!
     */
    void internetOff();
}
