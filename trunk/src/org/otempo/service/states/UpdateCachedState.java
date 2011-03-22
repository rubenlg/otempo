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
 * Estado que carga las estaciones pendientes de actualización desde la caché en la SD
 */
public class UpdateCachedState implements ServiceState {
    @Override
    public String toString() {
        return "UPDATE_CACHED";
    }

    @Override
    public void update(UpdateService context) {
        if (context.hasPendingStations()) {
            context.processNextStation(true);
        } else {
            context.setState(new WaitConnectionState());
        }
    }

    @Override
    public void requestWithPriority(UpdateService context, Station station) {
        context.addStationMaxPrio(station);
    }

    @Override
    public void connectivityAvailable(UpdateService context) {
        context.setState(new UpdateCycleState());
    }
}
