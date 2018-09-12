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
 * Estado inicial, carga de la SD la estación de máxima prio.
 * A coninuación deja una carga completa de todas las estaciones.
 */
public class CreatedState implements ServiceState {
    @Override
    public String toString() {
        return "CREATED";
    }

    @Override
    public void update(UpdateService context) {
        Station maxPrioStation = context.getWidgetStation();
        context.addStation(maxPrioStation);
        if (context.hasConnectivity()) {
            // Cambio de estado immediato al ciclo de actualización
            context.setState(new UpdateCycleState());
        } else {
            // Cambio de estado immediato al ciclo de actualización de cache
            context.setState(new UpdateCachedState());
        }
    }

    @Override
    public void requestWithPriority(UpdateService context, Station station) {
        context.addStationMaxPrio(station);
    }

    @Override
    public void connectivityAvailable(UpdateService context) {}
}
