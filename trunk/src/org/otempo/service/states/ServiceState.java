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
package org.otempo.service.states;

import org.otempo.model.Station;
import org.otempo.service.UpdateService;

/**
 * Estado del servicio
 */
public interface ServiceState {
    /**
     * Pide que se actualice el estado actual, haciendo lo que corresponda
     */
    void update(UpdateService context);

    /**
     * Pide que se carguen los datos de una estación con mucha prioridad
     * @param station la estación deseada
     */
    void requestWithPriority(UpdateService context, Station station);

    /**
     * Señal enviada cuando vuelve la conexión a Inet
     */
    void connectivityAvailable(UpdateService context);
}
