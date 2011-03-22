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
 * Estado que duerme en espera de conexión a Internet
 */
public class WaitConnectionState implements ServiceState {
    @Override
    public String toString() {
        return "WAIT_CONNECTION";
    }

    @Override
    public void update(UpdateService context) {
        if (context.hasConnectivity()) {
            if (context.getBackgroundDataAllowed()) {
                context.setState(new UpdateCycleState());
            } else {
                context.setState(null);
            }
        } else {
            sleep();
        }
    }

    synchronized void sleep() {
        try {
            wait(_sleepTimeout);
        } catch (InterruptedException e) {
            return;
        }
    }

    synchronized void awake() {
        notify();
    }

    @Override
    public void requestWithPriority(UpdateService context, Station station) {
        context.addStationMaxPrio(station);
        context.setState(new UpdateCachedState());
        awake();
    }

    @Override
    public void connectivityAvailable(UpdateService context) {
        awake();
    }

    public static final int _sleepTimeout = 1000*60*5; // 5 minutos
}
