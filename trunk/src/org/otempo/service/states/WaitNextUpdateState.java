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

import org.eclipse.jdt.annotation.Nullable;
import org.otempo.model.Station;
import org.otempo.service.UpdateService;

import android.preference.PreferenceManager;

/**
 * Estado que duerme hasta que toque el siguiente ciclo de actualización
 */
public class WaitNextUpdateState implements ServiceState {
    @Override
    public String toString() {
        return "WAIT_NEXT_UPDATE";
    }

    @Override
    public void update(UpdateService context) {
        int updatePeriod = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_UPDATE_PERIOD, String.valueOf(DEFAULT_UPDATE_PERIOD)));
        sleep(updatePeriod);
        if (_requestedPrioStation != null) {
            context.addStationMaxPrio(_requestedPrioStation);
            context.setState(new UpdateCycleState());
        } else {
            context.fillStations();
            if (context.getBackgroundDataAllowed()) {
                context.setState(new UpdateCycleState());
            } else {
                context.setState(null);
            }
        }
    }
    synchronized void sleep(int updatePeriod) {
        try {
            wait(updatePeriod);
        } catch (InterruptedException e) {
            return;
        }
    }

    synchronized void awake() {
        notify();
    }

    @Override
    public void requestWithPriority(UpdateService context, Station station) {
        _requestedPrioStation = station;
        awake();
    }

    @Override
    public void connectivityAvailable(UpdateService context) {}

    @Nullable private Station _requestedPrioStation = null;

    private static final int DEFAULT_UPDATE_PERIOD = 1000 * 3600; // 1 Hour
    private static final String PREF_UPDATE_PERIOD = "updatePeriod";
}
