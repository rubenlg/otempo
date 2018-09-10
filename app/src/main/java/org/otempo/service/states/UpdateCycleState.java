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

import org.otempo.R;
import org.otempo.model.Station;
import org.otempo.service.UpdateService;
import org.otempo.view.StationActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Este estado representa un ciclo normal de actualización de estaciones
 * Puede ser que actualicemos una sola (la primera vez que se arranca el servicio)
 * o bien que actualicemos todas.
 */
public class UpdateCycleState implements ServiceState {
    @Override
    public String toString() {
        return "UPDATE_CYCLE";
    }

    @Override
    public void update(UpdateService context) {
        if (context.hasPendingStations() && context.hasConnectivity()) {
            if (!_notificationActive && context.countPendingStations() >= MIN_STATIONS_TO_NOTIFY) {
                addNotification(context);
                _notificationActive = true;
            }
            context.processNextStation(false);
        } else {
            if (_notificationActive) {
                removeNotification(context);
                _notificationActive = false;
            }
            if (!context.hasPendingStations()) {
                // Se pueden usar datos en segundo plano
                if (context.getBackgroundDataAllowed()) {
                    // Cambio de estado a esperar por la siguiente actualización
                    context.setState(new WaitNextUpdateState());
                }
                // No se puede
                else {
                    // Cambio de estado para terminar el servicio
                    context.setState(null);
                }
            } else if (!context.hasConnectivity()) {
                context.setState(new WaitConnectionState());
            } else {
                Log.e("OTempo", "Situación imposible en doUpdateCycle()");
            }
        }
    }

    @Override
    public void requestWithPriority(UpdateService context, Station station) {
        context.addStationMaxPrio(station);
    }

    private void addNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.notification_update, context.getString(R.string.sync_short), System.currentTimeMillis());

        //Context context = getApplicationContext();
        CharSequence contentTitle = context.getString(R.string.sync_short);
        CharSequence contentText = context.getString(R.string.sync_desc);
        Intent notificationIntent = new Intent(context, StationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notificationManager.notify(UPDATING_NOTIF_ID, notification);
    }

    private void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(UPDATING_NOTIF_ID);
    }

    @Override
    public void connectivityAvailable(UpdateService context) {}

    private boolean _notificationActive = false;
    private static final int UPDATING_NOTIF_ID = 1;
    // Número mínimo de estaciones que se deben actualizar en un ciclo para molestar al usuario con la notificación
    private static final int MIN_STATIONS_TO_NOTIFY = 3;
}
